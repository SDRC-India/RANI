package org.sdrc.rani.service;

import static java.util.Comparator.comparing;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.sdrc.rani.document.CFInputFormData;
import org.sdrc.rani.models.DateModel;
import org.sdrc.rani.models.UserModel;
import org.sdrc.rani.models.ValueObject;
import org.sdrc.rani.repositories.CFInputFormDataRepository;
import org.sdrc.rani.util.TokenInfoExtracter;
import org.sdrc.usermgmt.mongodb.domain.Account;
import org.sdrc.usermgmt.mongodb.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import in.co.sdrc.sdrcdatacollector.document.EnginesForm;
import in.co.sdrc.sdrcdatacollector.document.Question;
import in.co.sdrc.sdrcdatacollector.document.TypeDetail;
import in.co.sdrc.sdrcdatacollector.engine.FormsServiceImpl;
import in.co.sdrc.sdrcdatacollector.handlers.ICameraAndAttachmentsDataHandler;
import in.co.sdrc.sdrcdatacollector.handlers.IDbFetchDataHandler;
import in.co.sdrc.sdrcdatacollector.handlers.IDbReviewQueryHandler;
import in.co.sdrc.sdrcdatacollector.models.DataModel;
import in.co.sdrc.sdrcdatacollector.models.DataObject;
import in.co.sdrc.sdrcdatacollector.models.QuestionModel;
import in.co.sdrc.sdrcdatacollector.models.ReviewPageModel;
import in.co.sdrc.sdrcdatacollector.mongorepositories.EngineFormRepository;
import in.co.sdrc.sdrcdatacollector.mongorepositories.QuestionRepository;
import in.co.sdrc.sdrcdatacollector.mongorepositories.TypeDetailRepository;
import in.co.sdrc.sdrcdatacollector.util.EngineUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author subham
 *
 */
@Service
@Slf4j
public class SubmissionManagementServiceImpl implements SubmissionManagementService {

	private final String BEGIN_REPEAT = "beginrepeat";
	
	@Autowired
	private CFInputFormDataRepository cfInputFormDataRepository;

	@Autowired
	private TokenInfoExtracter tokenInfoExtracter;

	@Autowired
	@Qualifier("mongoAccountRepository")
	private AccountRepository accountRepository;
	
	@Autowired
	private IDbFetchDataHandler iDbFetchDataHandler;
	
	@Autowired
	private EngineFormRepository engineFormRepository;

	@Autowired
	private QuestionRepository questionRepository;
	
	@Autowired
	private IDbReviewQueryHandler iDbReviewQueryHandler;
	
	@Autowired
	private TypeDetailRepository typeDetailRepository;
	
	@Autowired
	private EngineUtils engineUtils;
	
	@Autowired
	private FormsServiceImpl formsServiceImpl;
	
	@Autowired
	private ICameraAndAttachmentsDataHandler iCameraDataHandler;
	
	@Autowired
	private ConfigurableEnvironment configurableEnvironment;
	
	@Autowired
	private WebService webService;
	
	@Autowired
	private MongoTemplate mt;
	
	
	private SimpleDateFormat sdfDateTimeWithSeconds = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private DateFormat ymdDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	@Override
//	@Caching(evict = { @CacheEvict(value = "formData", key = "#valueObject.formId")})
	public ResponseEntity<String> rejectSubmissions(ValueObject valueObject, OAuth2Authentication auth) {

		try {
			UserModel user = tokenInfoExtracter.getUserModelInfo(auth);

			Account acc = accountRepository.findById(user.getUserId());

			Gson gson = new Gson();

			switch (valueObject.getFormId()) {

			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:{

				List<CFInputFormData> dataSubmit = cfInputFormDataRepository.findByIdIn(valueObject.getRejectionList());
				//for rejection isValis is true and isRejected true
				for(int i=0;i<dataSubmit.size();i++){
					CFInputFormData data = dataSubmit.get(i);
					data.setRejected(true);
					data.setRejectedDate(new Date());
					data.setRejectMessage(valueObject.getMessage());
					data.setRejectedBy(acc);
					
					//for deleted data isRejected is true and isValid is false
					if(valueObject.getIsDelete().get(i).equals("delete")){
						data.setIsValid(false);
						data.setIsDeleted(true);
					}
				}
//				Query updateQuery = new Query();
//				updateQuery.addCriteria(Criteria.where("_id").is(valueObject.getRejectionList().get(0)));
//				
// 				Update update=new Update();
//				update.set("rejected", true);
//				update.set("rejectedDate", new Date());
//				update.set("rejectMessage", valueObject.getMessage());
//				update.set("rejectedBy", acc);
//				
//				//for deleted data isRejected is true and isValid is false
//				if(valueObject.getIsDelete().get(0).equals("delete")){
//					update.set("isValid", false);
//					update.set("isDeleted", true);
//				}
//				mt.updateMulti(updateQuery, update, CFInputFormData.class);

				cfInputFormDataRepository.save(dataSubmit);
				log.info("Action : Rejection of data successfull with payload {}",valueObject);
				return new ResponseEntity<String>(gson.toJson(configurableEnvironment.getProperty("reject.record.success")), HttpStatus.OK);
			}

			}
			log.error("Action : Rejection of data with payload {}",valueObject);
			throw new RuntimeException("invalid formId");
		} catch (Exception e) {
			log.error("Action : while rejecting submission with payload {}", valueObject, e);
			throw new RuntimeException(e);
		}

	}

