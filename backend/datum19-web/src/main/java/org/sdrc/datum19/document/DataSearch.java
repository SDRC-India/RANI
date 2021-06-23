package org.sdrc.datum19.document;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Document
@Getter @Setter
public class DataSearch {
	private String id;
	private String indicator;
	private String inid;
	private Integer tp;
	private Integer datumId;
	private List<String> tags;
	private Double dataValue;
	
	public DataSearch(String indicator, String inid, Integer tp, Integer datumId, Double dataValue) {
		super();
		this.indicator = indicator;
		this.inid = inid;
		this.tp = tp;
		this.datumId = datumId;
		this.dataValue = dataValue;
	}
	
	
}
