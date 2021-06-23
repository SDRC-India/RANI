import { Injectable } from '@angular/core';
import { Constants } from 'src/app/constants';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  coverageDashboard: any = {};
  snapshotView: any = {};
  allcheckLists: any;
  checkLists:any;
  timeperiodLists: any;
  checkListDetails: any;  
  formFieldsAll: any;
  areaDetails: any;
  areaLevels: any;
  areaLevelID: number[] = [];
  selectedStateName: string;
  selectedDistrictName: string;
  selectedBlockName: string;
  selectedVillageName: string;
  selectedClusterName: string;
  areaLevelName: string;
  selectedTimePeriodName: string;
  checkListObj:any;
  sectionObj: any;
  clusterAreaDetails : any;
  clusters: any;
  isMapLoading: boolean;
  performanceDashboard:any;
  videoLists: any;
  // isEqual: any;
  
  constructor(private httpClient: HttpClient) { }
 /**
  * Get all indicators data for program dashboard
  * @param levelId 
  * @param areaId 
  * @param sectorName 
  * @param timperiodId 
  * @param formId 
  */
  getIndicatorData(levelId, areaId, sectorName, timperiodId,formId) {
    return this.httpClient.get(Constants.HOME_URL + 'getDashboardData?areaLevel=' + levelId + '&areaId=' + areaId + '&sectorName=' +sectorName + '&tpId='+timperiodId + '&formId=' +formId+ '&dashboardType=program');
  }
  /**
   * Get all checklists
   */
  getCheckListData() {
    return this.httpClient.get(Constants.HOME_URL + 'getAllChecklistSectors');
  }
  /**
   * Get all areaLevels
   */
  getUserRoles() {
    return this.httpClient.get("assets/dashboard-json/allAreaLevels.json");
  }
  /**
   * Get all time periods
   */
  getAllTimeperiods(){
    return this.httpClient.get(Constants.HOME_URL + 'getAllTimeperiods');
  }
  /**
   * Get all areas
   */
  getAreaDetails() {
    return this.httpClient.get(Constants.HOME_URL + 'getAllArea');
  }
  /**
   * Get all cluster related areas
   */
  getAllClusterAreas(){
    return this.httpClient.get(Constants.HOME_URL + 'getAllClusterArea');
  }
  /**
   * Get all thematic view data
   * @param indicatorId 
   * @param tpId 
   * @param areaLevel 
   * @param areaId 
   * @param sectorName 
   */
  getThemeData(indicatorId, tpId, areaLevel, areaId, sectorName):any{
    return this.httpClient.get(Constants.HOME_URL + 'getThematicViewData?indicatorId='+indicatorId+'&tpId='+tpId+'&areaLevel='+areaLevel+'&areaId='+areaId+'&sectorName='+sectorName)
    //return this.httpClient.get("assets/dashboard-json/thematicData.json");
  }
  /**
   * Get linechart data on click in thematic map
   * @param indicatorId 
   * @param tpId 
   * @param areaId 
   */
  getLineChartData(indicatorId, tpId, areaId):any{
      //return this.httpClient.get('assets/dashboard-json/linechartData.json');
      return this.httpClient.get(Constants.HOME_URL + 'getLineChartData?indicatorId='+indicatorId+'&tp='+tpId +'&areaId='+areaId)
  }
  /**
   * Get linechart data on click in performance dashboard
   * @param formId 
   * @param performanceId 
   */
  getSubmissionLineChartData(formId,performanceId):any{
     //return this.httpClient.get('assets/dashboard-json/linechartData.json');
      return this.httpClient.get(Constants.HOME_URL + 'getPerformanceTrend?formId='+formId+'&performanceType='+performanceId);
  }
  /**
   * Send all review details
   */
  sendReview(data): Observable<any> {
    return this.httpClient.post(Constants.HOME_URL + 'sendReview', data)
  }
  /**
   * Get form sector mapping data
   */
  getFormSectorMappingData(){
    return this.httpClient.get(Constants.HOME_URL + 'getFormSectorMappingData');
  }
  /**
   * Get indicators
   * @param formId 
   */
  getIndicatorsData(formId){
    return this.httpClient.get(Constants.HOME_URL + 'getIndicators?formId='+formId);
  }
  /**
   * Get data based on selecton in performance dashboard
   * @param dashboardType 
   */
  performancedashboard(dashboardType){
    if(dashboardType==1){ 
      return this.httpClient.get(Constants.HOME_URL + 'getPlannedData');
     
    }else{
      return this.httpClient.get(Constants.HOME_URL + 'getOntimeData');
    }
  }
  /**
   * Export data in PDF format
   * @param data 
   */
  exportData(data,fileType){
    if(fileType == 'pdf'){
      return this.httpClient.post(Constants.HOME_URL + 'thematicViewDownloadPDF', data, {responseType: "blob"})
    }else if(fileType == 'excel'){
      return this.httpClient.post(Constants.HOME_URL + 'thematicViewDownloadExcel', data, {responseType: "blob"})
    }
  }
  /**
   * Get form type in performance dashboard
   */
  performanceDashtype(){
    return this.httpClient.get('assets/json/performance-dashboard-type.json');
  }
  /**
   * Get video list on media selection in program dashboard
   */
  getMediaVideoList(){
    return this.httpClient.get('assets/dashboard-json/videoList.json');
  }

  /*** Download full page as pdf*/
  downloadFullpageSvg(listOfSvgs,areaLevelname,stateName, districtName, blockName,villageName,type, checkListName, timePeriod) {
    return this.httpClient.post(Constants.HOME_URL + 
      'downloadChartDataPDF?districtName=' + (districtName?districtName:null)
       + '&blockName=' + (blockName?blockName:null) + '&stateName='+(stateName?stateName:null)+ '&villageName='+(villageName?villageName:null)
       + '&areaLevel=' +(areaLevelname?areaLevelname:null ) + '&dashboardType=' +type + '&checkListName=' +checkListName
       + '&timePeriod=' +timePeriod
       , (listOfSvgs?listOfSvgs:null), {
      responseType: "blob"
    });
  }

  /*** Download full page as excel*/
  downloadFullpageExcel(listOfSvgs) {
    return this.httpClient.post(Constants.HOME_URL + 'downloadChartDataExcel',
    (listOfSvgs?listOfSvgs:null), {responseType: "blob"});
  }

  isEqual(value, other) {

    // Get the value type
    const type = Object.prototype.toString.call(value);

    // If the two objects are not the same type, return false
    if (type !== Object.prototype.toString.call(other)){ return false; }

    // If items are not an object or array, return false
    if (['[object Array]', '[object Object]'].indexOf(type) < 0) { return false;}

    // Compare the length of the length of the two items
    const valueLen = type === '[object Array]' ? value.length : Object.keys(value).length;
    let otherLen = type === '[object Array]' ? other.length : Object.keys(other).length;
    if (valueLen !== otherLen) return false;

    // Compare two items
    var compare = (item1, item2) => {

      // Get the object type
      var itemType = Object.prototype.toString.call(item1);

      // If an object or array, compare recursively
      if (['[object Array]', '[object Object]'].indexOf(itemType) >= 0) {
        if (!this.isEqual(item1, item2)) return false;
      }

      // Otherwise, do a simple comparison
      else {

        // If the two items are not the same type, return false
        if (itemType !== Object.prototype.toString.call(item2)) return false;

        // Else if it's a function, convert to a string and compare
        // Otherwise, just compare
        if (itemType === '[object Function]') {
          if (item1.toString() !== item2.toString()) return false;
        } else {
          if (item1 !== item2) return false;
        }

      }
    };

        // Compare properties
        if (type === '[object Array]') {
          for (var i = 0; i < valueLen; i++) {
            if (compare(value[i], other[i]) === false) return false;
          }
        } else {
          for (var key in value) {
            if (value.hasOwnProperty(key)) {
              if (compare(value[key], other[key]) === false){ return false;};
            }
          }
        }
    
        // If nothing failed, return true
        return true;
      }

     
}



