import { Injectable } from '@angular/core';
import { Constants } from '../../constants';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class UserManagementService {

  formFieldsAll: any;
  areaLevelDetails: any;
  areaDetails: any;  
  allTypes: any;
  editUserDetails: any;
  resetPasswordDetails: any={}; 
  roleWiseFields: IRoleWiseFields[];
  ifaSelectedIds: any=[];

  constructor(private httpClient: HttpClient) { }

  /** Get all user roles */
  getUserRoles(){
    return this.httpClient.get(Constants.HOME_URL + 'getAllDesignations');   
  }

  /** Get all areas */
  getAreaDetails(){
    return this.httpClient.get(Constants.HOME_URL + 'getAllArea');   
  } 

  /** Get all area level based on which area will display */
  getAreaLevels(){
    return this.httpClient.get(Constants.HOME_URL + 'getAreaLevel');   
  }

  /**
   * Get all user details
   * @param roleId 
   * @param username 
   */
  getUsersByRoleId(roleId, username){
    return this.httpClient.get(Constants.HOME_URL + 'getUsers?roleId='+roleId+'&userName='+username)
  }

  /**
   * Get fields by role selection  
   */
  getFieldsByRoleSelect(){
    //return this.httpClient.get('assets/json/dummytabledata2.json');   
    return this.httpClient.get(Constants.HOME_URL + 'getIFAData');
  }

  /**
   * Get parent area details depends on child areaId
   * @param areaDetails 
   * @param areaDetailId 
   */
  getElementByAreaId(areaDetails, areaDetailId){
    let areaJson: any=[];
    for (let i = 0; i < Object.keys(areaDetails).length; i++) {
      if(Object.keys(areaDetails)[i] != 'allChecked'){
      const key = Object.keys(areaDetails)[i];
      areaDetails[key].forEach(element => {
        if(areaDetailId.indexOf(element.areaId) !=-1){
          areaJson.push(element.parentAreaId);
        }
      });
     }
    }
    return areaJson;
  }
  /**
   * Uncheck the select box onselection of dropdown
   * @param field 
   */
  checkBoxValueChange(field){
    field.allChecked = false;
  } 
}
