package org.sdrc.rani.controller;

import java.util.List;
import java.util.Map;

import org.sdrc.rani.models.AccountModel;
import org.sdrc.rani.models.PlanningDataTargetModel;
import org.sdrc.rani.models.QualitativeJSONTableModel;
import org.sdrc.rani.models.TimePeriodModel;
import org.sdrc.rani.service.PlanningModuleService;
import org.sdrc.usermgmt.mongodb.domain.Designation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import in.co.sdrc.sdrcdatacollector.models.MessageModel;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
@RestController
public class PlanningModuleController {

	@Autowired
	private PlanningModuleService planningModuleService;

	@GetMapping("/downloadPlanningTemplate")
	public String downloadPlanningTemplate(@RequestParam("date") String date, @RequestParam("roleId") String roleId) {
		return planningModuleService.downloadTemplate(date, roleId);
	}

	@PostMapping("/uploadPlanningTemplate")
	public ResponseEntity<String> uploadPlanningTemplate(@RequestParam("file") MultipartFile file,
			@RequestParam("date") String date, @RequestParam("roleId") String roleId, OAuth2Authentication oauth) {
		return planningModuleService.uploadPlanningTemplate(file, date, roleId, oauth);
	}

	@GetMapping("/getPlanningTimePeriod")
	public Map<Boolean, List<Map<String, String>>> getTimePeriodForTemplateUpload() {
		return planningModuleService.getTimePeriodForTemplateUpload();
	}

	@GetMapping("/getManagePlanningTimePeriod")
	public List<Map<String, String>> getTimePeriodForManagePlanning() {
		return planningModuleService.getTimePeriodForManagePlanning();
	}

	@GetMapping("/managePlanning")
	public Map<String, QualitativeJSONTableModel> getManagePlanValue(@RequestParam("roleId") String roleId,
			@RequestParam("date") String date, @RequestParam("accId") String accId) {
		return planningModuleService.getManagePlanValue(roleId, date, accId);
	}

	@PostMapping("/updatePlan")
	public ResponseEntity<String> updatePlan(@RequestBody PlanningDataTargetModel model) {
		return planningModuleService.updatePlan(model);
	}

	/**
	 * get all supervisor and community facilitator users
	 */

	@GetMapping("/getManageUsers")
	public Map<String, List<AccountModel>> getUserAccounts(OAuth2Authentication oauth) {
		return planningModuleService.getUserAccounts(oauth);
	}

	/**
	 * only supervisor and cf designation is applicable for planing module
	 * template upload download
	 * 
	 * @param oauth
	 * @return
	 */
	@GetMapping("/getPlanningDesignations")
	public List<Designation> getPlanningDesignations(OAuth2Authentication oauth) {
		return planningModuleService.getPlanningDesignations(oauth);
	}

	/**
	 * fetch time period for dropdown selection
	 */
	@GetMapping("/getPlanningReportTimePeriod")
	public Map<String, List<TimePeriodModel>> getTimePeriod() {

		return planningModuleService.getTimePeriod();
	}

	@GetMapping("/getPlanningReport")
	public ResponseEntity<MessageModel> getPlanningReport(@RequestParam("roleId") String roleId,
			@RequestParam("timePeriodId") String timePeriodId) {

		return planningModuleService.getPlanningReport(roleId,timePeriodId);
	}
	
	@GetMapping("/getPlanVSAchievement")
	public Map<String, QualitativeJSONTableModel> getPlanningAndAchievemntForMobileData(@RequestParam("accId") String accId) {
		return planningModuleService.getPlanningAndAchievemntForMobileData(accId);
	}
	
	@GetMapping("/getPlanningReportPDF")
	public ResponseEntity<MessageModel> getPlanningReportPDF(@RequestParam(value="roleId",required=true) String roleId,
			@RequestParam(value="timePeriodId",required=true) String timePeriodId) {

		return planningModuleService.getPlanningReportPDF(roleId,timePeriodId);
	}
}
