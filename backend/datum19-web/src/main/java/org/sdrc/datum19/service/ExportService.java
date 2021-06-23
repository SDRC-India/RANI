package org.sdrc.datum19.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.sdrc.datum19.model.ParamModel;
import org.sdrc.datum19.model.SVGModel;

public interface ExportService {

	String downloadChartDataPDF(List<SVGModel> listOfSvgs, String sectorName, String dashboardId,
			HttpServletRequest request);

	String downloadChartDataExcel(List<SVGModel> listOfSvgs, ParamModel paramModel, HttpServletRequest request);
	
}
