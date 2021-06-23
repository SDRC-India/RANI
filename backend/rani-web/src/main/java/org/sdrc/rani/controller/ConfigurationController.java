package org.sdrc.rani.controller;

import org.sdrc.rani.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.co.sdrc.sdrcdatacollector.engine.UploadFormConfigurationService;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 */

@RestController
@RequestMapping("/api")
public class ConfigurationController {

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private UploadFormConfigurationService uploadFormConfigurationService;

	@GetMapping("/mongoClient")
	public String createMongoOauth2Client() {

		return configurationService.createMongoOauth2Client();

	}

	@GetMapping("/importQuestions")
	public ResponseEntity<String> importQuestions() {
		return uploadFormConfigurationService.importQuestionData();
	}

	@GetMapping("/area")
	public ResponseEntity<String> area() {
		return configurationService.importAreas();
	}

	@GetMapping("/config")
	public ResponseEntity<String> config() {
		return configurationService.config();
	}

	@GetMapping("/configureRoleFormMapping")
	public ResponseEntity<String> configureRoleFormMappingOfEngine() {
		return configurationService.configureRoleFormMapping();
	}

	@GetMapping("/formsValue")
	public ResponseEntity<String> formsValue() {
		return configurationService.formsValue();
	}

	@GetMapping("/configCluster")
	public ResponseEntity<String> configureClustersMapping() {
		return configurationService.configureClustersMapping();
	}

	@GetMapping("/configFormMapping")
	public ResponseEntity<String> configureFormMapping() {
		return configurationService.configureFormMapping();
	}

	@GetMapping("/configIFAsupplyPoint")
	public ResponseEntity<String> configIFAsupplyPoint() {
		return configurationService.configIFAsupplyPoint();
	}

	@GetMapping("/importIFAQuestions")
	public ResponseEntity<String> importIFAQuestions() {
		return configurationService.importIFAQuestions();
	}

	@GetMapping("/createDesgIFAMapping")
	public ResponseEntity<String> createDesgIFAMapping() {
		return configurationService.createDesgIFAMapping();
	}

	@GetMapping("/createPlanningQues")
	public ResponseEntity<String> createPlanningQuestions() {
		return configurationService.createPlanningQuestions();
	}
	
	@GetMapping("/configTestingDate")
	public ResponseEntity<String> configTestingDate() {
		return configurationService.configTestingDate();
	}
}
