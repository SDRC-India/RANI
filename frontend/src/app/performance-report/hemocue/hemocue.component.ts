import { Component, OnInit } from '@angular/core';
import { PerformanceServiceService } from '../service/performance-service.service';
declare var $: any;

@Component({
  selector: 'app-hemocue',
  templateUrl: './hemocue.component.html',
  styleUrls: ['./hemocue.component.scss']
})
export class HemocueComponent implements OnInit {
  reportService: PerformanceServiceService;
  tableData: any;
  hemocueReportNgModel:any = {};
  validationMsg:any;

  constructor(private reportProvider: PerformanceServiceService) { 
    this.reportService = reportProvider;
  }

  ngOnInit() {
    this.reportService.getAllTimeperiods().subscribe(res => {
      this.reportService.timeperiodLists = res;     // get timeperiod list      
    })

    this.reportService.geographytypeReport().subscribe(geography=>{
      this.reportService.geographytype = geography;    // get geography list
    })
  }

  /**
   * @author Pabitra
   * get all hemocue report list in tabular view
   */
  submithemocueReport(){
    this.reportService.hemocueReport(this.hemocueReportNgModel.geographytype,this.hemocueReportNgModel.from,this.hemocueReportNgModel.to).subscribe(userlist=>{
      this.tableData=userlist;
    })
  }

  /**
   * @author Pabitra
   * download hemocue report as excel format
   */
  downloadExcelreport(){
    this.reportService.downloadhemocueReport(this.hemocueReportNgModel.geographytype,this.hemocueReportNgModel.from,this.hemocueReportNgModel.to).subscribe(response => {
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
