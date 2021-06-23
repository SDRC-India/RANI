package org.sdrc.rani.document;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
@Data
@Document
public class TestingDateConfiguration {

	@Id
	private String id;
	
	private Date date = new Date();
	
	private Integer slugId;
	
}
