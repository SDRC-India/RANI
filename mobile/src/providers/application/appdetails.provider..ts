import { Injectable } from '@angular/core';

/*
  Generated class for the KspmisProvider provider.

  See https://angular.io/guide/dependency-injection for more info on providers
  and Angular DI.
*/
@Injectable()
export class ApplicationDetailsProvider {

  /**
   * This prperty will tell us version name of the app. It is set to '2.1.0' becuase the current version is
   * '2.1.0'.
   * We have to change the it accordingly. Yes we can get it from android device, when it comes to PWA,
   * the default will help us.
   */
  private appVersionName : string = '0.2.1'

  /**
   * It defines the ess data entry platform
   * @author Jagat Bandhu
   * @since 0.0.1
   */
  private platform: KspmisPlatform

  setAppVersionName(appVersionName: string){
    this.appVersionName = appVersionName
  }

  getAppVersionName() : string{
    return this.appVersionName
  }

  getPlatform(){
    return this.platform
  }

  setPlatform(platform: KspmisPlatform){
    this.platform = platform
  }

}
