import { BrowserModule } from '@angular/platform-browser';
import { ErrorHandler, NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { IonicApp, IonicErrorHandler, IonicModule } from 'ionic-angular';
import { SplashScreen } from '@ionic-native/splash-screen';
import { StatusBar } from '@ionic-native/status-bar';

import { MyApp } from './app.component';
import { DataSharingServiceProvider } from '../providers/data-sharing-service/data-sharing-service';
import { QuestionServiceProvider } from '../providers/question-service/question-service';
import { ConstantProvider } from '../providers/constant/constant';
import { MessageServiceProvider } from '../providers/message-service/message-service';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { HttpModule } from '@angular/http';
import { IonicStorageModule } from '@ionic/storage';
import { AppVersion } from '@ionic-native/app-version';
import { LoginServiceProvider } from '../providers/login-service/login-service';
import { UserServiceProvider } from '../providers/user-service/user-service';
import { FormServiceProvider } from '../providers/form-service/form-service';

import { SyncServiceProvider } from '../providers/sync-service/sync-service';
import { ValidationsProvider } from '../providers/validations/validations';
import { DatePipe, DecimalPipe } from '@angular/common';
import { DatePicker } from '@ionic-native/date-picker';
import { DatePickerModule } from 'ionic2-date-picker';
import { Network } from '@ionic-native/network';
import { UtilServiceProvider } from '../providers/util-service/util-service';
import { FormSearchPipe } from '../pipes/form-search/form-search';
import { MyDatePickerModule } from 'mydatepicker';
import { AmazingTimePickerModule } from 'amazing-time-picker';
import { SortRecordPipe } from '../pipes/sort-record/sort-record';
import { NetworkProvider } from '../providers/network/network';
import { EngineUtilsProvider } from '../providers/engine-utils/engine-utils';

import { Device } from '@ionic-native/device';
import { Geolocation } from "@ionic-native/geolocation";
import { Camera } from '@ionic-native/camera';
import { ApplicationDetailsProvider } from '../providers/application/appdetails.provider.';
import { FormProvider } from '../providers/registered-forms-provider/form.provider';
import { CommonsEngineProvider } from '../providers/commons-engine/commons-engine';
import { File } from '@ionic-native/file';
import { ImagePicker } from '@ionic-native/image-picker';
import { ConstraintTokenizer } from '../providers/engine-utils/ConstraintTokenizer';
import { InterceptorProvider } from '../providers/interceptor/interceptor';
import { Storage } from '@ionic/storage';
import { MobileAccessibility } from '@ionic-native/mobile-accessibility';
import { PlanServiceProvider } from '../providers/plan-service/plan-service';

@NgModule({
  declarations: [
    MyApp,
    SortRecordPipe
  ],
  imports: [
    BrowserModule,
    DatePickerModule,
    MyDatePickerModule,
    AmazingTimePickerModule,
    IonicModule.forRoot(MyApp,{
      preloadModules: true,
      tabsPlacement: 'top',
      platforms: {
        android: {
          tabsPlacement: 'top'
        },
        ios: {
          tabsPlacement: 'top'
        },
        windows:
        {
          tabsPlacement: 'top'
        }
      }
    }),
    IonicStorageModule.forRoot({
      driverOrder: ['indexeddb','sqlite', 'websql']
    }),
    HttpModule,
    HttpClientModule
  ],
  bootstrap: [IonicApp],
  entryComponents: [
    MyApp
  ],
  providers: [
    StatusBar,
    SplashScreen,
    DatePicker,
    {provide: ErrorHandler, useClass: IonicErrorHandler},
    {provide: HTTP_INTERCEPTORS, useClass: InterceptorProvider, multi: true},
    ApplicationDetailsProvider,
    DataSharingServiceProvider,
    QuestionServiceProvider,
    ConstantProvider,
    MessageServiceProvider,
    AppVersion,
    LoginServiceProvider,
    UserServiceProvider,
    FormServiceProvider,
    SyncServiceProvider,
    ValidationsProvider,
    DatePipe,
    Network, 
    Device,
    UtilServiceProvider,
    FormSearchPipe,
    SortRecordPipe,
    NetworkProvider,
    DecimalPipe,
    EngineUtilsProvider,
    Geolocation,
    Camera,
    FormProvider,
    CommonsEngineProvider,
    ConstraintTokenizer,
    File,
    ImagePicker,
    InterceptorProvider,
    MobileAccessibility,
    PlanServiceProvider
  ],
  schemas: [ CUSTOM_ELEMENTS_SCHEMA ],
})
export class AppModule {}