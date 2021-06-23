import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { PlanningRoutingModule } from './planning-routing.module';
import { ReactiveFormsModule,FormsModule } from '@angular/forms'; 
import { MatInputModule, MatFormFieldModule, MatSelectModule, MatCardModule, MatTooltipModule } from '@angular/material';
import { TableModule } from 'qualitative-table/public_api';

import { PlanningTemplateComponent } from './planning-template/planning-template.component';
import { PlanningSideMenuComponent } from './planning-side-menu/planning-side-menu.component';
import { PlanningReportComponent } from './planning-report/planning-report.component';
import { EditPlanningComponent } from './edit-planning/edit-planning.component';
import { RoleWiseUserPipe } from './filters/role-wise-user.pipe';

@NgModule({
  declarations: [PlanningTemplateComponent, PlanningSideMenuComponent, PlanningReportComponent, EditPlanningComponent, RoleWiseUserPipe],
  imports: [
    CommonModule,
    TableModule,
    PlanningRoutingModule,
    ReactiveFormsModule,FormsModule,
    MatInputModule, MatFormFieldModule, MatSelectModule, MatCardModule, MatTooltipModule
  ]
})
export class PlanningModule { }
