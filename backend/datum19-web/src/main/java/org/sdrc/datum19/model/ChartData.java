package org.sdrc.datum19.model;

import java.util.List;

import lombok.Data;


@Data
public class ChartData {
	
	private String headerIndicatorName;
	private String headerIndicatorValue;
	List<List<ChartDataModel>> chartDataValue;

}
