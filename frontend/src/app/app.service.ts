import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { Router} from '@angular/router'
import { Constants } from './constants';

@Injectable()
export class AppService {
    authenticated = false;
    logoutSuccess:boolean = false;
    isValid: boolean = true;
    _data : any;
    validationMsg: any;
    constructor(private http: HttpClient, private router: Router, private httpClient: HttpClient) {
    //this.userIdle.onTimeout().subscribe(() => console.log('Time is up!'));
    }
    
    authenticate(credentials, callback) {   
        this.isValid = false;
        this.callServer(credentials).subscribe(response=>{      
          this._data=response;   //store the token
          localStorage.setItem('access_token',this._data.access_token);
          localStorage.setItem('refresh_token',this._data.refresh_token);
                
          const httpOptions = {
            headers: new HttpHeaders({
              'Authorization': 'Bearer ' + this._data.access_token,
              'Content-type': 'application/json'
            })
          };
          this.http.get(Constants.HOME_URL+'oauth/user', httpOptions).subscribe(user=>{           
            let logedUserDetails:any = user;            
            if(logedUserDetails.designationNames[0]!== "COMMUNITY FACILITATOR"){
            localStorage.setItem('user_details',JSON.stringify(user));        
            this.router.navigateByUrl('/');    
            this.isValid = true;    
           }else{
            this.deleteCookies();
            this.isValid = false;
            this.validationMsg = "User credentials not authorized to access";
            setTimeout(()=>{
              this.validationMsg ="";
            }, 3000)           
           }
          });
        },  error=>{
          if(error == "User is disabled")
          this.validationMsg = "Given username has been disabled. Please contact your admin";
          else  if(error == "Invalid Credentials !" || error == "Bad credentials")
          this.validationMsg = "Wrong username/password entered";
          setTimeout(()=>{
            this.validationMsg ="";
          }, 3000)
          this.isValid = true;
        })
      }
    
      callServer(userDetails){

        const httpOptions = {
          headers: new HttpHeaders ({
            'Content-type': 'application/x-www-form-urlencoded; charset=utf-8'
          })
        };
    
        let URL: string =  Constants.HOME_URL + 'oauth/token'
        let params = new URLSearchParams();
        params.append('username', userDetails.username);
        params.append('password', userDetails.password);    
        params.append('grant_type','password');
    
        return this.http.post(URL, params.toString(), httpOptions)    
        .pipe(
          catchError(this.handleError)
        );
      }

      private handleError(error: HttpErrorResponse) {
        if (error.error instanceof ErrorEvent) {
          // A client-side or network error occurred. Handle it accordingly.
          console.error('An error occurred:', error.error.message);
        } else {
          // The backend returned an unsuccessful response code.
          // The response body may contain clues as to what went wrong,
          console.error(
            `Backend returned code ${error.status}, ` +
            `body was: ${error.error.error_description}`);       
        }
        // return an observable with a user-facing error message
        return throwError(
          //'Something bad happened; please try again later.');     
          error.error.error_description);
      };

    checkLoggedIn() : boolean{
        if (!localStorage.getItem('access_token')){
            return false
        }else{
          return true
        }
    }
    
    //handles nav-links which are going to be shown 
  checkUserAuthorization(expectedRoles) {
    if(localStorage.getItem('user_details')){
      var token = JSON.parse(localStorage.getItem('user_details'));
    }
    let flag = false;
    if(token !==undefined){
    if (this.checkLoggedIn() && token.authorities) {
      expectedRoles.forEach(expectedRole => {
        for(let i=0; i< token.authorities.length; i++){          
          if (token.authorities[i] == expectedRole) {
            flag = true;
          }  
        }      
      });      
    }
   }
    return flag;
  }

    logout() {
      this.deleteCookies()
      this.router.navigateByUrl('/');
      this.logoutSuccess = true;
      setTimeout(()=>{
          this.logoutSuccess = false;
      },2000)   
    }

    deleteCookies(){
      //Cookie.deleteAll();
      localStorage.clear();      
    }

    getUserDetails(){
      if(localStorage.getItem('user_details'))
      return JSON.parse(localStorage.getItem('user_details'));
      else
      return {}
    }    

    getHeaderTime() {
      return this.httpClient.get(Constants.HOME_URL + 'quickStartDates', {responseType: "text"});
    }
}
