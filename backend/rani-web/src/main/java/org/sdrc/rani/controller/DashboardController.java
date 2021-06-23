package org.sdrc.rani.controller;

import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sdrc.rani.document.AreaLevel;
import org.sdrc.rani.document.TimePeriod;
import org.sdrc.rani.models.FormSectorModel;
import org.sdrc.rani.models.IndicatorModel;
import org.sdrc.rani.models.ParamModel;
import org.sdrc.rani.models.PerformanceData;
import org.sdrc.rani.models.SVGModel;
import org.sdrc.rani.models.SectorModel;
import org.sdrc.rani.models.ThematicDashboardDataModel;
import org.sdrc.rani.models.TimePeriodModel;
import org.sdrc.rani.repositories.TimePeriodRepository;
import org.sdrc.rani.service.DashboardService;
import org.sdrc.rani.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@EnableScheduling
@Slf4j
public class DashboardController {
	
	@Autowired
	public DashboardService dashboardService;
	
	@Autowired
	private TimePeriodRepository timePeriodRepository;
	
	@Autowired
	private ExportService exportService;	
	
	private SimpleDateFormat simpleDateformater = new SimpleDateFormat("yyyy-MM-dd");
	private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	@GetMapping(value="/pushIndicatorGroupData")
	@ResponseBody
	public String pushIndicatorGroupData() {
		return	dashboardService.pushIndicatorGroupData();
	}
	
	@GetMapping(value="/getFormSectorMappingData")
	@ResponseBody
	public Map<String, List<FormSectorModel>> getFormSectorMappingData() {
		return	dashboardService.getFormSectorMappingData();
	}
	
	@GetMapping(value="/getIndicators")
	@ResponseBody
	public List<IndicatorModel> getIndicators(@RequestParam(value="formId") Integer formId) {
		return	dashboardService.getIndicators(formId);
	}
	
	@GetMapping(value="/getThematicViewData")
	@ResponseBody
	public Map<String, Object> getThematicViewData(@RequestParam(value="indicatorId") Integer indicatorId, 
										@RequestParam(value="tpId") Integer tpId,
										@RequestParam("areaLevel") Integer areaLevel,
										@RequestParam(value="areaId", required=true) Integer areaId,
										@RequestParam(value="sectorName", required=false) String sectorName) {
		/*Integer indicatorId = 1;
		Integer tpId = 7;
		Integer areaLevel = 1;
		Integer areaId = 4;
		String sectorName="T4";*/
		return	dashboardService.getThematicViewData(indicatorId, tpId, areaLevel, areaId, sectorName);
	}
	
	@GetMapping(value="/getDashboardData")
	public List<SectorModel> getDashboardData(@RequestParam("areaLevel") Integer areaLevel, 
			@RequestParam(value="areaId", required=true) Integer areaId,
			@RequestParam(value="sectorName", required=false) String sectorName, 
			@RequestParam(value="tpId", required=false) Integer tpId,
			@RequestParam(value="formId", required=false) Integer formId,
			@RequestParam(value="dashboardType", required=true)  String dashboardType) {
		
		return dashboardService.getDashboardData(areaLevel,areaId, sectorName, tpId, formId, dashboardType);
	}
	
	@GetMapping("/getAllChecklistSectors")
	public Map<String, List<FormSectorModel>> getAllChecklistSectors() {

		return dashboardService.getAllChecklistSectors();
	}
	
	@GetMapping("/getAllTimeperiods")
	public List<TimePeriodModel> getAllTimeperiods() {

		return dashboardService.getAllTimeperiods();
	}
	
	@GetMapping("/getAreaLevels")
	public List<AreaLevel> getAreaLevels() {

		return dashboardService.getAreaLevels();
	}
	
