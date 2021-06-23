package org.sdrc.datum19.document;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document
public class Theme {
	private String _id;
	private String themeName;
	private String primary;
	private String accent;
	private String warn;
	private String toolBarBG;
	private String toolBarText;
	private String clientId;
	private Boolean isActive;
	private String label;
	private String defaultThemeMode;
	private String onHover;
	private String onSelect;
	
	public Theme(String _id) {
		super();
		this._id = _id;
	}

	public Theme() {
		super();
	}
	
	
}
