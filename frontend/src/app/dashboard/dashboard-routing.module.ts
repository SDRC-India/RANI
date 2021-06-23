import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { RoleGuardGuard } from '../guard/role-guard.guard';
import { SnapshotViewComponent } from './snapshot-view/snapshot-view.component';
import { ThematicViewComponent } from './thematic-view/thematic-view.component';
import { SubmissionDashboardComponent } from './submission-dashboard/submission-dashboard.component';

const routes: Routes = [ 
  {
    path:"program-dashboard",
    component: SnapshotViewComponent,
    pathMatch: "full",
    canActivate: [RoleGuardGuard],
    data: { 
      expectedRoles: ["DASHBOARD"]
    }
  },
  {
    path:"thematic-view",
    component: ThematicViewComponent,
    pathMatch: "full",
    canActivate: [RoleGuardGuard],
    data: { 
      expectedRoles: ["DASHBOARD"]
    }
  },
  {
    path:"performance-dashboard",
    component: SubmissionDashboardComponent,
    pathMatch: "full",
    canActivate: [RoleGuardGuard],
    data: { 
      expectedRoles: ["PERFORMANCE_DASHBOARD"]
    }
  }               
];
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DashboardRoutingModule { }
