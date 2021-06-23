package org.sdrc.datum19.document;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Document
@Getter @Setter
public class RelatedIndicators {

	private String _id;
	private Integer indicatorId;
	private Integer groupId;
}
