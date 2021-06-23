package org.sdrc.datum19.model;

import lombok.Data;

@Data
public class SearchDataRequestModel {

	private String indicatorId;
	private Integer datumId;
	private Integer tp;
	private String username;
}
