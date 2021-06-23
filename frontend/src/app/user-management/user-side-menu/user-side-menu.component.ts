import { Component, OnInit, Input } from '@angular/core';
import { Router } from '@angular/router';
import { AppService } from 'src/app/app.service';

@Component({
  selector: 'app-user-side-menu',
  templateUrl: './user-side-menu.component.html',
  styleUrls: ['./user-side-menu.component.scss']
})
export class UserSideMenuComponent implements OnInit {

  router: Router;
  app: AppService;
  constructor(router:Router,private appService: AppService) { 
    this.router = router;
    this.app = appService;
  }

  ngOnInit() {
  }

}
