export class Constants {
    public static get HOME_URL(): string { return "/rani/"; };
    public static get CMS_URL(): string { return "cms/"; };
    public static get CONTACT_URL(): string { return "contact/"; };

    public static defaultImage:string;
    public static get ERROR_AREAMSG():string{return 'Please Select at least one area.'};
    public static get ERROR_TEXTAREA():string{return 'Please provide review.'};
    public static get STATE_CODE(): string {return "IND020"; }
    public static get DISCLAMER():string{return 'The numerator used for the indicator is number value of HMIS monthly report and denominator used is proposed target of each district and block estimated by NHM, Jharkhand '};

    static message: IMessages = { 
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
        finalizedSuccess: 'Finalized Successfully.',
        fillAtleastOnField: 'Please fill data of atleast one field',
        autoSave: 'Auto save Successfully',
        anganwadiCenter: 'Please select the anganwadi center number.',
        schoolname: 'Please enter the school name.',
        respondentName: 'Please enter the respondent name.',
        womanName: 'Please enter the woman name.',
        errorWhileClearingFile: 'Error while deleting data of previous user.',
        clearingDataPleaseWait: 'Clearing data, please wait...'
      }
}