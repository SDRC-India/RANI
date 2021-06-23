package org.sdrc.rani.service;

import java.util.List;
import java.util.Map;

import org.sdrc.rani.models.LineChartModel;
import org.sdrc.rani.models.PerformanceData;

/*
 * @author Biswabhusan Pradhan
 * 
 */

public interface PerformanceReportService {
	public PerformanceData getPerformanceData(Integer formId,String designation,Integer startTp, Integer endTp);
	
	public PerformanceData getHemocueData(String areaLevel,Integer startTp, Integer endTp);
	
	public PerformanceData getRejectionData(Integer formId,Integer startTp, Integer endTp);
	
	public List<LineChartModel> getLineChartData(Integer indicatorId, Integer tp, Integer areaId);
	
	public List<Map> getDesignationFormData();
}
