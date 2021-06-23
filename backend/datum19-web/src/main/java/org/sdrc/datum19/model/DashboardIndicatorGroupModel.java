package org.sdrc.datum19.model;

import java.util.List;

import org.sdrc.datum19.document.Area;
import org.sdrc.datum19.document.Indicator;

import lombok.Data;

@Data
public class DashboardIndicatorGroupModel {
	
	private String indicatorGroup;
	private Integer kpiIndicator;
	private List<String> chartType;
	private List<List<String>> chartIndicators;
	private List<List<String>> chartIndicatorNames;
	private String sector;
	private Integer sectorId;
	private String subSector;
	private Integer subSectorId;
	private String valueFrom;
	private String chartHeader;
	private String kpiChartHeader;
	private String align;
	private List<String> chartLegends;
	private List<String> colorLegends;
	private String unit;
	private String extraInfo;
	private Integer formId;
	private String chartGroup;
	private String saveOrEdit;
	private String groupName;
	private String id;
	private String dashboardId;
	private Integer thematicFileDataSlugId;
	private String areaCode;
	private Integer range;
	public Object shapePath;
	private String areaName;
	private Integer areaLevelId;
	private String areaLevelName;
	private String heading;
	private String subheading;
	private String fileName;
	private Area state;
	private Area district;
	private List<GeoLegends> geolegends;
	private String mulIndOrMulArea;
	private List<List<Integer>> areaIdsAreaNames;
	private List<List<String>> multipleAreasName;
	private List<List<String>> tableHeaders;
	private List<String> rowTitles;
	private List<List<Indicator>> indicators;
	
}
