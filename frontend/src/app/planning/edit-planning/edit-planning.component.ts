import { Component, OnInit } from '@angular/core';
import { PlanningServiceService } from '../services/planning-service.service';
import { NgForm } from '@angular/forms';
declare var $: any;

@Component({
  selector: 'app-edit-planning',
  templateUrl: './edit-planning.component.html',
  styleUrls: ['./edit-planning.component.scss']
})
export class EditPlanningComponent implements OnInit {
  planningService: PlanningServiceService;
  selectionsValue: any ={}
  editPlanningSelections: any={};
  editPlanning: any ={};
  validationMsg: any;

  constructor(private planninProvider: PlanningServiceService) { 
    this.planningService = planninProvider;
  }

  ngOnInit() {
    this.planningService.getEditPlanningTimePeriod().subscribe(data => {
      let timeList = data;  /** Get all timeperiod list */
      this.editPlanningSelections.timePeriodList = timeList;
    })   
    this.planningService.getPlanningRoles().subscribe(response=>{
      let roleList = response;  /** Get all user roles */
      this.editPlanningSelections.userRoles = roleList;
    })
    this.planningService.getEditPlanUserList().subscribe(userlist=>{
      let userList = userlist;  /** Get all users based on role selection */
      this.editPlanningSelections.userList = userList;
    })
  }
  showLists() {
    $(".left-list").attr("style", "display: block !important");
    $('.mob-left-list').attr("style", "display: none !important");
  }
  /**
   * Show table data on selection
   * @param date 
   * @param roleId 
   * @param accId 
   */
  editForm(date, roleId, accId){
    this.planningService.getEditPlanningData(date, roleId, accId).subscribe(data => {    
      let allTableData = data;
      let val:any = Object.keys(allTableData);
      this.editPlanning.tableColumn = allTableData[val].tableColumn;
      this.editPlanning.tableData = allTableData[val].tableData;
    })  
  }
  /**
   * Edit the target value
   * @param val 
   */
  editTarget(val){
    this.editPlanning.formName = val;
    let ngVal= this.editPlanning.formName.target;
    this.editPlanning.ngmodelValue = ngVal;
    $("#editPlanning").modal('show');
  }
  /**
   * Submit the edited target value on submit
   * @param form 
   * @param updatedValue 
   * @param id 
   */
  updatePlan(form:NgForm,updatedValue, id){
    let updatedDetails ={
      planId: id,
      targetValue: updatedValue
    }
    if(updatedValue<=50000 && updatedValue>=0){
    this.planningService.uploadPlanningTarget(updatedDetails).subscribe(data=>{
      console.log(data);
      $("#successModal").modal('show');
      this.validationMsg = data;
      $("#editPlanning").modal('hide');
    })
   }else{
     this.editPlanning.ngmodelValue = updatedValue;
   }
  }
  /**
   * Update the table with new target value
   * @param date 
   * @param roleId 
   * @param accId 
   */
  updateTableValue(date, roleId, accId){
    this.editForm(date, roleId, accId);
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
