package org.sdrc.rani.models;

import lombok.Data;
/**
 * @author Subham Ashish(subham@sdrc.co.in)
 */
@Data
public class AreaModel {

	private Integer areaId;

	private String areaName;

	private int parentAreaId;

	private String areaLevel;

	private String areaCode;
	
	private Integer clusterNumber;
	
	private String clusterName;
	
	private String value;
	
	private String cssColor;

}
