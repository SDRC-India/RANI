import {
  Component, HostListener
} from '@angular/core';
import {
  IonicPage,
  NavController,
  NavParams,
  Events,
  Platform,
  AlertController,
  Nav
} from 'ionic-angular';
import {
  QuestionServiceProvider
} from '../../providers/question-service/question-service';
import {
  MessageServiceProvider
} from '../../providers/message-service/message-service';
import {
  ConstantProvider
} from '../../providers/constant/constant';
import {
  Storage
} from '@ionic/storage';
import { UserServiceProvider } from '../../providers/user-service/user-service';

/**
 * Generated class for the HomePage page.
 *
 * See https://ionicframework.com/docs/components/#navigation for more info on
 * Ionic pages and navigation.
 */

@IonicPage()
@Component({
  selector: 'page-home',
  templateUrl: 'home.html',
})
export class HomePage {
  @HostListener('window:popstate', ['$event'])
  onbeforeunload(event) {
    if (window.location.href.substr(window.location.href.length - 5) == 'login') {
      history.pushState(null, null, "" + window.location.href);
    }
  }
  formList: any
  homePageModelArr: IHomePageModel[] = [];
  public unregisterBackButtonAction: any;
  searchTerm:string;
  restrictBackButton: number = 0;
  constructor(public navCtrl: NavController, public navParams: NavParams, public questionService: QuestionServiceProvider,
    public messageProvider: MessageServiceProvider, public events: Events, private platform:Platform,private storage:Storage,
    private alertCtrl: AlertController, private nav: Nav,private userService:UserServiceProvider) {}

  /**
   * This method call up the initial load. subscribe the syncStatus to refresh the page
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   */
  ngOnInit() {
    this.events.subscribe('syncStatus', data=>{
      if(data){
        this.loadForms()
      }
    })
  }

  ionViewDidEnter(){
    this.initializeBackButtonCustomHandler();
    this.loadForms()
  }

  /**
   * This method will fetch all the user specific froms
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   */
  async fetchData() {
    await this.questionService.getAllFormsId().then(async (formIds) => {
      this.formList = await formIds;
    });
    await this.questionService.getFormDisplayId();
  }

  /**
   * This method is called after fetchData to show the count of different submission of each form.
   *
   * @author Azhar (azaruddin@sdrc.co.in)
   */
  async loadForms(){
    let dbData = await this.questionService.getAllFilledFormsAgainstLoggedInUser()
    await this.fetchData()
    
    for (let i = 0; i < this.formList.length; i++) {

      let save: number = 0;
      let finalize: number = 0;
      let sent: number = 0;
      let reject: number = 0;
      let form: IHomePageModel = {
        formKeyName: this.formList[i],
        formName: this.formList[i].split("_")[1],
        formId: this.formList[i].split("_")[0],
        saveCount: save,
        rejectCount: reject,
        finalizeCount: finalize,
        sentCount: sent,
        displayId:parseInt(this.userService.designationSlugId)
      }

      if(dbData != null && dbData[form.formKeyName] != undefined){
        for (let i = 0; i < Object.keys(dbData[form.formKeyName]).length; i++) {
          if(dbData[form.formKeyName][Object.keys(dbData[form.formKeyName])[i]].formStatus == "save"){
            save++
          }else if(dbData[form.formKeyName][Object.keys(dbData[form.formKeyName])[i]].formStatus == "finalized"){
            finalize++
          }else if(dbData[form.formKeyName][Object.keys(dbData[form.formKeyName])[i]].formStatus == "sent"){
            sent++
          }else{
            reject++
          }
        }
      }

       form['saveCount'] = save
       form['rejectCount'] = reject
       form['finalizeCount'] = finalize
       form['sentCount'] = sent
       this.homePageModelArr[i] = form
     }
  }

  /**
   * This method is called when user click on specific from. This method take the specific from data to next page.
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param formId
   */

  form(formId: any,segment: any) {
    this.questionService.getQuestionBank(formId,null,ConstantProvider.lastUpdatedDate).then(data => {
        if (data) {
          this.navCtrl.push('FormListPage', {
            formId: formId,
            segment: segment
          })
        }
      })
      .catch((error) => {
        if (error.status == 500) {
          this.messageProvider.showErrorToast(ConstantProvider.message.networkError)
        }
      })
  }


    /**
   * This method will called, when user clicks on the add new button to fill a data in the new form
   *
   * @author Harsh Pratyush (Harsh@sdrc.co.in)
   */
  openNewBlankForm(formId: any,segment: any,xyz) {
    let currentTime=new Date();
    let curHour=currentTime.getHours();
    let resetFormName1= formId.split("_")[1].replace('_',' ')
    let resetFormName=resetFormName1.split("-")[1]

      if((curHour>5) && (curHour<22))
      {
        this.navCtrl.push("MobileFormComponent", {
         formId: formId,
         formTitle: formId.split("_")[1].replace('_',' '),
         isNew: true,
         segment: segment
       });
     }
     else
     {
       let confirm = this.alertCtrl.create({
          enableBackdropDismiss: false,
          title: 'Warning',
          message: "<strong>New "+ resetFormName+" form can be created between 6 AM to 10 PM</strong>",          
          buttons: [
            {
              text: "Ok",
              handler: () => {}
            }
          ]
        });
        confirm.present();
      }
 
  }



  /**
   * This method will initialize the hardware backbutton
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @since 1.0.0
   */
  public initializeBackButtonCustomHandler(): void {
    this.unregisterBackButtonAction = this.platform.registerBackButtonAction(() => {
        this.customHandleBackButton();
    }, 10);
  }

  /**
   * This method will show a confirmation popup to exit the app, when user click on the hardware back button
   * in the home page
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @since 1.0.0
   */
  private customHandleBackButton(): void {
    if (this.restrictBackButton == 0) {
      this.restrictBackButton = 1
      let confirm = this.alertCtrl.create({
        enableBackdropDismiss: false,
        title: 'Warning',
        message: "Are you sure you want to logout",
        buttons: [{
          text: 'No',
          handler: () => { this.restrictBackButton = 0  }
        },
        {
          text: 'Yes',
          handler: () => {
            this.restrictBackButton = 0 
            this.messageProvider.showSuccessToast("Logout Successfully.")
            this.nav.setRoot('LoginPage')
          }
        }
        ]
      });
      confirm.present();
    }
  }

  /**
   * Fired when you leave a page, before it stops being the active one
   * Unregister the hardware backbutton
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @since 1.0.0
   */
  ionViewWillLeave() {
    // Unregister the custom back button action for this page
    this.unregisterBackButtonAction && this.unregisterBackButtonAction();
  }

}