package org.sdrc.rani.service;

import static org.springframework.data.mongodb.core.aggregation.ConditionalOperators.when;
import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.sdrc.rani.document.ClusterDataValue;
import org.sdrc.rani.document.ClusterForAggregation;
import org.sdrc.rani.document.DataValue;
import org.sdrc.rani.document.DesignationFormMapping;
import org.sdrc.rani.document.TimePeriod;
import org.sdrc.rani.document.UserDatumValue;
import org.sdrc.rani.models.LineChartModel;
import org.sdrc.rani.models.PerformanceData;
import org.sdrc.rani.repositories.ClusterForAggregationRepository;
import org.sdrc.rani.repositories.DataDomainRepository;
import org.sdrc.rani.repositories.TimePeriodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AccumulatorOperators.Sum;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

/*
 * @author Biswabhusan Pradhan
 * 
 */

@Service
public class PerformanceReportServiceImpl implements PerformanceReportService {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private ConvertAggregatedData convertAggregatedData;
	
	@Autowired
	private ClusterForAggregationRepository clusterForAggregationRepository;
	
	@Autowired
	private TimePeriodRepository timePeriodRepository;
	
	@Autowired
	private ConfigurableEnvironment configurableEnvironment;
	
	@Autowired
	private DataDomainRepository dataDomainRepository;
	
	@Override
	public PerformanceData getPerformanceData(Integer formId,String designation,Integer startTp, Integer endTp) {
		// TODO Auto-generated method stub
		List<TimePeriod> timeperiods=timePeriodRepository.findTimePeriodRange("1", startTp, endTp);
		List<Integer> tpids=timeperiods.stream().map(m->m.getTimePeriodId()).collect(Collectors.toList());
		MatchOperation mop=Aggregation.match(Criteria.where("inid").in(
				Arrays.asList(configurableEnvironment.getProperty("submission_"+formId+"_"+designation).split(","))
				.stream().map(v->Integer.parseInt(v)).collect(Collectors.toList())).and("tp").in(tpids));
		LookupOperation lop=Aggregation.lookup("timePeriod", "tp", "timePeriodId", "timeperiod");
		UnwindOperation uop=Aggregation.unwind("timeperiod");
		
		LookupOperation lop1 = Aggregation.lookup("account", "datumId", "userName", "user");
		UnwindOperation uop1 = Aggregation.unwind("user");
		
		ProjectionOperation pop=Aggregation.project().and("timeperiod.timePeriodDuration").as("timeperiod").and("timeperiod.year").as("year")
								.and("datumId").as("datumId").and("user.userDetails.desgSlugId").as("designation")
								.and("user.userDetails.fullName").as("name")
								.and(when(where("inid").is(Integer.parseInt(configurableEnvironment.getProperty("ontime_"+formId)))).thenValueOf(Sum.sumOf("dataValue")).otherwise(0)).as("ontime")
								.and(when(where("inid").is(Integer.parseInt(configurableEnvironment.getProperty("delayed_"+formId)))).thenValueOf(Sum.sumOf("dataValue")).otherwise(0)).as("delayed")
								.and(when(where("inid").is(Integer.parseInt(configurableEnvironment.getProperty("invalid_"+formId)))).thenValueOf(Sum.sumOf("dataValue")).otherwise(0)).as("invalid");
		GroupOperation gop=Aggregation.group("timeperiod", "year", "datumId","designation","name").sum("ontime").as("ontime").sum("delayed").as("delayed").sum("invalid").as("invalid");
		ProjectionOperation pop2 = Aggregation.project().and("_id.timeperiod").as("timeperiod").and("_id.year").as("year")
				.and("_id.datumId").as("datumId").and("_id.designation").as("designation").and("_id.name").as("name")
				.and("ontime").as("ontime").and("delayed").as("delayed").and("invalid").as("invalid");
		MatchOperation mop1 = Aggregation.match(Criteria.where("designation").is(Integer.parseInt(designation)));
		
		Aggregation resultQuery=Aggregation.newAggregation(mop,lop,uop,lop1,uop1,pop,gop,pop2,mop1);
		List<Map> dataMap=mongoTemplate.aggregate(resultQuery,UserDatumValue.class, Map.class).getMappedResults();
		List<String> columnList=new ArrayList<>();
		columnList.add("ontime");
		columnList.add("delayed");
		columnList.add("invalid");

		return convertAggregatedData.collectIndicatorValueForMultipleTimePeriod(dataMap, "name", columnList);
	}

