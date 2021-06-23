package org.sdrc.rani.models;

import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * @author Subham Ashish
 *
 */
@Data
public class ThematicDashboardDataModel {

	private String areaLevelId;

	private Integer areaId;

	private String formId;

	private String indicatorId;

	private Integer timePeriodId;

	private String sector;

	private String svg;

	private List<Map<String, String>> legend;

	private List<Map<String, String>> tableData;

	private String unit;

	private String clusterName;
	
	private List<String> thematicLegends;
}
