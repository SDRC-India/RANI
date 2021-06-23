package org.sdrc.rani.job;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.sdrc.rani.document.CFInputFormData;
import org.sdrc.rani.document.PlanningData;
import org.sdrc.rani.document.TimePeriod;
import org.sdrc.rani.repositories.CFInputFormDataRepository;
import org.sdrc.rani.repositories.PlanningDataRepository;
import org.sdrc.rani.repositories.TimePeriodRepository;
import org.sdrc.usermgmt.mongodb.domain.Designation;
import org.sdrc.usermgmt.mongodb.repository.DesignationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mongodb.AggregationOptions;
import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import lombok.extern.slf4j.Slf4j;

/**
 * @author subham
 *
 */
@Slf4j
@Component
public class JobService implements TimePeriodService {

	@Autowired
	private TimePeriodRepository timePeriodRepository;

	@Autowired
	private PlanningDataRepository planningDataRepository;

	@Autowired
	@Qualifier("mongoDesignationRepository")
	private DesignationRepository designationRepository;

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private CFInputFormDataRepository cFInputFormDataRepository;

	/**
	 * @author Subham
	 * @throws ParseException
	 * 
	 * @Description On first day of every month this method will execute,
	 *              creating time period in TimePeriod Table. (in every month
	 *              for a periodicity 1). schedules on 12:05am
	 */
	@Scheduled(cron = "0 5 0 1 1/1 ?")
	public void createMonthlyTimePeriod() throws ParseException {

		log.info("Timperiod job for creating monthly timePeriod called!!!!!!!!!!");
		LocalDateTime time = LocalDateTime.now();
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("MMM");
		int month = cal.get(Calendar.MONTH);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 00);
		cal.set(Calendar.MINUTE, 00);
		cal.set(Calendar.SECOND, 00);
		cal.set(Calendar.MILLISECOND, 00);

		TimePeriod timePeriod = new TimePeriod();

		Date startDate = cal.getTime();

