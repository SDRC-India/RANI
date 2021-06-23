package org.sdrc.rani.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.time.DateUtils;
import org.sdrc.rani.document.Area;
import org.sdrc.rani.document.CFInputFormData;
import org.sdrc.rani.document.ClusterMapping;
import org.sdrc.rani.document.KeyGeneratorColumnsSetting;
import org.sdrc.rani.document.TimePeriod;
import org.sdrc.rani.models.DateModel;
import org.sdrc.rani.models.SubmissionStatus;
import org.sdrc.rani.models.UserModel;
import org.sdrc.rani.repositories.AreaRepository;
import org.sdrc.rani.repositories.CFInputFormDataRepository;
import org.sdrc.rani.repositories.ClusterMappingRepository;
import org.sdrc.rani.repositories.KeyGeneratorColumnsSettingRepository;
import org.sdrc.rani.repositories.TimePeriodRepository;
import org.sdrc.rani.util.TokenInfoExtracter;
import org.sdrc.usermgmt.mongodb.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;

import in.co.sdrc.sdrcdatacollector.document.TypeDetail;
import in.co.sdrc.sdrcdatacollector.models.FormAttachmentsModel;
import in.co.sdrc.sdrcdatacollector.models.ReceiveEventModel;
import in.co.sdrc.sdrcdatacollector.mongorepositories.TypeDetailRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
@Service
@Slf4j
public class SubmissionServiceImpl implements SubmissionService {

	@Autowired
	private CFInputFormDataRepository cFInputFormDataRepository;

	@Autowired
	private TokenInfoExtracter tokenInfoExtracter;

	@Autowired
	private ConfigurableEnvironment configurableEnvironment;

	@Autowired
	private KeyGeneratorColumnsSettingRepository keyGeneratorColumnsSettingRepository;

	@Autowired
	private AreaRepository areaRepository;

	@Autowired
	private TimePeriodRepository timePeriodRepository;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private ClusterMappingRepository clusterMappingRepository;

	@Autowired
	private TypeDetailRepository typeDetailRepository;

	@Autowired
	@Qualifier("mongoAccountRepository")
	private AccountRepository accountRepository;


