package org.sdrc.datum19.model;

import lombok.Data;

@Data
public class DataValueModel {
	
private Integer areaId;
private String areaCode;
private String areaName;
private Double dataValue;
private Double dashboardDataValue;
private Double number;
private String cssColor;
private Integer areaLevelId;
private Integer indicatorId;
private Integer timeperiod;

private Double latitude;
private Double longitude;
private String icon;
	

}
