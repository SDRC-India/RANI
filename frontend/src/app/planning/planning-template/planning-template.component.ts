import { Component, OnInit } from '@angular/core';
import { PlanningServiceService } from '../services/planning-service.service';
import { NgForm } from '@angular/forms';
declare var $: any;

@Component({
  selector: 'app-planning-template',
  templateUrl: './planning-template.component.html',
  styleUrls: ['./planning-template.component.scss']
})
export class PlanningTemplateComponent implements OnInit {
 planningSelection: any = {};
 selectionList: any = {};
 planningService: PlanningServiceService;
 uploadFileDetails: any;
 validationMsg: any;
 infoMsg: string="Planning of next quarter can be done between 20th till last date of last month of current quarter.";
 form: NgForm;

 constructor(private planningProvider: PlanningServiceService) { 
    this.planningService = planningProvider;
  }

  ngOnInit() {
    this.planningService.getTimeperiodList().subscribe(data => {
      let timeList = data;  /** Get all timeperiod list */
      this.selectionList.timePeriodKey =  Object.keys(timeList)[0];
      console.log(this.selectionList.timePeriodKey);
      if(this.selectionList.timePeriodKey == 'true')
      this.selectionList.timePeriodList = timeList[this.selectionList.timePeriodKey];
    })   
    this.planningService.getPlanningRoles().subscribe(response=>{
      let roleList = response;  /** Get all user roles */
      this.selectionList.userRoles = roleList;
    })
  }
  showLists() {
    $(".left-list").attr("style", "display: block !important");
    $('.mob-left-list').attr("style", "display: none !important");
  } 
  downloadPlanningTemplate(timeperiod, roles, form: NgForm){
    if(timeperiod && roles){
      this.planningService.downloadPlanningTemplate(timeperiod, roles).subscribe(response=>{
      let fileName = response;  
      this.planningService.download('fileName=' + fileName + '&access_token=' + localStorage.getItem("access_token"));
      form.resetForm();
     },err=>{
      form.resetForm();
     })
    } else{
      $("#errModal").modal('show');
      if(this.selectionList.timePeriodKey == 'true')
      this.validationMsg="Please select Time Period and User Role";
      else
      this.validationMsg='You have crossed deadline date to download this file'
    }
  } 
  /**
   * Upload file on button click
   */
  uploadClicked(){
    this.uploadFileDetails = null;
    $('#fileUpload').click();
  }
  onFileChange(event, form: NgForm){   
    if(this.planningSelection.timePeriod && this.planningSelection.role){
    this.uploadFileDetails = event.srcElement.files[0];
    const formdata: FormData = new FormData();
    formdata.append('file', this.uploadFileDetails);  
    if (((event.srcElement.files[0].name.split('.')[(event.srcElement.files[0].name.split('.') as string[]).length - 1] as String).toLocaleLowerCase() === 'xls') ) {
    this.planningService.uploadPlanningTemplate(formdata,this.planningSelection.timePeriod,this.planningSelection.role).subscribe(response=>{     
      $("#successModal").modal('show');
      this.validationMsg = response;
      event.srcElement.value = null;
      form.resetForm();
    }, err=>{
      if(err.status == 406)    
      event.srcElement.value = null;
      $("#errModal").modal('show');
      this.validationMsg = err.error.message;
    });
   }else{
    $("#errModal").modal('show');
    this.validationMsg="Upload xls format file only";
   }
  }else{
    $("#errModal").modal('show');
    if(this.selectionList.timePeriodKey == 'true'){
      this.validationMsg="Please select Time Period and User Role";
      event.srcElement.value = null;
    }
    else{
      this.validationMsg='You have crossed deadline date to upload this file';
      event.srcElement.value = null;
    }    
  }
 }
 modalClose(){   
  this.uploadFileDetails = null;
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
