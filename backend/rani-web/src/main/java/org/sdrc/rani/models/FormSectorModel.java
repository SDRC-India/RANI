package org.sdrc.rani.models;

import lombok.Data;

@Data
public class FormSectorModel {
	
	private Integer formId;
	private Integer sectorId;
	private String sectorName;
	private String formName;
}