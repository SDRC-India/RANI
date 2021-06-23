package org.sdrc.datum19.model;

import java.util.List;

import lombok.Data;

@Data
public class IndicatorConfigModel {
	
	private String id;
	
	private String indicatorNid;
	
	private String indicatorName;
	
	private String formId;
	
	private String questionName;
	
	private String questionId;
	
	private String questionColumnName;
	
	private String periodicity;
	
	private String highIsGood;
	
	private String aggregationType;
	
	private String aggregationRule;
	
	private String areaColumn;
	
	private List<Integer> typeDetails;
	
	private String controllerType;
	
	private String parentColumnName;
	
	private List<ConditionModel> conditions;
	
	private String numerator;
	
	private String denominator;
	
	private String sector;
	
	private String subsector;
	
	private String subgroup;
	
	private String unit;
	
	private String collection;
}
