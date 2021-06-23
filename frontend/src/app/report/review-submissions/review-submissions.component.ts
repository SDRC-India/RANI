import { Component, OnInit, Input } from '@angular/core';
import { ReportServiceService } from '../report-service.service';
import { FormGroup, NgForm } from '@angular/forms';
import { Router, RoutesRecognized } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Constants } from '../../constants';
import { TableDataFilterPipe } from '../filters/table-data-filter.pipe';
declare var $;

@Component({
  selector: 'app-review-submissions',
  templateUrl: './review-submissions.component.html',
  styleUrls: ['./review-submissions.component.scss']
})
export class ReviewSubmissionsComponent implements OnInit {
  reportService: ReportServiceService;
  errMessage: string;
  tableColumns: any;
  tableData: any;
  selectedFormId: number;
  remarksforReject: any;
  submmittedTab: number;
  isRejectedTab: boolean = false;
  ispageLoad: boolean = true;

  constructor(private reportServices: ReportServiceService, private router: Router, private http: HttpClient) {
    this.reportService = reportServices
  }

  ngOnInit() {         
    this.reportService.getAllReviewForms().subscribe(data => {
      this.reportService.reviewDetails = data;
      this.selectedFormId = data[0].formId;  
      if(this.ispageLoad)
      this.getAllSubmissions(this.selectedFormId, NgForm)
    })
  }
  /**
   * Based on tab select calls the submissions
   * @param e 
  */
  formTabChanged(e) {       
    for (let i = 0; i < this.reportService.reviewDetails.length; i++) {
      if(e.index === i){
        this.selectedFormId = this.reportService.reviewDetails[i].formId;    
      }      
    }
    if(!this.ispageLoad)
    this.getAllSubmissions(this.selectedFormId, NgForm)
    this.reportService.destroyModalData();
  }
  /**
   * Based on tab select calls the submissions
   * @param e 
  */
  tabChanged(e) {
    if (e.index == 1)
      this.isRejectedTab = true;
    if (e.index == 0)
      this.isRejectedTab = false;
    this.getAllSubmissions(this.selectedFormId, NgForm)
  }
  /**
   * Lists all the submissions 
   * @param formId 
   * @param f 
  */
  getAllSubmissions(formId, f = NgForm) {
    this.reportService.getAllSubmissionDatas(this.selectedFormId).subscribe(res => {
      this.reportService.reviewDetails.allSubmissions = res;      
      this.ispageLoad = false;  
      let tempTableData = this.reportService.reviewDetails.allSubmissions;
      let dynamicTableheightData: any;
      if (tempTableData.length > 0) {
        for (let obj of tempTableData) {
          let val: any = [];
          Object.keys(obj.formDataHead).sort().forEach(key => {
            val[key] = obj.formDataHead[key];
          })
          let allKeys = Object.keys(val);
          for (let i = 0; i < allKeys.length; i++) {
            if ((this.isRejectedTab)) {
              let columnNames = allKeys[i].split("_");
              if(columnNames[1] == "Date of submisson")
              columnNames[1] = "Date of rejection";
              obj[columnNames[1]] = obj.formDataHead[allKeys[i]];
            } else if (!this.isRejectedTab) {
              let columnName = allKeys[i].split("_");
              obj[columnName[1]] = obj.formDataHead[allKeys[i]];
            }
          }
        }
        this.tableData = tempTableData;
        if (this.isRejectedTab == true)
          dynamicTableheightData = new TableDataFilterPipe().transform(tempTableData, true);
        else if (this.isRejectedTab == false)
          dynamicTableheightData = new TableDataFilterPipe().transform(tempTableData, false);
      } else {
        this.tableData = "";
      }
      this.errMessage = "";
      if (this.tableData)
        this.tableColumns = Object.keys(this.tableData[0]);

      /** add page height based on table data shown */  
      if (this.tableData) {
        if (dynamicTableheightData.length > 6 && dynamicTableheightData.length <=8) {
          $('.btnTabs').addClass('submiitedTabs10');
        } else if (dynamicTableheightData.length > 8) {
          $('.btnTabs').addClass('submiitedTabs15');
          $('.btnTabs').removeClass('submiitedTabs10');
        } else {
          $('.btnTabs').removeClass('submiitedTabs10');
          $('.btnTabs').removeClass('submiitedTabs15');
        }
      }
    }, err => {
      this.errMessage = err.error.message;
    });
  }
  rejectSuccess() {
    $("#successMatch").modal('hide');
    this.getAllSubmissions(this.selectedFormId,NgForm)
  }
  downloadTableAsExcel(submissions) {
    let submissionIds: any = [];
    if (submissions.tableData) {
      submissions.tableData.forEach(element => {
        submissionIds.push(element.extraKeys.submissionId);
      });
    }
    this.http.get(Constants.HOME_URL + 'exportReviewDataReport?formId=' + this.selectedFormId + '&submissionIds=' + submissionIds).subscribe((response) => {
      if (response['statusCode'] == 200) {
        let fileName = response['message'];
        this.reportService.download('fileName=' + fileName + '&access_token=' + localStorage.getItem("access_token"));
      } else if (response['statusCode'] == 204) {
        this.reportService.validationMsg = response['message'];
      }
    }, err => {
      console.log(err);
    });
  }
  // getPaginatedData(val){
  //   this.getAllSubmissions(this.selectedFormId, NgForm)
  // }
  deleteSubmissions(){
    $("#confirmRejectModal").modal('hide');
    $("#deleteSubmissions").modal('show');
  }
}
