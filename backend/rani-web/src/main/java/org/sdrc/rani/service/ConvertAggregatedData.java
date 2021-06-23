package org.sdrc.rani.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.sdrc.rani.models.PerformanceData;
import org.sdrc.rani.models.TableHead;
import org.springframework.stereotype.Service;

@Service
public class ConvertAggregatedData {
	public PerformanceData collectIndicatorValueForMultipleTimePeriod(List<Map> dataMap, String identifier, List<String> columns) {
		Map<String,Map<String, String>> returnObjects=new HashMap<>();
		Set<String> keys=new LinkedHashSet<>();
		Map<String,TableHead> months=new HashMap<>();
		List<Map> resultMap=new ArrayList<>();
		List<TableHead> tableHeads=new ArrayList<>();
		
		keys.add(identifier);
		TableHead tableHead0=new TableHead();
		tableHead0.setColspan("1");
		tableHead0.setRowspan("1");
		tableHead0.setValue("");
		months.put("",tableHead0);
		dataMap.forEach(d->{
			Map<String, String> returnObject=new HashMap<>();
			TableHead tableHead=new TableHead();
			tableHead.setColspan(String.valueOf(columns.size()));
			tableHead.setRowspan("1");
			tableHead.setValue((String) d.get("timeperiod")+"-"+d.get("year"));
			months.put((String) d.get("timeperiod")+"-"+d.get("year"),tableHead);
		switch (identifier) {
		case "name":
			if(returnObjects.containsKey(String.valueOf(d.get("name")))) {
				columns.forEach(c->{
					returnObjects.get(String.valueOf(d.get("name"))).put(c+"_"+d.get("timeperiod")+"-"+d.get("year"),String.valueOf(d.get(c)));
				});
			}
			else {
			returnObject.put(identifier, String.valueOf(d.get("name")));
			columns.forEach(c->{
				returnObject.put(c+"_"+d.get("timeperiod")+"-"+d.get("year"),String.valueOf(d.get(c)));
			});
			returnObjects.put(String.valueOf(d.get("name")),returnObject);
			}
			break;
			
		case "area":
			if(returnObjects.containsKey(String.valueOf(d.get("datumId")))) {
				columns.forEach(c->{
					returnObjects.get(String.valueOf(d.get("datumId"))).put(c+"_"+d.get("timeperiod")+"-"+d.get("year"),String.valueOf(d.get(c)));
				});
			}
			else {
			returnObject.put(identifier, String.valueOf(d.get("datumId")));
			columns.forEach(c->{
				returnObject.put(c+"_"+d.get("timeperiod")+"-"+d.get("year"),String.valueOf(d.get(c)));
			});
			returnObjects.put(String.valueOf(d.get("datumId")),returnObject);
			}
			break;

		default:
			break;
		}	
			
		});
		
		tableHeads=months.entrySet().stream().map(m->m.getValue()).collect(Collectors.toList());
		for (int i = 1; i < tableHeads.size(); i++) {
			TableHead th = tableHeads.get(i);
			columns.forEach(c->{
				keys.add(c+"_"+th.getValue());
			});
		}
		
		resultMap = returnObjects.entrySet().stream().map(m->m.getValue()).collect(Collectors.toList());
		
		PerformanceData data=new PerformanceData();
		data.setTableColumns(keys.stream().map(m->m).collect(Collectors.toList()));
		data.setTableHead(tableHeads);
		data.setTableData(resultMap);
		
		return data;
	}
}
