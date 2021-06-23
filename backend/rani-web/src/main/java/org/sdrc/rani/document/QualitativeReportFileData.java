package org.sdrc.rani.document;

import java.util.Date;

import org.springframework.data.annotation.Id;

import lombok.Data;

/**
 *@author Subham Ashish(subham@sdrc.co.in)
 *
 *This document stores the file-path uploaded by DDM.
 */
@Data
public class QualitativeReportFileData {

	@Id
	private String id;

	private String name;

	private String extension;

	private String filePath;

	private Long size;

	private TimePeriod timePeriod;
	
	private Date createdDate;

}
