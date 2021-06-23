import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Storage } from '@ionic/storage';
import { ConstantProvider } from '../constant/constant';

/*
  Generated class for the PlanServiceProvider provider.

  See https://angular.io/guide/dependency-injection for more info on providers
  and Angular DI.
*/
@Injectable()
export class PlanServiceProvider {

  constructor(public http: HttpClient, private storage: Storage) {
    console.log('Hello PlanServiceProvider Provider');
  }

  doNetworkCall(): Promise < any > {
   
    let promise = new Promise < any > ((resolve, reject) => {
    
    let params = new URLSearchParams();
    this.storage.get(ConstantProvider.dbKeyNames.userId).then(async userId=>{
      let loginResponse = await this.storage.get(ConstantProvider.dbKeyNames.userAndForm);
      loginResponse = loginResponse['tokens']
      const httpOptions = {
        headers: new HttpHeaders({
          'Authorization': 'Bearer ' + loginResponse.accessToken,
          'Content-type': 'application/x-www-form-urlencoded; charset=utf-8'
        })
      };

    let URL: string = ConstantProvider.baseUrl +'getPlanVSAchievement?accId='+userId;
      this.http.get(URL,httpOptions)
      .subscribe(res => {
              resolve(res);
            }, (err) => {
              console.log(err)
              reject(err);
            });
      });
    })
    
    return promise
  }

}
