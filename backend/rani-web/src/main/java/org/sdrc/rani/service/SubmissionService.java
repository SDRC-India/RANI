package org.sdrc.rani.service;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.multipart.MultipartFile;

import in.co.sdrc.sdrcdatacollector.models.FormAttachmentsModel;
import in.co.sdrc.sdrcdatacollector.models.ReceiveEventModel;

/**
 * @author subham
 *
 */
public interface SubmissionService {

	ResponseEntity<String> saveSubmission(ReceiveEventModel receiveEventModel, OAuth2Authentication oauth);

	String uploadFiles(MultipartFile file, FormAttachmentsModel fileModel);

}
