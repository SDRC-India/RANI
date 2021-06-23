import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { RoleGuardGuard } from '../guard/role-guard.guard';
import { PlanningTemplateComponent } from './planning-template/planning-template.component';
import { PlanningReportComponent } from './planning-report/planning-report.component';
import { EditPlanningComponent } from './edit-planning/edit-planning.component';

const routes: Routes = [
  {
    path: 'planning', 
    pathMatch: 'full', 
    component: PlanningTemplateComponent,
    canActivate: [RoleGuardGuard],
    data: { 
      expectedRoles: ["PLANNING_MODULE"]
    }
  },
  {
    path: 'planning-report', 
    pathMatch: 'full', 
    component: PlanningReportComponent,
    canActivate: [RoleGuardGuard],
    data: { 
      expectedRoles: ["PLANNING_MODULE","PLANNING_REPORT"]
    }  
  },
  {
    path: 'edit-planning', 
    pathMatch: 'full', 
    component: EditPlanningComponent,
    canActivate: [RoleGuardGuard],
    data: { 
      expectedRoles: ["PLANNING_MODULE"]
    }  
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PlanningRoutingModule { }
