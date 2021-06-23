import { NgModule } from '@angular/core';
import { IonicPageModule } from 'ionic-angular';
import { MobileFormComponent } from './mobile.form';
import { ComponentsModule } from '../../components/components.module';
import { ObjIteratePipe } from '../../pipes/obj-iterate.pipe';

@NgModule({
  declarations: [
    MobileFormComponent,
    ObjIteratePipe
  ],
  imports: [
    IonicPageModule.forChild(MobileFormComponent),
    ComponentsModule
  ]
})
export class FromPageModule {}
