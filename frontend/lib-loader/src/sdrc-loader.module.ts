import { NgModule, ModuleWithProviders, Injectable } from "@angular/core";
import { CommonModule } from "@angular/common";
import { SdrcLoaderComponent } from "./sdrc-loader/sdrc-loader.component";
import { SdrcLoaderService } from "./sdrc-loader/sdrc-loader.service";
import { HTTP_INTERCEPTORS, HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpResponse } from "@angular/common/http";
import { Observable } from "rxjs";
import { SpinnerComponent } from "./spinner/spinner.component";
import { RollerComponent } from "./roller/roller.component";
import { DefaultComponent } from "./default/default.component";
import { RingComponent } from "./ring/ring.component";

@Injectable()
export class LoaderInterceptor implements HttpInterceptor {
 private requests: HttpRequest<any>[] = [];

 constructor(private loaderService: SdrcLoaderService) { }

 removeRequest(req: HttpRequest<any>) {
   const i = this.requests.indexOf(req);
   if (i >= 0) {
     this.requests.splice(i, 1);
   }
   this.loaderService.isLoading.next(this.requests.length > 0); //stops loader when this.requests is []
   if(this.requests.length == 0){
    document.body.classList.remove("loader-open");
    document.body.style.overflow = "auto";
   }
 }

 intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
   this.requests.push(req);
   if(req.url!="/rani/quickStart"){
   this.loaderService.isLoading.next(true); //starts loader
   document.body.classList.add("loader-open");
   document.body.style.overflow = "hidden";
   }
   // We create a new observable which we return instead of the original
   return Observable.create(observer => {
     // And subscribe to the original observable to ensure the HttpRequest is made
     const subscription = next.handle(req)
       .subscribe(
       event => {
         if (event instanceof HttpResponse) {
           this.removeRequest(req);
           observer.next(event);
         }
       },
       err => { this.removeRequest(req); observer.error(err); },
       () => { this.removeRequest(req); observer.complete(); });
     // return teardown logic in case of cancelled requests
     return () => {
       this.removeRequest(req);
       subscription.unsubscribe();
     };
   });
 }
}


@NgModule({
    imports: [
      CommonModule,
    
    ],
    declarations: [SdrcLoaderComponent, SpinnerComponent, RollerComponent, DefaultComponent, RingComponent],
    schemas: [],
    providers: [SdrcLoaderService, { provide: HTTP_INTERCEPTORS,
      useClass: LoaderInterceptor,
      multi: true
    }
      ],
    exports: [SdrcLoaderComponent, SpinnerComponent, RollerComponent, DefaultComponent, RingComponent]
  })
  export class SdrcLoaderModule {
    // public static forRoot(): ModuleWithProviders {
    //   return {
    //     ngModule: SdrcLoaderModule,
    //     providers: [
    //       SdrcLoaderService,
    //       provide: HttpService,
    //         useFactory: httpServiceFactory,
    //         deps: [XHRBackend, RequestOptions, LoaderService ]
    //     ]
    //   };
    // }
  }
  