package org.sdrc.rani.models;

import lombok.Data;

@Data
public class IndicatorModel {
	
	private Integer indicatorId;
	private String cssClass;
	
	private String indicatorName;
	private String unit;

}