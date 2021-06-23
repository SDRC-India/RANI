import {
  HttpClient,
  HttpHeaders,
  HttpParams
} from '@angular/common/http';
import {
  Injectable
} from '@angular/core';
import {
  ConstantProvider
} from '../constant/constant';
import {
  MessageServiceProvider
} from '../message-service/message-service';
import {
  Storage
} from '@ionic/storage';
import {
  UserServiceProvider
} from '../user-service/user-service';
import { App } from "ionic-angular";

/*
  Generated class for the QuestionServiceProvider provider.

  See https://angular.io/guide/dependency-injection for more info on providers
  and Angular DI.
*/
@Injectable()
export class QuestionServiceProvider {

  constructor(public http: HttpClient, public messageService: MessageServiceProvider, private storage: Storage,
    public constantService: ConstantProvider, public userService: UserServiceProvider, public app: App) {}

  /**
   *
   * This method  will  fetch the from data from  rest API
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @since 0.0.1
   */
  async getQuestionBank(formId: any, accessToken: any, lastUpdatedDate:string): Promise < any > {
 
     
      return new Promise < any > ((resolve, reject) => {
        this.storage.get(ConstantProvider.dbKeyNames.userAndForm).then((userAndForm) => {
          
          if (userAndForm) {
            // this.messageService.stopLoader();
            let oneFormData = userAndForm['getAllForm'][formId]
           
            resolve(oneFormData);
          } else {
            this.messageService.showLoader(ConstantProvider.message.getForm);
            const httpOptions = {
              headers: new HttpHeaders({
                'Authorization': 'Bearer ' + accessToken
              })
              ,
              params: new HttpParams().set('lastUpdatedDate', lastUpdatedDate)
            };
            let baseURL = ConstantProvider.baseUrl + 'api/getQuestion';
            this.http.get(baseURL, httpOptions)
              .subscribe(data => {
                this.messageService.stopLoader();
                resolve(data)
              
              }, err => {
                this.messageService.stopLoader();
                reject(err)
              })
          }
        });
      });
    
  }
  async getUpdateQuestionBank(formId: any, accessToken: any, lastUpdatedDate): Promise<any> {
    return new Promise<any>((resolve, reject) => {
      const httpOptions = {
        headers: new HttpHeaders({
          'Authorization': 'Bearer ' + accessToken
        }),
        params: new HttpParams().set('lastUpdatedDate', lastUpdatedDate)
      };
      let baseURL = ConstantProvider.baseUrl + 'api/getQuestion';
      this.http.get(baseURL, httpOptions)
        .subscribe(data => {
          resolve(data)
        }, err => {
          if (!navigator.onLine) {
            this.messageService.stopLoader();
            this.messageService.showErrorToast(ConstantProvider.message.checkInternetConnection)
          } else {
            this.messageService.stopLoader();
          }
          reject(err)
        })
    });

  }
  getAllFormsId(): Promise < any > {
    return new Promise < any > ((resolve, reject) => {
      this.storage.get(ConstantProvider.dbKeyNames.userAndForm)
        .then((val) => {
          resolve(Object.keys(val['getAllForm']));
        });
    });
  }

  async getFormDisplayId(){           
          this.userService.designationSlugId=await this.storage.get(ConstantProvider.dbKeyNames.displayId);        
  }
  
  async getAllFilledFormsAgainstLoggedInUser() {
    if(this.userService.getUserAndForm() == undefined){
      let nav = this.app.getActiveNav();
      nav.setRoot('LoginPage')
    }else{
      let data=await this.storage.get( ConstantProvider.dbKeyNames.form + "-" + this.userService.user.username)
      return data;
    }
  }
}