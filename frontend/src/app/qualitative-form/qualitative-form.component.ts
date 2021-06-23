import { Component, OnInit } from '@angular/core';
import { ReportServiceService } from '../report/report-service.service';
import { NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Constants } from '../constants';
import { DomSanitizer } from '@angular/platform-browser';
declare var $: any;

@Component({
  selector: 'app-qualitative-form',
  templateUrl: './qualitative-form.component.html',
  styleUrls: ['./qualitative-form.component.scss']
})
export class QualitativeFormComponent implements OnInit {
  reportService: ReportServiceService;
  qualitativeQuestionDetails: any;
  qualitativeReports: any;
  validationMsg: any;
  tableColumns: any;
  tableData: any;
  srcFile: any;
  qualitativeReportsDate: string;
  qualitativeReportsAdd: any;
  userToken: any;
  qualitativeDDMReportDetails: any;
  ddmTableData: any
  ddmTableColumnData: any
  ddmviewTableData: any
  ddmviewTableColumnData: any
  allowedExtension: string = "pdf";
  allowedFileSize: number = 2097152;
  qualitativeDDMViewData: any;
  uploadFileName: string;
  uploadFileDetails: any = [];

  constructor(private reportServices: ReportServiceService, private router: Router, private http: HttpClient, private dom: DomSanitizer) {
    this.reportService = reportServices
  }

  ngOnInit() {
    if (localStorage.getItem('user_details')) {
      this.userToken = JSON.parse(localStorage.getItem('user_details'));
    }
    this.reportService.getQualitativeFormQuestions().subscribe(data => {
      this.qualitativeQuestionDetails = data;
    });
    this.getSupervisorReportsOnDDM();    // supervior report on ddm view
    this.getAllQualitativeReports();     // supervior report on supervisor view
  }
  /**
   * By tab change this method will call
   * @param e 
   */
  tabChanged(e) {
    if (e.index == 1) {
      this.viewDDMUploadedData();
    }

    if (e.index == 0) {
      this.getSupervisorReportsOnDDM();
    }

  }
  /**
   * Supervisor report details on supervisor view
   */
  getAllQualitativeReports() {
    this.reportService.getQualitativeFormData().subscribe(data => {
      this.qualitativeReports = data;
      let qualitativeData: any = Object.keys(this.qualitativeReports);
      this.qualitativeReportsDate = qualitativeData[0].split("@AND@");
      this.qualitativeReportsAdd = this.qualitativeReportsDate[1];
      //console.log(this.qualitativeReportsAdd);
      this.tableData = this.qualitativeReports[qualitativeData].tableData;
      this.tableColumns = this.qualitativeReports[qualitativeData].tableColumn;
    })
  }
  /**
   * Supervisor report details on ddm view
   */
  getSupervisorReportsOnDDM() {
    this.reportService.getQualitativeSupervisorDataOnDDM().subscribe(data => {
      this.qualitativeDDMReportDetails = data;
      let val: any = Object.keys(this.qualitativeDDMReportDetails);
      this.ddmTableColumnData = this.qualitativeDDMReportDetails[val].tableColumn;
      this.ddmTableData = this.qualitativeDDMReportDetails[val].tableData;
    })
  }
  /**
   * Show new qualitative form on modal view
   */
  addNewQualiytativeForm() {
    $("#qualitativeForm").modal('show');
  }
  /**
   * Add new qualitative form
   * @param form 
   */
  submitForm(form: NgForm) {
    this.http.post(Constants.HOME_URL + 'api/saveQualitativeData', this.qualitativeQuestionDetails).subscribe((data) => {
      this.validationMsg = data;
      $("#qualitativeForm").modal('hide');
      $("#successMatch").modal('show');
      form.resetForm();
      this.getAllQualitativeReports();
    }, err => {
      this.validationMsg = err.error.message;
      $("#qualitativeForm").modal('hide');
      $("#errorMatch").modal('show');
      form.resetForm();
    });
  }
  /**
   * View the report on PDF format
   * @param attachment 
   */
  viewFile(attachment) {
    this.srcFile = this.dom.bypassSecurityTrustResourceUrl(Constants.HOME_URL + 'api/doc?id=' + attachment.action.id);
    setTimeout(() => {
      $('#previewModal').modal('show');
    }, 200);
    //this.downloadPdf('id=' + attachment.action.id + '&access_token=' + localStorage.getItem("access_token"));
  }
  /**
   * Destroy the data of each selection on modal close
   */
  destroyModalData() {
    this.srcFile = "";
    $('#previewModal').modal('hide');
  }
  /**
   * Upload report for DDM on DDM view
   * @param val 
   */
  uploadFile(val) {
    this.uploadFileDetails = val.event.srcElement.files;
    let fileExtension = this.uploadFileDetails[0].name.split('.').pop();
    let fileSize = this.uploadFileDetails[0].size;
    const formdata: FormData = new FormData();
    formdata.append('file', this.uploadFileDetails[0]);
    if (this.allowedExtension === fileExtension && fileSize <= this.allowedFileSize) {
      this.http.post(Constants.HOME_URL + 'api/uploadQualitativeReportFile', formdata).subscribe((data) => {
        this.validationMsg = data;
        val.event.srcElement.value = null;
        $("#confirmUploadModal").modal('hide');
        $("#fileMatch").modal('show');
      }, err => {
        val.fileName = undefined;
        this.uploadFileDetails = undefined;
        val.event.srcElement.value = null;
        this.validationMsg = err.error.message;
        $("#fileMatch").modal('hide');
        $("#confirmUploadModal").modal('hide');
        $("#fileNotMatch").modal('show');
      });
    } else {
      val.fileName = undefined;
      this.uploadFileDetails = undefined;
      val.event.srcElement.value = null;
      this.validationMsg = 'Please upload pdf file of maximum size 2 MB';
      $("#confirmUploadModal").modal('hide');
      $("#fileNotMatch").modal('show');
    }
  }
  confirmToUploadFile(uploadFileVal) {
    if (uploadFileVal.event.target.files.length > 0) {
      this.uploadFileName = uploadFileVal;
      $("#confirmUploadModal").modal('show');
    }
  }
  clearField(uploadFileVal) {
    uploadFileVal.event.srcElement.value = null;
  }
  /**
   * this model
   */
  uploadSuccess() {
    $("#fileMatch").modal('hide');
    this.viewDDMUploadedData();
  }

