package org.sdrc.rani.service;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.sdrc.rani.document.AreaLevel;
import org.sdrc.rani.models.AreaModel;
import org.sdrc.rani.models.FormModel;
import org.sdrc.rani.models.IFAValueModel;
import org.sdrc.rani.models.QuickStartModel;
import org.sdrc.rani.models.UserDetailsModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.multipart.MultipartFile;

import in.co.sdrc.sdrcdatacollector.models.ReviewPageModel;

/**
 * @author subham
 *
 */
public interface WebService {

	Map<String, List<AreaModel>> getAllAreaList();

	List<UserDetailsModel> getAllUsers(String roleId, List<Integer> areaId, String userName);

	List<AreaLevel> getAreaLevels();

	List<FormModel> getAllForms(OAuth2Authentication auth);

	ReviewPageModel getReviewData(ReviewPageModel model);

	Integer getSupervisorFormId(Integer formId);

	Map<String, List<IFAValueModel>> getIFASupplyMapping();

	QuickStartModel getQuickStartValue();

	ResponseEntity<String> bulkUserCreation(MultipartFile file, Principal p);

	String getTypeDetailsIdAndTypeDetailsName();

	Map<String, List<AreaModel>> getAllClusterArea();

	ResponseEntity<String> getQuickStartDates();

}
