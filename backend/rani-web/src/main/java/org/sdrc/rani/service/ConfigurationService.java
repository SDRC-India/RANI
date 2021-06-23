package org.sdrc.rani.service;

import org.springframework.http.ResponseEntity;

/**
 * @author subham
 *
 */
public interface ConfigurationService {

	ResponseEntity<String> importAreas();

	ResponseEntity<String> formsValue();

	String createMongoOauth2Client();

	ResponseEntity<String> config();

	ResponseEntity<String> configureClustersMapping();

	ResponseEntity<String> configureRoleFormMapping();

	ResponseEntity<String> configureFormMapping();

	ResponseEntity<String> configIFAsupplyPoint();

	ResponseEntity<String> importIFAQuestions();

	ResponseEntity<String> createDesgIFAMapping();

	ResponseEntity<String> createPlanningQuestions();

	ResponseEntity<String> configTestingDate();
}
