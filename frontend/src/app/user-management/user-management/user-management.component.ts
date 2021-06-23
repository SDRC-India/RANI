import { Component, OnInit } from '@angular/core';
import { FormGroup, NgForm, FormControl } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Constants } from '../../constants';
import { UserManagementService } from '../services/user-management.service';
import { AreaFilterPipe } from '../filters/area-filter.pipe';
declare var $: any;

@Component({
  selector: 'app-user-management',
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.scss']
})
export class UserManagementComponent implements OnInit {
  formFields: any;
  sdrcForm: FormGroup;

  validationMsg: any;
  selectedRoleId: number;
  selectedAreaLevelId: number;
  selectedDistrictId: number;
  selectedBlockId: any;
  selectedVillageId: any;

  fullName: string;
  userName: string;
  password: string;
  mobile: string;
  allChecked?: boolean = false;
  areaSearch: any;
  userManagementService: UserManagementService;
  questionMapByColumnName: any;
  F1: any;
  F2: any;
  F3: any;
  F4: any;
  F5: any;
  F6: any;
  F7: any;
  F8: any;

  constructor(private http: HttpClient, private userManagementProvider: UserManagementService) {
    this.userManagementService = userManagementProvider;
  }

  ngOnInit() {
    if (!this.userManagementService.formFieldsAll)
      this.userManagementService.getUserRoles().subscribe(data => {
        this.userManagementService.formFieldsAll = data;  /** Get user roles */
      })
    if (!this.userManagementService.areaDetails)
      this.userManagementService.getAreaDetails().subscribe(data => {
        this.userManagementService.areaDetails = data;  /** Get all areas */
      })
    if (!this.userManagementService.areaLevelDetails)
      this.userManagementService.getAreaLevels().subscribe(data => {
        this.userManagementService.areaLevelDetails = data;  /** Get area levels*/
      })

    if ((window.innerWidth) <= 767) {
      $(".left-list").attr("style", "display: none !important");
      $('.mob-left-list').attr("style", "display: block !important");
    }
  }
  /**
   * Create new user on submit
   * @param roleId 
   * @param areaLevelId 
   * @param form 
   */
  submitForm(roleId: any, areaLevelId: number, form: NgForm) {
    let areaId: any;
    let mobileNo: string ;
    if (areaLevelId == 3) {
      areaId = [this.selectedDistrictId];
    }
    if (areaLevelId == 4) {
      areaId = this.selectedBlockId;
    }
    if (areaLevelId == 5) {
      areaId = this.selectedVillageId;
    }
    if(this.mobile){
      mobileNo = this.mobile.toString();
    }
    let userDetails = {
      "userName": this.userName,
      "password": this.password,
      "designationIds": [roleId],
      "mbl": mobileNo,
      "areaId": areaId,
      "name": this.fullName,
      "F1": this.F1,
      "F2": this.F2,
      "F3": this.F3,
      "F4": this.F4,
      "F5": this.F5,
      "F6": this.F6,
      "F7": this.F7,
      "F8": this.F8
    }
    this.http.post(Constants.HOME_URL + 'createUser', userDetails).subscribe((data) => {
      this.validationMsg = data;
      $("#successMatch").modal('show');
      form.resetForm();
      this.F1 = "";
      this.F2 = "";
      this.F3 = "";
      this.F4 = "";
      this.F5 = "";
      this.F6 = "";
      this.F7 = "";
      this.F8 = "";
    }, err => {
      $("#oldPassNotMatch").modal('show');
      if (err.status == 409) {
        this.validationMsg = err.error.message;
      }
      else
        this.validationMsg = "Some server error occured"
    });
  }
  successModal() {
    $("#successMatch").modal('hide');
  }
  showLists() {
    $(".left-list").attr("style", "display: block !important");
    $('.mob-left-list').attr("style", "display: none !important");
  }