	@Override
	public PerformanceData getHemocueData(String areaLevel,Integer startTp, Integer endTp) {
		// TODO Auto-generated method stub
		List<ClusterForAggregation> areas=clusterForAggregationRepository.findAll();
		List<?> areaIds=new ArrayList<>();
		areaIds=areaLevel.equals("village")?areas.stream().map(m->m.getVillage()).collect(Collectors.toList())
				:areas.stream().map(m->m.getClusterNumber()).collect(Collectors.toList());
		List<TimePeriod> timeperiods=timePeriodRepository.findTimePeriodRange("1", startTp, endTp);
		List<Integer> tpids=timeperiods.stream().map(m->m.getTimePeriodId()).collect(Collectors.toList());
		MatchOperation mop=Aggregation.match(Criteria.where("inid").in(206,207,208,209).and("tp").in(tpids).and("datumId").in(areaIds));
		MatchOperation mop1=Aggregation.match(Criteria.where("inid").in(206,207,208,209).and("tp").in(tpids).and("areaId").in(areaIds));
		ProjectionOperation pop=Aggregation.project().and("tp").as("tp")
				.and("datumId").as("datumId").and("inid").as("inid")
								.and(when(where("inid").is(206)).thenValueOf(Sum.sumOf("dataValue")).otherwise(0)).as("severe")
								.and(when(where("inid").is(207)).thenValueOf(Sum.sumOf("dataValue")).otherwise(0)).as("moderate")
								.and(when(where("inid").is(208)).thenValueOf(Sum.sumOf("dataValue")).otherwise(0)).as("mild")
								.and(when(where("inid").is(209)).thenValueOf(Sum.sumOf("dataValue")).otherwise(0)).as("normal");
		ProjectionOperation pop1=Aggregation.project().and("tp").as("tp")
				.and("areaId").as("datumId").and("inid").as("inid")
								.and(when(where("inid").is(206)).thenValueOf(Sum.sumOf("dataValue")).otherwise(0)).as("severe")
								.and(when(where("inid").is(207)).thenValueOf(Sum.sumOf("dataValue")).otherwise(0)).as("moderate")
								.and(when(where("inid").is(208)).thenValueOf(Sum.sumOf("dataValue")).otherwise(0)).as("mild")
								.and(when(where("inid").is(209)).thenValueOf(Sum.sumOf("dataValue")).otherwise(0)).as("normal");
		GroupOperation gop=Aggregation.group("tp","datumId").sum("severe").as("severe").sum("moderate").as("moderate").sum("mild").as("mild")
				.sum("normal").as("normal");
		LookupOperation lop=Aggregation.lookup("timePeriod", "_id.tp", "timePeriodId", "timeperiod");
		UnwindOperation uop=Aggregation.unwind("timeperiod");
		LookupOperation lop1=Aggregation.lookup("area", "_id.datumId", "areaId", "area");
		UnwindOperation uop1=Aggregation.unwind("area");
		ProjectionOperation fpop = Aggregation.project().and("area.areaName").as("datumId").and("timeperiod.timePeriodDuration").as("timeperiod")
				.and("timeperiod.year").as("year").and("severe").as("severe").and("moderate").as("moderate").and("mild").as("mild")
				.and("normal").as("normal");
		ProjectionOperation fpop1 = Aggregation.project().and("datumId").as("datumId").and("timeperiod.timePeriodDuration").as("timeperiod")
				.and("timeperiod.year").as("year").and("severe").as("severe").and("moderate").as("moderate").and("mild").as("mild")
				.and("normal").as("normal");
		
		Aggregation resultQuery=areaLevel.equals("village")?Aggregation.newAggregation(mop,pop,gop,lop,lop1,uop,uop1,fpop):
			Aggregation.newAggregation(mop1,pop1,gop,lop,uop,fpop1);
		List<Map> dataMap=areaLevel.equals("village")?mongoTemplate.aggregate(resultQuery,DataValue.class, Map.class).getMappedResults():
			mongoTemplate.aggregate(resultQuery,ClusterDataValue.class, Map.class).getMappedResults();
		List<String> columnList=new ArrayList<>();
		columnList.add("severe");
		columnList.add("moderate");
		columnList.add("mild");
		columnList.add("normal");
		System.out.println(dataMap);
		return convertAggregatedData.collectIndicatorValueForMultipleTimePeriod(dataMap, "area", columnList);
	}
	
	
	public PerformanceData getRejectionData(Integer formId,Integer startTp, Integer endTp) {
		List<TimePeriod> timeperiods=timePeriodRepository.findTimePeriodRange("1", startTp, endTp);
		List<Integer> tpids=timeperiods.stream().map(m->m.getTimePeriodId()).collect(Collectors.toList());
		
		MatchOperation mop=Aggregation.match(Criteria.where("inid").in(Arrays.asList(configurableEnvironment.getProperty("rejection_"+formId).split(","))
				.stream().map(v->Integer.parseInt(v)).collect(Collectors.toList())).and("tp").in(tpids)
				/*.and("isValid").is(true).and("isDeleted").is(false)*/);
		LookupOperation lop=Aggregation.lookup("timePeriod", "tp", "timePeriodId", "timeperiod");
		UnwindOperation uop=Aggregation.unwind("timeperiod");
		
		LookupOperation lop1 = Aggregation.lookup("account", "datumId", "userName", "user");
		UnwindOperation uop1 = Aggregation.unwind("user");
		
		ProjectionOperation pop=Aggregation.project().and("timeperiod.timePeriodDuration").as("timeperiod").and("timeperiod.year").as("year").and("datumId").as("datumId")
								.and("user.userDetails.fullName").as("name").and("user.userDetails.desgSlugId").as("designation")
								.and(Sum.sumOf("dataValue")).as("total")
								.and(when(where("inid").is(Integer.parseInt(configurableEnvironment.getProperty("accepted_"+formId)))).thenValueOf(Sum.sumOf("dataValue")).otherwise(0)).as("accepted")
								.and(when(where("inid").is(Integer.parseInt(configurableEnvironment.getProperty("rejected_"+formId)))).thenValueOf(Sum.sumOf("dataValue")).otherwise(0)).as("rejected");
		GroupOperation gop=Aggregation.group("timeperiod","year", "datumId","designation","name").sum("total").as("total").sum("accepted").as("accepted").sum("rejected").as("rejected");
		ProjectionOperation pop2 = Aggregation.project().and("_id.timeperiod").as("timeperiod").and("_id.year").as("year").and("_id.datumId").as("datumId").and("_id.designation").as("designation")
				.and("_id.name").as("name").and("total").as("total").and("accepted").as("accepted").and("rejected").as("rejected");
		MatchOperation mop1 = Aggregation.match(Criteria.where("designation").is(3));
		
		Aggregation resultQuery=Aggregation.newAggregation(mop,lop,uop,lop1,uop1,pop,gop,pop2,mop1);
		List<Map> dataMap=new ArrayList<>();
		dataMap = mongoTemplate.aggregate(resultQuery,UserDatumValue.class, Map.class).getMappedResults();
//		mongoTemplate.aggregate(resultQuery,UserDatumValue.class, Map.class).getMappedResults()
//				.stream().forEach(m->{
//					m.replace("total", Integer.parseInt(String.valueOf(m.get("total"))));
//					m.replace("accepted", Integer.parseInt(String.valueOf(m.get("accepted"))));
//					m.replace("rejected", Integer.parseInt(String.valueOf(m.get("rejected"))));
//					dataMap.add(m);
//				});
//		total=6.0, accepted=6.0, rejected=0
		List<String> columnList=new ArrayList<>();
		columnList.add("total");
		columnList.add("accepted");
		columnList.add("rejected");
		return convertAggregatedData.collectIndicatorValueForMultipleTimePeriod(dataMap, "name", columnList);
		
	}
	
