import { Injectable } from '@angular/core';

/**
 * This provider will help to keep all the constants
 * @author Jagat Bandhu
 * @since 0.0.1
 */
@Injectable()
export class ConstantProvider {
    static baseUrl : string = 'https://testserver.sdrc.co.in:8443/rani/';  

    static defaultImage: string ='assets/imgs/uploadImage.png';
  static message: IMessages = { 
    formUpdationSuccess: 'Form Updated Successfully',
    formUpdationNotFound: 'No forms to update',
    formUpdating: 'Checking for updates, Please wait... ',
    checkInternetConnection: "Please check your internet connection.",
    serverError:"Error connecting to server ! Please try after some time.",
    networkError: 'Server error.',
    pleaseWait: 'Please wait..', 
    validUserName: 'Please enter username.',
    validPassword:'Please enter Password.',
    dataClearMsg:'Last user saved data will be erased. Are you sure you want to login?',
    invalidUser:'No data entry facility available for state and national level user.',
    invalidUserNameOrPassword:'Invalid usename or password.',
    syncingPleaseWait: 'Syncing please wait...',
    syncSuccessfull: 'Sync Successful.',
    getForm: 'Fetching forms from server, please wait...',
    warning: 'Warning',
    deleteFrom: 'Do you want to delete the selected record?',
    saveSuccess: 'Saved Successfully.',
    finalizedSuccess: 'Finalized Successfully. Item has been moved to finalized',
    fillAtleastOnField: 'Please fill data of atleast one field',
    autoSave: 'Auto save successful',
    anganwadiCenter: 'Please select the anganwadi center number.',
    schoolname: 'Please enter the school name.',
    respondentName: 'Please enter the respondent name.',
    womanName: 'Please enter the woman name.',
    errorWhileClearingFile: 'Error while deleting data of previous user.',
    clearingDataPleaseWait: 'Clearing data, please wait...',
    commonFinalizeErrorMsg: 'Mandatory for finalizing the form',
    commonSaveErrorMsg:'Mandatory for saving the form',
    movetoSave: 'Do you want to move the selected form to \'Finalized\' forms?'
  }
  static dbKeyNames: IDBKeyNames = {
    // user: "user",
    form: "form",
    // getAllForm: "getAllForm",
    // getBlankForm: "getBlankForm",
    submissionData:"submissionData",
    dataToSend:'dataToSend',
    // loginResponse: 'loginResponse'
    userAndForm : 'userAndForm',
    displayId:'displayId',
    userId: 'userId'
    
  }

  // anganwadiSection: AnganwadiSection = {
  //   section1: "Details of the Anganwadi workers",
  //   section2: "Reach of the center",
  //   section3: "Enrollment particulars",
  //   section4: "Facilities/ services available at the centre",
  //   section5: "Registers maintained at the centre  ",
  //   section6: "Services",
  //   section7: "Availability of services",
  //   section8: "Preschool Details",
  // }
  static lastUpdatedDate: string = "2019-07-03 12:08:14";
  static appFolderName: string ='Rani';
}
