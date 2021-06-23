package org.sdrc.rani.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * @author subham
 *
 */
@Document
@Data
public class DesignationIFAMapping {

	@Id
	private String id;
	
	private String desgId;
	
	private String ifaSuppyName;
}
