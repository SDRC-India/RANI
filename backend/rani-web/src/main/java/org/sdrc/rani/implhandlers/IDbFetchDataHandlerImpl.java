package org.sdrc.rani.implhandlers;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.time.DateUtils;
import org.sdrc.rani.document.CFInputFormData;
import org.sdrc.rani.document.FormMapping;
import org.sdrc.rani.document.TimePeriod;
import org.sdrc.rani.models.DateModel;
import org.sdrc.rani.repositories.CFInputFormDataRepository;
import org.sdrc.rani.repositories.FormMappingRepository;
import org.sdrc.rani.repositories.TimePeriodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import in.co.sdrc.sdrcdatacollector.document.EnginesForm;
import in.co.sdrc.sdrcdatacollector.document.TypeDetail;
import in.co.sdrc.sdrcdatacollector.handlers.IDbFetchDataHandler;
import in.co.sdrc.sdrcdatacollector.models.DataModel;
import in.co.sdrc.sdrcdatacollector.models.RawDataModel;
import in.co.sdrc.sdrcdatacollector.mongorepositories.EngineFormRepository;
import in.co.sdrc.sdrcdatacollector.mongorepositories.TypeDetailRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
@Component
@Slf4j
public class IDbFetchDataHandlerImpl implements IDbFetchDataHandler {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private CFInputFormDataRepository cfInputFormDataRepository;

	@Autowired
	private ConfigurableEnvironment configurableEnvironment;

	@Autowired
	private TypeDetailRepository typeDetailRepository;

	@Autowired
	private FormMappingRepository formMappingRepository;
	
	@Autowired
	private EngineFormRepository engineFormRepository;
	
	@Autowired
	private TimePeriodRepository timePeriodRepository;

