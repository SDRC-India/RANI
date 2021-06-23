package org.sdrc.datum19.document;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Document
@Setter
@Getter
public class IndicatorSuggestionModel {
	private String _id;
	private String username;
	private Integer indicatorId;
	private Double prediction;
}