	public List<LineChartModel> getLineChartData(Integer indicatorId, Integer tp, Integer areaId){
		List<TimePeriod> tp6=timePeriodRepository.findTop8ByPeriodicityOrderByTimePeriodIdAsc("1");
		List<Integer> tp6int=tp6.stream().map(v->v.getTimePeriodId()).collect(Collectors.toList());
		List<DataValue> dv=dataDomainRepository.findByDatumIdAndInidAndTpIn(areaId, indicatorId, tp6int);
		Map<Integer, String> tp6map=tp6.stream().collect(Collectors.toMap(v->v.getTimePeriodId(),v->v.getTimePeriodDuration()+v.getYear()));
		List<LineChartModel> lcds=new ArrayList<>();
		dv.forEach(v->{
			LineChartModel lcd=new LineChartModel();
			lcd.setAreaId(areaId);
			lcd.setAreaName("na");
			lcd.setAxis(tp6map.get(v.getTp()));
//			lcd.setValue(v.getDataValue());
			lcd.setValue(Double.parseDouble(new DecimalFormat("##.#").format(v.getDataValue())));
			lcd.setKey("performance");
			
			lcds.add(lcd);
		});
		return lcds;
	}
	
	public List<Map> getDesignationFormData(){
		MatchOperation mop=Aggregation.match(Criteria.where("accessType").is("DATA_ENTRY"));
		ProjectionOperation pop = Aggregation.project().and("form.formId").as("formId").and("form.displayName").as("name").and("designation.name").as("designation")
				.andExclude("_id");
		
		Aggregation resultQuery=Aggregation.newAggregation(mop,pop);
		List<Map> formMap=mongoTemplate.aggregate(resultQuery,DesignationFormMapping.class, Map.class).getMappedResults();
				
		return formMap;
		
	}
	

}

