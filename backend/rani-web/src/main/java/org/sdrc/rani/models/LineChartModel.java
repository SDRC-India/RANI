package org.sdrc.rani.models;

import lombok.Getter;
import lombok.Setter;

/*
 * @author Biswabhusan Pradhan
 * 
 */
@Setter
@Getter
public class LineChartModel {
	Integer areaId;
	String areaName;
	Double value;
	String axis;
	String key; 
}