	@Override
//	@Caching(evict = { @CacheEvict(value = "formData", key = "#event.formId")})
	public ResponseEntity<String> saveSubmission(ReceiveEventModel event, OAuth2Authentication oauth) {

		Gson gson = new Gson();
		try {
			if (event.getFormId() == null) {
				log.warn("Invalid submission request, missing formId in submission payload");
				return new ResponseEntity<>("Missing formId key in payload", HttpStatus.BAD_REQUEST);
			}

			UserModel principal = tokenInfoExtracter.getUserModelInfo(oauth);

			switch (event.getFormId()) {

			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
			case 12: {

				CFInputFormData dataSubmit = new CFInputFormData();
				Date currentDate = new Date();
				dataSubmit.setCreatedDate(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(event.getCreatedDate()));
				dataSubmit.setUpdatedDate(new SimpleDateFormat("dd-MM-yyyy").parse(event.getUpdatedDate()));
				dataSubmit.setSyncDate(currentDate);
				dataSubmit.setUserName(principal.getName());
				dataSubmit.setFormId(event.getFormId());
				dataSubmit.setUserId(principal.getUserId());
				dataSubmit.setUniqueId(event.getUniqueId());
				dataSubmit.setAttachmentCount(event.getAttachmentCount());

				/**
				 * set the value of district , block
				 */
				Map<String, Object> submissionDataMap=event.getSubmissionData();
				
				if(event.getFormId()!=6 && event.getFormId()!=12){
					submissionDataMap = setBlockDistrictValue(event.getSubmissionData(),
							event.getFormId());
				}
				
				
				if(event.getFormId()==7 || event.getFormId()==4){
					submissionDataMap = setRespondentSequemce(event.getSubmissionData(),
							event.getFormId(),currentDate);
				}
				
				// fetch date of visit from submitted data
				Date dateOfVisit = getDateOfVisit(submissionDataMap, event.getFormId());

				TimePeriod timePeriod = fetchTimePeriod(dateOfVisit, event.getFormId());

				dataSubmit.setTimePeriod(timePeriod);

				/**
				 * check if data is re-submitted(rejection data)
				 */
				List<CFInputFormData> rejectedDataSubmit = mongoTemplate
						.aggregate(getAggregationResults(event.getUniqueId()), CFInputFormData.class,
								CFInputFormData.class)
						.getMappedResults();

				DateModel dateModel = null;

				/**
				 * for form-2 ie community-engagement meeting check whether its
				 * T4-component for same village,month,T4 component number is
				 * present or not if present save the record in db
				 * 
				 * if not than rejecting the submission and saving it in db
				 * mentioning the rejected message accordingly
				 * 
				 */

				if (event.getFormId() == 2) {

					/**
					 * fetching village,component name,timeperiod
					 */
					Integer village = (Integer) submissionDataMap
							.get(configurableEnvironment.getProperty("form2.village.keyvalue"));

					Integer componentName = (Integer) submissionDataMap
							.get(configurableEnvironment.getProperty("form2.component.key"));

					/**
					 * T4componentName id is different in each form, so its
					 * obvious that it will never match,
					 * 
					 * get the equivalent id matching the same component name
					 */

					TypeDetail typeDe = typeDetailRepository.findBySlugId(componentName);
					String typeDetailNames = typeDe.getName();
					TypeDetail typeDetail = typeDetailRepository.findByFormIdAndName(1, typeDetailNames);

					List<CFInputFormData> t4Datas = cFInputFormDataRepository
							.findByVillageComponentNameAndFormId(village, typeDetail.getSlugId(),
									1);

					if (t4Datas.isEmpty()) {
						dataSubmit.setRejected(true);
						dataSubmit.setRejectedDate(new Date());
						dataSubmit.setRejectMessage("No T4 form found !");
					}

				}

				/**
				 * new entry
				 */
				if (rejectedDataSubmit.isEmpty()) {

					dateModel = getDatesForMonthlyDataCollection(dateOfVisit, "entry");

					/**
					 * check for valid date submission sync date value should be
					 * in between startdate and enddate(both inclusive)
					 * 
					 * get the equivalent id and replace it while querying
					 */
					if ((DateUtils.isSameDay(dataSubmit.getSyncDate(), dateModel.getStartDate())
							|| (dataSubmit.getSyncDate().after(dateModel.getStartDate()))
									&& (DateUtils.isSameDay(dataSubmit.getSyncDate(), dateModel.getEndDate())
											|| dataSubmit.getSyncDate().before(dateModel.getEndDate()))))
						dataSubmit.setIsValid(true);
					else
						dataSubmit.setIsValid(false);

				} else// re-submission entry
				{

					/**
					 * check for valid submission sync date value should be in
					 * between startdate and enddate(both inclusive)
					 * 
					 * enddate is 7th of every subsequent month
					 */
					dateModel = getDatesForMonthlyDataCollection(dateOfVisit, "rejection");
					if ((DateUtils.isSameDay(dataSubmit.getSyncDate(), dateModel.getStartDate())
							|| (dataSubmit.getSyncDate().after(dateModel.getStartDate()))
									&& (DateUtils.isSameDay(dataSubmit.getSyncDate(), dateModel.getEndDate())
											|| dataSubmit.getSyncDate().before(dateModel.getEndDate())))){
						dataSubmit.setIsValid(true);
						dataSubmit.setIsReSubmitted(true);
					}
						
					else{
						dataSubmit.setIsReSubmitted(true);
						dataSubmit.setIsValid(false);
					}
						

				}
				/**
				 * ontime and latesubmission if data is being synced within
				 * 4days of datacollection than it is ontime submission taking
				 * date of visit into account and validating whether the data is
				 * synced within or 4days
				 */
				DateModel datesForOntimeDataCollection = getDatesForOntimeDataCollection(dateOfVisit);
				
				if ((DateUtils.isSameDay(dataSubmit.getSyncDate(), datesForOntimeDataCollection.getStartDate())
						|| (dataSubmit.getSyncDate().after(datesForOntimeDataCollection.getStartDate())) && (DateUtils
								.isSameDay(dataSubmit.getSyncDate(), datesForOntimeDataCollection.getEndDate())
								|| dataSubmit.getSyncDate().before(datesForOntimeDataCollection.getEndDate()))))

					dataSubmit.setSubmissionStatus(SubmissionStatus.ONTIMESUBMISSION);
				else
					dataSubmit.setSubmissionStatus(SubmissionStatus.LATESUBMISSION);

				dataSubmit.setData(submissionDataMap);

				
				/**
				 * check for same unique id and formid whether any submission is exist which is fresh data that is reject=false
				 * if exist than make the previous data as invalid 
				 */
				List<CFInputFormData> dupData = cFInputFormDataRepository.findByFormIdAndUniqueIdAndRejectedFalse(dataSubmit.getFormId(),dataSubmit.getUniqueId());
				
				dupData.forEach(d->d.setIsValid(false));
				cFInputFormDataRepository.save(dupData);
				
				
				/**
				 * only form-1 and form-7 has image so at the time of its data
				 * submission it remains in pending status
				 */
				if (event.getFormId() != 1 && event.getFormId() != 7) {
					dataSubmit.setSubmissionCompleteStatus(SubmissionStatus.C);
					cFInputFormDataRepository.save(dataSubmit);
					return new ResponseEntity<String>(gson.toJson("success"), HttpStatus.OK);
				} else {
					CFInputFormData saveData = cFInputFormDataRepository.save(dataSubmit);
					return new ResponseEntity<String>(gson.toJson(saveData.getId()), HttpStatus.OK);
				}

			}
			}
		} catch (Exception e) {

			log.error("Action : while submitting new data with payload {}",event,e);
			throw new RuntimeException(e);
		}
		throw new RuntimeException("Invalid form id");
	}

