package org.sdrc.rani.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.sdrc.rani.document.QualitativeReportFileData;
import org.sdrc.rani.document.QualitativeReportFormData;
import org.sdrc.rani.models.QualitativeJSONTableModel;
import org.sdrc.rani.models.QualityReportModel;
import org.sdrc.rani.repositories.QualitativeReportFileDataRepository;
import org.sdrc.rani.repositories.QualitativeReportFormDataRepository;
import org.sdrc.rani.service.QualitativeFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
@RequestMapping("/api")
@Slf4j
public class QualitativeFormController {

	@Autowired
	private QualitativeFormService qualitativeFormService;

	@Autowired
	private QualitativeReportFormDataRepository qualitativeReportFormDataRepository;
	
	@Autowired
	private ConfigurableEnvironment configurableEnvironment;
	
	@Autowired
	private  QualitativeReportFileDataRepository qualitativeReportFileDataRepository;

	@RequestMapping(value = "/saveQualitativeData", method = { RequestMethod.POST, RequestMethod.OPTIONS })
	public ResponseEntity<String> saveQualitativeData(@RequestBody List<QualityReportModel> qualityReportModel
			, OAuth2Authentication oauth) throws Exception {
		
		return qualitativeFormService.saveQualitativeData(qualityReportModel,null, oauth);
	}
	
	@RequestMapping(value = "/saveQualitativeFile", method = { RequestMethod.POST, RequestMethod.OPTIONS },consumes = {"multipart/form-data" })
	public ResponseEntity<String> saveQualitativeFile(@RequestParam(name="file") MultipartFile file,OAuth2Authentication oauth) throws Exception {
		
		return qualitativeFormService.saveQualitativeData(null,file, oauth);
	}
	
	/**
	 * 
	 * @param oauth
	 * @return
	 */
	@GetMapping("getQualitativeData")
	private Map<String, QualitativeJSONTableModel> getQualitativeData(OAuth2Authentication oauth) {

		return qualitativeFormService.getQualitativeData(oauth);

	}

	/**
	 * this method is for DDM DATA views
	 * 
	 * @param oauth
	 * @return
	 */
	@GetMapping("getQualitativesDatas")
	private Map<String, QualitativeJSONTableModel> getQualitativeDDMview(OAuth2Authentication oauth) {

		return qualitativeFormService.getQualitativeDDMview(oauth);

	}

	/**
	 * fetches the record uploaded by ddm
	 * @param oauth
	 * @return
	 */
	@GetMapping("getDDMQualitativeData")
	private Map<String, QualitativeJSONTableModel> getDDMQualitativeData(OAuth2Authentication oauth) {

		return qualitativeFormService.getDDMQualitativeData(oauth);

	}
	
	/**
	 * This method takes the file uploaded by ddm and save its path
	 * @param file
	 * @param oauth
	 * @return
	 */
	@RequestMapping(value = "uploadQualitativeReportFile")
	private ResponseEntity<String> uploadQualitativeReport(@RequestParam("file") MultipartFile file,OAuth2Authentication oauth) {
		
		return qualitativeFormService.uploadQualitativeReport(oauth,file);

	}

	@RequestMapping(value = "/doc", method = RequestMethod.GET)
	public void downLoad(@RequestParam("id") String id, HttpServletResponse response) throws IOException {

		String name=null;
		String extension=null;
		
		QualitativeReportFormData data = qualitativeReportFormDataRepository.findById(id);
		
		if(data!=null){
			name = data.getFilePath();
			extension=data.getExtension();
		}
		else{
			QualitativeReportFileData fileData = qualitativeReportFileDataRepository.findById(id);
			name=fileData.getFilePath();
		}
		
		name=configurableEnvironment.getProperty("drive.path")+name;
		
		InputStream inputStream;
		try {
			String fileName = name.replaceAll("%3A", ":").replaceAll("%2F", "/").replaceAll("%2C", ",")
					.replaceAll("\\+", " ").replaceAll("%20", " ").replaceAll("%26", "&").replaceAll("%5C", "/");
			inputStream = new FileInputStream(fileName);
			String headerKey = "Content-Disposition";
			String headerValue = String.format("inline; filename=\"%s\"", new java.io.File(fileName).getName());
			response.setHeader(headerKey, headerValue);
			if(extension==null)
				response.setContentType("application/pdf");// pdf file type
			else
				response.setContentType("application/"+extension);
			ServletOutputStream outputStream = response.getOutputStream();
			FileCopyUtils.copy(inputStream, outputStream);
			inputStream.close();
			outputStream.flush();
			outputStream.close();

		} catch (IOException e) {
			log.error("error-while downloading Qualitative Report with payload : {}", name, e);
			throw new RuntimeException(e);
		}
	}
}
