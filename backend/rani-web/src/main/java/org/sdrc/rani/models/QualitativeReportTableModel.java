package org.sdrc.rani.models;

import java.util.Map;

import lombok.Data;

/**
 * This model used While sending data to DDM
 * @author subham
 *
 */
@Data
public class QualitativeReportTableModel {

	private String reportingMonth;

	private String dateOfCreation;

	private Map<String, String> action;
	
	private String supervisorName;
}
