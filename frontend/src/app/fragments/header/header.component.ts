import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AppService } from '../../app.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {
  router:Router;
  app: any;
  headerTimeStamp: any;

  constructor( router:Router, private appService: AppService) { 
    this.router = router;
    this.app = appService;
  }

  ngOnInit() {
    this.app.getHeaderTime().subscribe(data => {
      let res = data;
      this.headerTimeStamp = res;
    })
  }

  logout(){
    this.appService.logout();
    this.app.userName = "";
  }
}
