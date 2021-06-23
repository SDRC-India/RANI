package org.sdrc.rani.models;

import lombok.Data;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
@Data
public class QualityReportModel {

	private Integer id;

	private String header;

	private String label;

	private String value;

	private String type;

}
