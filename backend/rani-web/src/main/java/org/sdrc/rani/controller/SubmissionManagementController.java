package org.sdrc.rani.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.sdrc.rani.models.UserModel;
import org.sdrc.rani.models.ValueObject;
import org.sdrc.rani.service.SubmissionManagementService;
import org.sdrc.rani.service.WebService;
import org.sdrc.rani.util.TokenInfoExtracter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.co.sdrc.sdrcdatacollector.engine.FormsService;
import in.co.sdrc.sdrcdatacollector.models.DataObject;
import in.co.sdrc.sdrcdatacollector.models.QuestionModel;
import in.co.sdrc.sdrcdatacollector.models.ReviewPageModel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class SubmissionManagementController {

	@Autowired
	private SubmissionManagementService reviewService;

	@Autowired
	private TokenInfoExtracter tokenInfoExtracter;

	@Autowired
	private FormsService dataEntryService;

	@Autowired
	private WebService webService;

	@RequestMapping(value = "/rejectMultipleSubmission", method = RequestMethod.POST)
	public ResponseEntity<String> rejectSubmissions(@RequestBody ValueObject valueObject, OAuth2Authentication auth) {

		return reviewService.rejectSubmissions(valueObject, auth);

	}

	@GetMapping("/getRejectedData")
	public Map<String, List<DataObject>> getRejectedData(HttpSession session, OAuth2Authentication auth) {
		UserModel user = tokenInfoExtracter.getUserModelInfo(auth);

		return dataEntryService.getRejectedData(new HashMap<>(), session, user,
				(Integer) user.getDesgSlugIds().toArray()[0]);
	}

	@GetMapping("/getDataForReview")
	public ReviewPageModel getDataForReview(@RequestParam("formId") Integer formId,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam("pageNo") Integer pageNo,HttpSession session,
			OAuth2Authentication auth) {

		UserModel user = tokenInfoExtracter.getUserModelInfo(auth);
		Map<String, Object> paramKeyValMap = new HashMap<>();
		paramKeyValMap.put("pageNo", pageNo);
		paramKeyValMap.put("review", "reviewData");
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		Date date = new Date();
		startDate = sdf.format(date);
		endDate = sdf.format(date);

		ReviewPageModel model = dataEntryService.getDataForReview(formId, startDate, endDate, paramKeyValMap, session,
				user, (Integer) user.getDesgSlugIds().toArray()[0]);

		/**
		 * if the date between 4 to 10 inclusive last month data would be enable
		 * for rejection, current month data would be available for
		 * view(rejection button would be disabled)
		 */
		return webService.getReviewData(model);

	}

	/**
	 * get supervisor data against CF Form
	 */
	@GetMapping("/getSubmissionData")
	public ReviewPageModel getDataForReview(@RequestParam("formId") Integer formId,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam("submissionId") String submissionId, OAuth2Authentication auth, HttpSession session) {

		UserModel user = tokenInfoExtracter.getUserModelInfo(auth);
		Map<String, Object> paramKeyValMap = new HashMap<>();
		paramKeyValMap.put("submissionId", submissionId);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		Date date = new Date();
		startDate = sdf.format(date);
		endDate = sdf.format(date);

		formId = webService.getSupervisorFormId(formId);
		
		ReviewPageModel model = dataEntryService.getDataForReview(formId, startDate, endDate, paramKeyValMap, session,
				user, (Integer) user.getDesgSlugIds().toArray()[0]);

		return model;

	}

	@RequestMapping(value = "/submissionImage", method = RequestMethod.GET)
	public void downLoad(@RequestParam("filePath") String filePath, HttpServletResponse response) throws IOException {

		InputStream inputStream;
				
		try {
			String fileName = filePath.replaceAll("%3A", ":").replaceAll("%2F", "/").replaceAll("%2C", ",")
					.replaceAll("\\+", " ").replaceAll("%20", " ").replaceAll("%26", "&").replaceAll("%5C", "/");
			inputStream = new FileInputStream(fileName);
			String headerKey = "Content-Disposition";
			String headerValue = String.format("inline; filename=\"%s\"", new java.io.File(fileName).getName());
			response.setHeader(headerKey, headerValue);
			response.setContentType("image/jpeg");
			ServletOutputStream outputStream = response.getOutputStream();
			FileCopyUtils.copy(inputStream, outputStream);
			inputStream.close();
			outputStream.flush();
			outputStream.close();

		} catch (IOException e) {
			log.error("error-while downloading Submission image with payload : {}", filePath, e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * This method returns only review data head
	 * @param formId
	 * @param auth
	 * @return
	 */
	@GetMapping("/getReviewData")
	public List<DataObject> getDataForReview(@RequestParam("formId") Integer formId,OAuth2Authentication auth) {

		UserModel user = tokenInfoExtracter.getUserModelInfo(auth);
		Map<String, Object> paramKeyValMap = new HashMap<>();
		paramKeyValMap.put("review", "reviewData");
		return reviewService.getReiewDataHead(formId,user,paramKeyValMap);

	}
	
	@GetMapping("/reviewViewMoreData")
	public Map<String, List<Map<String, List<QuestionModel>>>> getViewMoreDataForReview(@RequestParam("formId") Integer formId,@RequestParam("submissionId") String submissionId,OAuth2Authentication auth,HttpSession session) {

		UserModel user = tokenInfoExtracter.getUserModelInfo(auth);
		Map<String, Object> paramKeyValMap = new HashMap<>();
		paramKeyValMap.put("review", "reviewData");
		return reviewService.getViewMoreDataForReview(formId,user,submissionId,paramKeyValMap,session);

	}
}
