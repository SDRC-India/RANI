import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AppService } from 'src/app/app.service';

@Component({
  selector: 'app-planning-side-menu',
  templateUrl: './planning-side-menu.component.html',
  styleUrls: ['./planning-side-menu.component.scss']
})
export class PlanningSideMenuComponent implements OnInit {
  router: Router;
  app: AppService;
  constructor(router:Router,private appService: AppService) { 
    this.router = router;
    this.app = appService;
  }
  ngOnInit() {
  }
}