  ngAfterViewInit() {
    $("input, textarea, .select-dropdown").focus(function () {
      $(this).closest(".input-holder").parent().find("> label").css({ "color": "#4285F4" })

    })
    $("input, textarea, .select-dropdown").blur(function () {
      $(this).closest(".input-holder").parent().find("> label").css({ "color": "#333" })
    })
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
  /**
   * Check and uccheck all check boxes on select all click
   * @param field 
   * @param fieldName 
   */
  checkUncheckAllSelection(field) {
    field.allChecked = !field.allChecked;
    let totalAreas = new AreaFilterPipe().transform(field, 4, this.selectedBlockId);
    if (field.allChecked) {
      let allOptionKeys = [];
      totalAreas.forEach(opt => {
        if (allOptionKeys.indexOf(opt) == -1) {
          allOptionKeys.push(opt.areaId)
        }
      });
      this.selectedVillageId = allOptionKeys;
    }
    else {
      this.selectedVillageId = [];
    }
  }
  /**
   * Uncheck and check select all check box based on all check box selection
   * @param field 
   * @param fieldName 
   */
  validateAllOptionSelected(field) {
    let allOptionKeys;
    let totalAreas = new AreaFilterPipe().transform(field, 4, this.selectedBlockId);
    allOptionKeys = JSON.parse(JSON.stringify(this.selectedVillageId));
    if (allOptionKeys.length == totalAreas.length) {
      return true
    }
    if (allOptionKeys.length < totalAreas.length) {
      return false
    }
  }
  /**
   * Get searched villages on dropdown search
   * @param area 
   * @param searchText 
   */
  searchText(area, searchText){
    if(searchText)
    return JSON.stringify(area).toLowerCase().includes(searchText);   
    else
    return JSON.stringify(area).toLowerCase();   
  }
  /**
   * get dependent option fields by role selection
   */
  getFieldsByRole() {
    //console.log(this.selectedRoleId);
    this.userManagementService.getFieldsByRoleSelect().subscribe(data => {
      let totalFields = data
      this.userManagementService.roleWiseFields = totalFields[this.selectedRoleId];
    })
  }
  /**
   * checkDependency and display the fields
   * @param field 
  */
  checkIsDependencyCondition(field) {
    let arrayOfFieldIds: any = field.value;
    let dependentValue: string;

    if (field.colName == "F5" || field.colName == "F1") {
      for (let i = 0; i < this.userManagementService.roleWiseFields.length; i++) {
        if (this.userManagementService.roleWiseFields[i].isDependencyOption) {
          dependentValue = this.userManagementService.roleWiseFields[i].isDependencyOption.split(":")[2];
          for (let index = 0; index < arrayOfFieldIds.length; index++) {
            const element = arrayOfFieldIds[index];
            if (dependentValue === element) {
              this.userManagementService.roleWiseFields[i].isDependency = false;
            } else {
              let flag = true;
              for (let arrayIndex = 0; arrayIndex < arrayOfFieldIds.length; arrayIndex++) {
                const dependentElement = arrayOfFieldIds[arrayIndex];
                if (dependentValue === dependentElement) {
                  flag = false;
                }
              }
              if (flag) {
                this.userManagementService.roleWiseFields[i].isDependency = true;
                this.userManagementService.roleWiseFields[i].value = '';
                this.checkSelectCondition(this.userManagementService.roleWiseFields[i].colName, this.userManagementService.roleWiseFields[i].value);
              }
            }
          }
          if (arrayOfFieldIds.length == 0) {
            this.userManagementService.roleWiseFields[i].isDependency = true;
            this.userManagementService.roleWiseFields[i].value = '';
            this.checkSelectCondition(this.userManagementService.roleWiseFields[i].colName, this.userManagementService.roleWiseFields[i].value);
          }
        }
      }
    }
    this.checkSelectCondition(field.colName, field.value);
  }
  checkSelectCondition(colName, colValue) {
    if (colName == 'F1') {
      this.F1 = colValue
    }
    if (colName == 'F2') {
      this.F2 = colValue;
    }
    if (colName == 'F3') {
      this.F3 = colValue
    }
    if (colName == 'F4') {
      this.F4 = colValue
    }
    if (colName == 'F5') {
      this.F5 = colValue
    }
    if (colName == 'F6') {
      this.F6 = colValue
    }
    if (colName == 'F7') {
      this.F7 = colValue
    }
    if (colName == 'F8') {
      this.F8 = colValue
    }
  }
}
