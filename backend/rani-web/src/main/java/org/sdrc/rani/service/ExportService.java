package org.sdrc.rani.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.sdrc.rani.models.ParamModel;
import org.sdrc.rani.models.SVGModel;

public interface ExportService {

	String downloadChartDataPDF(List<SVGModel> listOfSvgs, String districtName, String blockName,
			HttpServletRequest request, String stateName, String areaLevel, String dashboardType, String checkListName, String timePeriod, String villageName);

	String downloadChartDataExcel(List<SVGModel> listOfSvgs, ParamModel paramModel, HttpServletRequest request);

}
