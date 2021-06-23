import { Injectable } from '@angular/core';
import { LoadingController, ToastController, AlertController, Loading } from 'ionic-angular';

/*
  Generated class for the MessageServiceProvider provider.

  See https://angular.io/guide/dependency-injection for more info on providers
  and Angular DI.
*/
@Injectable()
export class MessageServiceProvider {
  loading: Loading;
  constructor(private toastCtrl: ToastController, private loadingCtrl: LoadingController,
    public alertCtrl :  AlertController) {
  }

  /**
   * This method will be used to show success toast to user
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param message The message we want to show the user
   * @since 0.0.1
   */
  showSuccessToast(message: string){
    let toast = this.toastCtrl.create({
      message: message,
      showCloseButton: true,
      duration: 5000
    });
    toast.present();
  }

  /**
   * This method will be used to show error toast to user
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param message The message we want to show the user
   * @since 0.0.1
   */
  showErrorToast(message: string){
    let toast = this.toastCtrl.create({
      message: message,
      showCloseButton: true,
      duration: 3000
    });
    toast.present();
  }

  /**
   * This method will display loader above the page which is being rendered
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param message The message which we want to show the user
   * @since 0.0.1
   */
  showLoader(message: string) {
    this.loading = this.loadingCtrl.create({
      spinner: 'crescent',
      content: message,
    });

    this.loading.present();
  }

  /**
   * This method will stop the showing  loader above the page
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @since 0.0.1
   */
  stopLoader(){
    this.loading.dismiss();
  }

  showSuccessAlert(title: string, message: string): Promise<boolean>{
    let promise: Promise<boolean> = new Promise((resolve, reject)=>{
      let confirm = this.alertCtrl.create({
        enableBackdropDismiss: false,
        title: title,
        message: message,
        buttons: [
          {
            text: 'Ok',
            handler: () => {
              resolve()
            }
          }]
      });
      confirm.present();
    })
    return promise;
  }

  showAlert(title: string, message: string): Promise<boolean>{
    let promise: Promise<boolean> = new Promise((resolve, reject)=>{
      let confirm = this.alertCtrl.create({
        enableBackdropDismiss: false,
        title: title,
        message: message,
        buttons: [
          {
            text: 'No',
            handler: () => {
              resolve(false)
            }
          },
          {
            text: 'Yes',
            handler: () => {
              resolve(true)
            }
          }
        ]
      });
      confirm.present();
    })
    return promise;
  }

}