	private Map<String, Object> setRespondentSequemce(Map<String, Object> submissionData, Integer formId,Date currentDate) {
		
		String brValue = configurableEnvironment.getProperty("form"+formId+".beginrepeat.colname");
		String colName = configurableEnvironment.getProperty("form"+formId+".resid.colname");
		
		List<Map<String,Object>> bgMap= (List<Map<String, Object>>) submissionData.get(brValue);
		
		for(int i = 0;i<bgMap.size();i++){
			
			Map<String, Object> valueMap = bgMap.get(i);
			
			//date-villagename-formname-count
			String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(currentDate);
			String village = areaRepository.findByAreaId(Integer.valueOf(submissionData.get(configurableEnvironment.getProperty("form"+formId+".village.keyvalue")).toString())).getAreaName();
			String formName = configurableEnvironment.getProperty("form"+formId+".name");
			
			String value = date.concat("/").concat(village).concat("/").concat(formName.split("-")[1]).concat("/").concat(String.valueOf(i+1));
			
			valueMap.put(colName, value);
		}
		
		return submissionData;
	}

	/**
	 * setting values of ditrict and block in submission data
	 * 
	 * @param submissionDataMap
	 * @param formId
	 * @return
	 */
	private Map<String, Object> setBlockDistrictValue(Map<String, Object> submissionDataMap, Integer formId) {

		Integer villageId = null;
		Integer districtId = null;
		Integer blockId = null;

		villageId = (Integer) submissionDataMap
				.get(configurableEnvironment.getProperty("form" + formId + ".village.keyvalue").toString());
		/*
		 * get the complete Area details from area document
		 */
		Area area = areaRepository.findByAreaId(villageId);
		districtId = areaRepository.findByAreaId(area.getDistrictId()).getAreaId();
		blockId = areaRepository.findByAreaId(area.getBlockId()).getAreaId();

		ClusterMapping clusterMapping = clusterMappingRepository.findByVillage(area);

		submissionDataMap.put(configurableEnvironment.getProperty("form" + formId + ".district.keyvalue"), districtId);
		submissionDataMap.put(configurableEnvironment.getProperty("form" + formId + ".block.keyvalue"), blockId);
		submissionDataMap.put(configurableEnvironment.getProperty("form" + formId + ".clusterno.keyvalue"),
				clusterMapping.getClusterNumber());

		return submissionDataMap;
	}