	@Override
//	@Cacheable(value="formData", key = "#formId")
	public List<DataObject> getReiewDataHead(Integer formId, UserModel user, Map<String, Object> paramKeyValMap) {
		
		EnginesForm form = engineFormRepository.findByFormId(formId);
		
		List<TypeDetail> typeDetails = typeDetailRepository.findByFormId(form.getFormId());

		Map<Integer, TypeDetail> typeDetailsMap = typeDetails.stream()
				.collect(Collectors.toMap(TypeDetail::getSlugId, typeDe -> typeDe));
		
		List<DataModel> submissionDatas = iDbFetchDataHandler.fetchDataFromDb(form, "dataReview", null, new Date(),
				new Date(), paramKeyValMap, null, user);
		
		
		List<DataObject> dataObjects = new ArrayList<>();
	
		for(DataModel submissionData : submissionDatas){
			
			DataObject dataObject = new DataObject();
			dataObject.setFormId(submissionData.getFormId());
			if(submissionData.isRejected()){
				dataObject.setTime(new Timestamp(((Date)submissionData.getExtraKeys().get("rejectedOn")).getTime()));
			}else{
				dataObject.setTime(new Timestamp(((Date)submissionData.getExtraKeys().get("syncDate")).getTime()));
			}
			dataObject.setUsername(submissionData.getUserName());
			dataObject.setExtraKeys(submissionData.getExtraKeys());
			dataObject.setCreatedDate(sdfDateTimeWithSeconds.format(submissionData.getCreatedDate()));
			dataObject.setUpdatedDate(sdfDateTimeWithSeconds.format(submissionData.getUpdatedDate()));
			dataObject.setUniqueId(submissionData.getUniqueId());
			dataObject.setRejected(submissionData.isRejected());
			dataObject.setUniqueName(submissionData.getUniqueName());
			
			Map<String, Object> extraKeys = dataObject.getExtraKeys();
			extraKeys.put("isRejectable", true);
			
			List<Question> questionList = questionRepository
					.findAllByFormIdAndFormVersionAndActiveTrueOrderByQuestionOrderAsc(form.getFormId(),
							submissionData.getFormVersion());
			
			for (Question question : questionList) {
				iDbReviewQueryHandler.setReviewHeaders(dataObject, question, typeDetailsMap, submissionData, "dataReview");
			}
			dataObjects.add(dataObject);
			
			/**
			 * for every submission check whether superviosr data exist or not!!
			 * 
			 * if exist than in extra key map add "isSupervisor" key to "true"
			 * if not than make the above key to false
			 * 
			 */
			Integer supformId = webService.getSupervisorFormId(formId);
			Map<String, Object> paramKeyValMapSupv = new HashMap<>();
			paramKeyValMapSupv.put("submissionId", submissionData.getExtraKeys().get("submissionId").toString());

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

			Date date = new Date();
			String startDate = sdf.format(date);
			String endDate = sdf.format(date);
			HttpSession session=null;
			
			ReviewPageModel model = formsServiceImpl.getDataForReview(supformId, startDate, endDate, paramKeyValMapSupv, session,
					user, (Integer) user.getDesgSlugIds().toArray()[0]);
			
//			System.out.println(model);
			if(model.getReviewDataMap().get(supformId).isEmpty())
				extraKeys.put("isSupervisor", false);
			else
				extraKeys.put("isSupervisor", true);
		}
		
//		setRejectedData(dataObjects);
		
		/**
		 * SUBMITTED TAB DATA-- SORTING IS BASED ON syncDate
		 * 
		 * REJECTED TAB DATA -- SORTING IS BASED ON rejectionDate
		 */
		List<DataObject> submittedTabData = new ArrayList<>();
		List<DataObject> rejectedTabData = new ArrayList<>();
		
		List<DataObject> datasz = new ArrayList<>();
		
		submittedTabData = dataObjects.stream().filter(v->v.getRejected()==false).collect(Collectors.toList());
	
		rejectedTabData = dataObjects.stream().filter(v->v.getRejected()==true).collect(Collectors.toList());
		
		
		Collections.sort(submittedTabData, comparing(DataObject::getTime).reversed());
		Collections.sort(rejectedTabData, comparing(DataObject::getTime).reversed());
		
		datasz.addAll(submittedTabData);
		datasz.addAll(rejectedTabData);
		
		return datasz;
	}
	
	
	// @formatter:off


//	/**
//	 * get time period value with dateOfvist date of every submission, also get
//	 * current time period if both matches than data belongs to the current
//	 * month and rejection button would be disabled.
//	 * 
//	 * if timeperiod doesnt match and current date is in between 4th-of current
//	 * month to 10th of current month than rejection button would be enabled
//	 * 
//	 * 
//	 */
//	private void setRejectedData(List<DataObject> dataList) {
//
//		// filter all the rejected data from the dataList
//		dataList = dataList.stream().filter(data -> data.getRejected() == false).collect(Collectors.toList());
//		Date currentDate = new Date();
//		
//		try {
//			for (DataObject data : dataList) {
//
//				Map<String, Object> extraKeys = data.getExtraKeys();
//
//				Date dateOfVisit = new SimpleDateFormat("dd-MM-yyyy").parse(((String) extraKeys.get("dateOfVisit")));
//
//				TimePeriod dateOfVistiTimePeriod = submissionServiceImpl.fetchTimePeriod(dateOfVisit, data.getFormId());
//
//				TimePeriod currentTimePeriod = submissionServiceImpl.fetchTimePeriod(currentDate, data.getFormId());
//
//				if (dateOfVistiTimePeriod.equals(currentTimePeriod)) {
//					extraKeys.put("isRejectable", false);
//				} else {
//					/**
//					 * timeperiods are not equal than check the current date is
//					 * in between 4 to 10th (both inclusive)
//					 */
//					DateModel rejectionDates = getRejectionDates(currentDate);
//					if ((DateUtils.isSameDay(currentDate, rejectionDates.getStartDate())
//							|| (currentDate.after(rejectionDates.getStartDate()))
//									&& (DateUtils.isSameDay(currentDate, rejectionDates.getEndDate())
//											|| currentDate.before(rejectionDates.getEndDate()))))
//						extraKeys.put("isRejectable", true);
//					else
//						extraKeys.put("isRejectable", false);
//				}
//			}
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//
//	}

