package org.sdrc.rani.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.sdrc.rani.models.UserModel;
import org.sdrc.rani.service.SubmissionService;
import org.sdrc.rani.util.TokenInfoExtracter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.co.sdrc.sdrcdatacollector.engine.FormsService;
import in.co.sdrc.sdrcdatacollector.models.FormAttachmentsModel;
import in.co.sdrc.sdrcdatacollector.models.QuestionUpdateModel;
import in.co.sdrc.sdrcdatacollector.models.ReceiveEventModel;
import in.co.sdrc.sdrcdatacollector.models.ReviewPageModel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Azar
 * 
 * @author Subham Ashish(subham@sdrc.co.in)
 */

@RestController
@RequestMapping("/api")
@Slf4j
public class DataEntryController {

	@Autowired
	private FormsService dataEntryService;

	@Autowired
	private SubmissionService submissionService;

	@Autowired
	private TokenInfoExtracter tokenInfoExtracter;

	@GetMapping("/getQuestion")
	@PreAuthorize("hasAuthority('DATA_ENTRY')")
	public QuestionUpdateModel getQuestions(@RequestParam(value = "lastUpdatedDate", required = false) String lastUpdatedDate,HttpSession session,
			OAuth2Authentication auth) {

		UserModel user = tokenInfoExtracter.getUserModelInfo(auth);

		Map<String, Object> map = new HashMap<>();

		return dataEntryService.getQuestions(map, session, user, (Integer) user.getDesgSlugIds().toArray()[0],lastUpdatedDate);
	}

	@RequestMapping(value = "/saveData", method = { RequestMethod.POST, RequestMethod.OPTIONS })
	public ResponseEntity<String> saveNewSubmission(@RequestBody ReceiveEventModel receiveEventModel,
			OAuth2Authentication oauth) throws Exception {
		
		int retryCount = 0;
		boolean uploaded = false;
		
		while (uploaded == false && retryCount < 10) {
			try {
				ResponseEntity<String> response = submissionService.saveSubmission(receiveEventModel, oauth);
				uploaded = true;
				return response;
			} catch (OptimisticLockingFailureException e) {
				retryCount++;
				continue;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		throw new RuntimeException("Error while saving data with payload {} "+receiveEventModel);
	}

	@RequestMapping(value = "uploadFile", method = { RequestMethod.POST, RequestMethod.OPTIONS }, consumes = {
			"multipart/form-data" })
	public String uploadFiles(@RequestParam("file") MultipartFile file, @RequestParam("fileModel") String fileModel)
			throws Exception {

		log.info("Action : while uploading attachment imageName {} and fileModel {}",file.getOriginalFilename(),fileModel);
		
		ObjectMapper objectMapper = new ObjectMapper();
		FormAttachmentsModel readValue = objectMapper.readValue(fileModel, FormAttachmentsModel.class);

		int retryCount = 0;
		boolean uploaded = false;

		while (uploaded == false && retryCount < 5) {
			try {
				String response = submissionService.uploadFiles(file, readValue);
				uploaded = true;
				return response;
			} catch (OptimisticLockingFailureException e) {
				retryCount++;
				continue;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		log.error("Error while uploading files with payload {} "+fileModel);
		throw new RuntimeException("Error while uploading files with payload {} "+fileModel);
	}

	@GetMapping("/getAllForms")
	public ReviewPageModel getAllForms(HttpSession session, OAuth2Authentication auth) {
		UserModel user = tokenInfoExtracter.getUserModelInfo(auth);
		Map<String, Object> map = new HashMap<>();

		return dataEntryService.getAllForms(map, session, user, (Integer) user.getDesgSlugIds().toArray()[0]);
	}

}
