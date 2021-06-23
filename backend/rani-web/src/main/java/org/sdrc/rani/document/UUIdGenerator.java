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
public class UUIdGenerator {

	@Id
	private String id;

	private String accountId;

	private Integer month;

	private Integer year;

	private String uuid;

	private Date createdDate;
	
	private String desgId;
}
