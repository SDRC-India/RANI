package org.sdrc.rani.controller;

import java.util.List;
import java.util.Map;

import org.sdrc.rani.models.LineChartModel;
import org.sdrc.rani.models.PerformanceData;
import org.sdrc.rani.service.PerformanceReportService;
import org.sdrc.rani.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import in.co.sdrc.sdrcdatacollector.models.MessageModel;

/**
 * @author Biswabhusan Pradhan
 *
 */
//@RequestMapping("/api")
@RestController
public class ReportController {
	
	@Autowired
	private PerformanceReportService performanceReportService;
	
	@Autowired
	private ReportService reportService;
	
	@RequestMapping("/report")
	@ResponseBody
	public PerformanceData getPerformanceReportData(@RequestParam("formId")Integer formId,@RequestParam("designation")String designation,@RequestParam("startTp") Integer startTp, @RequestParam("endTp") Integer endTp) {
		return performanceReportService.getPerformanceData(formId,designation,startTp,endTp);
	}
	
	@RequestMapping("/hemocueReport")
	public PerformanceData getHemocueReportData(@RequestParam("areaLevel") String areaLevel,@RequestParam("startTp") Integer startTp,@RequestParam("endTp") Integer endTp) {
		return performanceReportService.getHemocueData(areaLevel,startTp,endTp);
	}
	
	@RequestMapping("/rejectionData")
	public PerformanceData getRejectionData(@RequestParam("formId") Integer formId,@RequestParam("startTp") Integer startTp,@RequestParam("endTp") Integer endTp) {
		return performanceReportService.getRejectionData(formId,startTp,endTp);
	}
	
	/**
	 * @author Subham Ashish
	 * @param formId
	 * @param designation
	 * @param startTp
	 * @param endTp
	 * @return
	 */
	@RequestMapping("/submissionReport")
	public ResponseEntity<MessageModel> getSubmissionReport(@RequestParam("formId")Integer formId,@RequestParam("designation")String designation,@RequestParam("startTp") Integer startTp, @RequestParam("endTp") Integer endTp) {
		return reportService.getSubmissionReport(formId,designation,startTp,endTp);
	}
	
	/**
	 * @author Subham Ashish
	 * @param formId
	 * @param startTp
	 * @param endTp
	 * @return
	 */
	@RequestMapping("/rejectionReport")
	public ResponseEntity<MessageModel> getSubmissionReport(@RequestParam("formId") Integer formId,@RequestParam("startTp") Integer startTp,@RequestParam("endTp") Integer endTp) {
		return reportService.getRejectionReport(formId,startTp,endTp);
	}
	
	/**
	 * @author Subham Ashish
	 * @param areaLevel
	 * @param startTp
	 * @param endTp
	 * @return
	 */
	@RequestMapping("/downloadHemocueReport")
	public ResponseEntity<MessageModel> gethemocueReport(@RequestParam("areaLevel") String areaLevel,@RequestParam("startTp") Integer startTp,@RequestParam("endTp") Integer endTp) {
		return reportService.gethemocueReport(areaLevel,startTp,endTp);
	}
	
	@RequestMapping("/getLineChartData")
	public List<LineChartModel> getLineChartData(@RequestParam("indicatorId") Integer indicatorId,@RequestParam("tp") Integer tp,@RequestParam("areaId") Integer areaId){
		return performanceReportService.getLineChartData(indicatorId, tp, areaId);
	}
	
	@RequestMapping("/getDesignationForm")
	public List<Map> getDesignationForm(){
		return performanceReportService.getDesignationFormData();
	}
}
