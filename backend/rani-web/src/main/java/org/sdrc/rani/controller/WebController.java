package org.sdrc.rani.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.sdrc.rani.document.AreaLevel;
import org.sdrc.rani.models.AreaModel;
import org.sdrc.rani.models.FormModel;
import org.sdrc.rani.models.IFAValueModel;
import org.sdrc.rani.models.QuickStartModel;
import org.sdrc.rani.models.UserDetailsModel;
import org.sdrc.rani.service.WebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
@RestController
@Slf4j
public class WebController {

	@Autowired
	private ConfigurableEnvironment configurableEnvironment;

	@Autowired
	private WebService webService;

	@RequestMapping("/")
	public String welcome() {
		return (configurableEnvironment.getProperty("welcome"));

	}

	@RequestMapping(value = "/getAllArea")
	public Map<String, List<AreaModel>> getArea() {

		return webService.getAllAreaList();

	}
	
	@RequestMapping(value = "/getAllClusterArea")
	public Map<String, List<AreaModel>> getAllClusterArea() {

		return webService.getAllClusterArea();

	}

	@RequestMapping(value = "/getUsers")
	public List<UserDetailsModel> getAllUsers(@RequestParam(value = "roleId", required = false) String roleId,
			@RequestParam(value = "areaId", required = false) List<Integer> areaId,
			@RequestParam(value = "userName", required = false) String userName) {

		return webService.getAllUsers(roleId, areaId, userName);
	}

	@RequestMapping(value = "/getAreaLevel")
	public List<AreaLevel> getAreaLevels() {

		return webService.getAreaLevels();
	}

	@GetMapping("/getAllForms")
	public List<FormModel> getAllForms(OAuth2Authentication auth) {
		return webService.getAllForms(auth);
	}

	@RequestMapping(value = "/downloadImage", method = RequestMethod.GET)
	public void downLoad(@RequestParam("path") String name, HttpServletResponse response) throws IOException {

		byte[] decodedBytes = Base64.getUrlDecoder().decode(name);
		name = new String(decodedBytes);

		name = name.replace("\\", "//");

		InputStream inputStream;

		try {
			String fileName = name.replaceAll("%3A", ":").replaceAll("%2F", "/").replaceAll("%2C", ",")
					.replaceAll("\\+", " ").replaceAll("%20", " ").replaceAll("%26", "&").replaceAll("%5C", "/");
			inputStream = new FileInputStream(fileName);
			String headerKey = "Content-Disposition";
			String headerValue = String.format("attachment; filename=\"%s\"", new java.io.File(fileName).getName());
			response.setHeader(headerKey, headerValue);
			response.setContentType("application/octet-stream"); // for all file
																	// type
			ServletOutputStream outputStream = response.getOutputStream();
			FileCopyUtils.copy(inputStream, outputStream);
			inputStream.close();
			outputStream.flush();
			outputStream.close();

		} catch (IOException e) {
			log.error("error-while downloading image with payload : {}", name, e);
			throw new RuntimeException();
		}

	}

	/**
	 * get IFA details while creating user
	 */
	@GetMapping("getIFAData")
	public Map<String, List<IFAValueModel>> getIFASupplyMapping() {
		return webService.getIFASupplyMapping();
	}

	/**
	 * quick start indicators at home page
	 * @return
	 */
	@GetMapping("quickStart")
	public QuickStartModel getQuickStartValue() {
		return webService.getQuickStartValue();
	}

	
	/**
	 * quick start indicators at home page
	 * @return
	 */
	@GetMapping("quickStartDates")
	public ResponseEntity<String> getQuickStartDates() {
		return webService.getQuickStartDates();
	}
	
	@RequestMapping(value = "bulkUserCreation", method = { RequestMethod.POST, RequestMethod.OPTIONS }, consumes = {
			"multipart/form-data" })
	public ResponseEntity<String> bulkUserCreation(@RequestParam("file") MultipartFile file,Principal p) {
		return webService.bulkUserCreation(file,p);
	}
	
	@GetMapping("/getTypeDeatilsNameAndId")
	public String getTypeDetailsIdAndTypeDetailsName() {
		return webService.getTypeDetailsIdAndTypeDetailsName();
	}
}