	@PostMapping(value = "/downloadChartDataPDF")
	public ResponseEntity<InputStreamResource> downloadChartDataPDF(@RequestBody List<SVGModel> listOfSvgs,
			@RequestParam(value = "districtName", required = false) String districtName,
			@RequestParam(value = "blockName", required = false) String blockName, HttpServletResponse response,
			HttpServletRequest request, @RequestParam(value = "stateName", required = false) String stateName,
			@RequestParam(value = "villageName", required = false) String villageName,
			@RequestParam("areaLevel") String areaLevel, @RequestParam("dashboardType") String dashboardType,
			@RequestParam(value = "checkListName", required = false) String checkListName,
			@RequestParam(value = "timePeriod", required = false) String timePeriod) {
		
		String filePath = "";
		try {
			filePath = exportService.downloadChartDataPDF(listOfSvgs, districtName, blockName, 
					request,stateName, areaLevel,dashboardType, checkListName, timePeriod, villageName);
			File file = new File(filePath);

			HttpHeaders respHeaders = new HttpHeaders();
			respHeaders.add("Content-Disposition", "attachment; filename=" + file.getName());
			InputStreamResource isr = new InputStreamResource(new FileInputStream(file));

			file.delete();
			return new ResponseEntity<InputStreamResource>(isr, respHeaders, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error : error while exporting pdf {} ",e);
			throw new RuntimeException(e);
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@PostMapping(value = "/downloadChartDataExcel")
	public ResponseEntity<InputStreamResource> downloadChartDataExcel(@RequestBody ParamModel paramModel,
			HttpServletResponse response, HttpServletRequest request) {

		String filePath = "";
		try {
			filePath = exportService.downloadChartDataExcel(paramModel.getListOfSvgs(), paramModel, request);
			File file = new File(filePath);

			HttpHeaders respHeaders = new HttpHeaders();
			respHeaders.add("Content-Disposition", "attachment; filename=" + file.getName());
			InputStreamResource isr = new InputStreamResource(new FileInputStream(file));

			file.delete();
			return new ResponseEntity<InputStreamResource>(isr, respHeaders, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
	
	@GetMapping("/getOntimeData")
	public Map<String,PerformanceData> getOntimeData(){
		return dashboardService.getOntimeData();
	}
	
	@GetMapping("/getPlannedData")
	public Map<String,PerformanceData> getPlannedData(){
		return dashboardService.getPlanningdata();
	}
	
	/**
	 * @author Subham Ashish(subham@sdrc.co.in)
	 * 
	 * @param thematicDashboardDataModel
	 * @param request
	 * @return
	 */
	@PostMapping("/thematicViewDownloadPDF")
	public ResponseEntity<InputStreamResource> getThematicViewDownload(@RequestBody ThematicDashboardDataModel thematicDashboardDataModel,HttpServletRequest request) {
		
		String filePath = "";
		
		try {
			ResponseEntity<String> result = dashboardService.getThematicViewDownload(thematicDashboardDataModel,request);
			filePath=result.getBody();
			File file = new File(filePath);
			HttpHeaders respHeaders = new HttpHeaders();
			respHeaders.add("Content-Disposition", "attachment; filename=" + file.getName());
			InputStreamResource isr = new InputStreamResource(new FileInputStream(file));

			file.delete();
			return new ResponseEntity<InputStreamResource>(isr, respHeaders, HttpStatus.OK);
			
		}catch(Exception e) {
		throw new RuntimeException(e);	
		}
		
	}
	/**
	 * @author Subham Ashish(subham@sdrc.co.in)
	 * 
	 * @param thematicDashboardDataModel
	 * @param request
	 * @return
	 */
	@PostMapping("/thematicViewDownloadExcel")
	public ResponseEntity<InputStreamResource> getThematicViewDownloadExcel(@RequestBody ThematicDashboardDataModel thematicDashboardDataModel,HttpServletRequest request) {
		
		String filePath = "";
		
		try {
			ResponseEntity<String> result = dashboardService.getThematicViewDownloadExcel(thematicDashboardDataModel,request);
			filePath=result.getBody();
			File file = new File(filePath);
			HttpHeaders respHeaders = new HttpHeaders();
			respHeaders.add("Content-Disposition", "attachment; filename=" + file.getName());
			InputStreamResource isr = new InputStreamResource(new FileInputStream(file));

			file.delete();
			return new ResponseEntity<InputStreamResource>(isr, respHeaders, HttpStatus.OK);
			
		}catch(Exception e) {
		throw new RuntimeException(e);	
		}
		
	}
	@GetMapping("getPerformanceTrend")
	public Map<String, Object> getPerformanceTrend(@RequestParam("formId") Integer formId, @RequestParam("performanceType") Integer performanceType){
		return performanceType==1?dashboardService.getAchievementData(formId):dashboardService.getPerformanceTrend(formId);
	}
	
	@Scheduled(cron="0 0 0 11 * ?")
	public void saveAchievemaneData() throws ParseException {
		TimePeriod tp=getTimePeriodForAggregation();
		dashboardService.saveAchievemaneData(tp.getTimePeriodId());
	}
	
	@GetMapping("saveAchievemaneDataGet")
	public void saveAchievemaneDataGet(@RequestParam("tp") Integer tp) throws ParseException {
		dashboardService.saveAchievemaneData(tp);
	}
	
//	@Scheduled(cron="0 0 0 11 * ?")
	@GetMapping("/mapUser")
	public void mapUser() {
		dashboardService.mapUsers();
	}
	
	@GetMapping("/updateUserMap")
	public void updateUserMap(@RequestParam("username") String username, @RequestParam("areaIds") List<Integer> areaIds) {
		dashboardService.updateUserMap(username, areaIds);
	}
	
	public TimePeriod getTimePeriodForAggregation() throws ParseException {
		Calendar endDateCalendar = Calendar.getInstance();
		endDateCalendar.add(Calendar.MONTH, -1);
		endDateCalendar.set(Calendar.DATE, 1);
		endDateCalendar.set(Calendar.DATE, endDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));

		Date eDate = endDateCalendar.getTime();
		String endDateStr = simpleDateformater.format(eDate);
		Date endDate = (Date) formatter.parse(endDateStr + " 23:59:59.000");
		
		Calendar startDateCalendar1 = Calendar.getInstance();
		startDateCalendar1.add(Calendar.MONTH, -1);
		startDateCalendar1.set(Calendar.DATE, 1);
		Date startDate1 = (Date) formatter.parse(simpleDateformater.format(startDateCalendar1.getTime()) + " 00:00:00.000");
		String sd=toISO8601UTC(new java.util.Date(startDate1.getTime()));
		String ed=toISO8601UTC(new java.util.Date(endDate.getTime()));
		
//		TimePeriod timePeriod = timePeriodRepository.getTimePeriod(cd);
		TimePeriod timePeriod = timePeriodRepository.getTimePeriod(sd, ed);
		return timePeriod;
	}
	
	public static String toISO8601UTC(Date date) {
		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		df.setTimeZone(tz);
		return df.format(date);
		}
}
