package org.sdrc.rani.models;


import java.util.Date;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NewDataReceivedEvent {

	private Integer submissionKey;
	
	private String userName;
	
	private Integer userId;
	
	private Integer formId;
	
	private String name;

	private String emailId;

	private Date syncDate;
	
	private Date createdDate;
	
	private Date updatedDate;
	
	private Map<String,Object> data;
	
	private String uniqueId;
	
	private String  uniqueName;
	
	
}
