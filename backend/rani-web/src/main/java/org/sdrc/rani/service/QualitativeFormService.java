package org.sdrc.rani.service;

import java.util.List;
import java.util.Map;

import org.sdrc.rani.models.QualitativeJSONTableModel;
import org.sdrc.rani.models.QualityReportModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author subham
 *
 */
public interface QualitativeFormService {

//	ResponseEntity<String> saveQualitativeData(List<QualityReportModel> qualityReportModel, OAuth2Authentication oauth);

	Map<String,QualitativeJSONTableModel> getQualitativeData(OAuth2Authentication oauth);

	Map<String, QualitativeJSONTableModel> getQualitativeDDMview(OAuth2Authentication oauth);

	ResponseEntity<String> uploadQualitativeReport(OAuth2Authentication oauth, MultipartFile file);

	Map<String, QualitativeJSONTableModel> getDDMQualitativeData(OAuth2Authentication oauth);

	ResponseEntity<String> saveQualitativeData(List<QualityReportModel> qualityReportModel, MultipartFile file,
			OAuth2Authentication oauth);

}
