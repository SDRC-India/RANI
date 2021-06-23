package org.sdrc.rani.service;

import java.util.List;
import java.util.Map;

import org.sdrc.rani.models.AccountModel;
import org.sdrc.rani.models.PlanningDataTargetModel;
import org.sdrc.rani.models.QualitativeJSONTableModel;
import org.sdrc.rani.models.TimePeriodModel;
import org.sdrc.usermgmt.mongodb.domain.Designation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.multipart.MultipartFile;

import in.co.sdrc.sdrcdatacollector.models.MessageModel;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
public interface PlanningModuleService {

	Map<String, List<TimePeriodModel>> getTimePeriod();

	Map<Boolean, List<Map<String, String>>> getTimePeriodForTemplateUpload();

	String downloadTemplate(String date, String roleId);

	ResponseEntity<String> uploadPlanningTemplate(MultipartFile file, String date, String roleId,
			OAuth2Authentication oauth);

	List<Map<String, String>> getTimePeriodForManagePlanning();

	Map<String, QualitativeJSONTableModel> getManagePlanValue(String roleId, String date, String accId);

	ResponseEntity<String> updatePlan(PlanningDataTargetModel model);

	Map<String, List<AccountModel>> getUserAccounts(OAuth2Authentication oauth);

	List<Designation> getPlanningDesignations(OAuth2Authentication oauth);

	ResponseEntity<MessageModel> getPlanningReport(String roleId, String timePeriodId);

	Map<String, QualitativeJSONTableModel> getPlanningAndAchievemntForMobileData(String accId);

	ResponseEntity<MessageModel> getPlanningReportPDF(String roleId, String timePeriodId);

}
