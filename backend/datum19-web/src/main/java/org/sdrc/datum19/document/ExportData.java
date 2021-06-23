package org.sdrc.datum19.document;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Document
@Setter @Getter
public class ExportData {
	private String id;
	private Integer HSCode;
	private String Commodity;
	private Double value;
	private String country;
	private Integer year;
}
