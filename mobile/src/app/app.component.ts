import {
  Component,
  ViewChild,
  HostListener
} from '@angular/core';
import {
  Platform,
  AlertController,
  Nav,Events, MenuController, IonicApp, App
} from 'ionic-angular';
import {
  StatusBar
} from '@ionic-native/status-bar';
import {
  SplashScreen
} from '@ionic-native/splash-screen';
import {
  KspmisPlatformImpl
} from '../class/KspmisPlatformImpl';

import {
  MessageServiceProvider
} from '../providers/message-service/message-service';
import {
  ConstantProvider
} from '../providers/constant/constant';
import {
  SyncServiceProvider
} from '../providers/sync-service/sync-service';
import {
  Storage
} from '@ionic/storage'
import { UserServiceProvider } from '../providers/user-service/user-service';
import { LoginServiceProvider } from '../providers/login-service/login-service';
import { Network } from '@ionic-native/network';
import { ApplicationDetailsProvider } from '../providers/application/appdetails.provider.';
import { ImagePicker } from '@ionic-native/image-picker';
import { Camera } from '@ionic-native/camera';
import { MobileAccessibility } from '@ionic-native/mobile-accessibility';
import { NetworkProvider, ConnectionStatusEnum } from '../providers/network/network';
import { HttpClient } from '@angular/common/http';
import { File } from '@ionic-native/file';
import { QuestionServiceProvider } from '../providers/question-service/question-service';

@Component({
  templateUrl: 'app.html'
})
export class MyApp {
  @ViewChild(Nav) nav: Nav;

  rootPage: any;
  userdata: any;
  private _innerNavCtrl;
  activeComponent:string;
  splitEnabled:boolean=false;

  constructor(public platform: Platform, public statusBar: StatusBar, public splashScreen: SplashScreen, private questionService: QuestionServiceProvider,
    private applicationDetailsProvider: ApplicationDetailsProvider, public messageProvider: MessageServiceProvider, public constantProvider: ConstantProvider,
    private alertCtrl: AlertController, public syncSerivice: SyncServiceProvider, public storage: Storage, private userService: UserServiceProvider,
    public loginService: LoginServiceProvider, public events: Events, public network: Network,private http: HttpClient,
    private imagePicker: ImagePicker,private camera: Camera,private mobileAccessibility: MobileAccessibility,
    private _app: App, private _ionicApp: IonicApp, private _menu: MenuController, private networkProvider: NetworkProvider,
    private file:File) {
    this.initializeApp();
  }

   initializeApp() {
    this.platform.ready().then(async () => {
      // Okay, so the platform is ready and our plugins are available.
      // Here you can do any higher level native things you might need.
      this.statusBar.styleDefault();
      this.splashScreen.hide();

      this.networkProvider.init();
     
      this.mobileAccessibility.usePreferredTextZoom(false);

      // this.setupBackButtonBehavior ();
      //Setting platforms
      let kspmisPlatform: KspmisPlatform = new KspmisPlatformImpl()

      if (this.platform.is('mobileweb')) {
        kspmisPlatform.isMobilePWA = true
      } else if (this.platform.is('core')) {
        kspmisPlatform.isWebPWA = true
      } else if (this.platform.is('android') && this.platform.is('cordova')) {
        await this.createProjectFolder()
        kspmisPlatform.isAndroid = true
      }
      
      this.applicationDetailsProvider.setPlatform(kspmisPlatform)
      this.rootPage = 'LoginPage'
    

    });
  }

