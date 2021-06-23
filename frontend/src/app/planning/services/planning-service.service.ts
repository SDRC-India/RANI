import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Constants } from '../../constants';
declare var $: any;

@Injectable({
  providedIn: 'root'
})
export class PlanningServiceService {

  constructor(private httpClient: HttpClient) { }

 /**
 * Get time period list for download planning template
 */
  getTimeperiodList(){
    //return this.httpClient.get('assets/json/timeperiod.json');   
    return this.httpClient.get(Constants.HOME_URL +'getPlanningTimePeriod');   
  }
  /**
  * Get user role list for planning template/edit template
  */
  getPlanningRoles(){
    return this.httpClient.get(Constants.HOME_URL + 'getPlanningDesignations');
  }  
  /**
   * Download planning template for edit
   */
  downloadPlanningTemplate(timeperiod,roles){
    return this.httpClient.get(Constants.HOME_URL + 'downloadPlanningTemplate?date='+ timeperiod+'&roleId='+roles,{ responseType: 'text' });
  }
  /**
   * Upload planning template for edit
   */
  uploadPlanningTemplate(fileModal,date, roleId){
    return this.httpClient.post(Constants.HOME_URL + 'uploadPlanningTemplate?date='+date+'&roleId='+roleId,fileModal);
  }
  /**
   * Get time period list for edit planning template
   */
  getEditPlanningTimePeriod(){
    return this.httpClient.get(Constants.HOME_URL +'getManagePlanningTimePeriod');   
  }
  /**
   * Get user list based on role selection for edit planning template
   */
  getEditPlanUserList(){
    return this.httpClient.get(Constants.HOME_URL +'getManageUsers');  
  }
  /**
   *  Get all forms planning data in table format
   */
  getEditPlanningData(date, roleId, accId){
    //return this.httpClient.get('assets/json/editPlanning.json');   
    return this.httpClient.get(Constants.HOME_URL +'managePlanning?roleId='+roleId+'&date='+date+'&accId='+accId);   
  }
  /**
   * Edit the target value
   * @param uploadModel 
   */
  uploadPlanningTarget(uploadModel){
    return this.httpClient.post(Constants.HOME_URL +'updatePlan', uploadModel);   
  }
  /**
   * Get time period list for planning report
   */
  getPlanningReportTimePeriod(){
    return this.httpClient.get(Constants.HOME_URL +'getPlanningReportTimePeriod');   
  }
  /**
   * Download planning report in excl format
   */
  downloadPlanningReport(timePeriodId,roles, fileType){
    if(fileType == 'pdf')
    return this.httpClient.get(Constants.HOME_URL + 'getPlanningReportPDF?roleId='+ roles+'&timePeriodId='+timePeriodId);
    else
    return this.httpClient.get(Constants.HOME_URL + 'getPlanningReport?roleId='+ roles+'&timePeriodId='+timePeriodId);
  }
  /**
   * Download planning template/report 
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
