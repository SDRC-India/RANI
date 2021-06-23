package org.sdrc.datum19.model;

import java.util.List;

import lombok.Data;

@Data
public class Top5Bot5 {
	public String label;
	public List<AreaDataModel> areaDatamodel; 
}
