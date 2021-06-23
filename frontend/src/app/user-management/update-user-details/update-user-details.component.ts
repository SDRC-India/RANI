import { Component, OnInit } from '@angular/core';
import { FormGroup, NgModel } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Constants } from '../../constants';
import { UserManagementService } from '../services/user-management.service';
import { Router, RoutesRecognized } from '@angular/router';
import { filter, pairwise } from 'rxjs/operators';
import { AreaFilterPipe } from '../filters/area-filter.pipe';
declare var $: any;

@Component({
  selector: 'app-update-user-details',
  templateUrl: './update-user-details.component.html',
  styleUrls: ['./update-user-details.component.scss']
})

export class UpdateUserDetailsComponent implements OnInit {
  form: FormGroup;
  formFields: any;
  sdrcForm: FormGroup;
  selectRole: NgModel;
  validationMsg: any;
  UserForm: FormGroup;
  selectedDistrictId: number;
  selectedBlockId: any;
  selectedVillageId: any;
  selectedRoleId: string;
  userDetails: IUserDetails;
  allChecked?: boolean;
  areaSearch: any;
  F1: any;
  F2: any;
  F3: any;
  F4: any;
  F5: any;
  F6: any;
  F7: any;
  F8: any;

  constructor(private http: HttpClient, public userManagementService: UserManagementService, private router: Router) {}

  ngOnInit() {
    this.router.events
      .pipe(filter((e: any) => e instanceof RoutesRecognized)
      ).subscribe((e: any) => {
        if (this.router.url == "/update-user" && e.url != '/edit-user-details') {
          this.userManagementService.resetPasswordDetails = {};
        }
      });
    if (!this.userManagementService.editUserDetails) {
      this.router.navigateByUrl("edit-user-details");
    }
    this.userDetails = this.userManagementService.editUserDetails;

    console.log(this.userDetails);
    if (this.userManagementService.editUserDetails && this.userManagementService.editUserDetails.areaLevelId == 3) {
      this.selectedRoleId = this.userManagementService.editUserDetails.roleId[0] as string;
      this.selectedDistrictId = this.userManagementService.editUserDetails.areaId[0];
    }
    if (this.userManagementService.editUserDetails && this.userManagementService.editUserDetails.areaLevelId == 4) {
      this.selectedRoleId = this.userManagementService.editUserDetails.roleId[0] as string;
      this.selectedBlockId = this.userManagementService.editUserDetails.areaId;
      this.selectedDistrictId = (this.userManagementService.getElementByAreaId(this.userManagementService.areaDetails, this.selectedBlockId)[0]);
    }
    if (this.userManagementService.editUserDetails && this.userManagementService.editUserDetails.areaLevelId == 5) {
      this.selectedRoleId = this.userManagementService.editUserDetails.roleId[0] as string;
      this.selectedVillageId = this.userManagementService.editUserDetails.areaId;
      this.selectedBlockId = (this.userManagementService.getElementByAreaId(this.userManagementService.areaDetails, this.selectedVillageId));
      this.selectedDistrictId = (this.userManagementService.getElementByAreaId(this.userManagementService.areaDetails, this.selectedBlockId)[0]);
    }

    if ((window.innerWidth) <= 767) {
      $(".left-list").attr("style", "display: none !important");
      $('.mob-left-list').attr("style", "display: block !important");
    }

    this.setDefaultValues();
  }

  /**
   * Set default values to user fields
   * @author sasmita
   * @since 2.0.0
   */
  async setDefaultValues(){
    await this.getFieldsByRole()
    this.setSelectOptionValue()
  }

  /**
  * get dependent option fields by role selection
  * @author sasmita
  * @since 1.0.0
  */
 async getFieldsByRole() {
  this.selectedRoleId = this.userManagementService.editUserDetails.roleId[0] as string;
  let data = await this.userManagementService.getFieldsByRoleSelect().toPromise()
  this.userManagementService.roleWiseFields = data[this.selectedRoleId];
}

