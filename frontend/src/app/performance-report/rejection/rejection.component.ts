import { Component, OnInit } from '@angular/core';
import { PerformanceServiceService } from '../service/performance-service.service';
declare var $: any;

@Component({
  selector: 'app-rejection',
  templateUrl: './rejection.component.html',
  styleUrls: ['./rejection.component.scss']
})
export class RejectionComponent implements OnInit {
  reportService: PerformanceServiceService;
  tableData: any;
  rejectionReportNgModel:any = {};
  timeperiodlist:any;
  validationMsg: any;

  constructor(private reportProvider: PerformanceServiceService) { 
    this.reportService = reportProvider;
  }

  ngOnInit() {
    this.reportService.getAllTimeperiods().subscribe(res => {
      this.reportService.timeperiodLists = res;      // get timeperiod list
    })

    this.reportService.formtypeReport().subscribe(community =>{
      this.reportService.formType = community;      // get all forms
    })
  }
/**
 * @author Pabitra
 * get all rejected report list in tabular view
 */
  submitrejectionReport(){
    this.reportService.rejectionReport(this.rejectionReportNgModel.rejection,this.rejectionReportNgModel.from, this.rejectionReportNgModel.to ).subscribe(userlist=>{
      this.tableData=userlist;
    })
  }

  /**
   * @author Pabitra
   * download rejection report as excel format
   */
  downloadExcelreport(){
    this.reportService.downloadrejectionReport(this.rejectionReportNgModel.rejection,this.rejectionReportNgModel.from,this.rejectionReportNgModel.to).subscribe(response => {
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
