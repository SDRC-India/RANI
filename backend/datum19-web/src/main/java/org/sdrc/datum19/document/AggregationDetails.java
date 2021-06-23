package org.sdrc.datum19.document;

import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Document
@Setter @Getter
public class AggregationDetails {
	private String id;
	private Date startTime;
	private Date endTime;
	private Double timeTaken;
	private String status;
	private TimePeriod timePeriod;
}
