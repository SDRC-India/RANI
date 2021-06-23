package org.sdrc.rani.document;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * @author Subham Ashish
 *
 */
@Data
@Document
public class AggregationDetails {

	@Id
	private String id;
	
	private Date startTime;
	
	private Date endTime;
	
	private Double timeTaken;
	
	private String status;
	
	private TimePeriod timePeriod;
}
