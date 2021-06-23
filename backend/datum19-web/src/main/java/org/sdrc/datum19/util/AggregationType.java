package org.sdrc.datum19.util;

public enum AggregationType {
	
	COUNT(1,"count@count", "Count"), 
	REPEATCOUNT(2,"repeatCount@count", "Repeat Count"),
	UNIQUECOUNT(3,"unique@count", "Distinct Count"),
//	TOTALCOUNT(4,"total@count", "Total"),
	NUMBER(5, "number", "Numeric"),
//	PERCENT(6, "percent", "Percent"), 
	PROPORTION(6, "proportion", "Proportion"), 
	AVG(7, "avg", "Average"),
	RATE(8, "rate", "Rate/Ratio");
 
    private Integer id;
    private String aggregationType;
    private String displayName;
 
    private AggregationType(int id, String aggregationType, String displayName) {
        this.id = id;
        this.aggregationType = aggregationType;
        this.displayName = displayName;
    }

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getAggregationType() {
		return aggregationType;
	}

	public void setAggregationType(String aggregationType) {
		this.aggregationType = aggregationType;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
    
	
}
