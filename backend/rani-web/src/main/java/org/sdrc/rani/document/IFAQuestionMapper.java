package org.sdrc.rani.document;

import org.springframework.data.annotation.Id;

import lombok.Data;

/**
 * @author subham
 *
 */
@Data
public class IFAQuestionMapper {

	@Id
	private String id;

	private String label;

	private String controlType;

	private String fieldType;

	private String colName;

	private String dependentCondition;

	private String dropDownValue;

	private String desgName;

	private Integer slugId;

	private Integer questionOrder;
	
	private Boolean isDependency;
	
	private String name;

}
