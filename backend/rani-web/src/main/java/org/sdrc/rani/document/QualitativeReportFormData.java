package org.sdrc.rani.document;

import java.util.Date;
import java.util.List;

import org.sdrc.rani.models.QualityReportModel;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
@Data
@Document
public class QualitativeReportFormData {

	@Id
	private String id;

	private String userName;

	private String userId;

	private Date createdDate;

	private TimePeriod timePeriod;

	List<QualityReportModel> data;
	
	private String filePath;
	
	private String extension;

}