	/**
	 * getting time period
	 * 
	 * @return
	 */
	public TimePeriod fetchTimePeriod(Date createdDate, Integer formId) {

		TimePeriod currentTimePeriod = null;

		// int year = createdDate.getYear() + 1900;

		currentTimePeriod = timePeriodRepository.getCurrentTimePeriod(createdDate,
				configurableEnvironment.getProperty("timeperiod.periodicity.monthly"));

		return currentTimePeriod;
	}

	/**
	 * get date of visit from submitted data
	 * 
	 * @return date
	 * @throws ParseException
	 */
	private Date getDateOfVisit(Map<String, Object> data, Integer formId) throws ParseException {

		List<KeyGeneratorColumnsSetting> generator = keyGeneratorColumnsSettingRepository.findByFormFormId(formId);
		/*
		 * key is column-value, value is column-key
		 */
		Map<String, String> columnsSettingMap = generator.stream().collect(
				Collectors.toMap(KeyGeneratorColumnsSetting::getColValue, KeyGeneratorColumnsSetting::getColKey));

		String colValue = null;
		colValue = configurableEnvironment.getProperty("form" + formId + ".datevisit.value");

		String dateOfVisitKey = columnsSettingMap.get(colValue);
		String dateVisit = (String) data.get(dateOfVisitKey);
		dateVisit=dateVisit.concat(" 00:05");
		// convert string to date
		Date dateOfVisit = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(dateVisit);

		return dateOfVisit;
	}

	/*
	 * This method checks whether datasubmission is rejected data or fresh entry
	 */
	private Aggregation getAggregationResults(String uniqueId) {

		MatchOperation match = Aggregation.match(Criteria.where("uniqueId").is(uniqueId).and("rejected").is(true)
				.and("submissionCompleteStatus").is(SubmissionStatus.C));

		// last syncDate data
		SortOperation sort = Aggregation.sort(Sort.Direction.ASC, "syncDate");

		// @formatter:off
 
		GroupOperation group = Aggregation.group("uniqueId")
				.last("syncDate").as("syncDate")
				.last("data").as("data")
				.last("userName").as("userName")
				.last("userId").as("userId")
				.last("createdDate").as("createdDate")
				.last("updatedDate").as("updatedDate")
				.last("formId").as("formId")
				.last("uniqueId").as("uniqueId")
				.last("rejected").as("rejected")
				.last("rejectMessage").as("rejectMessage")
				.last("uniqueName").as("uniqueName")
				.last("rejectedDate").as("rejectedDate")
				.last("rejectedBy").as("rejectedBy")
				.last("isAggregated").as("isAggregated")
				.last("isValid").as("isValid")
				.last("timePeriod").as("timePeriod")
				.last("attachmentCount").as("attachmentCount")
				.last("attachments").as("attachments")
				.last("submissionCompleteStatus").as("submissionCompleteStatus")
				.last("submissionStatus").as("submissionStatus");
		// @formatter:on

		return Aggregation.newAggregation(match, sort, group);

	}

	/**
	 * This method returns startdate and enddate value for valid datasubmission
	 * 
	 * @param dateOfVisit
	 */
	private DateModel getDatesForMonthlyDataCollection(Date dateOfVisit, String type) {

		DateModel model = new DateModel();

		Calendar cal = Calendar.getInstance();

		cal.setTime(dateOfVisit);

		cal.set(Calendar.DATE, cal.getActualMinimum(Calendar.DATE));

		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MILLISECOND, 0);

		model.setStartDate(cal.getTime());

		cal.add(Calendar.MONTH, 1);

		// for data collection date is 3 and for rejection entry data date is 10
		if (type.equals("entry"))
			cal.set(Calendar.DATE, 3);

