import { NgModule } from '@angular/core';
import { WebFormComponent } from './web/web.form';
import { CommonModule } from '@angular/common';
import { IonicModule } from 'ionic-angular';
import { RemoveExtraKeysPipe } from '../pipes/remove-extra-keys';
import { MyDatePickerModule } from 'mydatepicker';
import { AmazingTimePickerModule } from 'amazing-time-picker';
import { IonicSelectableModule } from 'ionic-selectable';


@NgModule({
	declarations: [
        WebFormComponent,
        RemoveExtraKeysPipe
    ],
	imports: [
        CommonModule,
        IonicModule,
        MyDatePickerModule,
        AmazingTimePickerModule,
        IonicSelectableModule
    ],
	exports: [
        WebFormComponent]
})

export class ComponentsModule {}