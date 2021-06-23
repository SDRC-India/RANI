package org.sdrc.datum19.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sdrc.datum19.document.Area;
import org.sdrc.datum19.document.AreaLevel;
import org.sdrc.datum19.document.Dashboard;
import org.sdrc.datum19.document.DashboardIndicator;
import org.sdrc.datum19.document.Heading;
import org.sdrc.datum19.document.Indicator;
import org.sdrc.datum19.document.ThematicFileData;
import org.sdrc.datum19.document.TimePeriod;
import org.sdrc.datum19.model.DashboardIndicatorGroupModel;
import org.sdrc.datum19.model.DataValueModel;
import org.sdrc.datum19.model.FormSectorModel;
import org.sdrc.datum19.model.SectorModel;
import org.sdrc.datum19.model.TypeModel;
import org.sdrc.datum19.service.AreaService;
import org.sdrc.datum19.service.DashboardConfigService;
import org.sdrc.datum19.service.DashboardService;
import org.sdrc.datum19.util.ValueObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

//@CrossOrigin(origins = {"http://192.168.1.10:8080", "http://aggregation.sdrc.co.in:8080"})
//@CrossOrigin
@RestController
public class DashboardConfigController {

	@Autowired
	public DashboardConfigService dashboardConfigService;

	@Autowired
	public DashboardService dashboardService;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private AreaService areaService;

	@GetMapping("/getMSg")
	@ResponseBody
	public ResponseEntity<String> getMSg() {
		return new ResponseEntity<String>("Hello from datum resource server", HttpStatus.OK);
	}

	@PostMapping("/addDashboard")
	public Dashboard addDashboard(@RequestBody Dashboard dashboard) {
		return dashboardService.addDashboard(dashboard);
	}

	@GetMapping("/getDashboards")
	public List<Dashboard> getDashboards(@RequestParam(value = "username", required = false) String username) {
		return dashboardService.getDashboards(username);
	}

	@GetMapping("/getChartTypes")
	@ResponseBody
	public List<ValueObject> getChartTypes() {
		return dashboardConfigService.getChartTypes();
	}

	@GetMapping("/getUnits")
	@ResponseBody
	public List<ValueObject> getUnits() {
		return dashboardConfigService.getUnits();
	}

	@GetMapping("/getAlignments")
	@ResponseBody
	public List<ValueObject> getAlignments() {
		return dashboardConfigService.getAlignments();
	}

	@GetMapping("/getTypes")
	@ResponseBody
	public List<TypeModel> getTypes() {
		return dashboardConfigService.getTypes();
	}

	@PostMapping("/saveDashboardIndicatorConfig")
	@ResponseBody
	public ResponseEntity<String> saveIndiactor(@RequestBody DashboardIndicatorGroupModel indicatorGroupModel) {
		return dashboardConfigService.saveDashboardIndicatorConfig(indicatorGroupModel);
	}

	@GetMapping("/getDashboardIndicatorConfigGr")
	@ResponseBody
	public List<DashboardIndicator> getDashboardIndicatorConfigGr(@RequestParam("dashboardId") String dashboardId) {
		return dashboardConfigService.getDashboardIndicatorConfigGr(dashboardId);
	}

	@GetMapping("/getAllChecklistSectors")
	public Map<String, List<FormSectorModel>> getAllChecklistSectors() {

		return dashboardConfigService.getAllChecklistSectors();
	}

	@GetMapping(value = "/getDashboardData")
	public List<SectorModel> getDashboardData(@RequestParam(value = "sectorName") String sectorName,
			@RequestParam(value = "groupName", required = false) String groupName,
			@RequestParam(value = "dashboardId", required = false) String dashboardId,
			@RequestParam(value = "areaList", required = false) List<Integer> areaList,
			@RequestParam(value = "tp", required = false) Integer tp) {
		return dashboardService.getDashboardData(sectorName, groupName, dashboardId, areaList, tp);
	}

	@GetMapping(value = "/getDashboardGroupData")
	public List<SectorModel> getDashboardGroupData(@RequestParam(value = "groupName") String groupName,
			@RequestParam(value = "dashboardId", required = false) String dashboardId) {
		return dashboardService.getDashboardGroupData(groupName, dashboardId);
	}

	@GetMapping("/getIndicatorConfigEdit")
	@ResponseBody
	public ResponseEntity<DashboardIndicatorGroupModel> editIndiactor(@RequestParam("dashboardId") String dashboardId,
			@RequestParam(value = "chartGroupName") String chartGroupName) {
		return dashboardConfigService.getIndicatorConfigEdit(dashboardId, chartGroupName);
	}

	@PostMapping("/deleteChart")
	public String deleteChart(@RequestParam("id") String id) {
		dashboardConfigService.deleteChart(id);
		return "success";
	}

