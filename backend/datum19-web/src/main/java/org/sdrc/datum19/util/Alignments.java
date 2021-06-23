package org.sdrc.datum19.util;

public enum Alignments {
	
	_25_PERCENT(1,"col-md-3"), 
	_33_PERCENT(2,"col-md-4"),
	_50_PERCENT(4,"col-md-6"),
	_100_PERCENT(7, "col-md-12");
	
	
    private Integer id;
    private String align;
 
    private Alignments(int id, String align) {
        this.id = id;
        this.align = align;
    }

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getAlign() {
		return align;
	}

	public void setAlign(String align) {
		this.align = align;
	}

}
