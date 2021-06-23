import { Injectable } from '@angular/core';
import { Subject, Observable, timer, BehaviorSubject } from 'rxjs';
import { LoaderState } from './sdrc-loader.component';
declare var $: any;

@Injectable({
  providedIn: 'root'
})
export class SdrcLoaderService {
  public isLoading = new BehaviorSubject(false);  

  show(){
    this.isLoading.next(true);
    document.body.classList.add("loader-open");
    document.body.style.overflow = "hidden";
  }

  hide(){
    this.isLoading.next(false);
    document.body.classList.remove("loader-open");
    document.body.style.overflow = "auto";
  }
}

