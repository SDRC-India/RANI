import {
  Component, HostListener, ViewChild
} from '@angular/core';
import {
  IonicPage,
  NavController,
  MenuController,
  NavParams,
  AlertController,
  Content
} from 'ionic-angular';
import {
  FormControl
} from '@angular/forms';
import 'rxjs/add/operator/debounceTime';
import {
  MessageServiceProvider
} from '../../providers/message-service/message-service';

import {
  DataSharingServiceProvider
} from '../../providers/data-sharing-service/data-sharing-service';
import {
  ConstantProvider
} from '../../providers/constant/constant';
import { DatePipe } from '@angular/common';

import { SortRecordPipe } from '../../pipes/sort-record/sort-record';
import { FormProvider } from '../../providers/registered-forms-provider/form.provider';
import { ApplicationDetailsProvider } from '../../providers/application/appdetails.provider.';
import { FormServiceProvider } from '../../providers/form-service/form-service';

/**
 * Generated class for the FormSectionPage tabs.
 *
 * See https://ionicframework.com/docs/components/#navigation for more info on
 * Ionic pages and navigation.
 */

@IonicPage()
@Component({
  selector: 'page-form-list',
  templateUrl: 'form-list.html'
})
export class FormListPage {
  status: string = "Saved";
  hideNavBar: boolean;
  searchTerm: string = "";
  searchControl: FormControl;
  searching: any = false;
  submissions: IDbFormModel[];
  submissionsClone: IDbFormModel[] = [];
  anganwadiForm: any;
  formSubSections1: any = [];
  formSubSections2: any = [];
  dataSharingService: DataSharingServiceProvider;
  questionKeyValueMap: Map<Number, String> = new Map();
  formId: any;
  topLeft: String;
  topRight: String;
  buttonLeft: String;
  formTitle: String;
  searchBy: String;
  segment: String = "save";
  saveStatus: boolean = true;
  rejectedStatus: boolean = false;
  finalizedStatus: boolean = false;
  sentStatus: boolean = false;
  listMainHeading: String;
  isWeb: boolean = false;
  UniqueName: String;
  sortBy: string;
  saveCount: Number;
  rejectedCount: Number;
  finalizedCount: Number;
  sentCount: Number;
  saveCountStatus: boolean;
  rejectedCountStatus: boolean;
  finalizedCountStatus: boolean;
  sentCountStatus: boolean;
  
  @ViewChild(Content) content: Content;
  @HostListener("window:popstate", ["$event"])
  onbeforeunload(event) {
    if (
      window.location.href.substr(window.location.href.length - 5) == "login"
    ) {
      history.pushState(null, null, "" + window.location.href);
    }
  }

  constructor(
    public navCtrl: NavController,
    public menuCtrl: MenuController,
    public messageService: MessageServiceProvider,
    public formProvider: FormProvider,
    public navParams: NavParams,
    public dataSharingProvider: DataSharingServiceProvider,
    public alertCtrl: AlertController,
    public datepipe: DatePipe,
    public applicationDetailsProvider: ApplicationDetailsProvider,
    public sortRecord: SortRecordPipe,private formService: FormServiceProvider
  ) {
    this.dataSharingService = dataSharingProvider;
    
  }

  /**
   * Itâ€™s fired when entering a page, before it becomes the active one
   * find all the anganwadi Forms
   *
   * @author Harsh Pratyush (Harsh@sdrc.co.in)
   * @since 0.0.1
   */
  ionViewWillEnter() {
    this.findAllFroms();
    this.content.scrollTo(0, 0, 500);
  }

