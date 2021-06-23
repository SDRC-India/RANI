import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Constants } from '../constants';
import { Router } from '@angular/router';

@Injectable()
export class StaticHomeService {
  contentSection: any = {};
  countLoaded: boolean = false;
  quickStarts: any;
  quickVal: any ;
  quickKeys: any;
  quickAllKeys: any;

  constructor(private httpClient: HttpClient, private router: Router) { }
  getQuickStarts() {
    if (this.router.url == "/") {
    return this.httpClient.get(Constants.HOME_URL + "quickStart");
    //return this.httpClient.get("assets/json/quickStart.json");
    }
  }
}



