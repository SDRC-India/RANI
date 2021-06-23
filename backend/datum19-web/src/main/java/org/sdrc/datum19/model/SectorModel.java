package org.sdrc.datum19.model;

import java.util.List;

import lombok.Data;

@Data
public class SectorModel {
	
	private Integer formId;

	private String sectorName;
	
	private Integer sectorId;
	private String timePeriod;
	private List<SubsectorModel> subSectors;
}
