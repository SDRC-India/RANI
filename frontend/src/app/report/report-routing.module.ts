import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { RoleGuardGuard } from '../guard/role-guard.guard';
import { RawDataReportComponent } from './raw-data-report/raw-data-report.component';
import { ReviewSubmissionsComponent } from './review-submissions/review-submissions.component';
import { SubmissionDetailsComponent } from './submission-details/submission-details.component';

const routes: Routes = [
  { 
    path: 'raw-data', 
    pathMatch: 'full', 
    component: RawDataReportComponent,
    canActivate: [RoleGuardGuard],
    data: { 
      expectedRoles: ["DOWNLOAD_RAWDATA_REPORT"]
    }
  },
  { 
    path: 'submission-management', 
    pathMatch: 'full', 
    component: ReviewSubmissionsComponent,
    canActivate: [RoleGuardGuard],
    data: { 
      expectedRoles: ["REVIEW-REJECT","SUBMISSION_MANAGEMENT"]
    },   
  },
  { 
    path: 'submission-details', 
    pathMatch: 'full', 
    component: SubmissionDetailsComponent,
    canActivate: [RoleGuardGuard],
    data: { 
      expectedRoles: ["REVIEW-REJECT"]
    }, 
  }  
]

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ReportRoutingModule { }
