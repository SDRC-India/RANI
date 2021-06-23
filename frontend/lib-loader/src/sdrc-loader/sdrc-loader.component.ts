import { Component, OnInit, Input } from '@angular/core';
import { Subscription } from 'rxjs';
import { SdrcLoaderService } from './sdrc-loader.service';

@Component({
  selector: 'sdrc-loader',
  templateUrl: './sdrc-loader.component.html',
  styleUrls: ['./sdrc-loader.component.scss']
})
export class SdrcLoaderComponent implements OnInit {

  show = false;
  private subscription: Subscription;
  loaderService: SdrcLoaderService;
  @Input() loaderType: string;
 constructor(private loaderProvider: SdrcLoaderService){
  this.loaderService = loaderProvider;
 }

  ngOnInit() { 
}
// ngOnDestroy() {
//     this.subscription.unsubscribe();
// }

}

export interface LoaderState {
  show: boolean;
}