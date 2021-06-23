package org.sdrc.rani.models;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerformanceData{
	public List<TableHead> tableHead;
	public List<String> tableColumns;
	public List<Map> tableData;
}
