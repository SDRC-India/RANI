import { URLSearchParams } from '@angular/http';
import { Injectable } from '@angular/core';
import { ConstantProvider } from '../constant/constant';
import { MessageServiceProvider } from '../message-service/message-service';
import 'rxjs/add/operator/map';
import { HttpHeaders, HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Storage } from '@ionic/storage'
import { Events } from 'ionic-angular';
import { UserServiceProvider } from '../user-service/user-service';

/*
  Generated class for the LoginServiceProvider provider.

  See https://angular.io/guide/dependency-injection for more info on providers
  and Angular DI.
*/
@Injectable()
export class LoginServiceProvider {

  grant_type: any;
  headers: any;
  // loginResponse: ILoginResponse;
  // authenticated: boolean;
  // loginSuccess : boolean;
  constructor(private http: HttpClient, private messageService: MessageServiceProvider,
    private constantService: ConstantProvider, private storage: Storage, public events: Events, private userService: UserServiceProvider) {

  }
  /**
   * This method will authenticate the username and password by calling the rest api for authentication
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param Authorization(username and password are set in header)
   * @since 0.0.1
   */
  authenticate(credentials): Promise < any > {
   
    let promise = new Promise < any > ((resolve, reject) => {

    const httpOptions = {
      headers: new HttpHeaders({
        'Content-type': 'application/x-www-form-urlencoded; charset=utf-8'
      })
    };

    let URL: string = ConstantProvider.baseUrl +'oauth/token'

    let params = new URLSearchParams();
    params.append('username', credentials.username);
    params.append('password', credentials.password);
    params.append('grant_type','password');
 
    this.http.post(URL, params.toString(), httpOptions)
    .subscribe(res => {
          let  loginResponse = {
              accessToken: res['access_token'],
              tokenType : res['token_type'],
              refreshToken: res['refresh_token'],
              expires: res['expires_in']
            }
            this.userService.accessToken=loginResponse.accessToken
            this.userService.refreshToken=loginResponse.refreshToken
            resolve(loginResponse);
          }, (err) => {
            console.log(err)
            reject(err);
          });
    });
    return promise
  }

  async getUserName(){
    await this.storage.get(ConstantProvider.dbKeyNames.userAndForm).then(userAndForm=>{
      if(userAndForm){
        this.userService.user = userAndForm['user']
        this.events.publish('user',userAndForm['user'])
      }
    });
  }

  getUserDisplayId()
  {
    const httpOptions = {headers: new HttpHeaders({'Content-type': 'application/x-www-form-urlencoded; charset=utf-8'})};
    let URL: string = ConstantProvider.baseUrl +'oauth/user'
    this.http.get(URL,httpOptions).subscribe(res => 
      {          
          this.storage.set(ConstantProvider.dbKeyNames.displayId,res["desgSlugId"][0])
          this.storage.set(ConstantProvider.dbKeyNames.userId,res["userId"])
      }, (err) => 
      {console.log(err)});    
  }

}