		timePeriod.setStartDate(startDate);
		String sDate = sdf.format(startDate);
		
		
		cal.add(Calendar.MONTH, 0);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 59);
		
		Date endDate = cal.getTime();
		String eDate = sdf.format(endDate);

		timePeriod.setEndDate(endDate);

		timePeriod.setPeriodicity("1"); // for periodicity
		timePeriod.setTimePeriodDuration(sDate.equals(eDate) ? sDate : sDate + "-" + eDate);
		timePeriod.setYear(time.getYear()); // for year

		int preYear = 0, nextYear = 0;

		if (month > 2) {
			preYear = cal.get(Calendar.YEAR);
			cal.add(Calendar.YEAR, 1);
			nextYear = cal.get(Calendar.YEAR);
		} else {
			cal.add(Calendar.YEAR, -1);
			preYear = cal.get(Calendar.YEAR);
			cal.add(Calendar.YEAR, 1);
			nextYear = cal.get(Calendar.YEAR);
		}
		timePeriod.setFinancialYear(preYear + "-" + nextYear);

		/**
		 * ftech maximum timePeriod id present
		 */
		timePeriod.setTimePeriodId(getTimePeriodIdCount() + 1);
		timePeriodRepository.save(timePeriod);
		log.info("timeperiod created {}", timePeriod);
	}

	/**
	 * this cron job triggers in the month of april,july,oct,jan at 12:08am
	 */

	@Scheduled(cron = "0 8 0 1 1/3 ?")
	public void schedulePlanning() {

		/**
		 * executing plan for community facilitator
		 */
		schedulePlanningForCommunityFacilitator();
		/**
		 * executing plan for supervisor
		 */
		schedulePlanningForSupervisor();

	}

	public void schedulePlanningForCommunityFacilitator() {

		try {
			
		
		Date currentDate = new Date();

		/**
		 * if month is march than check april,may,june data is available or not
		 * if any of the month planning data is not available than enter the
		 * planning by taking feb month data.
		 * 
		 * subsequently its same as next 3 quarter
		 */

		Designation desg = designationRepository.findByCode("003");
		List<PlanningData> datas = null;

		List<PlanningData> newPlanDataList = new ArrayList<>();

		Calendar cal = Calendar.getInstance();
		cal.setTime(currentDate);
		int currentMonth = cal.get(Calendar.MONTH) + 1;
		int currentyear = cal.get(Calendar.YEAR);

		Integer month = null;
		Integer year = null;
		// checking april month data available or not
		datas = planningDataRepository.findByMonthAndYearAndDesgId(currentMonth, currentyear, desg.getId());

		/**
		 * if there is no data than insert new data
		 */
		if (datas.isEmpty()) {

			/**
			 * if month is april than get feb month data.
			 *
			 */
			cal.add(Calendar.MONTH, -1);
			month = cal.get(Calendar.MONTH);
			year = cal.get(Calendar.YEAR);

			datas = planningDataRepository.findByMonthAndYearAndDesgId(month, year, desg.getId());

			newPlanDataList = getNewPlanningListData(datas, newPlanDataList, currentMonth, currentyear);
		}

		cal = Calendar.getInstance();
		cal.setTime(currentDate);
		cal.add(Calendar.MONTH, 1);
		currentMonth = cal.get(Calendar.MONTH) + 1;
		currentyear = cal.get(Calendar.YEAR);

		/**
		 * checking may month data available or not
		 */
		datas = planningDataRepository.findByMonthAndYearAndDesgId(currentMonth, currentyear, desg.getId());

		/**
		 * if there is no data than insert new data
		 */
		if (datas.isEmpty()) {

			/**
			 * if month is may than get feb month data.
			 *
			 */
			cal.add(Calendar.MONTH, -2);
			month = cal.get(Calendar.MONTH);
			year = cal.get(Calendar.YEAR);

			datas = planningDataRepository.findByMonthAndYearAndDesgId(month, year, desg.getId());

			newPlanDataList = getNewPlanningListData(datas, newPlanDataList, currentMonth, currentyear);

		}

		cal = Calendar.getInstance();
		cal.setTime(currentDate);
		cal.add(Calendar.MONTH, 2);
		currentMonth = cal.get(Calendar.MONTH) + 1;
		currentyear = cal.get(Calendar.YEAR);

		/**
		 * checking june month data available or not
		 */
		datas = planningDataRepository.findByMonthAndYearAndDesgId(currentMonth, currentyear, desg.getId());

		/**
		 * if there is no data than insert new data
		 */
		if (datas.isEmpty()) {

			/**
			 * if month is june than get feb month data.
			 *
			 */
			cal.add(Calendar.MONTH, -3);
			month = cal.get(Calendar.MONTH);
			year = cal.get(Calendar.YEAR);

			datas = planningDataRepository.findByMonthAndYearAndDesgId(month, year, desg.getId());

			newPlanDataList = getNewPlanningListData(datas, newPlanDataList, currentMonth, currentyear);

		}
		if (!newPlanDataList.isEmpty())
			planningDataRepository.save(newPlanDataList);
		}catch(Exception e) {
			log.error("Action : while planning module cron job schedule >>>  ",e);
			throw new RuntimeException(e);
		}
	}

	public void schedulePlanningForSupervisor() {

		Date currentDate = new Date();

		/**
		 * if month is march than check april,may,june data is available or not
		 * if any of the month planning data is not available than enter the
		 * planning by taking feb month data.
		 * 
		 * subsequently its same as next 3 quarter
		 */

		Designation desg = designationRepository.findByCode("002");
		List<PlanningData> datas = null;

		List<PlanningData> newPlanDataList = new ArrayList<>();

		Calendar cal = Calendar.getInstance();
		cal.setTime(currentDate);
		int currentMonth = cal.get(Calendar.MONTH) + 1;
		int currentyear = cal.get(Calendar.YEAR);

		Integer month = null;
		Integer year = null;
		// checking april month data available or not
		datas = planningDataRepository.findByMonthAndYearAndDesgId(currentMonth, currentyear, desg.getId());

		/**
		 * if there is no data than insert new data
		 */
		if (datas.isEmpty()) {

			/**
			 * if month is april than get feb month data.
			 *
			 */
			cal.add(Calendar.MONTH, -1);
			month = cal.get(Calendar.MONTH);
			year = cal.get(Calendar.YEAR);

			datas = planningDataRepository.findByMonthAndYearAndDesgId(month, year, desg.getId());

			newPlanDataList = getNewPlanningListData(datas, newPlanDataList, currentMonth, currentyear);
		}

		cal = Calendar.getInstance();
		cal.setTime(currentDate);
		cal.add(Calendar.MONTH, 1);
		currentMonth = cal.get(Calendar.MONTH) + 1;
		currentyear = cal.get(Calendar.YEAR);

		/**
		 * checking may month data available or not
		 */
		datas = planningDataRepository.findByMonthAndYearAndDesgId(currentMonth, currentyear, desg.getId());

		/**
		 * if there is no data than insert new data
		 */
		if (datas.isEmpty()) {

			/**
			 * if month is may than get feb month data.
			 *
			 */
			cal.add(Calendar.MONTH, -2);
			month = cal.get(Calendar.MONTH);
			year = cal.get(Calendar.YEAR);

			datas = planningDataRepository.findByMonthAndYearAndDesgId(month, year, desg.getId());

			newPlanDataList = getNewPlanningListData(datas, newPlanDataList, currentMonth, currentyear);

		}

		cal = Calendar.getInstance();
		cal.setTime(currentDate);
		cal.add(Calendar.MONTH, 2);
		currentMonth = cal.get(Calendar.MONTH) + 1;
		currentyear = cal.get(Calendar.YEAR);

		/**
		 * checking june month data available or not
		 */
		datas = planningDataRepository.findByMonthAndYearAndDesgId(currentMonth, currentyear, desg.getId());

		/**
		 * if there is no data than insert new data
		 */
		if (datas.isEmpty()) {

			/**
			 * if month is june than get feb month data.
			 *
			 */
			cal.add(Calendar.MONTH, -3);
			month = cal.get(Calendar.MONTH);
			year = cal.get(Calendar.YEAR);

			datas = planningDataRepository.findByMonthAndYearAndDesgId(month, year, desg.getId());

			newPlanDataList = getNewPlanningListData(datas, newPlanDataList, currentMonth, currentyear);

		}
		if (!newPlanDataList.isEmpty())
			planningDataRepository.save(newPlanDataList);
		// log.info("Action: while");
		

	}

	private List<PlanningData> getNewPlanningListData(List<PlanningData> datas, List<PlanningData> newPlanDataList,
			int month, int year) {
		for (PlanningData planData : datas) {

			/**
			 * even though data is being entered by system autmatically we
			 * required
			 */
			PlanningData pData = new PlanningData();
			pData.setAccId(planData.getAccId());
			pData.setCreatedDate(new Date());
			pData.setDesgId(planData.getDesgId());
			pData.setFormId(planData.getFormId());
			pData.setMonth(month);
			pData.setTarget(planData.getTarget());
			pData.setUpdatedDate(new Date());
			pData.setUserName(planData.getUserName());
			pData.setYear(year);
			newPlanDataList.add(pData);
		}
		return newPlanDataList;
	}

	public Integer getTimePeriodIdCount() {

		try {
			DBCollection collection = mongoTemplate.getCollection("timePeriod");

			DBObject sort = new BasicDBObject("$sort", new BasicDBObject("timePeriodId", -1));
			DBObject group = new BasicDBObject("$group",
					new BasicDBObject("_id", null).append("total", new BasicDBObject("$max", "$timePeriodId")));

			AggregationOptions aggregationOptions = AggregationOptions.builder()
					.outputMode(AggregationOptions.OutputMode.CURSOR).batchSize(25).allowDiskUse(true).build();

			List<DBObject> pipeline = new ArrayList<>();
			pipeline.add(sort);
			pipeline.add(group);

			Cursor aggregate = collection.aggregate(pipeline, aggregationOptions);

			Integer result = 0;
			while (aggregate.hasNext()) {

				DBObject next = aggregate.next();

				result = Integer.valueOf(next.get("total").toString());

			}
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@Scheduled(cron="0 20 0 11 * ?")
	public void updateAggregatedData() {
		
		/**
		 * get the latest time period present in AggregationDetails, and check its status
		 */
		Integer timePeriodId=0;
		try {
			DBCollection collection = mongoTemplate.getCollection("aggregationDetails");

			DBObject sort = new BasicDBObject("$sort", new BasicDBObject("timePeriod.timePeriodId", -1));
			
			DBObject group = new BasicDBObject("$group",
					new BasicDBObject("_id", null).append("timePeriodId", new BasicDBObject("$max", "$timePeriod.timePeriodId")).append("aggregationStatus", new BasicDBObject("$max", "$status")));

			AggregationOptions aggregationOptions = AggregationOptions.builder()
					.outputMode(AggregationOptions.OutputMode.CURSOR).batchSize(25).allowDiskUse(true).build();

			List<DBObject> pipeline = new ArrayList<>();
			pipeline.add(sort);
			pipeline.add(group);

			Cursor aggregate = collection.aggregate(pipeline, aggregationOptions);

			String aggregationStatus=null;
			while (aggregate.hasNext()) {

				DBObject next = aggregate.next();
				timePeriodId = Integer.valueOf(next.get("timePeriodId").toString());
				aggregationStatus=next.get("aggregationStatus").toString();
			}
			
			if(timePeriodId!=0 && aggregationStatus!=null) {
				
				MatchOperation match = Aggregation.match(
						Criteria.where("isValid").is(true)
						.and("isAggregated").is(false)
						.and("rejected").is(false)
						.and("isDeleted").is(false)
						.and("timePeriod.timePeriodId").is(timePeriodId)
						.and("submissionCompleteStatus").is("C")
						);

				GroupOperation groupz = Aggregation.group("uniqueId")
					.push("$$ROOT").as("submissions");
			
				AggregationOperation replaceRoot = Aggregation.replaceRoot()
					.withValueOf(ArrayOperators.ArrayElemAt.arrayOf("submissions").elementAt(0));
			
				List<CFInputFormData> dataz = mongoTemplate.aggregate(
						Aggregation.newAggregation(match, groupz, replaceRoot),CFInputFormData.class, 
							CFInputFormData.class).getMappedResults();
				
				log.info("Total number of data aggregated is : {}",dataz.size());
				
				if(!dataz.isEmpty()) {
					for(CFInputFormData da : dataz) {
						da.setIsAggregated(true);
						da.setAggregatedDate(new Date());
					}
					cFInputFormDataRepository.save(dataz);
				}
			}
			
		} catch (Exception e) {
			log.error("Action : while updating aggregation result with time periodId {}",timePeriodId,e);
			throw new RuntimeException(e);
		}
	}

}
