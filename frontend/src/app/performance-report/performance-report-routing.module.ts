import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { HemocueComponent } from './hemocue/hemocue.component';
import { RoleGuardGuard } from '../guard/role-guard.guard';
import { SubmissionReportComponent } from './submission-report/submission-report.component';
import { RejectionComponent } from './rejection/rejection.component';

const routes: Routes = [
  { 
    path: 'hemocue', 
    pathMatch: 'full', 
    component: HemocueComponent,
    canActivate: [RoleGuardGuard],
    data: { 
      expectedRoles: ["DOWNLOAD_RAWDATA_REPORT"]
    }    
  },
  { 
    path: 'rejection', 
    pathMatch: 'full', 
    component: RejectionComponent,
    canActivate: [RoleGuardGuard],
    data: { 
      expectedRoles: ["REJECTION_REPORT"]
    }    
  },
  { 
    path: 'submission-report', 
    pathMatch: 'full', 
    component: SubmissionReportComponent,
    canActivate: [RoleGuardGuard],
    data: { 
      expectedRoles: ["SUBMISSION_REPORT"]
    }    
  }
]

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PerformanceReportRoutingModule { }
