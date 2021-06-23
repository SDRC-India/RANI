package org.sdrc.datum19.model;

import java.util.List;

import lombok.Data;

@Data
public class GroupChartDataModel {
	
	private String headerIndicatorName;
	private Integer headerIndicatorValue;
	List<List<ChartDataModel>> chartDataValue;
	List<LegendModel> legends;
	private ChartDataModel totalSubChartData;
	
	private String unit;
	private List<String> chartsAvailable;
	private ChartData chartData;

}