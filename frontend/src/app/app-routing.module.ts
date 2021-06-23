import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { LoggedinGuard } from './guard/loggedin.guard';
import { Exception404Component } from './exception404/exception404.component';
import { HomeComponent } from './home/home.component';
import { LoginComponent } from './login/login.component';
import { QualitativeFormComponent } from './qualitative-form/qualitative-form.component';
import { RoleGuardGuard } from './guard/role-guard.guard';

const routes: Routes = [

  {
    path: '',
    component: HomeComponent,
    pathMatch: 'full'
  },
  { 
    path: 'login', 
    pathMatch: 'full', 
    component: LoginComponent,
    canActivate: [LoggedinGuard]
  },
  {
    path: 'error',
    component: Exception404Component,
    pathMatch: 'full'
  },
  {
    path: 'exception',
    component: Exception404Component,
    pathMatch: 'full'
  },
  {
    path: 'user',
    loadChildren: './user-management/user-management.module#UserManagementModule',
  },
  {
    path: 'static',
    loadChildren: './static/static.module#StaticModule',
  },
  {
    path: 'qualitative-report',
    component: QualitativeFormComponent,
    pathMatch: 'full',
    canActivate: [RoleGuardGuard],
    data: { 
      expectedRoles: ["QUALITATIVE_REPORT_DATA_ENTRY","QUALITATIVE_REPORT_UPLOAD","QUALITATIVE_REPORT"]
    }, 
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
