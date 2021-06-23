import { Injectable } from '@angular/core';
import { ToastsManager } from 'ng6-toastr';
// import { LoadingController, ToastController, AlertController, Loading } from 'ionic-angular';

/*
  Generated class for the MessageServiceProvider provider.

  See https://angular.io/guide/dependency-injection for more info on providers
  and Angular DI.
*/
@Injectable()
export class MessageServiceProvider {
  
  constructor() {
  }



  showSuccessToast(message: string,toastr:ToastsManager) {
//     let toastNotificationConfiguration: ToastNotificationConfiguration = {
//       message: message,
//       displayDuration: 1000,
//       autoHide: true,
//       showCloseButton: true,
//       toastType: ToastType.SUCCESS
// };

    toastr.success(message);
  }

  showErrorToast(message: string,toastr:ToastsManager) {
//     let toastNotificationConfiguration: ToastNotificationConfiguration = {
//       message: message,
//       displayDuration: 1000,
//       autoHide: true,
//       showCloseButton: true,
//       toastType: ToastType.ERROR
// };
toastr.error(message);
  }





//   showSuccessToast(message: string,toastr:ToasterService) {
//     let toastNotificationConfiguration: ToastNotificationConfiguration = {
//       message: message,
//       displayDuration: 1000,
//       autoHide: true,
//       showCloseButton: true,
//       toastType: ToastType.SUCCESS
// };

//     toastr.showToastMessage(toastNotificationConfiguration);
//   }

//   showErrorToast(message: string,toastr:ToasterService) {
//     let toastNotificationConfiguration: ToastNotificationConfiguration = {
//       message: message,
//       displayDuration: 1000,
//       autoHide: true,
//       showCloseButton: true,
//       toastType: ToastType.ERROR
// };
// toastr.showToastMessage(toastNotificationConfiguration);
//   }

}