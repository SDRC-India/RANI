import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { UserManagementRoutingModule } from './user-management-routing.module';
import { MatInputModule, MatIconModule, MatFormFieldModule, MatSelectModule, MatCheckboxModule } from '@angular/material';
import { ReactiveFormsModule,FormsModule } from '@angular/forms'; 

import { UserManagementComponent } from './user-management/user-management.component';
import { UserSideMenuComponent } from './user-side-menu/user-side-menu.component';
import { AreaFilterPipe } from './filters/area-filter.pipe';
import { UserManagementService } from './services/user-management.service';
import { EditUserDetailsComponent } from './edit-user-details/edit-user-details.component';
import { FormModule } from 'sdrc-form';
import { UpdateUserDetailsComponent } from './update-user-details/update-user-details.component';
import { DropDownSearchrPipe } from './filters/dropdown-search.pipe';
import { ChangePasswordComponent } from './change-password/change-password.component';
import { TableModule } from 'lib/public_api';
import { RemoveArrayPipe } from './filters/remove-array.pipe';

@NgModule({
  imports: [
    CommonModule,    
    FormsModule,
    UserManagementRoutingModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    FormModule,
    TableModule
  ],
  declarations: [
    UserManagementComponent,
    EditUserDetailsComponent,
    UserSideMenuComponent,
    AreaFilterPipe,
    RemoveArrayPipe,
    UpdateUserDetailsComponent,
    DropDownSearchrPipe,
    ChangePasswordComponent
  ],
  providers:[UserManagementService]
})
export class UserManagementModule { }