	// @formatter:on
	
	private DateModel getRejectionDates(Date date) {

		DateModel model = new DateModel();

		Calendar cal = Calendar.getInstance();

		cal.setTime(date);

		cal.set(Calendar.DATE, cal.getActualMinimum(Calendar.DATE));

		cal.set(Calendar.DATE, 4);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MILLISECOND, 0);

		model.setStartDate(cal.getTime());

		cal.set(Calendar.DATE, 10);

		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 59);

		model.setEndDate(cal.getTime());

		return model;
	}
	
	@Override
//	@Cacheable(value="submissionData", key = "#submissionId")
	public Map<String, List<Map<String, List<QuestionModel>>>> getViewMoreDataForReview(Integer formId, UserModel user, String submissionId,Map<String, Object> paramKeyValMap,HttpSession session) {
		
		DataModel submissionData = iDbFetchDataHandler.getSubmittedData(submissionId,formId);
	
		EnginesForm form = engineFormRepository.findByFormId(formId);
		
		List<TypeDetail> typeDetails = typeDetailRepository.findByFormId(form.getFormId());

		Map<Integer, TypeDetail> typeDetailsMap = typeDetails.stream()
				.collect(Collectors.toMap(TypeDetail::getSlugId, typeDe -> typeDe));
		
		Map<String, List<Map<String, List<QuestionModel>>>> mapOfSectionSubsectionListOfQuestionModel = new LinkedHashMap<>();

		List<QuestionModel> listOfQuestionModel = new LinkedList<>();

		Map<String, Map<String, List<QuestionModel>>> sectionMap = new LinkedHashMap<String, Map<String, List<QuestionModel>>>();
		Map<String, List<QuestionModel>> subsectionMap = null;

		/**
		 * for accordion
		 */

		QuestionModel questionModel = null;

		List<Question> questionList = questionRepository
				.findAllByFormIdAndFormVersionAndActiveTrueOrderByQuestionOrderAsc(form.getFormId(),
						submissionData.getFormVersion());

		Map<String, Question> questionMap = questionList.stream()
				.collect(Collectors.toMap(Question::getColumnName, question -> question));

		for (Question question : questionList) {

			questionModel = null;
			switch (question.getControllerType()) {
			case "Date Widget":
				if (question.getParentColumnName() == null || question.getParentColumnName().isEmpty()) {
					questionModel = engineUtils.prepareQuestionModel(question);
					if(submissionData.getData().get(question.getColumnName()) instanceof Date) {
						questionModel.setValue(ymdDateFormat.format(submissionData.getData().get(question.getColumnName())));
					}else {
						if (String.class.cast(submissionData.getData().get(question.getColumnName())) != null) {
							String dt = formsServiceImpl.getDateFromString(
									String.class.cast(submissionData.getData().get(question.getColumnName())));
							questionModel.setValue(dt);
						} else
							questionModel.setValue(null);
					}
					
					
				}
				break;
			case "Time Widget":
				if (question.getParentColumnName() == null || question.getParentColumnName().isEmpty()) {
					questionModel = engineUtils.prepareQuestionModel(question);
					questionModel
							.setValue(String.class.cast(submissionData.getData().get(question.getColumnName())));
				}
				break;

			case "checkbox": {
				if (question.getParentColumnName() == null || question.getParentColumnName().isEmpty()) {

					questionModel = engineUtils.prepareQuestionModel(question);

					// setting model
					if (submissionData != null) {
						questionModel = engineUtils.setTypeDetailsAsOptions(questionModel, typeDetailsMap, question,
								String.class.cast(submissionData.getData().get(question.getColumnName())), user,
								paramKeyValMap, session);
					} else {
						questionModel = engineUtils.setTypeDetailsAsOptions(questionModel, typeDetailsMap, question,
								null, user, paramKeyValMap, session);
					}
				}
			}
				break;
			case "textbox":
			case "textarea":
			case "geolocation": 
			case "Month Widget":
			{
				if (question.getParentColumnName() == null || question.getParentColumnName().isEmpty()) {

					questionModel = engineUtils.prepareQuestionModel(question);
					switch (question.getFieldType()) {

					case "singledecimal":
					case "doubledecimal":
					case "threedecimal":
						questionModel.setValue(submissionData.getData().get(question.getColumnName()) != null
								? String.valueOf(submissionData.getData().get(question.getColumnName()).toString())
								: null);
						break;

					case "tel":
						questionModel.setValue(submissionData.getData().get(question.getColumnName()) != null
								? Long.parseLong(submissionData.getData().get(question.getColumnName()).toString())
								: null);

						break;
					default:
						questionModel.setValue(submissionData.getData().get(question.getColumnName()) != null
								? String.valueOf(submissionData.getData().get(question.getColumnName()).toString())
								: null);
						break;
					}

				}
			}
				break;

			case "dropdown":
			case "segment": {
				switch (question.getFieldType()) {

				case "checkbox":
					if (question.getParentColumnName() == null || question.getParentColumnName().isEmpty()) {

						questionModel = engineUtils.prepareQuestionModel(question);

						// setting model
						if (submissionData != null) {
							if (submissionData.getData().get(question.getColumnName()) != null && submissionData
									.getData().get(question.getColumnName()) instanceof ArrayList) {

								String values = ((List<Integer>) submissionData.getData()
										.get(question.getColumnName())).stream().map(e -> e.toString())
												.collect(Collectors.joining(","));
								questionModel = engineUtils.setTypeDetailsAsOptions(questionModel, typeDetailsMap,
										question, values, user, paramKeyValMap, session);

							} else if (submissionData.getData().get(question.getColumnName()) != null
									&& submissionData.getData().get(question.getColumnName()) instanceof String) {
								questionModel = engineUtils.setTypeDetailsAsOptions(questionModel, typeDetailsMap,
										question,
										String.class.cast(submissionData.getData().get(question.getColumnName())),
										user, paramKeyValMap, session);
							}

						} else {
							questionModel = engineUtils.setTypeDetailsAsOptions(questionModel, typeDetailsMap,
									question, null, user, paramKeyValMap, session);
						}

						questionModel.setValue(submissionData.getData().get(question.getColumnName()));
					}
					break;
				default:
					if (question.getParentColumnName() == null || question.getParentColumnName().isEmpty()) {
						questionModel = engineUtils.prepareQuestionModel(question);
						questionModel = engineUtils.setTypeDetailsAsOptions(questionModel, typeDetailsMap, question,
								null, user, paramKeyValMap, session);

						questionModel
								.setValue(submissionData.getData().get(question.getColumnName()) != null
										? Integer.parseInt(
												submissionData.getData().get(question.getColumnName()).toString())
										: null);
					}
				}

			}
				break;
			case "table":
			case "tableWithRowWiseArithmetic": {
				questionModel = engineUtils.prepareQuestionModel(question);
				/**
				 * from table question id and cell parent id getting all matched cells here
				 */
				List<Question> tableCells = questionList.stream()
						.filter(q -> q.getParentColumnName().equals(question.getColumnName()))
						.collect(Collectors.toList());

				Map<String, List<Question>> groupWiseQuestionsMap = new LinkedHashMap<>();

				tableCells.forEach(cell -> {

					if (groupWiseQuestionsMap.get(cell.getQuestion().split("@@split@@")[0].trim()) == null) {
						List<Question> questionPerGroup = new ArrayList<>();
						questionPerGroup.add(cell);
						groupWiseQuestionsMap.put(cell.getQuestion().split("@@split@@")[0].trim(),
								questionPerGroup);
					} else {
						List<Question> questionPerGroup = groupWiseQuestionsMap
								.get(cell.getQuestion().split("@@split@@")[0].trim());
						questionPerGroup.add(cell);
						groupWiseQuestionsMap.put(cell.getQuestion().split("@@split@@")[0].trim(),
								questionPerGroup);
					}

				});

				List<Map<String, Object>> array = new LinkedList<>();
				Integer index = 0;
				for (Map.Entry<String, List<Question>> map : groupWiseQuestionsMap.entrySet()) {
					List<Question> qs = map.getValue();
					;
					Map<String, Object> jsonMap = new LinkedHashMap<String, Object>();
					jsonMap.put(question.getQuestion(), map.getKey());

					for (Question qdomain : qs) {
						QuestionModel qModel = engineUtils.prepareQuestionModel(qdomain);

						qModel.setValue(submissionData == null ? null
								: (List<Map<String, Integer>>) submissionData.getData()
										.get(question.getColumnName()) != null
												? (((List<Map<String, Integer>>) submissionData.getData()
														.get(question.getColumnName())).get(index)
																.get(qdomain.getColumnName()))
												: null);
						jsonMap.put(qdomain.getQuestion().split("@@split@@")[1].trim(), qModel);
					}
					index++;
					array.add(jsonMap);
				}

				questionModel.setTableModel(array);
			}
				break;

			case BEGIN_REPEAT: {
				questionModel = formsServiceImpl.prepareBeginRepeatModelWithData(question, questionList, submissionData, questionMap,
						typeDetailsMap, paramKeyValMap, session, user);

			}

				break;

			case "camera": {
				if (question.getParentColumnName() == null || question.getParentColumnName().isEmpty()) {

					questionModel = engineUtils.prepareQuestionModel(question);
					questionModel = iCameraDataHandler.readExternal(questionModel, submissionData,paramKeyValMap);

				}
			}
				break;
			}

			if (sectionMap.containsKey(question.getSection())) {

				if (subsectionMap.containsKey(question.getSubsection())) {

					/**
					 * checking the type of accordian here ie. RepeatSubSection()==no means not an
					 * accordian, and yes means accordian
					 */
					List<QuestionModel> list = (List<QuestionModel>) subsectionMap.get(question.getSubsection());
					if (questionModel != null)
						list.add(questionModel);

				} else {
					listOfQuestionModel = new LinkedList<>();
					if (questionModel != null)
						listOfQuestionModel.add(questionModel);
					subsectionMap.put(question.getSubsection(), listOfQuestionModel);
				}

			} else {
				subsectionMap = new LinkedHashMap<>();
				listOfQuestionModel = new ArrayList<>();
				if (questionModel != null)
					listOfQuestionModel.add(questionModel);
				subsectionMap.put(question.getSubsection(), listOfQuestionModel);

				sectionMap.put(question.getSection(), subsectionMap);
			}
		}

		/**
		 * adding list of subsection against a section.
		 */

		for (Map.Entry<String, Map<String, List<QuestionModel>>> entry : sectionMap.entrySet()) {

			if (mapOfSectionSubsectionListOfQuestionModel.containsKey(entry.getKey())) {
				mapOfSectionSubsectionListOfQuestionModel.get(entry.getKey()).add(entry.getValue());
			} else {
				mapOfSectionSubsectionListOfQuestionModel.put(entry.getKey(), Arrays.asList(entry.getValue()));
			}
		}
		return mapOfSectionSubsectionListOfQuestionModel;
	}


}