  /**
   * This method call up the initial load of forms page.
   * initialize the searchControl
   * initlalize blank the searchTerm
   * sort the form list is descending order with respect to updated date
   *
   * @author Harsh Pratyush (Harsh@sdrc.co.in)
   * @since 0.0.1
   */
  ngOnInit() {
    
    this.isWeb = this.applicationDetailsProvider.getPlatform().isWebPWA;
    this.searchTerm = "";
    this.searchControl = new FormControl();
    if (this.navParams) {
      this.formId = this.navParams.get("formId");
      this.segment = this.navParams.get("segment");
      this.segmentChange();
      if (this.formId) {
       
            this.formTitle = this.formId.split("_")[1];
          
            this.UniqueName = this.formId.split("_")[1];
           
      } else {
        this.navCtrl.setRoot("LoginPage");
      }
    } else {
      this.navCtrl.setRoot("LoginPage");
    }
    
  }

 
    /**
   * This method will called, when user clicks on the add new button to fill a data in the new form
   *
   * @author Harsh Pratyush (Harsh@sdrc.co.in)
   */
  openNewBlankForm() {

    let currentTime=new Date();
    let curHour=currentTime.getHours();
    
    if(curHour>5 && curHour<22){
        this.navCtrl.push("MobileFormComponent", {
          formId: this.formId,
          formTitle: this.formId.split("_")[1].replace('_',' '),
          isNew: true,
          segment: 'save'
        });
  
      }else{
           let confirm = this.alertCtrl.create({
              enableBackdropDismiss: false,
              title: 'Warning',
              message: "<strong>New "+ this.formTitle+" Form can be created between 6 AM to 10 PM</strong>",
              
              cssClass:'buttonCss',
              buttons: [
                {
                  text: "Ok",
                  cssClass: 'exit-button',
                  handler: () => {}
                }
              ]
            });
            confirm.present();
      }
 }



  /**
   * This method will called, when user clicks on specific item from the list of forms
   *
   * @author Harsh Pratyush (Harsh@sdrc.co.in)
   * @param anganwadiItem
   */
  showExistingSubmission(record) {
    this.navCtrl.push("MobileFormComponent", {
      submission: record,
      formTitle: this.formTitle,
      formId: this.formId,
      segment: this.segment
    });
  }

  /**
   * Fired when entering a page, after it becomes the active page
   * initilize searchTerm and disable the swipe for the side menu
   *
   * @author Harsh Pratyush (Harsh@sdrc.co.in)
   * @since 0.0.1
   */
  ionViewDidEnter() {
    this.searchTerm = "";
    this.menuCtrl.swipeEnable(false);
  }

  /**
   * Fired only when a view is stored in memory
   * Initially, we set searching to be false, and we set it to be true when the user starts searching.
   * Once our observable triggers and we call the filter function, we set searching to be false again so
   * that the spinner is hidden.
   *
   * We are now importing FormControl and the debounceTime operator. We set up a new member variable called
   * searchControl, and then we create a new instance of Control and assign it to that. You will see how we
   * then tie that to the <ion-searchbar> in our template in just a moment. With the FormControl set up, we
   * can subscribe to the valueChanges observable that will emit some data every time that the value of the
   * input field changes. We donâ€™t just listen for changes though, we also chain the debounceTime operator,
   * which allows us to specify a time that we want to wait before triggering the observable. If the value
   * changes again before the debounceTime has expired, then it wonâ€™t be triggered. This will stop the
   * setFilteredItems function from being spam called and, depending on how fast the user types, it should
   * only be called once per search. In this case, we are setting a debounceTime of 700 milliseconds, but
   * you can tweak this to be whatever you like.
   *
   * @author Harsh Pratyush (Harsh@sdrc.co.in)
   * @since 0.0.1
   */
  ionViewDidLoad() {
    
    this.searchControl.valueChanges.debounceTime(700).subscribe(search => {
      this.searching = false;
    });
  }

  /**
   * This will make the search sprinner visible
   * @author Harsh Pratyush (Harsh@sdrc.co.in)
   * @since 0.0.1
   */
  onSearchInput() {
    this.searching = true;
  }

