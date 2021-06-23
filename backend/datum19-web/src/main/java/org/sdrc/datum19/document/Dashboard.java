package org.sdrc.datum19.document;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document
@Data
public class Dashboard {
	private String _id;
	private String name;
	private String description;
	private Boolean isPrivate;
	private String username;
}
