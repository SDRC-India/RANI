import { Injectable } from '@angular/core';
import { Router } from '@angular/router'
import { HttpClient, HttpHeaders } from '@angular/common/http'
import { Constants } from '../../constants';

@Injectable()
export class UserService {

  constructor(private router: Router, private http: HttpClient) { }

  checkLoggedIn() : boolean{
    if (!localStorage.getItem('access_token')){
        return false
    }else{
      return true
    }
  }

  logout() {
    localStorage.removeItem('access_token');
    this.router.navigate(['/login']);
  }

  obtainAccessToken(loginData){

    const httpOptions = {
      headers: new HttpHeaders({ 'Authorization': 'Basic ' + btoa("client:clientpassword"), 
      'Content-type': 'application/x-www-form-urlencoded; charset=utf-8' }, )
    };

    let params = new URLSearchParams();
    params.append('username',loginData.username);
    params.append('password', loginData.password);    
    params.append('grant_type','password');
    params.append('client_id','client');

    this.http.post(Constants.HOME_URL + 'user', params.toString(), httpOptions).subscribe(
      data=>{       
        console.log("First access token : ", data) 
        this.saveToken(data)
      },

      err=>{
        console.log(err)
      }
    )     
  }

  saveToken(token){
    var expireDate = new Date().getTime() + (1000 * token.expires_in);
    let date = new Date(expireDate);

    console.log("expireDate "+ date)
    //Cookie.set("access_token", token.access_token, expireDate);
    localStorage.setItem("access_token", token.access_token)
    this.router.navigate(['/']);
  }

}
