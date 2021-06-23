import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatFormFieldModule, MatInputModule, MatTooltipModule, MatTabsModule, MatBadgeModule } from '@angular/material'
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SdrcLoaderModule } from 'lib-loader/public_api';
import { ToastModule } from 'ng6-toastr/ng2-toastr';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { XhrInterceptorService } from './service/xhr-interceptor.service';
import { SessionCheckService } from './service/session-check.service';
import { AuthGuard } from './guard/auth.guard';
import { RoleGuardGuard } from './guard/role-guard.guard';
import { LoggedinGuard } from './guard/loggedin.guard';
import { UserManagementModule } from './user-management/user-management.module';
import { StaticModule } from './static/static.module';
import { ReportModule } from './report/report.module';
import { PlanningModule } from './planning/planning.module';
import { DashboardModule } from './dashboard/dashboard.module';
import { PerformanceReportModule } from './performance-report/performance-report.module';

import { AppService } from './app.service';
import { UserService } from './service/user/user.service';
import { HeaderComponent } from './fragments/header/header.component';
import { FooterComponent } from './fragments/footer/footer.component';
import { HomeComponent } from './home/home.component';
import { Exception404Component } from './exception404/exception404.component';
import { LoginComponent } from './login/login.component';
import { StaticHomeService } from './service/static-home.service';
import { DatePipe } from '@angular/common';
import { QualitativeFormComponent } from './qualitative-form/qualitative-form.component';
import { CommonsEngineProvider } from './report/engine/commons-engine';
import { DataSharingServiceProvider } from './report/engine/data-sharing-service/data-sharing-service';
import { EngineUtilsProvider } from './report/engine/engine-utils.service';
import { MessageServiceProvider } from './report/engine/message-service/message-service';
import { TableModule } from 'qualitative-table/public_api';
import { RemoveKeysPipe } from './rootFilters/remove-keys.pipe';


@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    FooterComponent,
    HomeComponent,
    Exception404Component,
    LoginComponent,
    QualitativeFormComponent,
    RemoveKeysPipe
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    BrowserAnimationsModule,
    MatFormFieldModule,
    HttpClientModule,
    UserManagementModule,
    PerformanceReportModule,
    MatFormFieldModule,
    MatInputModule,
    StaticModule,
    FormsModule,
    ReactiveFormsModule,
    SdrcLoaderModule,
    ReportModule,
    PlanningModule,
    MatTooltipModule,
    MatTabsModule, 
    MatBadgeModule,
    TableModule,
    DashboardModule,
    ToastModule.forRoot(),
    
  ],
  providers: [{ provide: HTTP_INTERCEPTORS, useClass: XhrInterceptorService, multi: true }, AppService, UserService, AuthGuard, RoleGuardGuard, LoggedinGuard, SessionCheckService, XhrInterceptorService,StaticHomeService,CommonsEngineProvider,DataSharingServiceProvider,EngineUtilsProvider,MessageServiceProvider,DatePipe],
  bootstrap: [AppComponent]
})
export class AppModule { }
