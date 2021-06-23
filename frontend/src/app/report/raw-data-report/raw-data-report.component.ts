import { Component, OnInit } from '@angular/core';
import { FormGroup, NgForm, FormBuilder, FormControl } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { ReportServiceService } from '../report-service.service';
import { Constants } from 'src/app/constants';
import { HttpClient } from '@angular/common/http';
declare var $;

@Component({
  selector: 'app-raw-data-report',
  templateUrl: './raw-data-report.component.html',
  styleUrls: ['./raw-data-report.component.scss']
})
export class RawDataReportComponent implements OnInit {
  /**
  * Declare the date validation
  */
  minDate = new Date(1990, 0, 1);
  maxDate = new Date(Number(this.datepipe.transform(new Date(), "yyyy-MM-dd").split('-')[0]), Number(this.datepipe.transform(new Date(), "yyyy-MM-dd").split('-')[1]) - 1, Number(this.datepipe.transform(new Date(), "yyyy-MM-dd").split('-')[2]));

  tominDate = new Date(2017, 0, 1);
  tomaxDate = new Date(Number(this.datepipe.transform(new Date(), "yyyy-MM-dd").split('-')[0]), Number(this.datepipe.transform(new Date(), "yyyy-MM-dd").split('-')[1]) - 1, Number(this.datepipe.transform(new Date(), "yyyy-MM-dd").split('-')[2]));;

  formGrp: FormGroup;
  reportService: ReportServiceService;
  selectedReportId: number;
  selectedFromDate: any;
  selectedToDate: any;
  validationMsg: any;
  infoMsg: string="Reports are being downloaded based on submission date.";

  constructor(private datepipe: DatePipe, private reportServices: ReportServiceService, private http: HttpClient, private fb: FormBuilder) {
    this.reportService = reportServices
    
    /** Declare the form field names of type FormGroup */
    this.formGrp = new FormGroup({
      report: new FormControl(),
      toDate: new FormControl(),
      fromDate:  new FormControl()
    });
  }
  ngOnInit() {
    this.reportService.getAllReportForms().subscribe(data => {
      this.reportService.reportForms = data;  /** Get all report list on dropdown */
    })
  } 
/**
 * Download the report after validation sucess
 * @param formId 
 * @param fromModel 
 * @param toModel 
 * @param f 
 */
  getAllRawData(formId, fromModel, toModel, f:NgForm) {
    if ((formId !== "" || formId !== undefined) && (fromModel !== "" || fromModel !== undefined) && (toModel !== "" || toModel !== undefined)) {
      var fromDate = this.convertStringToDate(fromModel);
      var toDate = this.convertStringToDate(toModel);
   
    if (fromDate && toDate) {
      this.http.get(Constants.HOME_URL + 'exportRawData?formId=' + formId + '&startDate=' + fromDate + '&endDate=' + toDate).subscribe((response) => {
        if(response['statusCode'] == 200){
        let fileName = response['message'];      
        this.reportService.download('fileName=' + fileName + '&access_token=' + localStorage.getItem("access_token"));
        }else if(response['statusCode'] == 204){
          this.validationMsg = response['message'];
          $("#oldPassNotMatch").modal('show');
        }
        f.resetForm();
      }, err => {
        console.log(err);     
        f.resetForm();  
        this.validationMsg = "No Data Found"
        $("#oldPassNotMatch").modal('show');
      });
    }
  }
  }
  /**
   * Convert date to 'yyyy-mm-dd' format
   * @param dates 
   */
  convertStringToDate(dates) {
    if(dates){
    var date = new Date(dates),
      mnth = ("0" + (date.getMonth() + 1)).slice(-2),
      day = ("0" + date.getDate()).slice(-2);
    return [date.getFullYear(), mnth, day].join("-");
    } 
  }
  /**
   * Change the to date on change of from date
   * @param e 
   */
  changeToDate(e){
    this.selectedToDate="";
  }
}
