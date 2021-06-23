package org.sdrc.datum19.service;

import static org.springframework.data.mongodb.core.aggregation.ConditionalOperators.when;
import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.util.ArrayList;
import java.util.List;

import org.sdrc.datum19.document.CumulativeData;
import org.sdrc.datum19.document.DataValue;
import org.sdrc.datum19.document.Indicator;
import org.sdrc.datum19.repository.CumulativeDataRepository;
import org.sdrc.datum19.repository.IndicatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AccumulatorOperators.Sum;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators.Divide;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators.Multiply;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators.Subtract;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.OutOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

/*
 * author : Biswabhusan Pradhan
 * email : biswabhusan@sdrc.co.in
 */
@Service
public class CumulativeAggregationServiceImpl implements CumulativeAggregationService {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private IndicatorRepository indicatorRepository;
	
	@Autowired
	private CumulativeDataRepository cumulativeDataRepository;
	
	@Override
	public void aggregateCumulativeData() {
		// TODO Auto-generated method stub
		mongoTemplate.dropCollection(CumulativeData.class);
		mongoTemplate.aggregate(aggregateNumberIndicators(), DataValue.class,CumulativeData.class);
		
		aggregateFinalIndicators("monthly","indicator");
	}

	private Aggregation aggregateNumberIndicators() {
		MatchOperation mop = Aggregation.match(Criteria.where("_case").in("count","number", "Count"));
		GroupOperation gop = Aggregation.group("inid", "datumId").sum("dataValue").as("dataValue");
		ProjectionOperation pop = Aggregation.project().and("_id.inid").as("inid").and("_id.datumId").as("datumId").and("dataValue").as("dataValue")
				.andExclude("_id");
		OutOperation op = Aggregation.out("cumulativeData");
		
		return Aggregation.newAggregation(mop,gop,pop,op);
	}
	
	
	List<CumulativeData> percentDataMap=null;
	List<CumulativeData> percentDataMapAll=null;
	private List<CumulativeData> aggregateFinalIndicators(String periodicity, String indicatorType) {
		percentDataMap=new ArrayList<>();
		percentDataMapAll=new ArrayList<>();
		List<Indicator> indicatorList = indicatorRepository.getPercentageIndicators(periodicity,indicatorType);
		indicatorList.forEach(indicator->{
			List<Integer> dependencies=new ArrayList<>();
			List<Integer> numlist=new ArrayList<>();
			String[] numerators=String.valueOf(indicator.getIndicatorDataMap().get("numerator")).split(",");
			Integer inid=Integer.parseInt(String.valueOf(indicator.getIndicatorDataMap().get("indicatorNid")));
			String aggrule=String.valueOf(indicator.getIndicatorDataMap().get("aggregationRule"));
			for (int i = 0; i < numerators.length; i++) {
				numlist.add(Integer.parseInt(numerators[i]));
				dependencies.add(Integer.parseInt(numerators[i]));
			}
			List<Integer> denolist=new ArrayList<>();
			String[] denominators=String.valueOf(indicator.getIndicatorDataMap().get("denominator")).split(",");
			for (int i = 0; i < denominators.length; i++) {
				denolist.add(Integer.parseInt(denominators[i]));
				dependencies.add(Integer.parseInt(denominators[i]));
			}
			try {
				switch (String.valueOf(indicator.getIndicatorDataMap().get("aggregationType"))) {
				case "percent":
					percentDataMap=mongoTemplate.aggregate(getPercentData(dependencies,numlist,denolist,aggrule),CumulativeData.class,CumulativeData.class).getMappedResults();
					percentDataMap.forEach(dv->{
						dv.setInid(inid);
						dv.set_case("percent");
						if (dv.getDenominator()==0) {
							dv.setDataValue(null);
						}
					});
					percentDataMapAll.addAll(percentDataMap);
					break;
					
				case "avg":
					percentDataMap=mongoTemplate.aggregate(getAvgData(dependencies,numlist,denolist,aggrule),CumulativeData.class,CumulativeData.class).getMappedResults();
					percentDataMap.forEach(dv->{
						dv.setInid(inid);
						dv.set_case("avg");
						if (dv.getDenominator()==0) {
							dv.setDataValue(null);
						}
					});
					percentDataMapAll.addAll(percentDataMap);
					break;

				default:
					break;
				}
					
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		cumulativeDataRepository.saveAll(percentDataMapAll);
		return percentDataMapAll;
	}
	
	
	private TypedAggregation<DataValue> getPercentData(List<Integer> dep,List<Integer> num,List<Integer> deno, String rule){
		MatchOperation matchOperation = Aggregation.match(Criteria.where("inid").in(dep));
		GroupOperation groupOperation=null;
		ProjectionOperation projectionOperation=null;
		ProjectionOperation p1=null;
		ProjectionOperation p2=null;
		if(rule.equals("sub")) {
			groupOperation=Aggregation.group("datumId").sum(when(where("inid").in(deno)).thenValueOf("$dataValue").otherwise(0)).as("denominator")
					.sum(Sum.sumOf(when(where("inid").is(num.get(0))).then("$dataValue").otherwise(0))).as("n1")
					.sum(when(where("inid").is(num.get(1))).then("$dataValue").otherwise(0)).as("n2");
			
			p1=Aggregation.project().and("_id").as("datumId").and(Subtract.valueOf("n1").subtract("n2")).as("numerator").and("denominator").as("denominator");
			
			p2=Aggregation.project().and("datumId").as("datumId").and("numerator").as("numerator").and("denominator").as("denominator")
					.and(when(where("denominator").gt(0)).thenValueOf(Divide.valueOf(Multiply.valueOf("numerator")
					.multiplyBy(100)).divideBy("denominator")).otherwise(0)).as("dataValue");
			return Aggregation.newAggregation(DataValue.class,matchOperation,groupOperation,p1,p2);
		}else {
		groupOperation=Aggregation.group("datumId").sum(when(where("inid").in(num)).thenValueOf("$dataValue").otherwise(0)).as("numerator")
				.sum(when(where("inid").in(deno)).thenValueOf("$dataValue").otherwise(0)).as("denominator");
		projectionOperation=Aggregation.project().and("_id").as("datumId")
				.and("numerator").as("numerator").and("denominator").as("denominator")
				.andExclude("_id")
				.and(when(where("denominator").gt(0)).thenValueOf(Divide.valueOf(Multiply.valueOf("numerator")
				.multiplyBy(100)).divideBy("denominator")).otherwise(0)).as("dataValue");
		return Aggregation.newAggregation(DataValue.class,matchOperation,groupOperation,projectionOperation);
		}
	}
	
	private TypedAggregation getAvgData(List<Integer> dependencies, List<Integer> numlist, List<Integer> denolist,
			String aggrule) {
		// TODO Auto-generated method stub
		MatchOperation matchOperation = Aggregation.match(Criteria.where("inid").in(dependencies));
		GroupOperation groupOperation=null;
		ProjectionOperation projectionOperation=null;
		ProjectionOperation p1=null;
		ProjectionOperation p2=null;
		groupOperation=Aggregation.group("datumId").sum(when(where("inid").in(numlist)).thenValueOf("$dataValue").otherwise(0)).as("numerator")
				.sum(when(where("inid").in(denolist)).thenValueOf("$dataValue").otherwise(0)).as("denominator");
		projectionOperation=Aggregation.project().and("_id").as("datumId")
				.and("numerator").as("numerator").and("denominator").as("denominator")
				.andExclude("_id")
				.and(when(where("denominator").gt(0)).thenValueOf(Divide.valueOf("numerator").divideBy("denominator")).otherwise(0)).as("dataValue");
		return Aggregation.newAggregation(DataValue.class,matchOperation,groupOperation,projectionOperation);
	}
}
