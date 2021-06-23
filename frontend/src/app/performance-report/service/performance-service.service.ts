import { Injectable } from '@angular/core';
import { Constants } from '../../constants';
import { HttpClient } from '@angular/common/http';
declare var $;

@Injectable({
  providedIn: 'root'
})
export class PerformanceServiceService {
  validationMsg: any;
  timeperiodLists: any;
  usertype:any;
  geographytype:any;
  rejection:any ;
  formType:any ;

  constructor(private httpClient: HttpClient) { }

   /**
   * Get all report forms
   */
  getAllReportForms() {
    return this.httpClient.get(Constants.HOME_URL + 'getReportForms');
  }
  /**
   * Get tabular data for submission report
   * @param form 
   * @param des 
   * @param start 
   * @param end 
   */
  submissionReport(form,des,start,end){
    // return this.httpClient.get('assets/json/advance-report.json');
    return this.httpClient.get(Constants.HOME_URL + 'report?formId='+form+'&designation='+des+'&startTp='+start+'&endTp='+end);
  }
  /**
   * Get timeperiod list 
   */
  getAllTimeperiods(){
    return this.httpClient.get(Constants.HOME_URL + 'getAllTimeperiods');
  }
  /**
   * Get user type (designations)
   */
  usertypeReport(){
    return this.httpClient.get('assets/json/user-type.json');
  }
 /**
  * Get hemocue geographic list
  */
  geographytypeReport(){
    return this.httpClient.get('assets/json/geography-type.json');
  }
  /**
   * Get hemocue report in tabular list
   * @param geographyType 
   * @param start 
   * @param end 
   */
  hemocueReport(geographyType,start,end){
    return this.httpClient.get(Constants.HOME_URL + 'hemocueReport?areaLevel='+geographyType+'&startTp='+start+'&endTp='+end);
  }
  /**
   * Get forms based on designation selection on reports
   */  
  formtypeReport(){
    return this.httpClient.get(Constants.HOME_URL + 'getDesignationForm');
  }
  /**
   * @author Pabitra
   * @param formId 
   * @param start 
   * @param end 
   * Get rejected report list in tabular form
   */
  rejectionReport(formId,start,end){
    return this.httpClient.get(Constants.HOME_URL + 'rejectionData?formId='+formId+'&startTp='+start+'&endTp='+end);
  }
  /**
   * Download submitted reports
   * @param formId 
   * @param designation 
   * @param startTp 
   * @param endTp 
   */
  downloadsubmissionReport(formId, designation, startTp,endTp){
    return this.httpClient.get(Constants.HOME_URL + 'submissionReport?formId='+formId+'&designation='+designation+'&startTp='+startTp+'&endTp='+endTp);
  }
 /**
  * Download rejected reports
  * @param formId 
  * @param startTp 
  * @param endTp 
  */
  downloadrejectionReport(formId, startTp, endTp ){
    return this.httpClient.get(Constants.HOME_URL + 'rejectionReport?formId='+formId+'&startTp='+startTp+'&endTp='+endTp);
  }
  /**
   * Download hemocue reports
   * @param areaLevel 
   * @param startTp 
   * @param endTp 
   */
  downloadhemocueReport(areaLevel, startTp, endTp ){
    return this.httpClient.get(Constants.HOME_URL + 'downloadHemocueReport?areaLevel='+areaLevel+'&startTp='+startTp+'&endTp='+endTp);
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
