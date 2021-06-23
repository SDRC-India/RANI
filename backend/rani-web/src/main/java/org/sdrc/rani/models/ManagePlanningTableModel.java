package org.sdrc.rani.models;

import java.util.Map;

import lombok.Data;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
@Data
public class ManagePlanningTableModel {

	private String formName;
	
	private Integer target;
	
	private Map<String, String> action;
}
