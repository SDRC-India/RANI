package org.sdrc.datum19.document;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;


@Document
@Data
public class Heading implements Serializable{
	@Id
	private String id;
	private Long slugId;
	private String title;
	private Date createdDate;

}