  /**
   * This method called on the initialize of this page, to get all the list of items specific to the form.
   *
   * @author Harsh Pratyush (Harsh@sdrc.co.in)
   */
  async findAllFroms() {
    if (this.formId) {
      this.saveCount = 0;
      this.rejectedCount = 0;
      this.finalizedCount = 0;
      this.sentCount = 0;
    await  this.formProvider.findAllFroms(this.formId).then(data => {
          if (data) {
            let ifroms: IDbFormModel[] = [];
            for (
              let submission = 0;
              submission < Object.keys(data).length;
              submission++
            ) {
             
              ifroms.push(data[Object.keys(data)[submission]] as IDbFormModel);
              if(submission==0)
              {
                this.searchBy = "Search by";

                Object.keys(ifroms[0].formDataHead).forEach(d=> {
                  if(this.searchBy=="Search by")
                  this.searchBy+=" "+d.split("_")[1]
                  else
                  this.searchBy+=" / "+d.split("_")[1]
                }
                   );
              }
              this.submissionsClone.push(data[
                Object.keys(data)[submission]
              ] as IDbFormModel);
            }
            this.saveCount = ifroms.filter(d => d.formStatus == "save").length;
            this.rejectedCount = ifroms.filter(
              d => d.formStatus == "rejected"
            ).length;
            this.finalizedCount = ifroms.filter(
              d => d.formStatus == "finalized"
            ).length;
            this.sentCount = ifroms.filter(d => d.formStatus == "sent").length;
            this.saveCount > 0
              ? (this.saveCountStatus = false)
              : (this.saveCountStatus = true);
            this.rejectedCount > 0
              ? (this.rejectedCountStatus = false)
              : (this.rejectedCountStatus = true);
            this.finalizedCount > 0
              ? (this.finalizedCountStatus = false)
              : (this.finalizedCountStatus = true);
            this.sentCount > 0
              ? (this.sentCountStatus = false)
              : (this.sentCountStatus = true);
            this.submissions = ifroms;
            this.submissions = this.sortRecord.transform(this.submissions);
          }
        })
        .catch(err => {
          console.log(err)
          this.messageService.showErrorToast(err);
          this.navCtrl.setRoot("LoginPage");
        });
    } else {
      this.navCtrl.setRoot("LoginPage");
    }
  }
 /**
   * This method will called, when user swipe on the item from the list to move to save.
   *
   * @author Sourav Nath
   * @param record
   */
  swipeEvent(record){
    if (record.formStatus == "sent") {
      this.messageService.showLoader(ConstantProvider.message.pleaseWait)
      let confirm = this.alertCtrl.create({
        enableBackdropDismiss: false,
        cssClass: 'custom-font',
        title: ConstantProvider.message.warning,
        message: ConstantProvider.message.movetoSave,
        buttons: [{
            text: 'No',
            handler: () => {
              this.messageService.stopLoader()
            }
          },
          {
            text: 'Yes',
            handler: () => {
              this.formProvider
                .changeStatusOfRecord(record, this.formId,"finalized")
                .then(async data => {
                  await this.findAllFroms();
                  this.messageService.stopLoader()
                  await this.messageService.showSuccessToast(
                    "Moved Submission to Finalized forms successfully"
                  );
                })
                .catch(err => {
                  this.messageService.stopLoader()
                  this.messageService.showErrorToast(
                    "Could not move submission to Finalized forms, error:" + err
                  );
                });
            }
          }
        ]
      });
      confirm.present();
    }
  }
  /**
   * This method will called, when user long press on the item from the list to delete it.
   *
   * @author Harsh Pratyush (Harsh@sdrc.co.in)
   * @param anganwadiItem
   */
  deleteSubmission(record) {
    // if (this.segment != "sent") {
      this.messageService
        .showAlert(
          ConstantProvider.message.warning,
          ConstantProvider.message.deleteFrom
        )
        .then(data => {
          if (data) {
            this.formProvider
              .deleteSingleRecord(record, this.formId)
              .then(async data => {
                await this.findAllFroms();
                await this.messageService.showSuccessToast(
                  "Deleted successfully"
                );
              })
              .catch(err => {
                this.messageService.showErrorToast(
                  "Could not delete anganwadi record, error:" + err
                );
              });
          }
        });
    // } else {
    //   let confirm = this.alertCtrl.create({
    //     enableBackdropDismiss: false,
    //     title: "Warning",
    //     message: "Deletion of record not possible",
    //     buttons: [
    //       {
    //         text: "Ok",
    //         handler: () => {}
    //       }
    //     ]
    //   });
    //   confirm.present();
    // }
  }

  /**
   * This method will called, when user clicks on the different tab to switch the view.
   *
   * @author Harsh Pratyush (Harsh@sdrc.co.in)
   */
  segmentChange() {
    switch (this.segment) {
      case "save":
        this.saveStatus = true;
        this.rejectedStatus = false;
        this.finalizedStatus = false;
        this.sentStatus = false;
        this.searchTerm = "";
        break;
      case "rejected":
        this.saveStatus = false;
        this.rejectedStatus = true;
        this.finalizedStatus = false;
        this.sentStatus = false;
        this.searchTerm = "";
        break;
      case "finalized":
        this.saveStatus = false;
        this.rejectedStatus = false;
        this.finalizedStatus = true;
        this.sentStatus = false;
        this.searchTerm = "";
        break;
      case "sent":
        this.saveStatus = false;
        this.rejectedStatus = false;
        this.finalizedStatus = false;
        this.sentStatus = true;
        this.searchTerm = "";
        break;
    }
  }

  getKeys(map:Map<any,any>)
  {
    return Object.keys(map)
  }
}