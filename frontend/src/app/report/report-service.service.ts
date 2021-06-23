import { Injectable } from '@angular/core';
import { Constants } from '../constants';
import { HttpClient } from '@angular/common/http';
declare var $;

@Injectable({
  providedIn: 'root'
})
export class ReportServiceService {
  reportForms: any;
  reviewDetails: any;
  seletedSubmissionDetails: any={};
  submissionId: number;
  rejectDetails: any;
  viewMoreClicked: boolean = false;
  viewSupervisorDetails: boolean = false;
  validationMsg: any;
  selectedSupervisorSubmissions: any;
  supervisorFormId: any;
  rejectedStatus: boolean;
  isRejectedSubmission: boolean;
  timeperiodLists: any;
  performanceView: any = {};
  usertype:any;
  geographytype:any;
  rejection:any ;
  formType:any ;

  constructor(private httpClient: HttpClient) { }

  /**
   * Get qualitative form questions  
   */
  getQualitativeFormQuestions() {
    return this.httpClient.get('assets/json/qualitativeForm.json');   
  }
  /**
   * Get qualitative form datas for supervisor
   */
  getQualitativeFormData() {
    return this.httpClient.get(Constants.HOME_URL + 'api/getQualitativeData');
  }  
  /**
   * Get qualitative supervisor form datas on DDM view
   */
  getQualitativeSupervisorDataOnDDM() {
    return this.httpClient.get(Constants.HOME_URL + 'api/getQualitativesDatas');
  }  

  /**
   * Get DDM uploaded file view in DDM login
   */
  getQualitativeDDMDataView() {
    return this.httpClient.get(Constants.HOME_URL + 'api/getDDMQualitativeData');
  } 
  /**
   * Get all report forms
   */
  getAllReportForms() {
    return this.httpClient.get(Constants.HOME_URL + 'getReportForms');
  }
  /**
   * Get all Submission data
   * @param formId 
   */
  getAllSubmissionDatas(formId) {
    //return this.httpClient.get(Constants.HOME_URL + 'api/getDataForReview?formId=' + formId + '&pageNo=' + pageNo);
    return this.httpClient.get(Constants.HOME_URL + 'api/getReviewData?formId=' + formId);
  }
  /**
   * Get all review forms
   */
  getAllReviewForms() {
    return this.httpClient.get(Constants.HOME_URL + 'getAllForms');
  }
  /**
   * Set view more data for each submission
   * @param details 
   */
  setviewMoreData(details) {
    this.rejectedStatus = details.tableRow.rejected;
    this.isRejectedSubmission = details.tableRow.extraKeys.isRejectable;
    this.httpClient.get(Constants.HOME_URL + 'api/reviewViewMoreData?formId=' + details.tableRow.formId + '&submissionId=' + details.tableRow.extraKeys.submissionId).subscribe((response) => {
      this.seletedSubmissionDetails.res = response;
      this.seletedSubmissionDetails.submissions = details;
      this.viewMoreClicked = true;
      $("#submissionDetailsModal").modal('show');
    });     
  }
  /**
   * Get view more data for each submission
   * 
   */
  getviewMoreData() {
    return this.seletedSubmissionDetails;
  }
  /**
   * Destroy the open modal data on button close
   */
  destroyModalData() {
    this.viewMoreClicked = false;
    this.viewSupervisorDetails = false;
    $("#submissionDetailsModal").modal('hide');
    $("#supervisorDetails").modal('hide');
  }
  /**
   * List submission id/ids for rejection
   * @param details 
   */
  submissionIdsForReject(details) {
    $("#submissionDetailsModal").modal('hide');
    $("#confirmRejectModal").modal('show');
    this.rejectDetails = details;
  }
  /**
   * Reject the selected submission 
   * @param formId 
   * @param submissionId 
   * @param rejectMessage 
   */
  rejectSubmission(formId, submissionId, rejectionMessage, form, isDelete) {
    if (formId) {
      let rejectDetails = {
        "formId": formId,
        "rejectionList": submissionId,
        "message": rejectionMessage,
        "isDelete": [isDelete]
      }
      this.httpClient.post(Constants.HOME_URL + 'api/rejectMultipleSubmission', rejectDetails).subscribe(data => {
        this.validationMsg = data;
        $("#confirmRejectModal").modal('hide');
        form.resetForm();
        $("#successMatch").modal('show');
      }, err => {
        console.log(err);
        form.resetForm();
        $("#confirmRejectModal").modal('hide');
      });
    }
  }
  /**
   * Set supervisor detailed submissions on each CF submission
   * @param data 
   */
  setSupervisorDetails(data) {
    this.httpClient.get(Constants.HOME_URL + 'api/getSubmissionData?submissionId=' + data.extraKeys.submissionId + '&formId=' + data.formId).subscribe(res => {
      let supervisorData: any = res;
      this.supervisorFormId = Object.keys(supervisorData.reviewDataMap)[0];
      if (supervisorData.reviewDataMap) {
        this.selectedSupervisorSubmissions = supervisorData.reviewDataMap[this.supervisorFormId];
        this.viewSupervisorDetails = true;
        if (this.selectedSupervisorSubmissions)
          $("#supervisorDetails").modal('show');
      } else {
        this.viewSupervisorDetails = true;
        $("#supervisorDetails").modal('show');
      }
    });
  }
  /**
   * Get supervisor detailed submissions on each CF submission
   */
  getSuperviorDetails() {
    return this.selectedSupervisorSubmissions;
  }
  /**
   * Download the report excel
   * @param data 
   */
  download(data) {
    if (data) {
      //data can be string of parameters or array/object
      data = typeof data == 'string' ? data : $.param(data);
      //split params into form inputs
      var inputs = '';
      let url = Constants.HOME_URL + 'downloadReport';
      $.each(data.split('&'), function () {
        var pair = this.split('=');
        inputs += '<input type="hidden" name="' + pair[0] + '" value="' + pair[1] + '" />';
      });
      //send request
      $('<form action="' + url + '" method="post">' + inputs + '</form>')
        .appendTo('body').submit().remove();
    };
  }
 
}
