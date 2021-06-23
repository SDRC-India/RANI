package org.sdrc.datum19.util;

public enum Unit {

	NUMBER(1,"number"), 
	PERCENTAGE(2,"percent"),
	AVERAGE(3, "avg"),
	RATE(4, "rate");
	
    private Integer id;
    private String unit;
 
    private Unit(int id, String unit) {
        this.id = id;
        this.unit = unit;
    }

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

}
