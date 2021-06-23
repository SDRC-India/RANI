package org.sdrc.rani.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
@Data
@Document
public class RespondentSequenceGenerator {

	
	@Id
	private String id;
	
	private Integer formId;
	
	private Area village;
	
	private Long count;
}