	@GetMapping("/getSearchedIndicatorForDashboard")
	List<Indicator> getSearchedIndicatorForDashboard(@RequestParam("formId") String formId,
			@RequestParam("unit") String unit, @RequestParam("serachText") String serachText) {
		List<Indicator> indicators = new ArrayList<>();
		if (!serachText.isEmpty()) {

			Query query = new Query();
			query.addCriteria(Criteria.where("indicatorDataMap.formId").is(formId).and("indicatorDataMap.unit").is(unit)
					.and("indicatorDataMap.indicatorName").regex(serachText)).limit(10);
			indicators = mongoTemplate.find(query, Indicator.class);
		}
		return indicators;
	}

	@GetMapping("/getSearchedIndicatorForDbTblWithGid")
	List<Indicator> getSearchedIndicatorForDbTblWithGid(@RequestParam("indicatorGid") String indicatorGid,
			@RequestParam("subgroupType") String subgroupType) {
		return dashboardConfigService.getSearchedIndicatorForDbTblWithGid(indicatorGid, subgroupType);
	}

	@GetMapping("/getSearchedIndicatorForDbTbl")
	List<Indicator> getSearchedIndicatorForDbTbl(@RequestParam(value = "subgroupTypeList") List<String> subgroupTypeList) {
		return dashboardConfigService.getSearchedIndicatorForDbTbl(subgroupTypeList);
	}

	@GetMapping("/getAllAreaLevel")
	@ResponseBody
	public List<AreaLevel> getAreaLevel() {
		return dashboardConfigService.getAreaLevel();
	}

	@GetMapping("/getAreaByAreaLevelId")
	@ResponseBody
	public List<Area> getAreaByAreaLevelId(@RequestParam("areaLevelId") Integer areaLevelId,
			@RequestParam("parentAreaId") Integer parentAreaId) {
		return dashboardConfigService.getAreaByAreaLevelId(areaLevelId, parentAreaId);
	}

	@ResponseBody
	@RequestMapping(value = "/uploadFiles")
	public ResponseEntity<String> uploadFiles(@RequestBody MultipartFile file,
			@RequestParam("filePath") String filePath) {
		return dashboardConfigService.uploadFile(file, filePath);

	}

	@GetMapping(value = "/gethematictDashboardData")
	public List<SectorModel> geThematictDashboardData(@RequestParam(value = "sectorName") String sectorName,
			@RequestParam(value = "groupName", required = false) String groupName) {
		return dashboardService.getThematic(sectorName, groupName);
	}

	@GetMapping(value = "/gethematictData")
	public Map<String, List<DataValueModel>> gethematictData(@RequestParam(value = "indecatorId") String indecatorId,
			@RequestParam(value = "tp", required = false) String tp,
			@RequestParam(value = "areaCode", required = false) String areaCode,
			@RequestParam(value = "indicatorGroup") String indicatorGroup) {
		return dashboardService.gethematictData(indecatorId, tp, areaCode, indicatorGroup);
	}

	@ResponseBody
	@RequestMapping(value = "/saveThematicFileData")
	public ResponseEntity<String> saveThematicFileData(@RequestBody ThematicFileData thematicFileData) {
		return new ResponseEntity<String>(dashboardService.saveThematicFileData(thematicFileData), HttpStatus.OK);

	}

	@ResponseBody
	@RequestMapping(value = "/getThematicMapValidation")
	public ResponseEntity<Boolean> getThematicMapValidation(
			@RequestParam(value = "parentAreaCode") String parentAreaCode, @RequestBody List<String> childAreaCodes) {

		return new ResponseEntity<Boolean>(dashboardService.getThematicMapValidation(parentAreaCode, childAreaCodes),
				HttpStatus.OK);

	}

	@ResponseBody
	@RequestMapping(value = "/getAllTimePeriod")
	public ResponseEntity<List<TimePeriod>> getListOfTimePeriod(
			@RequestParam(value = "periodicity") String periodicity) {
		return new ResponseEntity<List<TimePeriod>>(dashboardService.getListOfTimePeriod(periodicity), HttpStatus.OK);
	}

	@GetMapping(value = "/getGisData")
	public Map<String, List<DataValueModel>> geGisData(@RequestParam(value = "indecatorId") String indecatorId,
			@RequestParam(value = "tp", required = false) String tp,
			@RequestParam(value = "areaCode", required = false) String areaCode,
			@RequestParam(value = "indicatorGroup") String indicatorGroup) {
		return dashboardService.getGisData(indecatorId, tp, areaCode, indicatorGroup);
	}

	@ResponseBody
	@GetMapping("/getAreaByAreaName")
	public List<Area> getAreaByAreaName(@RequestParam("areaname") String areaname) {
		return areaService.getAreaByName(areaname);
	}

	@GetMapping("/saveHeading")
	@ResponseBody
	public Long saveHeading(@RequestParam(value = "title") String title) {
		return dashboardService.saveHeaderAndSubheader(title);
	}

	@GetMapping("/searchHeading")
	@ResponseBody
	public List<Heading> searchHeading(@RequestParam(value = "title") String title) {
		return dashboardService.searchHeading(title);
	}

	@GetMapping("/sectorByDashboard")
	@ResponseBody
	public List<String> sectorByDashboard(@RequestParam(value = "dashboardId") String dashboardId) {
		return dashboardService.getHeadingByDashboard(dashboardId);
	}

}