		if (type.equals("rejection"))
			cal.set(Calendar.DATE, 10);

		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 59);

		model.setEndDate(cal.getTime());

		return model;
	}

	/**
	 * This method returns startdate and enddate value for ontime and late data
	 * submission
	 * 
	 * @param dateOfVisit
	 */
	private DateModel getDatesForOntimeDataCollection(Date dateOfVisit) {

		DateModel model = new DateModel();

		Calendar cal = Calendar.getInstance();

		cal.setTime(dateOfVisit);

		model.setStartDate(cal.getTime());

		cal.add(Calendar.DATE, 4);

		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 59);
		model.setEndDate(cal.getTime());

		return model;
	}

	@Override
	public String uploadFiles(MultipartFile file, FormAttachmentsModel fileModel) {

		switch (fileModel.getFormId()) {

		case 1:
		case 7: {

			List<FormAttachmentsModel> modelList = new ArrayList<FormAttachmentsModel>();

			FormAttachmentsModel model = new FormAttachmentsModel();

			CFInputFormData submissionData = cFInputFormDataRepository.findByIdAndFormId(fileModel.getSubmissionId(),
					fileModel.getFormId());

			Map<String, List<FormAttachmentsModel>> attachments = submissionData.getAttachments();

			String filePath = getFilePath(file, configurableEnvironment.getProperty("form"+fileModel.getFormId()+".name"),
					fileModel.getFileExtension(), fileModel.getOriginalName(),configurableEnvironment.getProperty("upload.file.path"));

			model.setFilePath(filePath);
			model.setFileSize(file.getSize());
			model.setOriginalName(
					fileModel.getOriginalName().substring(0, fileModel.getOriginalName().lastIndexOf('.')));
			model.setFileExtension(fileModel.getFileExtension());
			model.setColumnName(fileModel.getColumnName());
			model.setFileExtension(FilenameUtils.getExtension(file.getOriginalFilename()));
			model.setFormId(fileModel.getFormId());
			model.setLocalDevicePath(fileModel.getLocalDevicePath());

			if (attachments == null) {
				attachments = new HashMap<>();
				modelList.add(model);
				attachments.put(configurableEnvironment.getProperty("form" + fileModel.getFormId() + ".camera.key"),
						modelList);
			} else {
				List<FormAttachmentsModel> list = attachments
						.get(configurableEnvironment.getProperty("form" + fileModel.getFormId() + ".camera.key"));
				list.add(model);
				attachments.put(configurableEnvironment.getProperty("form" + fileModel.getFormId() + ".camera.key"),
						list);
			}

			if (submissionData.getAttachmentCount() == attachments
					.get(configurableEnvironment.getProperty("form" + fileModel.getFormId() + ".camera.key")).size()) {
				submissionData.setSubmissionCompleteStatus(SubmissionStatus.C);
			}
			submissionData.setAttachments(attachments);

			cFInputFormDataRepository.save(submissionData);

			return "success";

		}

		}

		throw new RuntimeException("Invalid formId");
	}

	/*
	 * it save the file in hard disk and return the complete file path
	 */
	public String getFilePath(MultipartFile file, String formName, String extension, String originalFileName,String dir) {
		String path = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(file.getBytes());
			ByteArrayOutputStream bos = new ByteArrayOutputStream();

			for (int readNum; (readNum = bis.read(file.getBytes())) != -1;) {
				bos.write(file.getBytes(), 0, readNum);
			}

			byte[] getFileBytes = bos.toByteArray();

			dir = dir + formName.concat("/");

			File filePath = new File(dir);

			/*
			 * make directory if doesn't exist
			 */
			if (!filePath.exists())
				filePath.mkdirs();

			String name = originalFileName.substring(0, originalFileName.lastIndexOf('.'))
					+ new SimpleDateFormat("ddMMyyyyHHmmssSSSS").format(new Date()).concat(".") + extension;

			path = dir + name;

			FileOutputStream fos = new FileOutputStream(path);
			fos.write(getFileBytes);
			fos.flush();
			fos.close();

		} catch (Exception e) {
			log.error("Action : While uploading file formName {}", formName,e);
			throw new RuntimeException(e);
		}

		return path;

	}

	

}
