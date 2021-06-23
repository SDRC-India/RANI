package org.sdrc.datum19.document;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document
@Data
public class DashboardHeader {
	private String _id;
	public Integer slugId;
	public String name;
	public String description;
}