	@Override
	public List<DataModel> fetchDataFromDb(EnginesForm mapping, String type, Map<Integer, String> mapOfForms,
			Date startDate, Date endDate, Map<String, Object> paramKeyValMap, HttpSession session, Object user) {

		List<CFInputFormData> entriesList = null;
		List<DataModel> submissionDatas = new ArrayList<>();
//		PaginationModel paginationModel = null;
		/**
		 * for supervisor forms
		 * 
		 * get the submission id from paramKeyValMap query the db with
		 * submissionId and formid
		 */
		if (paramKeyValMap.containsKey("submissionId")) {

			/**
			 * get CF form id for supervisor id
			 */
			FormMapping formMapping = formMappingRepository.findBySupervisorFormId(mapping.getFormId());

			CFInputFormData submissionData = cfInputFormDataRepository
					.findByIdAndFormId(paramKeyValMap.get("submissionId").toString(), formMapping.getCfFormId());

			Integer villageId = null;
			String dateOfVisit = null;
			Integer componentId = null;
			Map<String, Object> dataMap = null;

			if (submissionData != null) {

				switch (mapping.getFormId()) {

				case 7:// T4-APPROACH
				{

					/**
					 * get the village,dateofvisit,and t4component
					 */
					dataMap = submissionData.getData();
					villageId = (Integer) dataMap.get(configurableEnvironment.getProperty("form1.village.keyvalue"));
					dateOfVisit = (String) dataMap.get(configurableEnvironment.getProperty("form1.datevisit.key"));
					componentId = (Integer) dataMap.get(configurableEnvironment.getProperty("form1.component.key"));

					/**
					 * T4componentName id is different in each form, so its
					 * obvious that it will never match,
					 * 
					 * get the equivalent id matching the same component name
					 */

					TypeDetail typeDe = typeDetailRepository.findBySlugId(componentId);
					String typeDetailNames = typeDe.getName();
					TypeDetail typeDetail = typeDetailRepository.findByFormIdAndName(mapping.getFormId(),
							typeDetailNames);

					entriesList = cfInputFormDataRepository.getT4DataByVillageDateOfVisitComponentNameAndFormId(
							villageId, dateOfVisit, typeDetail.getSlugId(), mapping.getFormId());
				}
					break;

				case 8:// CEM
				{
					/**
					 * get the village,dateofvisit,and t4component
					 */
					dataMap = submissionData.getData();
					villageId = (Integer) dataMap.get(configurableEnvironment.getProperty("form2.village.keyvalue"));
					dateOfVisit = (String) dataMap.get(configurableEnvironment.getProperty("form2.datevisit.key"));
					componentId = (Integer) dataMap.get(configurableEnvironment.getProperty("form2.component.key"));

					/**
					 * T4componentName id is different in each form, so its
					 * obvious that it will never match,
					 * 
					 * get the equivalent id matching the same component name
					 */

					TypeDetail typeDe = typeDetailRepository.findBySlugId(componentId);
					String typeDetailNames = typeDe.getName();
					TypeDetail typeDetail = typeDetailRepository.findByFormIdAndName(mapping.getFormId(),
							typeDetailNames);

					entriesList = cfInputFormDataRepository.getCEMdataByVillageDateOfVisitComponentNameAndFormId(
							villageId, dateOfVisit, typeDetail.getSlugId(), mapping.getFormId());
				}
					break;

				/**
				 * get the village id and date of visit form submissionData than
				 * with that value query to get supervisor data
				 */
				case 9:// MEDIA

					dataMap = submissionData.getData();
					villageId = (Integer) dataMap.get(configurableEnvironment.getProperty("form3.village.keyvalue"));
					dateOfVisit = (String) dataMap.get(configurableEnvironment.getProperty("form3.datevisit.key"));

					entriesList = cfInputFormDataRepository.getMediaDataByVillageDateOfVisitAndFormId(villageId,
							dateOfVisit, mapping.getFormId());

					break;

				case 10:// HT

					dataMap = submissionData.getData();
					villageId = (Integer) dataMap.get(configurableEnvironment.getProperty("form4.village.keyvalue"));
					dateOfVisit = (String) dataMap.get(configurableEnvironment.getProperty("form4.datevisit.key"));

					entriesList = cfInputFormDataRepository.getHemocueDataByVillageDateOfVisitAndFormId(villageId,
							dateOfVisit, mapping.getFormId());

					break;

				case 11: // SMS

					dataMap = submissionData.getData();
					villageId = (Integer) dataMap.get(configurableEnvironment.getProperty("form5.village.keyvalue"));
					dateOfVisit = (String) dataMap.get(configurableEnvironment.getProperty("form5.datevisit.key"));

					entriesList = cfInputFormDataRepository.getSMSdataByVillageDateOfVisitAndFormId(villageId,
							dateOfVisit, mapping.getFormId());

					break;

				}
			}

		} else {

			switch (mapping.getFormId()) {

			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
				switch (type) {

				case "dataReview":
				{
					
				
					entriesList = mongoTemplate
							.aggregate(getAggregationResults(mapping, type, startDate, endDate, paramKeyValMap),
									CFInputFormData.class, CFInputFormData.class)
							.getMappedResults();
					
					
					Date currentDate = new Date();
					
					
					/**
					 * filter data to be present in current month time period  and last month timeperiod only
					 */
					
					TimePeriod currentTimePeriod = timePeriodRepository.getCurrentTimePeriod(currentDate,"1");
					
					Date lastMonthDate = getLastMonthDateFromCurrentDate(currentDate);
					
					TimePeriod lastMonthTimePeriod = timePeriodRepository.getCurrentTimePeriod(lastMonthDate, "1");
					
					DateModel dateModel = checkDateForCurrentTimePeriod(currentDate);
					
						//only for current time period ie. date from 11 to last day of month
					if ((DateUtils.isSameDay(currentDate, dateModel.getStartDate())
							|| (currentDate.after(dateModel.getStartDate()))
									&& (DateUtils.isSameDay(currentDate, dateModel.getEndDate())
											|| currentDate.before(dateModel.getEndDate())))) {
							
							entriesList = entriesList.stream().filter(d->d.getTimePeriod().getTimePeriodId()==currentTimePeriod.getTimePeriodId())
									.collect(Collectors.toList());
						}else {
							
							entriesList = entriesList.stream().filter(d->d.getTimePeriod().getTimePeriodId()==currentTimePeriod.getTimePeriodId()
									|| d.getTimePeriod().getTimePeriodId()==lastMonthTimePeriod.getTimePeriodId()).collect(Collectors.toList());
							
						}

				}
					break;

				case "rejectedData":
					entriesList = mongoTemplate
							.aggregate(getAggregationResults(mapping, type, null, null, paramKeyValMap),
									CFInputFormData.class, CFInputFormData.class)
							.getMappedResults().stream().filter(value -> value.isRejected() == true)
							.collect(Collectors.toList());
					
					
					Date currentDate = new Date();
					
					
					/**
					 * filter data to be present in current month time period  and last month timeperiod only
					 */
					
					TimePeriod currentTimePeriod = timePeriodRepository.getCurrentTimePeriod(currentDate,"1");
					
					Date lastMonthDate = getLastMonthDateFromCurrentDate(currentDate);
					
					TimePeriod lastMonthTimePeriod = timePeriodRepository.getCurrentTimePeriod(lastMonthDate, "1");
					
					DateModel dateModel = checkDateForCurrentTimePeriod(currentDate);
					
						//only for current time period ie. date from 11 to last day of month
					if ((DateUtils.isSameDay(currentDate, dateModel.getStartDate())
							|| (currentDate.after(dateModel.getStartDate()))
									&& (DateUtils.isSameDay(currentDate, dateModel.getEndDate())
											|| currentDate.before(dateModel.getEndDate())))) {
							
							entriesList = entriesList.stream().filter(d->d.getTimePeriod().getTimePeriodId()==currentTimePeriod.getTimePeriodId())
									.collect(Collectors.toList());
						}else {
							
							entriesList = entriesList.stream().filter(d->d.getTimePeriod().getTimePeriodId()==currentTimePeriod.getTimePeriodId()
									|| d.getTimePeriod().getTimePeriodId()==lastMonthTimePeriod.getTimePeriodId()).collect(Collectors.toList());
							
						}
						
					break;
				}

				break;

			}

		}

		if (entriesList != null && !entriesList.isEmpty()) {

			for (CFInputFormData data : entriesList) {

				Map<String, Object> extraKeys = new HashMap<>();

				extraKeys.put("submittedOn",new SimpleDateFormat("dd-MM-yyyy").format(data.getSyncDate()));
				extraKeys.put("syncDate",data.getSyncDate());
				
				if (data.getRejectMessage() != null) {

					extraKeys.put("rejectMessage", data.getRejectMessage());
					
					if(data.getRejectedDate()!=null){
						extraKeys.put("rejectedOn", data.getRejectedDate());
						extraKeys.put("rejectionDate", new SimpleDateFormat("dd-MM-yyyy").format(data.getRejectedDate()));
					}
						
					
					if (data.getRejectedBy() == null) {
						extraKeys.put("rejectedBy", "Auto rejected by system");
						
					} else{
						extraKeys.put("rejectedBy", data.getRejectedBy().getUserName());
					}
					
				}

				extraKeys.put("submissionId", data.getId());
				extraKeys.put("isReSubmitted", data.getIsReSubmitted());
				
				DataModel model = new DataModel();
				model.setCreatedDate(data.getCreatedDate());
				model.setData(data.getData());
				model.setFormId(data.getFormId());
				model.setId(data.getId());
				model.setRejected(data.isRejected());
				model.setUniqueId(data.getUniqueId());
				model.setUpdatedDate(data.getUpdatedDate());
				model.setUserId(data.getUserId());
				model.setUserName(data.getUserName());
				model.setExtraKeys(extraKeys);
				model.setFormVersion(mapping.getVersion());
				model.setAttachments(data.getAttachments());
				submissionDatas.add(model);

			}
		}
		return submissionDatas;
	}

