package org.sdrc.rani.models;

import java.util.Map;

import lombok.Data;

/**
 * @author subham
 *
 */
@Data
public class QualitativeTableModel {

	private String reportingMonth;

	private String dateOfCreation;

	private Map<String, String> action;
}