  /**
   * Set values tp option of dropdown
   * @author sasmita
   * @since 2.0.0
   */
  setSelectOptionValue(){
    /** Setting value of IFA supply point access */    
    if(this.userManagementService.roleWiseFields.findIndex(d=>d.colName === 'F5') > -1){
      let value: string[] = this.userDetails.f5
      this.userManagementService.roleWiseFields[this.userManagementService.roleWiseFields.findIndex(d=>d.colName === 'F5')].value = value
      this.checkIsDependencyCondition(this.userManagementService.roleWiseFields[this.userManagementService.roleWiseFields.findIndex(d=>d.colName === 'F5')])
    }
   if(this.userManagementService.roleWiseFields.findIndex(d=>d.colName === 'F1') > -1){
      let cfValue: string[] = this.userDetails.f1
      this.userManagementService.roleWiseFields[this.userManagementService.roleWiseFields.findIndex(d=>d.colName === 'F1')].value = cfValue
      this.checkIsDependencyCondition(this.userManagementService.roleWiseFields[this.userManagementService.roleWiseFields.findIndex(d=>d.colName === 'F1')])
   }
  }
  /**
   * Update the user details
   * @param roleId 
   * @param areaLevelId 
   */
  updateUserDetails(roleId: any, areaLevelId: number) {
    let areaId: any;
    if (areaLevelId == 3) {
      areaId = [this.selectedDistrictId];
    }
    if (areaLevelId == 4) {
      areaId = this.selectedBlockId;
    }
    if (areaLevelId == 5) {
      areaId = this.selectedVillageId;
    }
    let mobileNo: string = this.userManagementService.editUserDetails.mobileNumber?this.userManagementService.editUserDetails.mobileNumber.toString():'';
    let userDetails = {
      "id": this.userManagementService.editUserDetails.userId,
      "name": this.userManagementService.editUserDetails.name,
      "mbl": mobileNo,
      "areaId": areaId,
      "designationIds": this.userManagementService.editUserDetails.roleId,
      "F1": this.userDetails.f1,
      "F2": this.userDetails.f2,
      "F3": this.userDetails.f3,
      "F4": this.userDetails.f4,
      "F5": this.userDetails.f5,
      "F6": this.userDetails.f6,
      "F7": this.userDetails.f7,
      "F8": this.userDetails.f8
    }
    this.http.post(Constants.HOME_URL + 'updateUser', userDetails).subscribe((data) => {
      this.validationMsg = data;
      $("#successMatch").modal('show');
    }, err => {
      $("#oldPassNotMatch").modal('show');
      this.validationMsg = err.error.message;
    });
  }
  successModal() {
    $("#successMatch").modal('hide');
    this.router.navigateByUrl("edit-user-details");
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
  searchText(area, searchText) {
    if (searchText)
      return JSON.stringify(area).toLowerCase().includes(searchText);
    else
      return JSON.stringify(area).toLowerCase();
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

              //Setting the value in child drop downs
              switch(this.userManagementService.roleWiseFields[i].colName){
                case 'F2':
                  this.userManagementService.roleWiseFields[i].value = this.userDetails.f2
                  break
                case 'F3':
                    this.userManagementService.roleWiseFields[i].value = this.userDetails.f3
                    break
                case 'F4':
                  this.userManagementService.roleWiseFields[i].value = this.userDetails.f4
                  break
                case 'F6':
                    this.userManagementService.roleWiseFields[i].value = this.userDetails.f6
                    break
                case 'F7':
                    this.userManagementService.roleWiseFields[i].value = this.userDetails.f7
                    break
                case 'F8':
                    this.userManagementService.roleWiseFields[i].value = this.userDetails.f8
                    break              
              }
              // if(this.userManagementService.roleWiseFields[i].colName === 'F6'){
              //   this.userManagementService.roleWiseFields[i].value = this.userDetails.f6
              // }
              
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
      this.userDetails.f1 = colValue
    }
    if (colName == 'F2') {
      this.userDetails.f2 = colValue;
    }
    if (colName == 'F3') {
      this.userDetails.f3 = colValue
    }
    if (colName == 'F4') {
      this.userDetails.f4 = colValue
    }
    if (colName == 'F5') {
      this.userDetails.f5 = colValue
    }
    if (colName == 'F6') {
      this.userDetails.f6 = colValue
    }
    if (colName == 'F7') {
      this.userDetails.f7 = colValue
    }
    if (colName == 'F8') {
      this.userDetails.f8 = colValue
    }
  }
}

