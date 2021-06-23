import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatInputModule, MatIconModule, MatFormFieldModule, MatSelectModule, MatCheckboxModule,MatDatepickerModule, MatTabsModule, MatBadgeModule, MatTooltipModule } from '@angular/material';
import { ReactiveFormsModule,FormsModule } from '@angular/forms'; 
import { MyDatePickerModule } from 'mydatepicker';
import { TableModule } from 'reportTable-lib/public_api'

import { HemocueComponent } from './hemocue/hemocue.component';
import { RejectionComponent } from './rejection/rejection.component';
import { SubmissionReportComponent } from './submission-report/submission-report.component';
import { TimeperiodSelectionPipe } from './filters/timeperiod-selection.pipe';
import { TimePeriodsortPipe } from './filters/time-periodsort.pipe';
import { FormOnDesginationPipe } from './filters/form-on-desgination.pipe';

import { PerformanceReportRoutingModule } from './performance-report-routing.module';

@NgModule({
  declarations: [HemocueComponent, RejectionComponent, SubmissionReportComponent, TimeperiodSelectionPipe, TimePeriodsortPipe, FormOnDesginationPipe],
  imports: [
    CommonModule,
    PerformanceReportRoutingModule,
    FormsModule,
    MyDatePickerModule,
    ReactiveFormsModule,
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
export class PerformanceReportModule { }
