import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatInputModule, MatIconModule, MatFormFieldModule, MatSelectModule, MatCheckboxModule,MatDatepickerModule, MatTabsModule, MatBadgeModule, MatTooltipModule } from '@angular/material';
import { ReactiveFormsModule,FormsModule } from '@angular/forms'; 
import { MyDatePickerModule } from 'mydatepicker';
import { TableModule } from 'review-lib/public_api'

import { ReportRoutingModule } from './report-routing.module';
import { RawDataReportComponent } from './raw-data-report/raw-data-report.component';
import { ReviewSubmissionsComponent } from './review-submissions/review-submissions.component';
import { RemoveArrayPipe } from '../rootFilters/remove-array.pipe';
import { TableDataFilterPipe } from './filters/table-data-filter.pipe';
import { SubmissionDetailsComponent } from './submission-details/submission-details.component';
import { SupervivorDetailsComponent } from './supervivor-details/supervivor-details.component';

@NgModule({
  declarations: [RawDataReportComponent, ReviewSubmissionsComponent,RemoveArrayPipe, TableDataFilterPipe, SubmissionDetailsComponent, SupervivorDetailsComponent],
  imports: [
    CommonModule,
    FormsModule,
    MyDatePickerModule,
    ReactiveFormsModule,
    ReportRoutingModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatDatepickerModule,
    MatIconModule,
    MatTabsModule,
    MatBadgeModule,
    MatTooltipModule,
    TableModule
  ]
})
export class ReportModule { }