  async setUsrName(){
    let userAndForm = await this.userService.getUserAndForm()
    if(userAndForm){
      this.userdata = userAndForm['user'].username
    }else{
      this.storage.get(ConstantProvider.dbKeyNames.userAndForm).then(data=>{
        if(data){
          this.userdata = data.user.username
        }
      })
    }
    
  }
  ngOnInit(){

    this.events.subscribe('user', data=>{
      if(data.username != undefined){
        this.userdata = data.username;
      }else{
        this.rootPage = 'LoginPage'
      }
    })
    
    this.nav.viewWillEnter.subscribe(view => {
      
      this.activeComponent = view.instance.constructor.name;
      if (this.activeComponent == "HomePage") {
        this.setUsrName();
      this.splitEnabled = true;
      } else {
      this.splitEnabled = false;
      }
      });
  }
  async updateForms() {
    this.messageProvider.showLoader(ConstantProvider.message.formUpdating);
    this.storage.get(ConstantProvider.dbKeyNames.userAndForm).then(async (item) => {
      if (item) {
        this.getAllFormsForUser(item.tokens.accessToken, item.lastUpdatedDate).then(async forms => {
          if (Object.keys(forms.allQuestions).length > 0) {
            let newObjForm = {};
            for (let key in item.getAllForm) {
              let newKey = key.split("_")[0];
              await this.getNewFormIndex(newKey, forms.allQuestions).then((index) => {
                if (index == -1) {
                  newObjForm[key] = item.getAllForm[key];
                } else {
                  newObjForm[index] = forms.allQuestions[index];
                }
              }).catch((error) => {
                console.log(error)
                //reject(error)
              });
            }
            for (let key in forms.allQuestions) {
              let newKey = key.split("_")[0];
              await this.getNewFormIndex(newKey, item.getAllForm).then((index) => {
                if (index == -1) {
                  newObjForm[key] = item.getAllForm[key];
                }
              }).catch((error) => {
                console.log(error)
                //reject(error)
              });
            }
            item.getAllForm = newObjForm;
            let userAndForm: IUserAndForm = {
              user: item.user,
              getAllForm: newObjForm,
              tokens: item.tokens,
              lastUpdatedDate: forms.lastUpdatedDate
            }
            this.messageProvider.stopLoader();
            await this.userService.saveNewForms(userAndForm).then(async a => {
              await this.checkFormExists(item, forms).then(val => {
                if (val) {
                  let confirm = this.alertCtrl.create({
                    enableBackdropDismiss: false,
                    title: 'Confirmation',
                    message: "All 'Saved' and 'Finalized' data for the updated forms shall erased from the device on confirmation. <br><br> Are you sure you want to proceed with the update?",
                    buttons: [{
                      text: 'Cancel',
                      handler: () => { }
                    },
                    {
                      text: 'Update',
                      handler: () => {
                        let user = item.user["username"]
                        this.storage.get("form-" + user).then(async (data) => {
                          if (data) {
                            for (let key in data) {
                              let newKey = key.split("_")[0];
                              await this.getNewFormIndex(newKey, forms.allQuestions).then((index) => {
                                if (index !== -1) {
                                  delete data[key];
                                }
                              }).catch((error) => {
                                console.log(error)
                                //reject(error)
                              });
                            }
                            this.userService.updateSaveAndFinalizeForm("form-" + user, data)
                            this.messageProvider.showSuccessToast(ConstantProvider.message.formUpdationSuccess)
                          }
                        })
                      }
                    }
                    ]
                  });
                  confirm.present();

                } else {
                  this.messageProvider.showSuccessToast(ConstantProvider.message.formUpdationSuccess)
                }

              }).catch((error) => {
                //reject(error)
              })
            }).catch((error) => {
              //reject(error)
            })
          } else {
            this.messageProvider.stopLoader();
            this.messageProvider.showSuccessToast(ConstantProvider.message.formUpdationNotFound)
          }
        }).catch((error) => {
          //reject(error)
        })
      }
    })
  }
  checkFormExists(item, newforms): Promise<any> {
    return new Promise<any>(async (resolve, reject) => {
      let user = item.user["username"];
      await this.storage.get("form-" + user).then(async (data) => {
        if (data) {
          for (let key in data) {
            let newKey = key.split("_")[0];
            await this.checkFormStatus(data[key]).then(async (status) => {
              if (status) {
                await this.getNewFormIndex(newKey, newforms.allQuestions).then((index) => {
                  if (index !== -1) {
                    resolve(true)
                  }
                }).catch((error) => {
                  resolve(true)
                });
              }
            }).catch((error) => {
              resolve(true)
            });
          }
        }
      })
      resolve(false)
    });
  }
  checkFormStatus(checkList): Promise<any> {
    return new Promise<any>((resolve, reject) => {
      for (let key in checkList) {
        if (checkList[key].formStatus == "save" || checkList[key].formStatus == "finalized") {
          resolve(true)
          break
        }
      }
      resolve(false)
    });
  }
  getNewFormIndex(newKey, checkList): Promise<any> {
    return new Promise<any>((resolve, reject) => {
      for (let key in checkList) {
        let oldKey = key.split("_")[0];
        if (oldKey == newKey) {
          resolve(key)
          break
        }
      }
      resolve(-1)
    });
  }
  async getAllFormsForUser(accessToken, lastUpdatedDate): Promise<any> {
    return new Promise<any>((resolve, reject) => {
      this.questionService.getUpdateQuestionBank(null, accessToken, lastUpdatedDate).then(data => {
        resolve(data)
      })
        .catch((error) => {
          reject(error)
        })
    });
  }
  
