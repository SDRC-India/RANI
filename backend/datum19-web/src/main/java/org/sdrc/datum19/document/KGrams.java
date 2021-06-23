package org.sdrc.datum19.document;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document
@Data
public class KGrams {
	private String _id;
	private Integer gram;
	private String kgram;
	private String word;
	private Double weight;
}
