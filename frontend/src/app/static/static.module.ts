import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { StaticRoutingModule } from './static-routing.module';
import { TermsOfUseComponent } from './terms-of-use/terms-of-use.component';
import { PrivacyPolicyComponent } from './privacy-policy/privacy-policy.component';
import { DisclaimerComponent } from './disclaimer/disclaimer.component';
import { SitemapComponent } from './sitemap/sitemap.component';
import { AboutUsComponent } from './about-us/about-us.component';


@NgModule({
  declarations: [TermsOfUseComponent, PrivacyPolicyComponent, DisclaimerComponent, SitemapComponent, AboutUsComponent],
  imports: [
    CommonModule,
    StaticRoutingModule
  ]
})
export class StaticModule { }
