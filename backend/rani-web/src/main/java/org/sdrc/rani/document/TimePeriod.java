package org.sdrc.rani.document;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.ToString;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 */

@Document
@Data
@ToString
//@CompoundIndexes({
//		@CompoundIndex(name = "tpIndex", unique = true, def = "{'periodicity' : 1, 'timePeriodDuration' : 1,'financialYear' : 1, 'year' : 1}") })
public class TimePeriod implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2226822562688452799L;

	@Id
	private String id;

	private String timePeriodDuration;

	private Date startDate;

	private Date endDate;

	private String periodicity;

	private Date createdDate = new Date();

	private String financialYear;

	private Integer year;

	private Integer timePeriodId;
	
	private String timePeriod;
}
