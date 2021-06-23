package org.sdrc.rani.models;

import java.util.List;
import java.util.Map;

import lombok.Data;
/**
 * @author Subham Ashish(subham@sdrc.co.in)
 */
@Data
public class IFAValueModel {

	private String label;

	private String controlType;

	private String fieldType;

	private String colName;

	private List<Map<String, String>> options;

	private Object value;
	
	private String isDependencyOption;
	
	private Boolean isDependency;
	
	private String name;
}
