import { Component, OnInit } from '@angular/core';
import { PlanningServiceService } from '../services/planning-service.service';
import { NgForm } from '@angular/forms';
declare var $: any;

@Component({
  selector: 'app-planning-report',
  templateUrl: './planning-report.component.html',
  styleUrls: ['./planning-report.component.scss']
})
export class PlanningReportComponent implements OnInit {
  planningService: PlanningServiceService;
  reportVariables: any = {};
  planningReportNgModel: any = {};
  validationMsg: any;
  form: NgForm;
  type: string;

  constructor(private planningProvider: PlanningServiceService) {
    this.planningService = planningProvider;
  }

  ngOnInit() {
    this.planningService.getPlanningRoles().subscribe(response => {
      let roleList = response;  /** Get all user roles */
      this.reportVariables.userRoles = roleList;
    })
    this.planningService.getPlanningReportTimePeriod().subscribe(res => {
      this.reportVariables.timePeriod = res;  /** Get all time periods */
      console.log(res);
      this.reportVariables.timePeriodKeys = Object.keys(this.reportVariables.timePeriod);
    })
  }
  showLists() {
    $(".left-list").attr("style", "display: block !important");
    $('.mob-left-list').attr("style", "display: none !important");
  }
  /**
   * Get years on month selection
   * @param selectedMnth 
   */
  yearSelection(selectedMnth) {
    this.reportVariables.timePeriodLists = this.reportVariables.timePeriod[selectedMnth];
  }
  /**
   * Download planned report as EXcel
   */
  submitForm(timeperiodId, roleId, form: NgForm ) {
    let fileType = this.type;
    if (timeperiodId && roleId) {
      this.planningService.downloadPlanningReport(timeperiodId, roleId, fileType).subscribe(response => {
        if (response['statusCode'] == 200) {
          let fileName = response['message'];
          this.planningService.download('fileName=' + fileName + '&access_token=' + localStorage.getItem("access_token"));
          this.type=null;
        } else if (response['statusCode'] == 204) {
          this.validationMsg = response['message'];
          $("#errModal").modal('show');
          this.type=null;
        }
        form.resetForm();
      }, err => {
        form.resetForm();
        this.validationMsg = "No Data Found"
        $("#errModal").modal('show');
        this.type =null;
      })
    }
  }
   /**
   * Download planned report as PDF
   */
  downloadPdf(timeperiodId,roleId,type) {
    if (timeperiodId && roleId) 
     this.type = type;    
  }
  ngAfterViewInit() {
    $('body,html').click(function (e) {
      if ((window.innerWidth) <= 767) {
        if (e.target.className == "mob-left-list") {
          return;
        } else {
          $(".left-list").attr("style", "display: none !important");
          $('.mob-left-list').attr("style", "display: block !important");
        }
      }
    });
  }
}
