package org.sdrc.datum19.document;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Document
@Setter @Getter
public class CumulativeData {
	private String id;
	private Integer datumId;
	private Double dataValue;
	private String _case;
	private Integer inid;
	private Double numerator;
	private Double denominator;
	private String datumtype;
}
