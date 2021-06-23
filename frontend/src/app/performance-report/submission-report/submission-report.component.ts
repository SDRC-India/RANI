import { Component, OnInit } from '@angular/core';
import { PerformanceServiceService } from '../service/performance-service.service';
import { NgForm } from '@angular/forms';
declare var $: any;

@Component({
  selector: 'app-submission-report',
  templateUrl: './submission-report.component.html',
  styleUrls: ['./submission-report.component.scss']
})
export class SubmissionReportComponent implements OnInit {
  reportService: PerformanceServiceService;
  tableData: any;
  submissionReportNgModel:any = {};
  form: NgForm;
  type: string;
  validationMsg: any;


  constructor(private reportProvider: PerformanceServiceService) { 
    this.reportService = reportProvider;
  }

  ngOnInit() {
     this.reportService.getAllTimeperiods().subscribe(res => {
      this.reportService.timeperiodLists = res;      // get timeperiod list
    })

    this.reportService.formtypeReport().subscribe(formList =>{
      this.reportService.formType = formList;   // get all forms based on designation selection
    })

    this.reportService.usertypeReport().subscribe(userType =>{
      this.reportService.usertype = userType;   // get user designation list
    })
  }

  /**
   * @author Pabitra
   * get all submitted report list in tabular view
   */
  submitsubmissionReport(){
    this.reportService.submissionReport(this.submissionReportNgModel.formName,this.submissionReportNgModel.userType,this.submissionReportNgModel.from,this.submissionReportNgModel.to).subscribe(userlist=>{
      this.tableData=userlist;
    })
  }

  /**
   * @author Pabitra
   * download submitted report as excel format
   */
  downloadExcelreport(){
      this.reportService.downloadsubmissionReport(this.submissionReportNgModel.formName,this.submissionReportNgModel.userType,this.submissionReportNgModel.from,this.submissionReportNgModel.to).subscribe(response => {
        if (response['statusCode'] == 200) {
          let fileName = response['message'];
          this.reportService.download('fileName=' + fileName + '&access_token=' + localStorage.getItem("access_token"));
        } else if (response['statusCode'] == 204) {
          this.validationMsg = response['message'];
          $("#errModal").modal('show');        
        }
      }, err => {    
        this.validationMsg = "No Data Found"
        $("#errModal").modal('show');
      })
  }

}
