import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { TermsOfUseComponent } from './terms-of-use/terms-of-use.component';
import { PrivacyPolicyComponent } from './privacy-policy/privacy-policy.component';
import { DisclaimerComponent } from './disclaimer/disclaimer.component';
import { SitemapComponent } from './sitemap/sitemap.component';
import { AboutUsComponent } from './about-us/about-us.component';


const routes: Routes = [
  {
    path: 'about-us',
    component: AboutUsComponent,
    pathMatch: 'full'
  },  
  {
    path: 'terms-of-use',
    component: TermsOfUseComponent,
    pathMatch: 'full'
  },
  {
    path: 'privacy-policy',
    component: PrivacyPolicyComponent,
    pathMatch: 'full'
  },
  {
    path: 'disclaimer',
    component: DisclaimerComponent,
    pathMatch: 'full'
  },
  {
    path: 'sitemap',
    component: SitemapComponent,
    pathMatch: 'full'
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class StaticRoutingModule { }
