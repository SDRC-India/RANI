package org.sdrc.rani.service;

import org.springframework.http.ResponseEntity;

import in.co.sdrc.sdrcdatacollector.models.MessageModel;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
public interface ReportService {

	ResponseEntity<MessageModel> getSubmissionReport(Integer formId, String designation, Integer startTp, Integer endTp);

	ResponseEntity<MessageModel> getRejectionReport(Integer formId, Integer startTp, Integer endTp);

	ResponseEntity<MessageModel> gethemocueReport(String string, Integer startTp, Integer endTp);

}
