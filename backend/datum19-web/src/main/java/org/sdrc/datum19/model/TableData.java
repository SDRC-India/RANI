package org.sdrc.datum19.model;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class TableData {

	private List<TableHead> tableHeads;
	private List<String> tableColumns;
	private List<Map<String,String>> cellData;
	private String indName;
}
