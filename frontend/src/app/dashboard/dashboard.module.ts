import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DashboardRoutingModule } from './dashboard-routing.module';
import { DashboardService } from './services/dashboard.service';
import { MatCardModule, MatListModule, MatNativeDateModule, MatDatepickerModule, MatRadioModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatTabsModule, MatCheckboxModule, MatSortModule, MatButtonModule, MatIconModule, MatTooltipModule, MAT_SELECT_SCROLL_STRATEGY  } from '@angular/material';
import { FormsModule } from '@angular/forms';
import { AreaFilterPipe } from './filters/area-filter.pipe';
import { BarChartComponent } from './bar-chart/bar-chart.component';
import { PieChartComponent } from './pie-chart/pie-chart.component';
import { LineChartComponent } from './line-chart/line-chart.component';
import { StackedBarChartComponent } from './stacked-bar-chart/stacked-bar-chart.component';
import { SpiderChartComponent } from './spider-chart/spider-chart.component';
import { HorizonatlBarChartComponent } from './horizonatl-bar-chart/horizonatl-bar-chart.component';
import { SnapshotViewComponent } from './snapshot-view/snapshot-view.component';
import { BoxViewComponent } from './box-view/box-view.component';
import { GroupBarChartComponent } from './group-bar-chart/group-bar-chart.component';
import { ThreeDBarChartComponent } from './three-d-bar-chart/three-d-bar-chart.component';
import { CardViewComponent } from './card-view/card-view.component';
import { ThematicViewComponent } from './thematic-view/thematic-view.component';
import { ClusterAreaPipe } from './filters/cluster-area.pipe';
import { ThematicMapComponent } from './thematic-map/thematic-map.component';
import { SubmissionDashboardComponent } from './submission-dashboard/submission-dashboard.component';
import { DropDownSearchrPipe } from './filters/dropdown-search.pipe';
import { TableModule } from 'performance-dashboard-lib/public_api';
import { PerformanceDashboardPipe } from './filters/performance-dashboard.pipe'
import { TimePeriodsortPipe } from './filters/time-periodsort.pipe';
import { MediaExtraInfoPipe } from './filters/media-extra-info.pipe';
// import { BlockScrollStrategy, Overlay } from '@angular/cdk/overlay';
import { MatSelectFilterModule } from 'lib-select-filter/public_api';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    DashboardRoutingModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatSortModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatRadioModule,
    MatDatepickerModule,
    MatTooltipModule,
    MatTabsModule,
    TableModule,
    MatNativeDateModule,
    MatListModule,
    MatSelectFilterModule
  ],
  declarations: [
    AreaFilterPipe,
    BarChartComponent,
    PieChartComponent,
    LineChartComponent,
    StackedBarChartComponent,
    SpiderChartComponent,
    HorizonatlBarChartComponent,
    SnapshotViewComponent,
    BoxViewComponent,
    GroupBarChartComponent,
    ThreeDBarChartComponent,
    CardViewComponent,
    ThematicViewComponent,
    ClusterAreaPipe,
    ThematicMapComponent,
    SubmissionDashboardComponent,
    DropDownSearchrPipe,
    PerformanceDashboardPipe,
    TimePeriodsortPipe,
    MediaExtraInfoPipe
  ],
  providers:[DashboardService]
  //providers: [DashboardService, { provide: MAT_SELECT_SCROLL_STRATEGY, useFactory: scrollFactory, deps: [Overlay] }]
})
export class DashboardModule { }

// export function scrollFactory(overlay: Overlay): () => BlockScrollStrategy {
//   return () => overlay.scrollStrategies.block();
// }