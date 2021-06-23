import { Component, OnInit} from '@angular/core';
import { FormGroup, NgForm } from '@angular/forms';
import { HttpClient } from '@angular/common/http/';
import { UserManagementService } from '../services/user-management.service';
import { Constants } from '../../constants';
import { Router, RoutesRecognized } from '@angular/router';
import { filter, pairwise } from 'rxjs/operators';

declare var $: any;

@Component({
  selector: 'app-edit-user-details',
  templateUrl: './edit-user-details.component.html',
  styleUrls: ['./edit-user-details.component.scss']
})
export class EditUserDetailsComponent implements OnInit{
  form: FormGroup;
  formFields: any;
  formFieldsAll: any;
  payLoad = '';
  areaDetails: any;  

  newPassword: string;
  confirmPassword: string;
  userId: any;
  validationMsg: any;
  user: any;
  disableUserId: number;
  errMessage: string;
  allChecked?: boolean;
  areaSearch: string;
  userManagementService: UserManagementService;
  tableColumns: any;

  constructor(private http: HttpClient, private userManagementProvider: UserManagementService, private router: Router) {
    this.userManagementService = userManagementProvider;
   }

  ngOnInit() {
    this.router.events
    .pipe(filter((e: any) => e instanceof RoutesRecognized))
    .subscribe((e: any) => {
        //console.log(e); 
        // route to previous url
        if(this.router.url =="/edit-user-details" &&e.url != '/update-user' ){
          this.userManagementService.resetPasswordDetails ={};
        }
    });
    
    if(!this.userManagementService.formFieldsAll)
      this.userManagementService.getUserRoles().subscribe(data=>{
        this.userManagementService.formFieldsAll = data;    /** Get user roles */
    }) 
    if(!this.userManagementService.areaDetails)   
      this.userManagementService.getAreaDetails().subscribe(data=>{
        this.userManagementService.areaDetails = data;    /** Get all areas */         
    })     
    if(!this.userManagementService.areaLevelDetails) 
      this.userManagementService.getAreaLevels().subscribe(data=>{
        this.userManagementService.areaLevelDetails = data;    /** Get area levels*/
    })            
    if((window.innerWidth)<= 767){
      $(".left-list").attr("style", "display: none !important"); 
      $('.mob-left-list').attr("style", "display: block !important");
    }
    this.getUsers();   
  }
/**
 * Get all the user listed on page load
 */
 getUsers(){
    let areaId:any =  this.userManagementService.resetPasswordDetails.selectedVillageId ? this.userManagementService.resetPasswordDetails.selectedVillageId: this.userManagementService.resetPasswordDetails.selectedBlockId ? this.userManagementService.resetPasswordDetails.selectedBlockId: this.userManagementService.resetPasswordDetails.selectedDistrictId ? this.userManagementService.resetPasswordDetails.selectedDistrictId: null;
    let roleId = this.userManagementService.resetPasswordDetails.selectedRoleId;
    if(!areaId)
      areaId=null;
    if(!roleId)
      roleId=null;
    if(!this.userManagementService.resetPasswordDetails.userName)
    this.userManagementService.resetPasswordDetails.userName=null;

    this.userManagementService.getUsersByRoleId(roleId, this.userManagementService.resetPasswordDetails.userName).subscribe(res => {
      this.userManagementService.resetPasswordDetails.allUser  = res;
      this.tableColumns = Object.keys(this.userManagementService.resetPasswordDetails.allUser[0]);
      this.errMessage="";
    }, err=>{
      this.errMessage = err.error.message;
    });   
 }
 resetModal(user){
  $("#resetPassModal").modal('show');
  this.user = user;
 }
 resetBox(user){
  this.newPassword = "";
  this.confirmPassword = "";
 }
 /**
  * Reset password for selected user
  * @param form 
  */
 submitModal(form:NgForm){   
  let passDetails = {
    'userId' : this.user['tableRow'].userId,
    'newPassword': this.newPassword
  };

 if(this.newPassword === this.confirmPassword) {
    this.http.post(Constants.HOME_URL + 'resetPassword', passDetails).subscribe((data)=>{  
        $("#resetPassModal").modal('hide');
        $("#successMatch").modal('show');
        this.newPassword = "";
        this.confirmPassword = "";
        form.resetForm();
    }, err=>{
      $("#oldPassNotMatch").modal('show');
      this.validationMsg ="Error occurred";
      form.resetForm();
    });
  }
}
/**
 * Navigate to update-user with details of user clicked
 * @param data 
 */
editUserDetails(data){
  this.userManagementService.editUserDetails = data;  
  this.router.navigateByUrl("update-user");
}
/**
 * Enable the selected user
 * @param id 
 */
enableUser(id){
  this.http.get(Constants.HOME_URL + 'enableUser?userId='+id).subscribe((data)=>{
    $("#enableUser").modal('show'); 
    this.validationMsg = data;
  }, err=>{      
  }); 
}
disableUser(id){
  this.disableUserId = id;
  $("#disableUserModal").modal('show');
}
/**
 * Disable the selected user
 * @param id 
 */
disableUserDetails(id){
  this.http.get(Constants.HOME_URL +'disableUser?userId='+id).subscribe((data)=>{   
   $("#disableUserModal").modal('hide');
   $("#enableUser").modal('show'); 
     this.validationMsg = data;        
   }, err=>{      
     console.log(err);       
     $("#disableUserModal").modal('hide');     
   }); 
}
userStatus(){
  $("#enableUser").modal('hide'); 
  this.getUsers();
}
showLists(){    
  $(".left-list").attr("style", "display: block !important"); 
  $('.mob-left-list').attr("style", "display: none !important");
}
ngAfterViewInit(){
    $("input, textarea, .select-dropdown").focus(function() {
      $(this).closest(".input-holder").parent().find("> label").css({"color": "#4285F4"})
      
    })
    $("input, textarea, .select-dropdown").blur(function(){
      $(this).closest(".input-holder").parent().find("> label").css({"color": "#333"})
    })
    $('body,html').click(function(e){    
      if((window.innerWidth)<= 767){
      if(e.target.className == "mob-left-list"){
        return;
      } else{ 
          $(".left-list").attr("style", "display: none !important"); 
          $('.mob-left-list').attr("style", "display: block !important");  
      }
     }
    });   
 }
}
