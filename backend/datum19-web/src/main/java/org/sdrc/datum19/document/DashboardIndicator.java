package org.sdrc.datum19.document;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.sdrc.datum19.model.GeoLegends;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document
public class DashboardIndicator implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	@Id
	private String id;
	private String indicatorGroup;
	private Integer kpiIndicator;
	private List<String> chartType;
	private List<List<Integer>> chartIndicators;
	private List<List<String>> chartIndicatorNames;
	private String sector;
	private String sectorId;
	private String subSector;
	private String subSectorId;
	private String valueFrom;
	private String chartHeader;
	private String kpiChartHeader;
	private String align;
	private List<String> chartLegends;
	private List<String> colorLegends;
	private String unit;
	private String chartGroup;
	private String extraInfo;
	private Date createdDate;
	private Date updatedDate;
	private Integer formId;
	private String groupName;
	private String dashboardId;
	private Integer thematicFileDataSlugId;
	private String areaCode;
	private Integer range;
	private String heading;
	private String subheading;
	private List<GeoLegends> geolegends;
	private String mulIndOrMulArea;
	private List<List<Integer>> areaIdsAreaNames;
	private List<List<String>> multipleAreasName;
	private List<List<String>> tableHeaders;
	private List<String> rowTitles;
}
