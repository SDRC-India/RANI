package org.sdrc.datum19.model;

import java.util.List;

import lombok.Data;

@Data
public class IndicatorGroupModel {
	private String id;
	private String indicatorName;
	private Integer indicatorId;
	private String indicatorValue;
	private String indicatorGroupName;
	private String timeperiod;
	private String periodicity;
	private Integer timeperiodId;
	private List<String> chartsAvailable;
	private String align;
	private String cardType;
	private String unit;
	private String chartAlign;
	private List<GroupChartDataModel> chartData;
	private List<ThematicIndicatorModel> thematicData;
	private String chartGroup;
	private String extraInfo;
	private String groupName;
	private Object shapePath;
	private String areaCode;
	private String areaName;
	private Integer areaLevelId;
	private String areaLevelName;
	private Integer range;
	private String source;
	private List<GeoLegends> geolegends;
	private List<TableData> tableData;
}