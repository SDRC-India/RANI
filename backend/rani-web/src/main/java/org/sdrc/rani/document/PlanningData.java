package org.sdrc.rani.document;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
@Document
@Data
public class PlanningData {

	@Id
	private String id;

	private Integer formId;

	private Integer target;

	private Integer month;

	private Integer year;

	private String desgId;

	private String userName;

	private String accId;
	
	private Date createdDate=new Date();
	
	private Date updatedDate=new Date();
}
