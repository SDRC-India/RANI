import { Component, OnInit, AfterViewInit } from '@angular/core';
import { Router } from '@angular/router';
import { StaticHomeService } from '../service/static-home.service';
import { AppService } from '../app.service';
declare var $: any;
import { Observable } from 'rxjs/Observable';
import { interval, timer } from "rxjs";
import { mergeMap } from 'rxjs/operators';
@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit, AfterViewInit {
  whatnew: string = "What's New";
  appService: AppService;
  staticService: StaticHomeService;
  private flag: boolean;
  reloadInterval = 10000;
  tVal: number = 1;
  constructor(private app: AppService, private staticServiceProvider: StaticHomeService, private router: Router) {
    this.appService = app;
    this.staticService = staticServiceProvider;
  }

  ngOnInit() {
    this.tVal = 0;
    // this.onTimeOut();
    if (this.router.url == "/") {
      timer(0, this.reloadInterval).pipe(
        mergeMap(_ => this.staticService.getQuickStarts())
      ).subscribe(data => {
        this.staticService.quickStarts = data;
        this.staticService.quickKeys = Object.keys(this.staticService.quickStarts);
        setTimeout(() => {
          if (this.staticService.countLoaded == false) {
            this.startCount();
          }
        }, 200)
      }
      )
    }
    
    // this.staticService.getQuickStarts().subscribe(data => {
    //   this.staticService.quickStarts = data;
    //   this.staticService.quickKeys = Object.keys(this.staticService.quickStarts);
    //   // setTimeout(()=>{
    //   //   this.startCount();
    //   // },200)
    // })
  }

  // onTimeOut() {
  //   setTimeout (() => {
  //     this.staticService.getQuickStarts().subscribe(data => {
  //       this.staticService.quickStarts = data;
  //       this.staticService.quickKeys = Object.keys(this.staticService.quickStarts);
  //     })
  //  }, 10000);
  // }

  getKeys(obj) {
    return Object.keys(obj);
  }

  ngAfterViewInit() {
    // this.startCount();
    setTimeout(() => {
      if (this.staticService.countLoaded == false) {
            this.startCount();
          }
    }, 200)
  }

  startCount() {
    $('.count').each(function (index) {
      var size = $(this).text().split(".")[1] ? $(this).text().split(".")[1].length : 0;
      $(this).prop('Counter', 0).animate({
        Counter: $(this).text()
      }, {
          duration: 2000,
          easing: 'swing',
          step: function (now) {
            $(this).text(parseFloat(now).toFixed(size));
          }
        });
    });
    this.staticService.countLoaded = true;
  }
}