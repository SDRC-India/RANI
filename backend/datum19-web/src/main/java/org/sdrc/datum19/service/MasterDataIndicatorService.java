package org.sdrc.datum19.service;

import static org.springframework.data.mongodb.core.aggregation.ConditionalOperators.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

/*
 * author : Biswabhusan Pradhan
 * email : biswabhusan@sdrc.co.in
 */
@Service
public class MasterDataIndicatorService {
	
	public Aggregation aggregateCountData(Integer formId, String groupKeys, Integer timePeriodId, String conditions) {
		//projecting required fields
		ProjectionOperation projectionOperation=Aggregation.project().and("data").as("data").and("rejected").as("rejected")
				.and("uniqueId").as("uniqueId").and("userName").as("userName");
		
		//creating array of conditions
		List<String> condarr=new ArrayList<>();
		if(!conditions.equals(null) && !conditions.equals(""))
			condarr=Arrays.asList(conditions.split(";"));
		
		//creating criteria with some predefined conditions
		Criteria matchCriteria=Criteria.where("formId").is(formId).and("timePeriod.timePeriodId").is(timePeriodId)
				.and("isValid").is(true).and("isDeleted").is(false);
		
		//creating criteria query from dynamic conditions from aggregation rule
		Criteria criteriaQuery=new Criteria();
		if(!conditions.equals(null) && !conditions.equals("")) {
		for (int i = 0; i < condarr.size(); i++) {
			String condition=condarr.get(i).split("\\(")[0];
			String expression=condarr.get(i).split("\\(")[1].split("\\)")[0];
			switch (condition) {
			case "and$lte":
				if(i==0)
					criteriaQuery=Criteria.where(expression.split(":")[0]).lte(Integer.parseInt(expression.split(":")[1]));
				else
					criteriaQuery=criteriaQuery.lte(Integer.parseInt(expression.split(":")[1]));
				break;
			case "and$eq":
				switch (expression.split(":")[2]) {
				case "boolean":
					matchCriteria.and(expression.split(":")[0]).is(Boolean.parseBoolean(expression.split(":")[1]));
					break;
				case "number":
					matchCriteria.and(expression.split(":")[0]).is(Integer.parseInt(expression.split(":")[1]));
					break;

				default:
					matchCriteria.and(expression.split(":")[0]).is(expression.split(":")[1]);
					break;
				}
				break;
			case "and$gte":
				if(i==0)
					criteriaQuery=Criteria.where(expression.split(":")[0]).gte(Integer.parseInt(expression.split(":")[1]));
				else
					criteriaQuery=criteriaQuery.gte(Integer.parseInt(expression.split(":")[1]));
				break;
			case "and$gt":
				if(i==0)
					criteriaQuery=Criteria.where(expression.split(":")[0]).gt(Integer.parseInt(expression.split(":")[1]));
				else
					criteriaQuery=criteriaQuery.gt(Integer.parseInt(expression.split(":")[1]));
				break;
			case "and$lt":
				if(i==0)
					criteriaQuery=Criteria.where(expression.split(":")[0]).lt(Integer.parseInt(expression.split(":")[1]));
				else
					criteriaQuery=criteriaQuery.lt(Integer.parseInt(expression.split(":")[1]));
				break;

			default:
				break;
			}
		}
		}
		//combining all conditions to create match operation
		MatchOperation mp1=Aggregation.match(matchCriteria);
		//grouping records on multiple fields separated by ';' and getting the count
		GroupOperation groupop=Aggregation.group(groupKeys.split(",")).sum(when(criteriaQuery).then(1).otherwise(0)).as("_value");
		//this step is aggregating the aggregated data by grouping them with area to finf aggregated sum of count
		GroupOperation go2=Aggregation.group(groupKeys.split(",")[0].contains("data.")?groupKeys.split(",")[0].split("data.")[1]:groupKeys.split(",")[0]).sum("_value").as("value");
		System.out.println("master data query :: "+Aggregation.newAggregation(mp1,projectionOperation,groupop,go2));
		return Aggregation.newAggregation(mp1,projectionOperation,groupop,go2);
	}

}