  viewDDMUploadedData() {
    this.reportService.getQualitativeDDMDataView().subscribe(data => {
      this.qualitativeDDMViewData = data;
      let val: any = Object.keys(this.qualitativeDDMViewData);
      this.ddmviewTableColumnData = this.qualitativeDDMViewData[val].tableColumn;
      this.ddmviewTableData = this.qualitativeDDMViewData[val].tableData;
    });
  }
  /**
   * Download report 
   * @param data 
   */
  downloadPdf(data) {
    if (data) {
      //data can be string of parameters or array/object
      data = typeof data == 'string' ? data : $.param(data);
      //split params into form inputs
      var inputs = '';
      let url = Constants.HOME_URL + 'api/downloadQualitativeReport';
      $.each(data.split('&'), function () {
        var pair = this.split('=');
        inputs += '<input type="hidden" name="' + pair[0] + '" value="' + pair[1] + '" />';
      });
      //send request
      $('<form action="' + url + '" method="post">' + inputs + '</form>')
        .appendTo('body').submit().remove();
    };
  }
  /**
   * Updoad qualitative report
   * @param event 
   * @param form 
   */
  onFileChange(event, form: NgForm) {
    this.uploadFileDetails = event.srcElement.files[0];
    const formdata: FormData = new FormData();
    formdata.append('file', this.uploadFileDetails);
    if(((event.srcElement.files[0].name.split('.')[(event.srcElement.files[0].name.split('.') as string[]).length - 1] as String).toLocaleLowerCase() === 'pdf') ) {
    if (event.srcElement.files[0].size <= 2097152) {
      this.http.post(Constants.HOME_URL + 'api/saveQualitativeFile', formdata).subscribe((data) => {
        $("#successModal").modal('show');
        this.validationMsg = data;
        event.srcElement.value = null;
        this.uploadFileDetails = undefined;
        form.resetForm();
        this.getAllQualitativeReports();
      }, err => {
        if (err.status == 406)
        event.srcElement.value = null;
        $("#errModal").modal('show');
        this.validationMsg = err.error.message;
      });
    } else {
      $("#errModal").modal('show');
      this.validationMsg = "Maximum file size is 2MB";
      event.srcElement.value = null;
    }

  } else{
    $("#errModal").modal('show');
    this.validationMsg="Upload pdf format file only";
    event.srcElement.value = null;
   }
  }

  modalClose() {
    this.uploadFileDetails = null;
  }

}
