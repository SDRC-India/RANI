import { Component } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';

declare var $: any;

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  router: Router;
  constructor(router: Router) {
    this.router = router;
  }

  ngOnInit() {
    /** start of header fix on scroll down **/
    $(window).scroll(function () {
      // console.log($(window).scrollTop())
      if ($(window).scrollTop() > 149 && $(window).width() < 2000) {
        $('#header').addClass('navbar-fixed');
        $(".left-list").addClass('left-side-scroll');
      }
      if ($(window).scrollTop() < 149 && $(window).width() < 2000) {
        $('#header').removeClass('navbar-fixed');
        $(".left-list").removeClass('left-side-scroll');
      }
    });
    /** end of header fix on scroll down **/

    this.router.events.subscribe((evt) => {
      if (!(evt instanceof NavigationEnd)) {
        return;
      }
      window.scrollTo(0, 0)
    });
  }
  ngAfterViewChecked() {
    if ($(window).width() <= 992) {
      $(".navbar .collapse").removeClass("show");
      $(".navbar-nav .nav-item").not('.dropdown').click(function () {
        $(".collapse").removeClass("show");
      })
    }
    if ($(window).width() <= 1024) {
       $(".main-content").css("min-height", $(window).height() - 163);
    }
    if ( ($(window).width() > 1366) && ($(window).width() <= 1920) ) {
      $(".main-content").css("min-height", $(window).height() - 100);
   }

    /** close modal on browser back button press */
    $(window).on('popstate', function () {
      $('.modal').modal('hide');
      $(".modal-backdrop").remove();
      $(".in").remove();
    });
  }
}
