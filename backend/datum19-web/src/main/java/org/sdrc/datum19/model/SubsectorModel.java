package org.sdrc.datum19.model;

import java.util.List;

import lombok.Data;

@Data
public class SubsectorModel {

	private Integer formId;

	private Integer subSectorId;

	private String subsectorName;
	
	private Integer sectorId;
	
	private Object shapeFile;
	
	private List<IndicatorGroupModel> indicators;
	
	//private Map<Object, List<IndicatorGroupModel>> indicatorsMap;
	
}
