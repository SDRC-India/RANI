package org.sdrc.datum19.util;

public enum ChartType {
	
	CARD(1,"card"), 
	DOUGHNUT(2,"doughnut"),
	COLUMN(3,"column"),
	PIE(4,"pie"),
	BAR(5, "BAR"),
	TREND(6, "trend"), 
	STACK(7, "stack"),
	GEOMAP(8, "geo"),
	THEMATICMAP(9,"thematic"),
	TABLE(10,"table"),
//	GROUPBAR(8,"groupbar"),
//	SPIDER(9,"spider"),
//	BOX(10,"box"),
//	_3DBAR(11, "threeDBar"),
	ALL(12, "all");
	
	
    private Integer id;
    private String chartType;
 
    private ChartType(int id, String chartType) {
        this.id = id;
        this.chartType = chartType;
    }

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getChartType() {
		return chartType;
	}

	public void setChartType(String chartType) {
		this.chartType = chartType;
	}

}