  async sync() {
    try
    {
      let submissionId = await this.http.get(ConstantProvider.baseUrl,{observe: 'response'}).toPromise();
     
    }catch(err) {
        if (err.status == 200) {
         await  this.initializeSync()
        }
        else if(err.status == 500){
          this.messageProvider.showErrorToast(ConstantProvider.message.serverError)
        }
        else if(err.status ==401){
          console.log("401");
        }
        else{
          this.messageProvider.showErrorToast(ConstantProvider.message.checkInternetConnection)
        }
      }
  }
  async initializeSync(){
    this.messageProvider.showLoader(ConstantProvider.message.syncingPleaseWait);
    let rcount = 0, scount =0;
    try {
      
      scount = await  this.syncSerivice.getAllDataFromDb()
      rcount = await  this.syncSerivice.getRejectedForms();
      this.messageProvider.stopLoader();
      if((scount > 0 && rcount > 0) || (scount > 0 || rcount > 0)){
        this.messageProvider.stopLoader();
        this.messageProvider.showSuccessAlert("Info", "(" +scount +") Forms succesfully sent.<br>" + "(" +rcount + ") Forms rejected." )
        this.events.publish('syncStatus', true)
       }else{
        this.messageProvider.stopLoader();
        this.messageProvider.showSuccessAlert("Info", "No data to sync" )
       }

    } catch (error) {
      console.log(error)
      if (error['status'] !=undefined && error.status == 101) {
        this.messageProvider.stopLoader();
        this.messageProvider.showErrorToast("Sync Failed. Please use high speed internet connectivity during sync.")
        throw (error);
      }
      else if (error['status'] !=undefined && error.status == 412) {
        this.messageProvider.stopLoader();
        this.messageProvider.showErrorToast(error.error)
        throw (error);
      } else if (error['status'] !=undefined && error.status == 417) {
        this.messageProvider.stopLoader();
        this.messageProvider.showErrorToast(error.error)
        throw (error);
      } else if (error['status'] !=undefined && error.status == 401) {
        this.messageProvider.stopLoader();
        this.messageProvider.showErrorToast(error.error)
        throw (error);
      }else if (!navigator.onLine) {
        this.messageProvider.stopLoader();
        this.messageProvider.showErrorToast(ConstantProvider.message.checkInternetConnection)
      } else {
        this.messageProvider.stopLoader();
        this.messageProvider.showErrorToast(JSON.stringify(error))
      }
      this.events.publish('syncStatus', true)
    }
    
   
  }
  
  logout() {
    let confirm = this.alertCtrl.create({
      enableBackdropDismiss: false,
      title: 'Warning',
      message: "<strong>Are you sure you want to logout</strong>",
      cssClass:'buttonCss',
      buttons: [{
          text: 'No',
          cssClass: 'cancel-button',
          handler: () => {}
        },
        {
          text: 'Yes',
          cssClass: 'exit-button',
          handler: () => {
            if(this.messageProvider.loading){
              this.messageProvider.stopLoader();
            }
           
            let isLogoutClicked = 'true';
            localStorage.setItem('isLogoutClicked', isLogoutClicked);
            this.messageProvider.showSuccessToast("Logout Successfully.")
            this.nav.setRoot('LoginPage')
          }
        }
      ]
    });
    confirm.present();
  }

  plan(){
    if (NetworkProvider.status == ConnectionStatusEnum.Offline) {
      this.messageProvider.showErrorToast(ConstantProvider.message.checkInternetConnection)
    } else {
      this.nav.push('PlanPage');
    }
  }

  private setupBackButtonBehavior () {

    // If on web version (browser)
    if (window.location.protocol !== "file:") {

      // Listen to browser pages
      this.events.subscribe("navController:current", (navCtrlData) => {
        this._innerNavCtrl = navCtrlData[0];
      });

      // Register browser back button action(s)
      window.onpopstate = (evt) => {

        // Close menu if open
        if (this._menu.isOpen()) {
          this._menu.close ();
          return;
        }

        // Close any active modals or overlays
        let activePortal = this._ionicApp._loadingPortal.getActive() ||
          this._ionicApp._modalPortal.getActive() ||
          this._ionicApp._toastPortal.getActive() ||
          this._ionicApp._overlayPortal.getActive();

        if (activePortal) {
          activePortal.dismiss();
          return;
        }

        // Navigate back
        if (this._app.getRootNav().canGoBack()) this.nav.setRoot('LoginPage');

      };

      // Fake browser history on each view enter
      this._app.viewDidEnter.subscribe((app) => {
        history.pushState (null, null, "");
      });

    }

  }
  createProjectFolder() {
    //checking folder existance
      this.file.checkDir(this.file.externalRootDirectory, ConstantProvider.appFolderName)
      .catch(err => {
        if (err.code === 1) {
          // folder not present, creating new folder
          this.file.createDir(this.file.externalRootDirectory, ConstantProvider.appFolderName, false)
            .then(data => {
            })
            .catch(err => {
              this.messageProvider.showErrorToast(err.message)
            })
        }
      })
  }
}