	private Date getLastMonthDateFromCurrentDate(Date currentDate) {
		
		try {
			Instant instant = Instant.ofEpochMilli(currentDate.getTime());
			LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
			
			localDateTime = localDateTime.minusMonths(1);
			
			Date date = Date.from( localDateTime.atZone( ZoneId.systemDefault()).toInstant());
			
			return date;
		}catch(Exception e) {
			log.error("Action while converting java.util.date to localdate time in IDbFetchDataHandler class : ",e);
			throw new RuntimeException(e);
		}
		
		
	}

	private DateModel checkDateForCurrentTimePeriod(Date date) {
		
		DateModel model = new DateModel();

		Calendar cal = Calendar.getInstance();

		cal.setTime(date);

//		cal.set(Calendar.DATE, cal.getActualMinimum(Calendar.DATE));

		cal.set(Calendar.DATE, 11);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MILLISECOND, 0);

		model.setStartDate(cal.getTime());

		cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE));
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 59);

		model.setEndDate(cal.getTime());

		return model;
		
		
	}

	private Aggregation getAggregationResults(EnginesForm mapping, String type, Date startDate, Date endDate,
			Map<String, Object> paramKeyValMap) {

		MatchOperation match = null;

		String userName = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		// @formatter:off
		if ("rejectedData".equals(type)) {
			
			match = Aggregation.match(Criteria.where("formId").is(mapping.getFormId())
					.and("isValid").is(true)
					.and("isAggregated").is(false)
					.and("userName").is(userName)
					.and("submissionCompleteStatus").is("C"));
		}
		
		SortOperation sort = Aggregation
				.sort(Sort.Direction.ASC, "userId")
				.and(Sort.Direction.ASC, "formId")
				.and(Sort.Direction.DESC, "uniqueId")
				.and(Sort.Direction.ASC, "syncDate");

		
		if ("dataReview".equals(type)) {

//			/**
//			 * For pagination in submission management,
//			 * considering 10 records per page
//			 *  
//			 *  for pageno-1
//			 *  skipValue=0
//			 *  limitValue=10
//			 *  
//			 *  for pageno - 2
//			 * 	
//			 * (2-1)*10==10 so skip 10 and limit 10.
//			 * 
//			 * 
//			 */
//			Integer limitValue = Integer.valueOf(configurableEnvironment.getProperty("limit.submission.management.value"));
//			Integer pageNo = (Integer) paramKeyValMap.get("pageNo");
//			Integer skipValue = (pageNo-1)*limitValue;
			
			SortOperation sortreviewData = Aggregation
					.sort(Sort.Direction.DESC, "uniqueId")
					.and(Sort.Direction.DESC,"syncDate");

			match = Aggregation.match(
						Criteria.where("formId").is(mapping.getFormId())
						.and("isValid").is(true)
						.and("isAggregated").is(false)
						.and("submissionCompleteStatus").is("C"));

			GroupOperation groupReview = Aggregation.group("uniqueId")
					.push("$$ROOT").as("submissions");
			
			
			AggregationOperation replaceRoot = Aggregation.replaceRoot()
					.withValueOf(ArrayOperators.ArrayElemAt.arrayOf("submissions").elementAt(0));
			
//			AggregationOperation countOperation = (Aggregation.group().count().as("count"));
			
//			FacetOperation facetPagination = Aggregation.facet(new SkipOperation(skipValue), new LimitOperation(limitValue)).as("results").and(countOperation).as("count");

			/**
			 * SkipOperation-skip the number of records, while aggregating
			 * LimitOperation-limit the number of record to be fetched
			 */
					
			return Aggregation.newAggregation(match, sortreviewData, groupReview, replaceRoot);
		}
		
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
				.last("submissionStatus").as("submissionStatus")
				.last("isReSubmitted").as("isReSubmitted");
		
		// @formatter:on
		return Aggregation.newAggregation(match, group, sort);
	}

	@Override
	public DataModel getSubmittedData(String submissionId, Integer formId) {
		
		CFInputFormData submittedData = cfInputFormDataRepository.findByIdAndFormId(submissionId, formId);
		EnginesForm form = engineFormRepository.findByFormId(submittedData.getFormId());
		
		DataModel model = new DataModel();
		
		model.setAttachments(submittedData.getAttachments());
		model.setCreatedDate(submittedData.getCreatedDate());
		model.setData(submittedData.getData());
		model.setFormId(submittedData.getFormId());
		model.setFormVersion(form.getVersion());
		model.setId(submittedData.getId());
		model.setRejected(submittedData.isRejected());
		model.setUniqueId(submittedData.getUniqueId());
		model.setUpdatedDate(submittedData.getUpdatedDate());
		model.setUserId(submittedData.getUserId());
		model.setUserName(submittedData.getUserName());
		
		return model;
	}

	@Override
	public RawDataModel findAllByRejectedFalseAndSyncDateBetween(Integer formId, Date startDate, Date endDate) {

		return null;
	}
}
