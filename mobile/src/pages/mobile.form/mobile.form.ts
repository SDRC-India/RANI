import {
  Component,
  ViewChild,
  HostListener,
  ChangeDetectorRef
} from '@angular/core';
import {
  IonicPage,
  NavController,
  NavParams,
  ViewController,
  Content,
  AlertController,
  Platform,
  IonicApp,
  Navbar,
  ActionSheetController
} from 'ionic-angular';

import {
  QuestionServiceProvider
} from '../../providers/question-service/question-service';
import {
  MessageServiceProvider
} from '../../providers/message-service/message-service';
import {
  FormServiceProvider
} from '../../providers/form-service/form-service';
import {
  DataSharingServiceProvider
} from '../../providers/data-sharing-service/data-sharing-service';
import {
  ConstantProvider
} from '../../providers/constant/constant';
import {
  DatePipe
} from '@angular/common';
import {
  DatePicker
} from '@ionic-native/date-picker';
import {
  EngineUtilsProvider
} from '../../providers/engine-utils/engine-utils';
import {
  AmazingTimePickerService
} from 'amazing-time-picker';
import {
  UUID
} from 'angular2-uuid';
import {
  WebFormComponent
} from '../../components/web/web.form';
import {
  IMyDpOptions,
  IMyDateModel
} from 'mydatepicker';
import {
  Geolocation
} from '@ionic-native/geolocation';
import {
  Camera,
  CameraOptions
} from '@ionic-native/camera';
import { ApplicationDetailsProvider } from '../../providers/application/appdetails.provider.';
import { CommonsEngineProvider } from '../../providers/commons-engine/commons-engine';
import { UserServiceProvider } from '../../providers/user-service/user-service';
import { ImagePicker } from '@ionic-native/image-picker';
import {
  Storage
} from '@ionic/storage';
import { File, IWriteOptions } from '@ionic-native/file';
import { ConstraintTokenizer } from '../../providers/engine-utils/ConstraintTokenizer';

declare var $;
@IonicPage()
@Component({
  selector: 'mobile-form',
  templateUrl: 'mobile.form.html'
})

export class MobileFormComponent {

  initialCreatedTime: string = null;
  backButtonFlag: boolean = true
  backButtonCheckData: Map<String, Array<Map<String, Array<IQuestionModel>>>> = new Map();
  statusSaveMandatory: boolean = false;
  isWeb: boolean = false;
  section: String;
  dataSharingService: DataSharingServiceProvider;
  repeatSubSection: Map<Number, IQuestionModel> = new Map()
  tempFormSubSections2;
  sectionNames = []
  sectionHeading: any;
  subSections: Array<Map<String, Array<IQuestionModel>>>
  sectionMap: Map<String, Array<Map<String, Array<IQuestionModel>>>> = new Map();
  data: Map<String, Array<Map<String, Array<IQuestionModel>>>> = new Map();
  dbFormModel: IDbFormModel;
  maxDate: any;
  questionMap: {} = {}
  formId: Number
  formTitle: String
  formTitleActive: boolean = false;
  errorStatus: boolean = false;
  mandatoryQuestion: {} = {};
  disableStatus: boolean = false;
  disablePrimaryStatus: boolean = false;
  saveType: String
  uniqueId: String = "";
  createdDate: String = "";
  updatedDate: String = "";
  updatedTime: String = "";
  checkFieldContainsAnyvalueStatus: boolean = false;
  segment: boolean = false
  countBeginRepeat = 0;
  beginrepeatArraySize: number = 0;
  isNewFormEntry: boolean;
  beginRepeatKey: Number;
  public unregisterBackButtonAction: any;
  base64Image: any;
  options = {
    enableHighAccuracy: true
  };


  questionDependencyArray: {} = {}
  questionFeaturesArray: {} = {}
  constraintsArray: {} = {}
  beginRepeatArray: {} = {}
  scoreKeyMapper: {} = [];
  testErrMsgStatus: boolean = false;
  beginReapetAlertStatus: boolean = false;
  public photos: any = [];
  /**
   * If it is new form entry, the vaue will be true otherwise the value will be false
   * @author Ratikanta
   * @type {boolean}
   * @memberof FormListPage
   */

  @ViewChild(Navbar) navBar: Navbar;
  @ViewChild(Content) content: Content;
  @ViewChild(WebFormComponent) customComponent: WebFormComponent;
  @HostListener('window:popstate', ['$event'])
  onbeforeunload(event) {
    if (window.location.href.substr(window.location.href.length - 4) == 'form') {

    }
    if (window.location.href.substr(window.location.href.length - 5) == 'login') {
      history.pushState(null, null, "" + window.location.href);
    }
  }
  constructor(private cf: ChangeDetectorRef, private commonsEngineProvider: CommonsEngineProvider, private applicationDetailsProvider: ApplicationDetailsProvider, public questionService: QuestionServiceProvider,
    public messageService: MessageServiceProvider, private navCtrl: NavController, public datepipe: DatePipe, public datePicker: DatePicker,
    public viewCtrl: ViewController, public formService: FormServiceProvider, public navParams: NavParams, private dataSharingProvider: DataSharingServiceProvider,
    private atp: AmazingTimePickerService, private alertCtrl: AlertController, private platform: Platform, public storage: Storage, private datePipe: DatePipe,
    private app: IonicApp, private engineUtilsProvider: EngineUtilsProvider, private userService: UserServiceProvider, private constraintTokenizer: ConstraintTokenizer,
    private geolocation: Geolocation, private camera: Camera, public actionSheetCtrl: ActionSheetController, private imagePicker: ImagePicker, private file: File) {
    this.dataSharingService = dataSharingProvider;
  }

  getGeoLocation(question) {
    this.messageService.showLoader(ConstantProvider.message.pleaseWait);
    this.geolocation.getCurrentPosition(this.options).then((resp) => {
      // this.questionMap[question.columnName].value = "Latitude :" + resp.coords.latitude + " Longitude :" + resp.coords.longitude;
      this.questionMap[question.columnName].value = "Lat:" + resp.coords.latitude + " Long:" + resp.coords.longitude;
      this.messageService.stopLoader()
    }).catch((error) => {
      console.log('Error getting location', error);
      this.messageService.stopLoader()
    });
  }
  openCamera(question) {
    if (this.applicationDetailsProvider.getPlatform().isAndroid) {
      const options: CameraOptions = {
        quality: 90,
        sourceType: this.camera.PictureSourceType.PHOTOLIBRARY,
        destinationType: this.camera.DestinationType.DATA_URL,
        encodingType: this.camera.EncodingType.JPEG,
        mediaType: this.camera.MediaType.PICTURE,
        targetWidth: 800,
        targetHeight: 600
      }
      this.camera.getPicture(options).then((imageData) => {
        this.geolocation.getCurrentPosition(this.options).then(resp => {
          this.questionMap[question.columnName].value = {
            src: "data:image/jpeg;base64," + imageData,
            // meta_info: "Latitude :" + resp.coords.latitude + "; Longitude :" + resp.coords.longitude + "; Accuracy :" + resp.coords.accuracy
            meta_info: "Lat:" + resp.coords.latitude + "; Long:" + resp.coords.longitude + "; Accuracy :" + resp.coords.accuracy
          }
        }).catch(error => {
          this.questionMap[question.columnName].value = {
            src: "data:image/jpeg;base64," + imageData
          }
        });
      }, (err) => {
        // Handle error
      });
    } else {
      document.getElementById(question.columnName + "file-input").click();
    }
  }

  onCameraFileChange($event, question) {
    let files = $event.target.files;
    let file = files[0];

    if (
      (file.name.split(".")[(file.name.split(".") as string[]).length - 1] as String)
        .toLocaleLowerCase() === "png" || (file.name.split(".")[(file.name.split(".") as string[]).length - 1] as String)
          .toLocaleLowerCase() === "jpg" || (file.name.split(".")[(file.name.split(".") as string[]).length - 1] as String)
            .toLocaleLowerCase() === "jpeg"
    ) {
      let reader = new FileReader()
      reader.onload = this._handleReaderLoaded.bind(this);
      this.base64Image = question;
      reader.readAsBinaryString(file);
    } else {
      this.messageService.showErrorToast("Please select a image")
    }
  }

  openCameraGallery(question) {
    if (question.value != null) {
      if (question.value.length > 1) {
        let confirm = this.alertCtrl.create({
          enableBackdropDismiss: false,
          title: 'Warning',
          message: "<strong> Maximum 2 images can be uploaded.Please delete image, if you want to upload new one </strong>",
          buttons: [
            {
              text: "Ok",
              handler: () => { }
            }
          ]
        });
        confirm.present();
      }
      else {
        let actionSheet = this.actionSheetCtrl.create({
          title: 'Select Image Source',
          buttons: [{
            text: 'Load from Library',
            handler: () => {
              this.openGallery(question);
            }
          },
          {
            text: 'Use Camera',
            handler: () => {
              this.takePhoto(question);
            }
          },
          {
            text: 'Cancel',
            role: 'cancel'
          }
          ]
        });
        actionSheet.present();
      }

    }
    else {
      // alert("e")
      let actionSheet = this.actionSheetCtrl.create({
        title: 'Select Image Source',
        buttons: [{
          text: 'Load from Library',
          handler: () => {
            this.openGallery(question);
          }
        },
        {
          text: 'Use Camera',
          handler: () => {
            this.takePhoto(question);
          }
        },
        {
          text: 'Cancel',
          role: 'cancel'
        }
        ]
      });
      actionSheet.present();
    }


  }

  openGallery(question) {
    if (!this.questionMap[question.columnName].value)
      this.questionMap[question.columnName].value = [];
    let numOfImage = parseInt(question.constraints.split(':')[1]) - question.value.length

    let options = {
      maximumImagesCount: numOfImage,
      quality: 50, // picture quality
      width: 800,
      height: 600,
      destinationType: this.camera.DestinationType.FILE_URI,
      encodingType: this.camera.EncodingType.JPEG,
      mediaType: this.camera.MediaType.PICTURE
    };
    const iWriteOptions: IWriteOptions = {
      replace: true
    }
    this.imagePicker.hasReadPermission().then(
      (result) => {
        if (result == false) {
          // no callbacks required as this opens a popup which returns async
          this.imagePicker.requestReadPermission();
        }
        else if (result == true) {


          this.imagePicker.getPictures(options).then(async (imageData) => {

            for (var i = 0; i < imageData.length; i++) {
              let fileName = imageData[i].substr(imageData[i].lastIndexOf('/') + 1)
              let base64String = await this.file.readAsDataURL(imageData[i].substr(0, imageData[i].lastIndexOf('/') + 1), fileName)
              let imageBlob = await this.commonsEngineProvider.dataURItoBlob(base64String, "image/jpeg")

              this.commonsEngineProvider.createFoldersInMobileDevice(this.formId, this.uniqueId, this.file, this.messageService).then((async d => {
                let writeToFilePath = this.file.externalRootDirectory + ConstantProvider.appFolderName + "/" + this.formId + "/" + this.uniqueId;
                let writtenFile = await this.file.writeFile(writeToFilePath, fileName, imageBlob, iWriteOptions)
                this.photos.push(writtenFile.nativeURL);
                this.questionMap[question.columnName].value.push(writtenFile.nativeURL)
                this.messageService.stopLoader();
              })).catch(err => {
                this.messageService.stopLoader()
                this.messageService.showErrorToast(err)
              })
              // this.photos.push(imageData[i]);
              // this.questionMap[question.columnName].value.push(imageData[i])
            }
            this.photos.reverse();
          }, (err) => {
            console.log(err);
          });
        }
      }, (err) => {
        console.log(err);
      });

  }

  takePhoto(question) {
    if (!this.questionMap[question.columnName].value)
      this.questionMap[question.columnName].value = [];
    const options: CameraOptions = {
      quality: 50, // picture quality
      destinationType: this.camera.DestinationType.DATA_URL,
      encodingType: this.camera.EncodingType.JPEG,
      mediaType: this.camera.MediaType.PICTURE,
      targetHeight: 600,
      targetWidth: 800
    }
    const iWriteOptions: IWriteOptions = {
      replace: true
    }
    this.camera.getPicture(options).then(async (base64) => {
      let image = "data:image/jpeg;base64," + base64
      let imageBlob = await this.commonsEngineProvider.dataURItoBlob(image, this.commonsEngineProvider.getContentType(image))

      this.commonsEngineProvider.createFoldersInMobileDevice(this.formId, this.uniqueId, this.file, this.messageService).then((async d => {
        let currentTime = +new Date();
        let random = Math.random()
        let trends = random + ""
        let filename = trends.replace('.', '') + '_' + question.columnName + '_' + currentTime + ".jpg";

        let writeToFilePath = this.file.externalRootDirectory + ConstantProvider.appFolderName + "/" + this.formId + "/" + this.uniqueId;
        let writtenFile = await this.file.writeFile(writeToFilePath, filename, imageBlob, iWriteOptions)

        
        this.photos.push(writtenFile.nativeURL);
        this.questionMap[question.columnName].value.push(writtenFile.nativeURL)
        this.photos.reverse();
        
        this.messageService.stopLoader()
      })).catch(err => {
        this.messageService.stopLoader()
        this.messageService.showErrorToast(err)
      })



      // this.photos.push(imageData);
      // this.questionMap[question.columnName].value.push(imageData)
      // this.photos.reverse();
    }, (err) => {
      console.log(err);
    });
  }

  deletePhoto(question, index) {
    if (!this.disableStatus) {
      let confirm = this.alertCtrl.create({
        title: 'Warning',
        message: '<strong>Are sure you want to delete this photo?</strong>',
        buttons: [{
          text: 'No(ନାହିଁ)',
          handler: () => {
            console.log('Disagree clicked');
          }
        }, {
          text: 'Yes(ହଁ)',
          handler: () => {
            console.log('Agree clicked');
            this.questionMap[question.columnName].value.splice(index, 1);
          }
        }]
      });
      confirm.present();
    }
  }


  _handleReaderLoaded(readerEvt) {
    let binaryString = readerEvt.target.result;
    this.geolocation
      .getCurrentPosition(this.options)
      .then(resp => {
        this.questionMap[this.base64Image.columnName].value = {
          src: "data:image/jpeg;base64," + btoa(binaryString),
          meta_info: "Latitude :" + resp.coords.latitude + "; Longitude :" + resp.coords.longitude + "; Accuracy :" + resp.coords.accuracy
        }
      })
      .catch(error => {
        this.questionMap[this.base64Image.columnName].value = {
          src: "data:image/jpeg;base64," + btoa(binaryString)
        }
      });

  }


  async ngOnInit() {
    this.isWeb = this.applicationDetailsProvider.getPlatform().isWebPWA
    this.formId = this.navParams.get('formId')
    this.formTitle = this.navParams.get('formTitle')
    this.isNewFormEntry = this.navParams.get('isNew') ? true : false
    this.segment = this.navParams.get('segment') == 'save' ? true : false
    this.maxDate = this.datepipe.transform(new Date(), 'yyyy-MM-dd')
    if (!this.isWeb) {
      if (!(this.navParams.get('submission') == undefined)) {

        this.saveType = 'old'
        this.statusSaveMandatory = true;
        let tempData = await this.storage.get(ConstantProvider.dbKeyNames.form + "-" + this.userService.user.username);
        let tempSubmissions = tempData[this.formId as any]
        let uq = tempSubmissions
        let submission = tempSubmissions[(this.navParams.get("submission") as IDbFormModel).uniqueId as any] as any
        this.data = submission.formData
        // this.backButtonCheckData=submission.formData
        this.disableStatus = submission.formStatus == "save" || submission.formStatus == "rejected" ? false : true;
        this.uniqueId = submission.uniqueId;
        this.createdDate = submission.createdDate;
        this.updatedDate = submission.updatedDate;
        this.updatedTime = submission.updatedTime;

        this.disablePrimaryStatus = true;
        await this.loadQuestionBankIntoUI(this.data)
      } else {
        this.saveType = 'new'
        this.statusSaveMandatory = false;
        this.uniqueId = UUID.UUID();
        if (this.initialCreatedTime == null) {
          this.initialCreatedTime = this.datePipe.transform(new Date(), 'dd-MM-yyyy HH:mm:ss')
        }
        this.createdDate = this.initialCreatedTime;
        await this.questionService.getQuestionBank(this.formId, null, ConstantProvider.lastUpdatedDate).then((data) => {
          let formData = data
          // this.backButtonCheckData=formData
          this.loadQuestionBankIntoUI(formData)
        });
      }
    }
    this.checkSaveMandatory();
  }

  public myDatePickerOptions: IMyDpOptions = {
    // other options...
    dateFormat: 'dd-mm-yyyy',
    disableSince: {
      year: Number(this.datepipe.transform(new Date(), "yyyy-MM-dd").split('-')[0]),
      month: Number(this.datepipe.transform(new Date(), "yyyy-MM-dd").split('-')[1]),
      day: Number(this.datepipe.transform(new Date(), "yyyy-MM-dd").split('-')[2]) + 1
    },
    editableDateField: false,
    showTodayBtn: false,
    showClearDateBtn: false
  };

  /**
   * This method is for table data calculation.
   *
   * @author Azhar (azaruddin@sdrc.co.in)
   * @param cell
   * @param columnIndex
   * @param rowIndex
   * @param tableModel
   */
  loadQuestionBankIntoUI(data) {

    this.data = data;
    for (let index = 0; index < Object.keys(data).length; index++) {
      this.sectionMap.set(Object.keys(data)[index], data[Object.keys(data)[index]]);
      for (let j = 0; j < data[Object.keys(data)[index]].length; j++) {
        let subSections = data[Object.keys(data)[index]][0];
        // this.subSectionNames.set(""+Array.from(this.sectionMap.keys())[index],Object.keys(subSections)[0])
        for (let qs = 0; qs < Object.keys(subSections).length; qs++) {

          for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
            let question: IQuestionModel = subSections[Object.keys(subSections)[qs]][q];
            if (question.attachedFiles == null) question.attachedFiles = [];
            switch (question.controlType) {
              case "sub-score-keeper":
              case "score-keeper":
              case "score-holder":

                question.dependecy = false;
                //      question.displayComponent = (question.defaultSettings && question.defaultSettings.includes("hidden")) ? false : true 
                question.displayComponent = true
                this.questionMap[question.columnName] = question;
                break
              case "table":
              case "tableWithRowWiseArithmetic":
                question.displayComponent = true;
                for (let row = 0; row < question.tableModel.length; row++) {
                  for (let column = 0; column < Object.keys(question.tableModel[row]).length; column++) {
                    let value = question.tableModel[row][Object.keys(question.tableModel[row])[column]];
                    if (typeof value == "object") {
                      let cell = value;
                      if (cell.value != null) {
                        cell.value = String(cell.value)
                      }
                      cell.dependecy = cell.relevance != null ? true : false;
                      cell.displayComponent = cell.relevance == null ? true : false;
                      this.questionMap[cell.columnName] = cell;
                      // cell = this.setupDefaultSettingsAndConstraintsAndFeatureGraph(cell);
                      cell.relevance != null ? this.drawDependencyGraph(cell.relevance, cell) : null;
                      this.mandatoryQuestion[cell.columnName] = cell.finalizeMandatory;

                    }
                  }
                }
                break;
              case 'beginrepeat':
                question.displayComponent = true
                question.beginRepeatMinusDisable = false
                question.beginrepeatDisableStatus = false
                question.tempFinalizedMandatory = question.finalizeMandatory;
                question.tempSaveMandatory = question.saveMandatory;
                this.repeatSubSection.set(question.key, question)
                question.beginRepeatMinusDisable = false
                // if (question.beginRepeat.length == 1) {
                //   question.beginRepeatMinusDisable = true
                // }
                this.questionMap[question.columnName] = question;
                for (let index = 0; index < question.beginRepeat.length; index++) {
                  let beginRepeatQuestions: IQuestionModel[] = question.beginRepeat[index];
                  for (let beginRepeatQuestion of beginRepeatQuestions) {
                    beginRepeatQuestion.dependecy = beginRepeatQuestion.relevance != null ? true : false;
                    beginRepeatQuestion.displayComponent = beginRepeatQuestion.relevance == null ? true : false;
                    this.questionMap[beginRepeatQuestion.columnName] = beginRepeatQuestion;
                    beginRepeatQuestion.relevance != null ? this.drawDependencyGraph(beginRepeatQuestion.relevance, beginRepeatQuestion) : null;
                    this.mandatoryQuestion[beginRepeatQuestion.columnName] = beginRepeatQuestion.finalizeMandatory;
                    beginRepeatQuestion.tempFinalizedMandatory = beginRepeatQuestion.finalizeMandatory;
                    beginRepeatQuestion.tempSaveMandatory = beginRepeatQuestion.saveMandatory;

                  }
                }
                break;
              case 'camera':
                if (question.attachmentsInBase64) {
                  let index = 0;
                  for (let fpath of question.attachmentsInBase64) {
                    this.photos.push(fpath);
                    question.value[index] = fpath
                    question.attachmentsInBase64 = null
                    index++;
                  }
                  this.photos.reverse();
                }

                question.dependecy = question.relevance != null ? true : false;
                question.displayComponent = question.relevance == null ? true : false;
                question.tempFinalizedMandatory = question.finalizeMandatory;
                question.tempSaveMandatory = question.saveMandatory;
                this.questionMap[question.columnName] = question;
                question.relevance != null ? this.drawDependencyGraph(question.relevance, question) : null;
                this.mandatoryQuestion[question.columnName] = question.finalizeMandatory;


                break;
              case "dropdown":
              case "textbox":
              case "Time Widget":
              case "cell":
              case "sub-score-keeper":
              case "score-keeper":
              case "uuid":
              case "file":
              case "mfile":
              case 'geolocation':
              case 'heading':
              case 'segment':
              case 'textarea':
                if (question.controlType == 'textbox' && question.type == 'tel' && question.value != null) {
                  question.value = String(question.value)
                }
                question.dependecy = question.relevance != null ? true : false;
                question.displayComponent = question.relevance == null ? true : false;
                question.tempFinalizedMandatory = question.finalizeMandatory;
                question.tempSaveMandatory = question.saveMandatory;
                this.questionMap[question.columnName] = question;
                question.relevance != null ? this.drawDependencyGraph(question.relevance, question) : null;
                this.mandatoryQuestion[question.columnName] = question.finalizeMandatory;
                // if(question.columnName =='F1QN2'){                  
                //   console.log(" finss1 "+question.label,question.finalizeMandatory,question.tempFinalizedMandatory);
                // }  
                // if(question.columnName=='F1QN2'){                  
                //   console.log(" finta "+question.label,question.finalizeMandatory,question.tempFinalizedMandatory);

                // }


                break;
              case "Date Widget":
                question.dependecy = question.relevance != null ? true : false;
                question.displayComponent = question.relevance == null ? true : false;
                question.tempFinalizedMandatory = question.finalizeMandatory;
                question.tempSaveMandatory = question.saveMandatory;
                this.questionMap[question.columnName] = question;
                this.mandatoryQuestion[question.columnName] = question.finalizeMandatory;
                question.relevance != null ? this.drawDependencyGraph(question.relevance, question) : null;

                break;
              case "checkbox":
                question.dependecy = question.relevance != null ? true : false;
                question.displayComponent = question.relevance == null ? true : false;
                question.tempFinalizedMandatory = question.finalizeMandatory;
                question.tempSaveMandatory = question.saveMandatory;
                this.questionMap[question.columnName] = question;
                this.mandatoryQuestion[question.columnName] = question.finalizeMandatory;
                question.relevance != null ? this.drawDependencyGraph(question.relevance, question) : null;

                break;
            }
            //set the default
          }
        }
      }
    }


    for (let q of Object.keys(this.questionMap)) {
      let ques = this.questionMap[q]
      this.setupDefaultSettingsAndConstraintsAndFeatureGraph(ques)
      // if(ques.columnName =='F1QN2'){                  
      //   console.log(" finss2 "+ques.label,ques.finalizeMandatory,ques.tempFinalizedMandatory);
      // }  
    }


    for (let questionKey of this.dataSharingService.getKeys(this.beginRepeatArray)) {
      let bgQuestion = this.beginRepeatArray[questionKey];
      if (this.questionMap[questionKey].value == null || this.questionMap[questionKey].value == 0 || this.questionMap[questionKey].value == "" || this.questionMap[questionKey].value == NaN) {
        bgQuestion.beginrepeatDisableStatus = true;
      }
    }
    this.checkRelevanceForEachQuestion()

    // console.log(this.questionMap);
    // console.log("features array: ", this.questionFeaturesArray);
    // console.log("dependencies array: ", this.questionDependencyArray);
    // console.log("beginRepeatArray", this.beginRepeatArray);
    // console.log("repeat Subsection", this.repeatSubSection);
    // console.log('constraints array', this.constraintsArray)
    this.sectionNames = Array.from(this.sectionMap.keys());
    this.section = this.sectionNames[0];
    this.sectionSelected(this.section);

    for (let q of Object.keys(this.questionMap)) {
      let ques = this.questionMap[q]

      // if(ques.columnName =='F1QN2'){                  
      //   console.log(" finss3 "+ques.label,ques.finalizeMandatory,ques.tempFinalizedMandatory);
      // }  
    }
  }
  //Biswa
  tempCaryId: any;
  sectionSelected(sectionHeading: any) {
    this.tempCaryId = null;
    this.content.scrollToTop(300);
    this.sectionHeading = sectionHeading;
    this.subSections = this.sectionMap.get(sectionHeading);
  }

  /**
   * This method is for table data calculation.
   *
   * @author Azhar (azaruddin@sdrc.co.in)
   * @param cell
   * @param columnIndex
   * @param rowIndex
   * @param tableModel
   */
  addAnotherWorker(key: Number) {
    let beginRepeatParent: IQuestionModel = this.repeatSubSection.get(key);
    if (beginRepeatParent.beginRepeatSize == undefined) {
      beginRepeatParent.beginRepeatSize = beginRepeatParent.beginRepeat.length - 1
    }
    let beginRepeatQuestionList: IQuestionModel[] = beginRepeatParent.beginRepeat[beginRepeatParent.beginRepeat.length - 1];
    let size = beginRepeatParent.beginRepeatSize + 1;
    let clonedQuestion: IQuestionModel[];
    clonedQuestion = JSON.parse(JSON.stringify(beginRepeatQuestionList));

    for (let index = 0; index < clonedQuestion.length; index++) {
      //if dependent question is inside begin repeat section,
      // we have rename dependent column name as we have renamed depending question column name
      let colName = (clonedQuestion[index].columnName as String).split("-")[3];
      let colIndex = (clonedQuestion[index].columnName as String).split("-")[2];

      clonedQuestion[index].value = null;
      clonedQuestion[index].othersValue = false;
      clonedQuestion[index].isOthersSelected = false;
      clonedQuestion[index].dependecy = clonedQuestion[index].relevance != null ? true : false;
      clonedQuestion[index].columnName = beginRepeatParent.columnName + "-" + size + "-" + colIndex + "-" + colName;
      clonedQuestion[index] = this.commonsEngineProvider.renameRelevanceAndFeaturesAndConstraintsAndScoreExpression(clonedQuestion[index], this.questionMap, beginRepeatParent, size);

      //setting up default setting and added to dependency array and feature array
      clonedQuestion[index].displayComponent = clonedQuestion[index].relevance == null ? true : false;
      this.questionMap[clonedQuestion[index].columnName] = clonedQuestion[index];
    }

    for (let index = 0; index < clonedQuestion.length; index++) {
      clonedQuestion[index] = this.setupDefaultSettingsAndConstraintsAndFeatureGraph(clonedQuestion[index]);
      clonedQuestion[index].relevance != null ? this.drawDependencyGraph(clonedQuestion[index].relevance, clonedQuestion[index]) : null;

    }

    this.checkRelevanceForEachQuestion()
    if (beginRepeatParent.limit_bg_repeat) {
      if (this.questionMap[beginRepeatParent.bgDependentColumn as any].value != null) {
        if (beginRepeatParent.beginRepeat.length < this.questionMap[beginRepeatParent.bgDependentColumn as any].value) {
          beginRepeatParent.beginRepeat.push(clonedQuestion);
        } else {
          this.messageService.showErrorToast("Exceed Size");
        }
      } else {
        this.sectionSelected(Object.keys(this.data)[0]);
        this.messageService.showErrorToast("Please enter " + this.questionMap[beginRepeatParent.bgDependentColumn as any].label);
      }
    } else {
      beginRepeatParent.beginRepeat.push(clonedQuestion);
    }
    if (beginRepeatParent.beginRepeat.length > 1) {
      beginRepeatParent.beginRepeatMinusDisable = false;
    }
    beginRepeatParent.beginRepeatSize = beginRepeatParent.beginRepeatSize + 1
  }
  checkRelevanceForEachQuestion() {
    for (let questionKey of this.dataSharingService.getKeys(this.questionMap)) {
      let question = this.questionMap[questionKey];

      switch (question.controlType) {
        case "sub-score-keeper":
        case "score-keeper":
        case "score-holder":
          question.scoreExp != null ? this.drawScoreDependencyGraph(question) : null;
          break;
        case "dropdown":
        case "segment":
          if (question.groupType == "area_group" || question.groupType == "filter_single" || question.groupType == "filter_multiple") {
            if (question.groupQuestions != null) {
              let childLevelQuestion = this.questionMap[question.groupQuestions];
              let optionCount = 0;
              for (let option of childLevelQuestion.options) {
                if (option["parentId"] == question.value) {
                  option["visible"] = true;
                } else {
                  option["visible"] = false;
                  optionCount++;
                }
              }
              if (optionCount == childLevelQuestion.options.length) {
                childLevelQuestion.constraints = "disabled";
              } else {
                childLevelQuestion.constraints = "";
              }
            }
          }
          this.checkRelevance(question)
          break;
        case "table":
        case "tableWithRowWiseArithmetic":
          question.displayComponent = true;
          for (let row = 0; row < question.tableModel.length; row++) {
            for (let column = 0; column < Object.keys(question.tableModel[row]).length; column++) {
              let value = question.tableModel[row][Object.keys(question.tableModel[row])[column]];
              if (typeof value == "object") {
                let cell = value;
                this.checkRelevance(cell)
              }
            }
          }
          break;
        case 'beginrepeat':
          for (let index = 0; index < question.beginRepeat.length; index++) {
            let beginRepeatQuestions: IQuestionModel[] = question.beginRepeat[index];
            for (let beginRepeatQuestion of beginRepeatQuestions) {
              this.checkRelevance(beginRepeatQuestion)
            }
          }
          break;
        default:
          this.checkRelevance(question)
          break;
      }
    }
  }
  /**
   * This method is for table data calculation.
   *
   * @author Azhar (azaruddin@sdrc.co.in)
   * @param cell
   * @param columnIndex
   * @param rowIndex
   * @param tableModel
   */
  deleteLastWorker(key: Number, bgquestion: IQuestionModel, num?: number) {
    let confirm = this.alertCtrl.create({
      enableBackdropDismiss: false,
      title: 'Warning',
      message: "</strong>Are you sure you want to delete?</strong>",
      buttons: [{
        text: 'No',
        handler: () => { }
      },
      {
        text: 'Yes',
        handler: () => {
          this.deleteLastWorkerConfirmed(key, bgquestion, num);
        }
      }
      ]
    });
    confirm.present();

  }

  deleteLastWorkerConfirmed(key: Number, bgquestion: IQuestionModel, num: number) {
    let beginRepeatParent: IQuestionModel = this.repeatSubSection.get(key);
    let clonedQuestion: IQuestionModel[] = beginRepeatParent.beginRepeat[beginRepeatParent.beginRepeat.length - 1];
    for (let index = 0; index < clonedQuestion.length; index++) {
      clonedQuestion[index].relevance != null ? this.removeFromDependencyGraph(clonedQuestion[index].relevance, clonedQuestion[index]) : null
    }

    if (bgquestion.beginRepeat.length > 1) {
      if (num != undefined || num != null) {
        bgquestion.beginRepeat.splice(num, 1)
      }
      else {
        bgquestion.beginRepeat.pop();
      }

      if (bgquestion.beginRepeat.length == 1) {
        bgquestion.beginRepeatMinusDisable = true;
      } else {
        bgquestion.beginRepeatMinusDisable = false;
      }
    } else {
      for (let i = 0; i < bgquestion.beginRepeat.length; i++) {
        for (let j = 0; j < bgquestion.beginRepeat[i].length; j++) {
          bgquestion.beginRepeat[i][j].value = null;
        }
      }
    }
  }
  removeFromDependencyGraph(expression: String, question: IQuestionModel) {
    if (this.questionMap[question.parentColumnName] && this.questionMap[question.parentColumnName].controlType == 'beginrepeat') {
      delete this.questionDependencyArray[question.columnName]
      return
    }
    for (let str of expression.split("}")) {
      let expressions: String[] = str.split(":");
      for (let i = 0; i < expressions.length; i++) {
        let exp: String = expressions[i];
        switch (exp) {
          case "optionEquals":
          case "optionEqualsMultiple":
            {
              let dColName: any = expressions[i - 1];
              if (question.dependecy && this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label] == undefined) {
                this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label] = [this.questionMap[question.columnName]];
              } else if (question.dependecy == true) {
                let a = this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label];
                let keyFoundIndex = -1
                for (let i = 0; a.length; i++) {
                  if (a[i].columnName == question.columnName) {
                    // remove the object from dependent key. if array size is 1, remove the key itself
                    if (a.length == 1) {
                      delete this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label]
                    } else {
                      keyFoundIndex = i
                      break
                    }
                  }

                }
                if (keyFoundIndex > -1) {
                  a.splice(keyFoundIndex, 1)
                }

              }
              i = i + 2;
            }
            break;
          case "textEquals":
          case "equals":
          case "greaterThan":
          case "greaterThanEquals":
          case "lessThan":
          case "lessThanEquals":
            {
              let dColName: any = expressions[i - 1];
              if (question.dependecy && this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label] == undefined) {
                this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label] = [this.questionMap[question.columnName]];
              } else if (question.dependecy == true) {
                let a = this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label];
                let keyFound = false
                for (let dps of a) {
                  if (dps.columnName == question.columnName)
                    keyFound = true
                }
                if (!keyFound) {
                  a.push(this.questionMap[question.columnName]);
                  this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label] = a;

                }
              }
              i = i + 1;
            }
            break;
        }
      }
    }
  }

  async onSave(type: String) {
    let formId;
    // let headerData: Map<string, string | number | any[]> = new Map();
    let headerData: {} = {}
    let image: string = ConstantProvider.defaultImage;


    if (type == 'save') {
      this.errorStatus = false;
      loop1: for (let index = 0; index < Object.keys(this.data).length; index++) {
        this.sectionMap.set(Object.keys(this.data)[index], this.data[Object.keys(this.data)[index]])
        for (let j = 0; j < this.data[Object.keys(this.data)[index]].length; j++) {
          let subSections = this.data[Object.keys(this.data)[index]][0]
          for (let qs = 0; qs < Object.keys(subSections).length; qs++) {
            for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
              let question: IQuestionModel = subSections[Object.keys(subSections)[qs]][q]
              formId = question.formId
              
              if (question.tempSaveMandatory == true) {
                switch (question.controlType) {
                  case "geolocation":
                    if (question.value == null || question.value == "") {
                      this.errorStatus = true;
                      // this.errorColor(question.columnName);
                      this.errorColor('geoLocz');
                      // this.messageService.showErrorToast("Please enter a valid value for " +"'"+ question.label+"'");
                      this.messageService.showErrorToast(ConstantProvider.message.commonSaveErrorMsg)
                      break loop1;
                    }
                    break;

                  case "camera":
                    if (question.value == null || question.value == "") {
                      this.errorStatus = true;
                      this.errorColor(question.columnName);
                      //  this.messageService.showErrorToast("Please enter a valid value for " +"'"+ question.label+"'");
                      this.messageService.showErrorToast(ConstantProvider.message.commonSaveErrorMsg)
                      break loop1;
                    }
                    break;
                  case 'dropdown':
                    for (let i = 0; i < question.options.length; i++) {
                      if (question.value == null || question.value == "") {
                        this.errorStatus = true;
                        this.errorColor(question.columnName);
                        // this.messageService.showErrorToast("Please select " + question.label)
                        this.messageService.showErrorToast(ConstantProvider.message.commonSaveErrorMsg)
                        break loop1
                      }
                    }
                    break;
                  case 'textbox':
                    if (question.value == null || question.value == "") {
                      this.errorStatus = true;
                      this.errorColor(question.columnName);
                      // this.messageService.showErrorToast("Please select " + question.label)
                      this.messageService.showErrorToast(ConstantProvider.message.commonSaveErrorMsg)
                      setTimeout(() => {
                        $(document.getElementById(question.columnName + "")).children(":first").focus();
                      }, 1000)

                      break loop1
                    }
                    break;
                  case "Date Widget":
                    if (question.value == null || question.value == "") {
                      this.errorStatus = true;
                      this.errorColor(question.columnName);
                      // this.messageService.showErrorToast("Please select " + question.label);
                      this.messageService.showErrorToast(ConstantProvider.message.commonSaveErrorMsg)
                      break loop1;
                    }
                    break;
                  case "Time Widget":
                    if (question.value == null || question.value == "") {
                      this.errorStatus = true;
                      this.errorColor(question.columnName);
                      // this.messageService.showErrorToast("Please select " + question.label);
                      this.messageService.showErrorToast(ConstantProvider.message.commonSaveErrorMsg)
                      break loop1;
                    }
                    break;
                  case 'checkbox':
                    if (question.value == null || question.value == "") {
                      this.errorStatus = true;
                      // this.messageService.showErrorToast("Please enter a valid value for " +"'"+ question.label+"'");
                      this.messageService.showErrorToast(ConstantProvider.message.commonSaveErrorMsg)
                      break loop1
                    }
                    break;
                  case "segment":
                    if (question.value == null || question.value == "") {
                      this.errorStatus = true;
                      this.errorColor(question.columnName);
                      // this.messageService.showErrorToast("Please select " + question.label);
                      this.messageService.showErrorToast(ConstantProvider.message.commonSaveErrorMsg)
                      break loop1;
                    }
                    break;
                  case 'tableWithRowWiseArithmetic':
                    let tableData = question.tableModel
                    for (let i = 0; i < tableData.length; i++) {
                      for (let j = 0; j < Object.keys(tableData[i]).length; j++) {
                        let cell = (tableData[i])[Object.keys(tableData[i])[j]]
                        if (typeof cell == 'object') {
                          if (cell.dependecy == false && cell.mandatory == 'yes' && (cell.value == null || (cell.value as string).trim() == "")) {
                            this.errorStatus = true;
                            this.errorColor(cell.columnName);
                            // this.messageService.showErrorToast("Please enter a valid value for "  +"'"+  question.label+"'" + " " + (cell.label).replace('@@split@@', ''))
                            this.messageService.showErrorToast(ConstantProvider.message.commonSaveErrorMsg)
                            break loop1;
                          } else if (cell.dependecy == true && cell.mandatory == 'yes' && cell.typeDetailIdOfDependencyType == this.questionMap[cell.dependentColumn.split(',')[0] as any].value &&
                            (cell.value == null || (cell.value as string).trim() == "")) {
                            this.errorStatus = true;
                            this.errorColor(cell.columnName);
                            // this.messageService.showErrorToast("Please enter a valid value for "  +"'"+  question.label+"'" + " " + (cell.label).replace('@@split@@', ''))
                            this.messageService.showErrorToast(ConstantProvider.message.commonSaveErrorMsg)
                            break loop1;
                          }
                        }
                      }
                    }
                    break;
                  case 'textarea':
                    if (question.value == null || question.value == "") {
                      this.errorStatus = true;
                      this.errorColor(question.columnName);
                      // this.messageService.showErrorToast("Please enter a valid value for "  +"'"+  question.label+"'")
                      this.messageService.showErrorToast(ConstantProvider.message.commonSaveErrorMsg)
                      setTimeout(() => {
                        $(document.getElementById(question.columnName + "")).children(":first").focus();
                      }, 1000)

                      break loop1
                    }
                    break;
                }

              }
              if (question.columnName == 'F1203' || question.columnName == 'F603') {
                headerData['ext_IFA supply point'] = question.options && question.options.length ? question.options.filter(d => d.key === question.value)[0].value : question.value;
              }
              if (question.reviewHeader) {
                // headerData['ext_Created Date']=this.createdDate
                switch (question.controlType) {
                  case 'dropdown':
                  case 'segment':
                    if (question.value == null || question.value == "" || question.value == undefined) {
                      headerData[question.reviewHeader] = "";
                      break;
                    }
                    if (question.type == "option") {
                      headerData[question.reviewHeader] = question.options && question.options.length ?
                        question.options.filter(d => d.key === question.value)[0].value : question.value;
                    } else {
                      // for checkbox
                      let names = "";
                      let vals = question.options.filter(d => question.value.find(element => {
                        return (d.key === element)
                      }))
                      vals.forEach(e => {
                        names = names + e.value + ","
                      })
                      if (names.includes(",")) {
                        names = names.substring(0, names.lastIndexOf(","))
                      }
                      headerData[question.reviewHeader] = names;
                    }
                    break;
                  case 'textbox':
                    headerData[question.reviewHeader] = question.value;
                    // headerData['L2_createdDate']=this.createdDate
                    break;
                  case 'camera':
                    headerData[question.reviewHeader] = question.value.src;
                    image = question.value.src;
                    // headerData['L2_createdDate']=this.createdDate
                    break;
                }
              }
             
            }
          }
        }
      }
    }
    if (type == 'finalized') {
      this.messageService.showLoader("Please wait...")
      this.errorStatus = false;
      loop1: for (let index = 0; index < Object.keys(this.data).length; index++) {
        this.sectionMap.set(Object.keys(this.data)[index], this.data[Object.keys(this.data)[index]])
        for (let j = 0; j < this.data[Object.keys(this.data)[index]].length; j++) {
          let subSections = this.data[Object.keys(this.data)[index]][0]


          for (let qs = 0; qs < Object.keys(subSections).length; qs++) {
            for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
              let question: IQuestionModel = subSections[Object.keys(subSections)[qs]][q]
              formId = question.formId
             
              if (question.tempFinalizedMandatory == true) {

                this.removeColor(question.columnName)

                switch (question.controlType) {
                  case "geolocation":
                    if (question.value == null || question.value == "") {
                      this.errorStatus = true;
                      // this.errorColor(question.columnName);
                      this.errorColor(question.columnName);
                      // this.messageService.showErrorToast("Please enter a valid value for "  +"'"+  question.label+"'")
                      this.messageService.showErrorToast(ConstantProvider.message.commonFinalizeErrorMsg)
                      break loop1;
                    }
                    break;

                  case "camera":
                    if (question.value == null || question.value == "") {
                      this.errorStatus = true;
                      this.errorColor(question.columnName);
                      // this.messageService.showErrorToast("Please enter a valid value for "  +"'"+  question.label+"'")
                      this.messageService.showErrorToast(ConstantProvider.message.commonFinalizeErrorMsg)
                      break loop1;
                    }
                    break;

                  case 'dropdown':
                    if (question.options.length == 0) {
                      this.errorStatus = true;
                      this.messageService.showErrorToast("Please select " + question.label)
                      break loop1;
                    } else {

                      for (let i = 0; i < question.options.length; i++) {

                        if (question.value == null || question.value == "") {
                          this.errorStatus = true;
                          this.errorColor(question.columnName);
                          // this.messageService.showErrorToast("Please select " + question.label)
                          this.messageService.showErrorToast(ConstantProvider.message.commonFinalizeErrorMsg)
                          break loop1
                        }

                      }
                    }

                    break;
                  case 'textbox':
                    if (question.constraints != null) {
                      this.checkConstraints(question)
                    }
                    // alert(question.label+"++++"+question.value)
                    if (question.value == null || question.value == "") {
                      this.errorStatus = true;
                      this.errorColor(question.columnName);
                      // this.messageService.showErrorToast("Please enter a valid value for "  +"'"+  question.label+"'")
                      this.messageService.showErrorToast(ConstantProvider.message.commonFinalizeErrorMsg)
                      setTimeout(() => {
                        $(document.getElementById(question.columnName + "")).children(":first").focus();
                      }, 1000)

                      break loop1
                    } else if (question.showErrMessage === true) {
                      question.showErrMessage = false
                      this.errorStatus = true
                      // alert(question.label+"1111")
                      this.errorColor(question.columnName);
                      break loop1;

                    }
                    // alert("before"+this.testErrMsgStatus)
                    this.validateBeginRepeatMsg(question)
                    // alert("after"+this.testErrMsgStatus)
                    if (this.testErrMsgStatus == true) {
                      this.testErrMsgStatus = false;
                      this.errorStatus = true
                      // alert(question.label+"2222")
                      this.errorColor(question.columnName);
                      break loop1;


                    }
                    break;
                  case 'Time Widget':
                    if (question.value == null || question.value == "") {
                      this.errorStatus = true;
                      this.errorColor(question.columnName);
                      // this.messageService.showErrorToast("Please select " + question.label)
                      this.messageService.showErrorToast(ConstantProvider.message.commonFinalizeErrorMsg)
                      break loop1
                    }
                    break;
                  case 'Date Widget':
                    if (question.value == null || question.value == "") {
                      this.errorStatus = true;
                      this.errorColor(question.columnName);
                      // this.messageService.showErrorToast("Please select " + question.label)
                      this.messageService.showErrorToast(ConstantProvider.message.commonFinalizeErrorMsg)
                      break loop1
                    }
                    break;
                  case 'checkbox':
                    if (question.value == null || question.value == "") {
                      this.errorStatus = true;
                      this.errorColor(question.columnName);
                      // this.messageService.showErrorToast("Please enter a valid value for "  +"'"+  question.label+"'")
                      this.messageService.showErrorToast(ConstantProvider.message.commonFinalizeErrorMsg)
                      break loop1
                    }
                    break;
                  case "segment":
                    if (question.value == null || question.value == "") {
                      this.errorStatus = true;
                      this.errorColor(question.columnName);
                      // this.messageService.showErrorToast("Please select " + question.label);
                      this.messageService.showErrorToast(ConstantProvider.message.commonFinalizeErrorMsg)
                      break loop1;
                    }
                    break;
                  case 'tableWithRowWiseArithmetic':
                    let tableData = question.tableModel
                    for (let i = 0; i < tableData.length; i++) {
                      for (let j = 0; j < Object.keys(tableData[i]).length; j++) {
                        let cell = (tableData[i])[Object.keys(tableData[i])[j]]
                        if (typeof cell == 'object') {
                          this.removeColor(cell.columnName)
                          if (cell.dependecy == false && cell.mandatory == 'yes' && (cell.value == null || (cell.value as string).trim() == "")) {
                            this.errorStatus = true;
                            this.errorColor(cell.columnName);
                            // this.messageService.showErrorToast("Please enter a valid value for " +"'"+question.label +"'"+ " " + (cell.label).replace('@@split@@', ''))
                            this.messageService.showErrorToast(ConstantProvider.message.commonFinalizeErrorMsg)
                            break loop1;
                          } else if (cell.dependecy == true && cell.mandatory == 'yes' && cell.typeDetailIdOfDependencyType == this.questionMap[cell.dependentColumn.split(',')[0] as any].value &&
                            (cell.value == null || (cell.value as string).trim() == "")) {
                            this.errorStatus = true;
                            this.errorColor(cell.columnName);
                            // this.messageService.showErrorToast("Please enter a valid value for " +"'"+question.label +"'"+ " " + (cell.label).replace('@@split@@', ''))
                            this.messageService.showErrorToast(ConstantProvider.message.commonFinalizeErrorMsg)
                            break loop1;
                          }
                        }
                      }
                    }
                    break;
                  case 'textarea':
                    if (question.value == null || question.value == "") {
                      this.errorStatus = true;
                      this.errorColor(question.columnName);
                      // this.messageService.showErrorToast("Please enter a valid value for "  +"'"+  question.label+"'")
                      this.messageService.showErrorToast(ConstantProvider.message.commonFinalizeErrorMsg)
                      break loop1
                    }
                    break;
                  case 'beginrepeat':
                    let questionQolumnName: Number;
                    if (Number(String(this.formId).split("_")[0]) == 4) {
                      questionQolumnName = this.questionMap['F4QT1'].value
                    } else if (Number(String(this.formId).split("_")[0]) == 7) {
                      questionQolumnName = this.questionMap['F7Q18'].value
                    }

                    if (question.beginrepeatDisableStatus == false)
                      if (questionQolumnName == question.beginRepeat.length) {
                        for (let bgindex = 0; bgindex < question.beginRepeat.length; bgindex++) {
                          let beginRepeatQuestions: IQuestionModel[] = question.beginRepeat[bgindex];

                          for (let beginRepeatQuestion of beginRepeatQuestions) {
                            if (beginRepeatQuestion.type == 'number') {

                              beginRepeatQuestion.value = Number(beginRepeatQuestion.value);
                              console.log(beginRepeatQuestion);
                              
                            }

                            // if(beginRepeatQuestion.tempFinalizedMandatory)

                            this.removeColor(beginRepeatQuestion.columnName)
                            if (beginRepeatQuestion.tempFinalizedMandatory && (beginRepeatQuestion.value == null || beginRepeatQuestion.value == '')) {
                              // if (this.questionDependencyArray[question.columnName + ":" + question.key + ":" + question.controlType + ":" + question.label] != null){
                              //   this.checkRelevance(beginRepeatQuestion);
                              // }

                              // if(beginRepeatQuestion.tempFinalizedMandatory || !beginRepeatQuestion.beginrepeatDisableStatus){

                              this.errorStatus = true
                              this.errorColor(beginRepeatQuestion.columnName);
                              // this.messageService.showErrorToast("Please select " + beginRepeatQuestion.label)
                              this.messageService.showErrorToast(ConstantProvider.message.commonFinalizeErrorMsg)
                              setTimeout(() => {
                                $(document.getElementById(question.columnName + "")).children(":first").focus();
                              }, 1000)
                              break loop1
                              // }
                              // if(!beginRepeatQuestion.beginrepeatDisableStatus)

                            }

                          }
                        }
                      } else {
                        this.beginReapetAlertStatus = true;
                        this.errorStatus = true
                        break loop1
                      }

                    break;
                }



              }
              if (question.columnName == 'F1203' || question.columnName == 'F603') {
                headerData['ext_IFA supply point'] = question.options && question.options.length ? question.options.filter(d => d.key === question.value)[0].value : question.value;
              }
              if (question.reviewHeader) {
                // headerData['ext_Created Date']=this.createdDate
                switch (question.controlType) {
                  case 'dropdown':
                  case 'segment':
                    if (question.value == null || question.value == "" || question.value == undefined) {
                      headerData[question.reviewHeader] = "";
                      break;
                    }
                    if (question.type == "option") {
                      headerData[question.reviewHeader] = question.options && question.options.length ?
                        question.options.filter(d => d.key === question.value)[0].value : question.value;
                    } else {
                      // for checkbox
                      let names = "";
                      let vals = question.options.filter(d => question.value.find(element => {
                        return (d.key === element)
                      }))
                      vals.forEach(e => {
                        names = names + e.value + ","
                      })
                      if (names.includes(",")) {
                        names = names.substring(0, names.lastIndexOf(","))
                      }
                      headerData[question.reviewHeader] = names;
                    }
                    break;
                  case 'textbox':
                    headerData[question.reviewHeader] = question.value;
                    // headerData['L2_createdDate']=this.createdDate 
                    break;
                  case 'camera':
                    headerData[question.reviewHeader] = question.value.src;
                    image = question.value.src;
                    // headerData['L2_createdDate']=this.createdDate
                    break;
                }
              }
              
            }
          }
        }
      }

      setTimeout(() => {

      }, 1000);
      setTimeout(() => {
        if (!this.errorStatus) {
          this.messageService.stopLoader()
          this.dbFormModel = {
            createdDate: this.createdDate,
            updatedDate: this.updatedDate,
            updatedTime: this.updatedTime,
            formStatus: type == 'save' ? 'save' : 'finalized',
            extraKeys: null,
            formData: this.data,
            formSubmissionId: formId,
            uniqueId: this.uniqueId,
            formDataHead: headerData,
            image: image,
            attachmentCount: 0
          }

          if (type == 'finalized') {
            let confirm = this.alertCtrl.create({
              enableBackdropDismiss: false,
              title: 'Warning',
              // message: "Do you want to finalize this data? ",
              message: "Once you finalize the form, it can't be further edited.<br><br><strong>Do you want to finalize this form?</strong>",
              buttons: [{
                text: 'No',
                handler: () => {
                  // this.navCtrl.pop()
                }
              },
              {
                text: 'Yes',
                handler: () => {

                  this.formService.saveData(this.formId, this.dbFormModel, this.saveType).then(data => {
                    if (data == 'data') {
                      this.navCtrl.pop()
                      this.messageService.showSuccessToast(ConstantProvider.message.finalizedSuccess)

                    } else {
                      this.navCtrl.pop()
                    }
                  });

                }
              }
              ]
            });
            confirm.present();


          } else {
            this.formService.saveData(this.formId, this.dbFormModel, this.saveType).then(data => {
              if (data == 'data') {
                this.navCtrl.pop()
                if (type == 'save') {
                  this.messageService.showSuccessToast(ConstantProvider.message.saveSuccess)
                } else {
                  this.messageService.showSuccessToast(ConstantProvider.message.finalizedSuccess)
                }
              } else {
                this.navCtrl.pop()
              }
            });

          }


          // await this.formService.saveData(this.formId, this.dbFormModel, this.saveType).then(data => {
          //   if (data == 'data') {
          //     this.navCtrl.pop()
          //     if (type == 'save') {
          //       this.statusSaveMandatory=true;
          //       this.messageService.showSuccessToast(ConstantProvider.message.saveSuccess)
          //     } else {
          //       this.statusSaveMandatory=true;
          //       this.messageService.showSuccessToast(ConstantProvider.message.finalizedSuccess)
          //     }
          //   } else {
          //     this.navCtrl.pop()
          //   }
          // });
        } else {
          this.messageService.stopLoader()
          if (Number(String(this.formId).split("_")[0]) == 4) {
            if (this.beginReapetAlertStatus) {
              let confirm = this.alertCtrl.create({
                enableBackdropDismiss: false,
                title: 'Warning',
                message: '"Total number of women tested" does not match with number of hemocue test details entered',
                buttons: [
                  {
                    text: "Ok",
                    handler: () => {
                      setTimeout(() => {
                        this.beginReapetAlertStatus = false;
                      }, 20);
                    }
                  }
                ]
              });
              confirm.present();
            }
          } else if (Number(String(this.formId).split("_")[0]) == 7) {
            if (this.beginReapetAlertStatus) {
              let confirm = this.alertCtrl.create({
                enableBackdropDismiss: false,
                title: 'Warning',
                message: '"Total number of T4 Approach Session" does not match with number of people attended todays session',
                buttons: [
                  {
                    text: "Ok",
                    handler: () => {
                      setTimeout(() => {
                        this.beginReapetAlertStatus = false;
                      }, 20);
                    }
                  }
                ]
              });
              confirm.present();
            }
          }
        }
      }, 1000);
    } else {
      if (!this.errorStatus) {
        this.dbFormModel = {
          createdDate: this.createdDate,
          updatedDate: this.updatedDate,
          updatedTime: this.updatedTime,
          formStatus: type == 'save' ? 'save' : 'finalized',
          extraKeys: null,
          formData: this.data,
          formSubmissionId: formId,
          uniqueId: this.uniqueId,
          formDataHead: headerData,
          image: image,
          attachmentCount: 0
        }
        if (type == 'finalized') {
          let confirm = this.alertCtrl.create({
            enableBackdropDismiss: false,
            title: 'Warning',
            // message: "Do you want to finalize this data? ",
            message: "Once you Finalize the form, it can't be further edited.<br><br><strong>Do you want to finalize this form?</strong>",

            buttons: [{
              text: 'No',
              handler: () => {
                // this.navCtrl.pop()
              }
            },
            {
              text: 'Yes',
              handler: () => {

                this.formService.saveData(this.formId, this.dbFormModel, this.saveType).then(data => {
                  if (data == 'data') {
                    this.navCtrl.pop()
                    this.messageService.showSuccessToast(ConstantProvider.message.finalizedSuccess)

                  } else {
                    this.navCtrl.pop()
                  }
                });

              }
            }
            ]
          });
          confirm.present();


        } else {
          await this.formService.saveData(this.formId, this.dbFormModel, this.saveType).then(data => {
            if (data == 'data') {
              this.navCtrl.pop()
              if (type == 'save') {
                this.messageService.showSuccessToast(ConstantProvider.message.saveSuccess)
              } else {
                this.messageService.showSuccessToast(ConstantProvider.message.finalizedSuccess)
              }
            } else {
              this.navCtrl.pop()
            }
          });

        }

      }
    }
  }

  removeEmojis(tempStr: string) {
    var regex = /(?:[\u2700-\u27bf]|(?:\ud83c[\udde6-\uddff]){2}|[\ud800-\udbff][\udc00-\udfff]|[\u0023-\u0039]\ufe0f?\u20e3|\u3299|\u3297|\u303d|\u3030|\u24c2|\ud83c[\udd70-\udd71]|\ud83c[\udd7e-\udd7f]|\ud83c\udd8e|\ud83c[\udd91-\udd9a]|\ud83c[\udde6-\uddff]|\ud83c[\ude01-\ude02]|\ud83c\ude1a|\ud83c\ude2f|\ud83c[\ude32-\ude3a]|\ud83c[\ude50-\ude51]|\u203c|\u2049|[\u25aa-\u25ab]|\u25b6|\u25c0|[\u25fb-\u25fe]|\u00a9|\u00ae|\u2122|\u2139|\ud83c\udc04|[\u2600-\u26FF]|\u2b05|\u2b06|\u2b07|\u2b1b|\u2b1c|\u2b50|\u2b55|\u231a|\u231b|\u2328|\u23cf|[\u23e9-\u23f3]|[\u23f8-\u23fa]|\ud83c\udccf|\u2934|\u2935|[\u2190-\u21ff])/;
    // let returnString=tempStr.replace(regex, '*');
    return tempStr.replace(regex, '*')
  }
  countDots(s1, letter) {
    if (s1.match(new RegExp(letter, 'g'))) {
      return s1.match(new RegExp(letter, 'g')).length;
    }
    else {
      return 0;
    }

  }

  checkDecimalMax(question1: IQuestionModel) {
    if (question1.type == 'number') {
      let s: string = question1.value
      question1.value = question1.value.replace('+', '');
      question1.value = question1.value.replace('-', '');
      question1.value = question1.value.replace('e', '');
      if (question1.value > question1.maxValue) {
        setTimeout(() => {
          question1.value = null;
          this.errorColorNoScroll(question1.columnName)
        }, 100)
      } else {
        if (question1.value != null) {
          const pattern = /[0-9.\+\-\ ]/;
          let inputChar = String.fromCharCode(question1.value.charCodeAt(question1.value.length));
          let numDots = this.countDots(String(question1.value), '\\.');
          if (numDots > 1) {
            inputChar = null
            question1.value = null
           
          }
          if (!pattern.test(inputChar)) {
            question1.value = question1.value.replace(/[^0-9.]/g, '');
          }
          let decPart = question1.value.split('.')[1]
          if (decPart) {
            if (decPart.length > 2) {
              setTimeout(() => {
              question1.value = question1.value.split('.')[0] + '.' + decPart.substring(0, 2);
              }, 100)
            }
          }
        }
      }
    }
  }
  /**
   * This method checks ie the field should not take the value after current year
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param e
   * @param type
   * @param question
   */
  checkNumber(e, type, question) {
   
    if (question.type == 'tel') {
      let newValue = this.removeEmojis(e.target.value)

      let pass = /[4][8-9]{1}/.test(e.charCode) || /[5][0-7]{1}/.test(e.charCode) || e.keyCode === 8 || e.keyCode === 32;
      if (!pass) {
        let regExp = new RegExp('^[0-9?]+$');
        if (!regExp.test(newValue)) {
          newValue = newValue.slice(0, -1);
        }
      }

      question.value = newValue;
    
    }
    else if (question.type == 'number') {
      let newValue = this.removeEmojis(e.target.value)
      
      question.value = question.value.replace('+', '');
      question.value = question.value.replace('-', '');
      question.value = question.value.replace('e', '');
      let s:string=question.value
      newValue = newValue.replace('+', '');
      newValue = newValue.replace('-', '');
      if (String(question.value) == 'null') {
        newValue = e.target.value.slice(0, -1);
        question.value = e.target.value.slice(0, -1);
        return false
      }
      let numDots = this.countDots(String(s), '\\.');
      if (numDots > 1 || s=="") {

        question.value = null
        e.target.value = null
        return false
      }
      this.checkDecimalMax(question);
    }
    else if (question.type == 'text') {
      question.value = e.target.value.replace(/(?:[\u2700-\u27bf]|(?:\ud83c[\udde6-\uddff]){2}|[\ud800-\udbff][\udc00-\udfff]|[\u0023-\u0039]\ufe0f?\u20e3|\u3299|\u3297|\u303d|\u3030|\u24c2|\ud83c[\udd70-\udd71]|\ud83c[\udd7e-\udd7f]|\ud83c\udd8e|\ud83c[\udd91-\udd9a]|\ud83c[\udde6-\uddff]|\ud83c[\ude01-\ude02]|\ud83c\ude1a|\ud83c\ude2f|\ud83c[\ude32-\ude3a]|\ud83c[\ude50-\ude51]|\u203c|\u2049|[\u25aa-\u25ab]|\u25b6|\u25c0|[\u25fb-\u25fe]|\u00a9|\u00ae|\u2122|\u2139|\ud83c\udc04|[\u2600-\u26FF]|\u2b05|\u2b06|\u2b07|\u2b1b|\u2b1c|\u2b50|\u2b55|\u231a|\u231b|\u2328|\u23cf|[\u23e9-\u23f3]|[\u23f8-\u23fa]|\ud83c\udccf|\u2934|\u2935|[\u2190-\u21ff])/, '')
    }

  }

  /**
   * This method is for table data calculation.
   *
   * @author Azhar (azaruddin@sdrc.co.in)
   * @param cell
   * @param columnIndex
   * @param rowIndex
   * @param tableModel
   */
  calculateTableArithmetic(cellQ: any, columnIndex: number, rowIndex: number, tableModel: IQuestionModel[]) {
    //-------------calculation using features array-------------------------
    let cellEventSource = tableModel[rowIndex][Object.keys(tableModel[rowIndex])[columnIndex]];
    let fresult: number = null;
    let cells = this.questionFeaturesArray[cellEventSource.columnName];
    if (cells) {
      for (let cell of cells) {
        if (typeof cell == "object" && cell.features != null && cell.features.includes("exp:") && cell.features.includes("{" + cellEventSource.columnName + "}")) {
          for (let feature of cell.features.split("@AND")) {
            switch (feature.split(":")[0]) {
              case "exp":
                for (let cols of feature.split(":")[1].split("&")) {
                  let arithmeticExpression = feature.split(":")[1];
                  let result = this.engineUtilsProvider.resolveExpression(arithmeticExpression, this.questionMap, "default");
                  if (result != null && result != "NaN" && cell.type == "tel") {
                    fresult = parseInt(result as string);
                    cell.value = fresult;
                  }
                  if (fresult == null) {
                    cell.value = null;
                  }
                }
                break;
            }
          }
        }
      }
    }
    let constraintsCells = this.constraintsArray[cellEventSource.columnName];
    if (constraintsCells) {
      for (let constraintsCell of constraintsCells) {
        if ((constraintsCell.controlType == 'cell') && (constraintsCell.constraints != null) && (constraintsCell.constraints.includes('exp:'))) {
          for (let c of constraintsCell.constraints.split('@AND')) {
            switch (c.split(":")[0]) {
              case 'exp':
                {
                  let exp = c.split(":")[1]
                  let rr = this.engineUtilsProvider.resolveExpression(exp, this.questionMap, "constraint")
                  if (parseInt(rr) == 0) {
                    // if in some devices data is not clear make sure to increase delay time
                    setTimeout(() => {
                      cellEventSource.value = null;
                    }, 500)
                  }
                }
                break;
            }
          }
        }
      }
    }
  }


  /**
   * This method check the number pattern and decimal pattern
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param event
   * @param question
   */
  numberInput(event, question) {
    if (question.type == 'tel') {

      let pass = /[4][8-9]{1}/.test(event.charCode) || /[5][0-7]{1}/.test(event.charCode) || event.keyCode === 8;
      if (!pass) {
        return false;
      }
    }
    else if (question.type == 'number') {
      let pass = /[4][6-6]{1}/.test(event.charCode) || /[4][8-9]{1}/.test(event.charCode) || /[5][0-7]{1}/.test(event.charCode) || event.keyCode === 8 || event.keyCode === 46;
      if (!pass) {
        return false;
      }
    }
    else if (question.type == 'text') {
      // return event.charCode >= 97 && event.charCode <= 122 || event.charCode >= 65 && event.charCode <= 90 || event.charCode == 32
      return true;
    }
  }



  /**
   * This method is use to compute 2 field and the result should be shown in another filed
   *
   * @author Azhar (azaruddin@sdrc.co.in)
   * @param event
   * @param focusedQuestion
   */
  compute(focusedQuestion: IQuestionModel) {
    if (focusedQuestion.type == "tel") {
      let dependencies = this.questionFeaturesArray[focusedQuestion.columnName];
      if (dependencies) {
        for (let question of dependencies) {
          if (question.controlType == "textbox" && question.features != null && question.features.includes("exp:")) {
            for (let feature of question.features.split("@AND")) {
              switch (feature.split(":")[0]) {
                case "exp":
                  let expression = feature.split(":")[1];
                  let result = this.engineUtilsProvider.resolveExpression(expression, this.questionMap, "default");
                  if (result != null && result != "NaN" && !isNaN(result) && result != "null" && result != undefined)
                    question.value = String(result);
                  else question.value = null;
                  break;
              }
            }
          }
        }
      }
      if (this.beginRepeatArray[focusedQuestion.columnName]) this.validateBeginRepeat(focusedQuestion);
    }
  }

  validateBeginRepeat(dQuestion) {
    if (this.beginRepeatArray[dQuestion.columnName]) {
      let bgQuestion = this.beginRepeatArray[dQuestion.columnName];
      let dependentQuestion = this.questionMap[dQuestion.columnName];
      let dqValue = null;
      if ((dependentQuestion.value != null && dependentQuestion.value != "") && typeof dependentQuestion.value == "string") {
        dqValue = parseInt(dependentQuestion.value)
      } else if ((dependentQuestion.value != null && dependentQuestion.value != "") && typeof dependentQuestion.value == "number") {
        dqValue = (dependentQuestion.value)
      } else {
        dqValue = 0
      }
      if (dqValue > 0) {
        bgQuestion.beginrepeatDisableStatus = false;
        this.setRenderDefault(true, bgQuestion);
      }
      let tempBgLength = 0;
      if (bgQuestion.beginrepeatDisableStatus == true) {
        tempBgLength = 0;
      }
      else {
        tempBgLength = bgQuestion.beginRepeat.length
      }
      if (dqValue < tempBgLength && bgQuestion.beginrepeatDisableStatus == false) {
        console.log("refer validateBeginRepeatMsg")
      }
      else {
        this.testErrMsgStatus = false
        // dQuestion.showErrMessage=false
      }

      if ((dqValue == 0 || dqValue == null) && (tempBgLength == 1)) {
        // while (bgQuestion.beginRepeat.length > 1) {
        //     // bgQuestion.beginRepeat.pop();

        // }
        if (tempBgLength == 1) {
          for (let i = 0; i < tempBgLength; i++) {
            for (let j = 0; j < bgQuestion.beginRepeat[i].length; j++) {
              bgQuestion.beginRepeat[i][j].value = null;
            }
          }
          bgQuestion.beginrepeatDisableStatus = true;

        }

      }
      if (tempBgLength == 1) {
        bgQuestion.beginRepeatMinusDisable = true;
      } else {
        bgQuestion.beginRepeatMinusDisable = false;
      }
    }
  }
  validateBeginRepeatMsg(dQuestion) {
    // alert("Enter")
    if (this.beginRepeatArray[dQuestion.columnName]) {
      let bgQuestion = this.beginRepeatArray[dQuestion.columnName];
      let dependentQuestion = this.questionMap[dQuestion.columnName];
      let dqValue = null;
      if ((dependentQuestion.value != null && dependentQuestion.value != "") && typeof dependentQuestion.value == "string") {
        dqValue = parseInt(dependentQuestion.value)
      } else if ((dependentQuestion.value != null && dependentQuestion.value != "") && typeof dependentQuestion.value == "number") {
        dqValue = (dependentQuestion.value)
      } else {
        dqValue = 0
      }
      if (dqValue > 0) {
        bgQuestion.beginrepeatDisableStatus = false;
        this.setRenderDefault(true, bgQuestion);
      }
      // alert(dqValue+"--"+bgQuestion.beginRepeat.length)
      let tempBgLength = 0;
      if (bgQuestion.beginrepeatDisableStatus == true) {
        tempBgLength = 0;
      }
      else {
        tempBgLength = bgQuestion.beginRepeat.length
      }
      if (dqValue < tempBgLength && bgQuestion.beginrepeatDisableStatus == false) {

        this.testErrMsgStatus = true
        // dQuestion.showErrMessage=true
        this.errorColor(dQuestion.columnName);
        let confirm = this.alertCtrl.create({
          enableBackdropDismiss: false,
          // data-keyboard:false,
          title: 'Warning',
          message: "'" + dQuestion.label + "'" + "<strong> value can not be less than number of </strong>" + "'" + bgQuestion.label + "'",
          buttons: [
            {
              text: "Ok",
              handler: () => {
                setTimeout(() => {
                  this.testErrMsgStatus = false;
                  this.errorColor(dQuestion.columnName);
                }, 20);
              }
            }
          ]
        });
        confirm.onDidDismiss((data, role) => {
          console.log('Data:', data);
          console.log('Role:', role);
        });
        confirm.present();

      }
      else {

        this.testErrMsgStatus = false
        // dQuestion.showErrMessage=false
      }

      if ((dqValue == 0 || dqValue == null) && (tempBgLength == 1)) {
        // while (bgQuestion.beginRepeat.length > 1) {
        //     // bgQuestion.beginRepeat.pop();

        // }
        if (tempBgLength == 1) {
          for (let i = 0; i < tempBgLength; i++) {
            for (let j = 0; j < bgQuestion.beginRepeat[i].length; j++) {
              bgQuestion.beginRepeat[i][j].value = null;
            }
          }
          bgQuestion.beginrepeatDisableStatus = true;

        }

      }
      if (tempBgLength == 1) {
        bgQuestion.beginRepeatMinusDisable = true;
      } else {
        bgQuestion.beginRepeatMinusDisable = false;
      }
    }
  }

  setRenderDefault(status: boolean, bgquestion: IQuestionModel) {

    let beginrepeat = bgquestion.beginRepeat
    for (let i = 0; i < beginrepeat.length; i++) {
      for (let j = 0; j < beginrepeat[i].length; j++) {
        if (beginrepeat[i][j].controlType == 'dropdown') {
          if (beginrepeat[i][j].features && beginrepeat[i][j].defaultValue) {
            if (status) {
              beginrepeat[i][j].value = Number(beginrepeat[i][j].defaultValue)
            } else {
              beginrepeat[i][j].value = null
            }
          }
        }
      }
    }
  }

  formatDate(date) {
    var d = new Date(date),
      month = "" + (d.getMonth() + 1),
      day = "" + d.getDate(),
      year = d.getFullYear();
    if (month.length < 2) month = "0" + month;
    if (day.length < 2) day = "0" + day;
    return [day, month, year].join("-");
  }

  checkRelevance(question: IQuestionModel) {

    if (this.questionDependencyArray[question.columnName + ":" + question.key + ":" + question.controlType + ":" + question.label] != null)
      for (let q of this.questionDependencyArray[question.columnName + ":" + question.key + ":" + question.controlType + ":" + question.label]) {
        let arithmeticExpression: String = this.engineUtilsProvider.expressionToArithmeticExpressionTransfomerForRelevance(q.relevance, this.questionMap);
        let rpn: String[] = this.engineUtilsProvider.transformInfixToReversePolishNotationForRelevance(arithmeticExpression.split(" "));
        let isRelevant = this.engineUtilsProvider.arithmeticExpressionResolverForRelevance(rpn);
        q.tempFinalizedMandatory = false;
        q.tempSaveMandatory = false;

        if (isRelevant) {
          q.displayComponent = true;
          q.disabled = false
          q.dependecy = true;
          if (q.finalizeMandatory && isRelevant) q.tempFinalizedMandatory = true;
          if (q.saveMandatory && isRelevant) q.tempSaveMandatory = true;
        } else {
          q.displayComponent = false;
          q.disabled = true
          q.value = null;
          if (q.controlType == "file" || q.controlType == "mfile") q.attachedFiles = [];
          q.dependecy = false;
          q.duplicateFilesDetected = false;
          q.errorMsg = null;
          q.wrongFileExtensions = false;
          q.fileSizeExceeds = false;
          if (q.finalizeMandatory && !isRelevant) q.tempFinalizedMandatory = false;
          if (q.saveMandatory && !isRelevant) q.tempSaveMandatory = false;
        }

      }

  }

  clearFeatureFilters(question: IQuestionModel) {

    if (this.questionFeaturesArray[question.columnName + ':' + question.key + ':' + question.controlType + ':' + question.label] != null)
      for (let q of this.questionFeaturesArray[question.columnName + ':' + question.key + ':' + question.controlType + ':' + question.label]) {
        for (let feature of q.features.split("@AND")) {
          switch (feature) {
            case 'area_group':
            case "filter_single":
            case "filter_multiple":
              q.value = null;
              break;
          }
        }
      }
  }
  syncGroup(question: IQuestionModel, parentQuestion: IQuestionModel, event) {
    if (question.features == null) return;
    for (let feature of question.features.split("@AND")) {
      switch (feature.split(":")[0].trim()) {
        case "date_sync":
          {
            let groupQuestions = "";
            for (let f of feature.split(":")[1].split("&")) {
              groupQuestions = groupQuestions + f + ",";
            }
            groupQuestions = groupQuestions.substring(0, groupQuestions.length - 1);
            switch (question.controlType) {
              case "Date Widget":
                {
                  if (question.value != null) {
                    for (let qcolname of groupQuestions.split(",")) {
                      let groupQuestion: IQuestionModel;
                      if (parentQuestion == null) groupQuestion = this.questionMap[qcolname] as IQuestionModel;
                      else if (parentQuestion.controlType == "beginrepeat") {
                        let rowIndexOfQuestion = question.columnName.split("-")[1];
                        let questions: IQuestionModel[] = parentQuestion.beginRepeat[rowIndexOfQuestion];
                        for (let ques of questions) {
                          if (ques.columnName == qcolname) {
                            groupQuestion = this.questionMap[ques.columnName] as IQuestionModel;
                            break;
                          }
                        }
                      }
                      switch (groupQuestion.controlType) {
                        case "textbox":
                          {
                            let dt1 = new Date();
                            let dt2 = new Date(question.value);
                            var diff = (dt1.getTime() - dt2.getTime()) / 1000;
                            diff /= 60 * 60 * 24;
                            let yearDiff = Math.abs(Math.round(diff / 365.25));
                            groupQuestion.value = String(yearDiff);
                          }
                          break;
                        case "dropdown":
                          {
                            let dt1 = new Date();
                            let dt2 = new Date(question.value);
                            let diff = (dt1.getTime() - dt2.getTime()) / 1000;
                            diff /= 60 * 60 * 24;
                            let yearDiff = Math.abs(Math.round(diff / 365.25));
                            let enteredValue = yearDiff;
                            for (let option of groupQuestion.options) {
                              let start: number;
                              let end: number;
                              if ((option["value"] as String).includes("-")) {
                                start = parseInt(option["value"].split("-")[0]);
                                end = parseInt(option["value"].split("-")[1]);
                                if (enteredValue >= start && enteredValue <= end) {
                                  groupQuestion.value = option["key"];
                                  break;
                                }
                              } else {
                                start = parseInt(option["value"].split(" ")[0]);
                                if (enteredValue >= start) {
                                  groupQuestion.value = option["key"];
                                  break;
                                }
                              }
                            }
                          }
                      }
                    }
                  }
                }
                break;
              case "textbox":
                {
                  for (let qcolname of groupQuestions.split(",")) {
                    let groupQuestion: IQuestionModel = this.questionMap[qcolname] as IQuestionModel;

                    if (question.value == null || question.value == "") {
                      groupQuestion.value = null;
                    }
                    switch (groupQuestion.controlType) {
                      case "dropdown":
                        {
                          let enteredValue = question.value;
                          for (let option of groupQuestion.options) {
                            let start: number;
                            let end: number;
                            if ((option["value"] as String).includes("-")) {
                              start = parseInt(option["value"].split("-")[0]);
                              end = parseInt(option["value"].split("-")[1]);
                              if (parseInt(enteredValue) >= start && parseInt(enteredValue) <= end) {
                                groupQuestion.value = option["key"];
                                break;
                              }
                            } else {
                              start = parseInt(option["value"].split(" ")[0]);
                              if (parseInt(enteredValue) >= start) {
                                groupQuestion.value = option["key"];
                                break;
                              }
                            }
                          }
                          if (question.value == null || question.value == "") {
                            groupQuestion.value = null;
                          }
                        }
                        break;
                      case "Date Widget":
                        {
                          if (event == "" || (event.keyCode != undefined && event.keyCode === 8)) {
                            groupQuestion.value = null;
                          }
                        }
                        break;
                    }
                  }
                }
                break;
            }
            break;
          }
        case "area_group":
        case "filter_single":
          {
            if (question.features != null && (question.features.includes("area_group") || question.features.includes("filter_single"))) {
              let groupQuestions = feature.split(":")[1];
              let childLevelQuestion = this.questionMap[groupQuestions];
              for (let option of childLevelQuestion.options) {
                if (option["parentId"] == question.value) {
                  option["visible"] = true;
                } else {
                  option["visible"] = false;
                }
              }
              childLevelQuestion.value = null;
            }
          }
          break;
        case "filter_multiple":
          {
            if (question.features != null && question.features.includes("filter_multiple")) {
              let groupQuestions = feature.split(":")[1];
              let childLevelQuestion = this.questionMap[groupQuestions];
              for (let option of childLevelQuestion.options) {
                option["visible"] = false;
                for (let parentId of option["parentIds"]) {
                  if (parentId == question.value) {
                    option["visible"] = true;
                    break;
                  }
                }
              }
              childLevelQuestion.value = null;
            }
          }
          break;
        case "dropdown_auto_select":
          { }
      }
    }
  }
  onPaste(question: any) {
    setTimeout(() => {
      question.value = null;
    }, 0);
  }

  checkFieldContainsAnyvalue(data: any) {
    for (let index = 0; index < Object.keys(this.data).length; index++) {
      for (let j = 0; j < this.data[Object.keys(this.data)[index]].length; j++) {
        let subSections = this.data[Object.keys(this.data)[index]][0]
        for (let qs = 0; qs < Object.keys(subSections).length; qs++) {
          for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
            let question: IQuestionModel = subSections[Object.keys(subSections)[qs]][q]

            switch (question.controlType) {
              case "textbox":
              case "textarea":
                if (question.value != null && (question.value as string).trim() != "")
                  return true
                break;
              case "dropdown":
              case "segment":
                if (question.value != null && question.value != "")
                  return true
                break;
              case "Time Widget":
                if (question.value != null && question.value != "")
                  return true
                break;
              case "Date Widget":
                if (question.value != null && question.value != "")
                  return true
                break;
              case "checkbox":
                if (question.value != null && question.value != "")
                  return true
                break;
              case 'tableWithRowWiseArithmetic':
                {
                  let tableData = question.tableModel
                  let tableArray: any[] = []
                  for (let i = 0; i < tableData.length; i++) {
                    let tableRow: {} = {}
                    for (let j = 0; j < Object.keys(tableData[i]).length; j++) {
                      let cell = (tableData[i])[Object.keys(tableData[i])[j]]
                      if (typeof cell == 'object') {
                        if (cell.value != null && cell.value.trim() != "")
                          return true
                        break;
                      }
                    }
                  }
                }
                break;
              case "beginrepeat":
                let beginrepeat = question.beginRepeat
                let beginrepeatArray: any[] = []
                let beginrepeatMap: {} = {}
                for (let i = 0; i < beginrepeat.length; i++) {
                  beginrepeatMap = {}
                  for (let j = 0; j < beginrepeat[i].length; j++) {
                    let colName = (beginrepeat[i][j].columnName as String).split('-')[3]
                    beginrepeatMap[colName] = beginrepeat[i][j].value
                    console.log('begin-repeat', beginrepeat[i][j])
                    switch (beginrepeat[i][j].controlType) {
                      case "textbox":
                      case "textarea":
                        if (beginrepeat[i][j].value != null && (beginrepeat[i][j].value as string).trim() != "")
                          return true
                        break;
                      case "dropdown":
                      case "segment":
                        if (beginrepeat[i][j].value != null && beginrepeat[i][j].valu != "")
                          return true
                        break;
                      case "Time Widget":
                        if (beginrepeat[i][j].value != null && beginrepeat[i][j].value != "")
                          return true
                        break;
                      case "Date Widget":
                        if (beginrepeat[i][j].value != null && beginrepeat[i][j].value != "")
                          return true
                        break;
                      case "checkbox":
                        if (beginrepeat[i][j].value != null && beginrepeat[i][j].value != "")
                          return true
                        break;
                    }
                  }
                  beginrepeatArray.push(beginrepeatMap)
                }
                break;
            }
          }
        }
      }
    }
    return false;
  }

  showCalendar(bgquestion: any, question: any) {
    this.datePicker.show({
      date: new Date(),
      mode: 'date',
      maxDate: new Date().valueOf(),
      androidTheme: this.datePicker.ANDROID_THEMES.THEME_HOLO_DARK
    }).then(
      date => {
        bgquestion.value = this.datepipe.transform(date, "dd-MM-yyyy")
        // this.syncGroup(bgquestion,question)
      },
      err => console.log('Error occurred while getting date: ', err)
    );
  }
  drawDependencyGraph(expression: String, question: IQuestionModel) {
    for (let str of expression.split("}")) {
      let expressions: String[] = str.split(":");
      for (let i = 0; i < expressions.length; i++) {
        let exp: String = expressions[i];
        switch (exp) {
          case "optionEquals":
          case "optionEqualsMultiple":
            {
              let dColName: any = expressions[i - 1];
              if (question.dependecy && this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label] == undefined) {
                this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label] = [this.questionMap[question.columnName]];
              } else if (question.dependecy == true) {
                let a = this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label];
                let keyFound = false
                for (let dps of a) {
                  if (dps.columnName == question.columnName)
                    keyFound = true
                }
                if (!keyFound) {
                  a.push(this.questionMap[question.columnName]);
                  this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label] = a;

                }

              }
              i = i + 2;
            }
            break;
          case "textEquals":
          case "equals":
          case "greaterThan":
          case "greaterThanEquals":
          case "lessThan":
          case "lessThanEquals":
            {
              let dColName: any = expressions[i - 1];
              if (question.dependecy && this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label] == undefined) {
                this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label] = [this.questionMap[question.columnName]];
              } else if (question.dependecy == true) {
                let a = this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label];
                let keyFound = false
                for (let dps of a) {
                  if (dps.columnName == question.columnName)
                    keyFound = true
                }
                if (!keyFound) {
                  a.push(this.questionMap[question.columnName]);
                  this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label] = a;

                }
              }
              i = i + 1;
            }
            break;
        }
      }
    }
  }
  digits_count(n: number) {
    let count = 0;
    if (n >= 1) {
      ++count;
    }
    while (n / 10 >= 1) {
      n /= 10;
      ++count;
    }

    return count;
  }

  setupDefaultSettingsAndConstraintsAndFeatureGraph(question: IQuestionModel): IQuestionModel {
    if (question.defaultSettings != null) {
      for (let settings of question.defaultSettings.split(",")) {
        switch (settings.split(":")[0]) {
          case "current_date":
            question.value = this.datepipe.transform(new Date(), "yyyy-MM-dd");
            break;
          case "prefetchNumber":
            question.value = parseInt(settings.split(":")[1])
            break;
          case "prefetchText":
            question.value = new String(settings.split(":")[1]);
            break;
          case "prefetchDropdownWithValue":
            question.value = new Number(settings.split(":")[1]);
            break;
          case "disabled":
            question.disabled = true;
            break;
          case "prefetchDate":
            if (settings.split(":")[1] == 'current_date') {
              if (question.value == null) {
                question.value = this.datepipe.transform(new Date(), "yyyy-MM-dd")
              }
            }
            break;
          case "prefetchTime":
            if (settings.split(":")[1] == 'current_time') {
              if (question.value == null) {
                let time = this.datepipe.transform(new Date(), "hh:mm:ss a")

                // question.value = this.datepipe.transform(new Date(), "hh:mm:ss")
                question.value = time

              }
            }
            break;
        }
      }
    }
    if (question.constraints != null) {
      for (let settings of question.constraints.split("@AND")) {
        switch (settings.split(":")[0].trim()) {
          case "maxLength":
            question.maxLength = parseInt(settings.split(":")[1]);
            break;
          case "minLength":
            question.minLength = parseInt(settings.split(":")[1]);
            break;
          case "maxValue":
            question.maxValue = parseInt(settings.split(":")[1]);

            if (question.type != 'number') {
              question.maxLength = this.digits_count(question.maxValue)
            } else {
              question.maxLength = this.digits_count(9999)
            }
            break;
          case "minValue":
            question.minValue = parseInt(settings.split(":")[1]);
            break;
          case 'lessThan':
          case 'lessThanEquals':
          case 'greaterThan':
          case 'greaterThanEquals':
          case 'exp':
            if (this.constraintsArray[question.columnName] == null) this.constraintsArray[question.columnName] = [question];
            else {
              let constraints = this.constraintsArray[question.columnName];
              constraints.push(question);
              this.constraintsArray[question.columnName] = constraints;
            }
            break;
          case "limit_bg_repeat":
            question.limit_bg_repeat = settings;
            let dcolName = settings.split(":")[1];
            question.bgDependentColumn = dcolName;
            this.beginRepeatArray[dcolName] = question;
            break;
        }
      }
    }
    if (question.features != null) {
      for (let features of question.features.split("@AND")) {
        switch (features.split(":")[0]) {
          case "exp":
            let exp = features.split(":")[1] as String;
            let str = exp.split("");
            for (let i = 0; i < str.length; i++) {
              if (str[i] == "$") {
                let qName = "";
                for (let j = i + 2; j < str.length; j++) {
                  if (str[j] == "}") {
                    i = j;
                    break;
                  }
                  qName = qName + str[j];
                }
                if (this.questionFeaturesArray[qName] == undefined) this.questionFeaturesArray[qName] = [question];
                else {
                  let a = this.questionFeaturesArray[qName];
                  a.push(question);
                  this.questionFeaturesArray[qName] = a;
                }
              }
            }
            break;
        }
      }
    }
    return question;
  }

  /**
   * This method is called when user clicks on time widget to set the appropriate time in 24hr format.
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param question
   */
  open(question: any) {
    if (!this.disableStatus) {
      let temptimez: string = ''
      if (question.value === null) {
        let currentTimed = this.datePipe.transform(new Date(), 'MM-dd-yyyy HH:mm:ss')
        temptimez = currentTimed.split(" ")[1]
      }
      else {
        if (question.value.split(' ')[1] == 'PM') {
          let tm = question.value.split(' ')[0]
          if (question.value.split(':')[0] != '12') {
            temptimez = (Number(question.value.split(':')[0]) + 12) + ':' + tm.split(':')[1]
          }
          else {
            temptimez = (Number(question.value.split(':')[0])) + ':' + tm.split(':')[1]
          }
        }
        else if (question.value.split(' ')[1] == 'AM') {
          temptimez = question.value.split(' ')[0]
        }
        else {
          temptimez = question.value
        }
      }

      const amazingTimePicker = this.atp.open({
        theme: 'material-red',
        time: temptimez
      });
      amazingTimePicker.afterClose().subscribe(time => {

        let startTime = this.createdDate
        let tempx = this.createdDate.split(" ")[0]
        let tempy = this.createdDate.split(":")[0] + ":" + this.createdDate.split(":")[1] + ":" + "00"
        let startTimed = tempy

        let tempet: any = startTime.split(' ')[0] + ' ' + time + ':00'
        let endTime: any = tempet
        let currentTime: any = this.datePipe.transform(new Date(), 'MM-dd-yyyy HH:mm:ss')
        let startTimes = startTimed.split('-')[1] + '-' + startTimed.split('-')[0] + '-' + startTimed.split('-')[2]
        let endTimes = endTime.split('-')[1] + '-' + endTime.split('-')[0] + '-' + endTime.split('-')[2]
        let st = new Date(startTimes);
        let et = new Date(endTimes);
        let ct = new Date(currentTime);
        let timeDiff1 = et.getTime() - st.getTime();
        let timeDiff2 = ct.getTime() - et.getTime();

        if (timeDiff1 >= 0 && timeDiff2 >= 0) {

          if (Number(time.split(':')[0]) > 5 && Number(time.split(':')[0]) < 22) {
            let tmm = time
            let tempTime = (Number(time.split(':')[0]) < 12 ? ' AM' : ' PM');
            question.value = (tempTime == ' PM') ? Number(time.split(':')[0]) - 12 + ':' + time.split(':')[1] + tempTime : time + tempTime;
            if (question.value.split(":")[0].length == 1) {
              question.value = '0' + question.value.split(":")[0] + ":" + question.value.split(":")[1]
            }
            if (question.value.split(":")[0] == '00') {
              question.value = '12' + ":" + question.value.split(":")[1]
            }
            if (question.constraints != null && question.constraints != "" && question.controlType == "Time Widget") {
              for (let settings of question.constraints.split("@AND")) {
                switch (settings.split(":")[0]) {
                  case "lessThan":
                  case "greaterThan": {
                    if (question.value != null && this.questionMap[question.constraints.split(":")[1]].value != null) {
                      this.questionMap[question.constraints.split(":")[1]].value = null
                    }
                  }
                }
              }
            }
          }
          else {
            question.value = null;
            let confirm = this.alertCtrl.create({
              enableBackdropDismiss: false,
              title: 'Warning',
              message: "<strong>Please select a time between 6 AM to 10 PM</strong>",
              buttons: [
                {
                  text: "Ok",
                  handler: () => { }
                }
              ]
            });
            confirm.present();

          }

        } else {

          question.value = null;
          let confirm = this.alertCtrl.create({
            enableBackdropDismiss: false,
            title: 'Warning',
            message: "<strong>End time can not be earlier than Time of Visit and can not be later than Current time</strong>",
            buttons: [
              {
                text: "Ok",
                handler: () => { }
              }
            ]
          });
          confirm.present();
        }
      });
    }
  }

  checkMinMax(question1, question2) {
    // let newValue = this.removeEmojis(question1.value)
    // question1.value=newValue
    if (question1.value != null && question1.constraints != "" && question1.controlType == "textbox") {
      if (question1.maxValue != null && Number(question1.value) > question1.maxValue) {
        question1.value = null;
        this.syncGroup(question1, question2, null);
        return (question1.value = null);
      } else if (question1.maxValue != null && Number(question1.value) < question1.minValue) {
        question1.value = null;
        this.syncGroup(question1, question2, null);
        return (question1.value = null);
      }
    } else if (question1.value != null && question1.constraints != "" && question1.controlType == "Time Widget") {
      for (let settings of question1.constraints.split("@AND")) {
        switch (settings.split(":")[0].trim()) {
          case "greaterThan":
            {
              if (this.questionMap[settings.split(":")[1]].value != null && question1.value) {
                let timeOfConstraint = this.questionMap[settings.split(":")[1]].value;
                let timeOfActiveQ = question1.value;
                let hourOfConstraint = parseInt(timeOfConstraint.split(":")[0]);
                let minuteOfConstraint = parseInt(timeOfConstraint.split(":")[1]);
                let hourOfActiveQ = parseInt(timeOfActiveQ.split(":")[0]);
                let minuteOfActiveQ = parseInt(timeOfActiveQ.split(":")[1]);
                // passing year, month, day, hourOfA and minuteOfA to Date()
                let dateOfConstraint: Date = new Date(2010, 6, 15, hourOfConstraint, minuteOfConstraint);
                let dateOfActiveQ: Date = new Date(2010, 6, 15, hourOfActiveQ, minuteOfActiveQ);
                if (dateOfConstraint > dateOfActiveQ) {
                  setTimeout(() => {
                    question1.value = null;
                  }, 100)
                }
              }
            }
          case "lessThan":
            {
              if (this.questionMap[settings.split(":")[1]].value != null && question1.value) {
                let timeOfConstraint = this.questionMap[settings.split(":")[1]].value;
                let timeOfActiveQ = question1.value;
                let hourOfConstraint = parseInt(timeOfConstraint.split(":")[0]);
                let minuteOfConstraint = parseInt(timeOfConstraint.split(":")[1]);
                let hourOfActiveQ = parseInt(timeOfActiveQ.split(":")[0]);
                let minuteOfActiveQ = parseInt(timeOfActiveQ.split(":")[1]);
                // passing year, month, day, hourOfA and minuteOfA to Date()
                let dateOfConstraint: Date = new Date(2010, 6, 15, hourOfConstraint, minuteOfConstraint);
                let dateOfActiveQ: Date = new Date(2010, 6, 15, hourOfActiveQ, minuteOfActiveQ);
                if (dateOfActiveQ > dateOfConstraint) {
                  setTimeout(() => {
                    question1.value = null;
                  }, 100)
                }
              }
            }
        }
      }
    }
  }



  updateCheckboxSelection(opt, question) {
    let tempValues = null
    for (let option of question.options) {
      if (option.isSelected) {
        if (tempValues) {
          tempValues = tempValues + option.key + ","
        } else {
          tempValues = option.key + ","
        }
      }
    }
    if (tempValues) {
      tempValues = tempValues.substr(0, tempValues.length - 1);
    }
    question.value = tempValues
    console.log(question)
  }

  onFileChange(event, question: IQuestionModel) {
    if (event.target.files) {
      let files = event.target.files;
      console.log(files);
      let totalFileSize = 0;
      question.duplicateFilesDetected = false;
      question.wrongFileExtensions = false;
      let testDuplicate: boolean = false;
      let fileSizeLimit: number = 100;
      if (question.controlType == "mfile") {
        for (let j = 0; j < question.attachedFiles.length; j++) {
          if (question.attachedFiles.length > 1 && question.attachedFiles[question.attachedFiles.length - 1] && event.target.files[event.target.files.length - 1].name == question.attachedFiles[j].fileName) {
            // duplicate file being attached
            // show error msg and return
            testDuplicate = true;
          }
        }
      } else {
        question.attachedFiles = [];
      }
      if (files.length) {
        for (let a = 0; a < files.length; a++) {
          let file = files[a];
          let extension = file.name.split(".")[file.name.split(".").length - 1];
          if (extension.toLowerCase() == "pdf" || extension.toLowerCase() == "doc" || extension.toLowerCase() == "docx" || extension.toLowerCase() == "xls" || extension.toLowerCase() == "xlsx") {
            let reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = () => {
              let f = {
                base64: (reader.result as String).split(",")[1],
                fileName: file.name,
                fileSize: file.size,
                columnName: question.columnName,
                attachmentId: null,
                fileType: extension.toLowerCase()
              };
              if (testDuplicate) {
                question.errorMsg = file.name + " :File has been already attached!!";
              } else if (Math.round(files[a].size / 1024) >= fileSizeLimit) {
                question.errorMsg = "Can not upload!! size limit exceeds (" + fileSizeLimit + " kb) for " + file.name + " !!";
                question.fileSizeExceeds = true;
              } else {
                question.errorMsg = null;
                question.attachedFiles.push(f as any);
                question.fileSizeExceeds = false;
              }
            };
          } else {
            question.wrongFileExtensions = true;
            //  question.fileSizeExceeds = false;
          }

        }
      } else {
        question.duplicateFilesDetected = true;
        //  $(event.target).parent().parent().find('input').val("");
      }
    } else {
      question.attachedFiles = [];
    }
    console.log(question);
  }


  deleteFile(fIndex, question) {
    question.attachedFiles.splice(fIndex, 1)
    console.log(question.attachedFiles)
  }


  restrictValue(question: IQuestionModel) {

    if (question.value > question.maxValue) {
      question.showErrMessage = false
      this.messageService.showErrorToast("maximum value can be " + "'" + question.maxValue + "'")
      this.errorColorNoScroll(question.columnName)
      setTimeout(() => {
        $(document.getElementById(question.columnName + "")).children(":first").focus();
      }, 1000)
      // let confirm = this.alertCtrl.create({
      //       enableBackdropDismiss: false,
      //       title: 'Warning',
      //       message: "<strong> maximum value can be "+"'"+question.maxValue+"'</strong>",
      //       buttons: [
      //         {
      //           text: "Ok",
      //           handler: () => {}
      //         }
      //       ]
      //     });
      //     confirm.present();

      return false
    }
    if (question.value < question.minValue && question.value != null && question.value != undefined) {
      question.showErrMessage = false
      this.messageService.showErrorToast("minimum value can be " + "'" + question.minValue + "'")
      this.errorColorNoScroll(question.columnName)
      setTimeout(() => {
        $(document.getElementById(question.columnName + "")).children(":first").focus();
      }, 1000)

      // let confirm = this.alertCtrl.create({
      //     enableBackdropDismiss: false,
      //     title: 'Warning',
      //     message: "<strong> Minimum value can be "+"'"+question.minValue+"'</strong>",
      //     buttons: [
      //       {
      //         text: "Ok",
      //         handler: () => {}
      //       }
      //     ]
      //   });
      //   confirm.present();
      return false
    }

    return true
  }
  //   checkConstraints(fq: IQuestionModel) {
  //     let ccd = this.constraintsArray[fq.columnName as any]
  //     if (ccd) {
  //         for (let qq of ccd) {
  //             if ((qq.controlType == 'textbox' || qq.controlType == 'cell') && (qq.constraints != null) && (qq.constraints.includes('exp:'))) {
  //                 for (let c of qq.constraints.split('@AND')) {
  //                     switch (c.split(":")[0].replace(/\s/g,'')) {
  //                         case 'exp':
  //                             {
  //                                 let exp = c.split(":")[1]
  //                                 let rr = this.constraintTokenizer.resolveExpression(exp, this.questionMap,"constraint")
  //                                 if (rr!=null  && rr!=NaN && rr!="null" && rr!="NaN") {
  //                                     // if in some devices data is not clear make sure to increase delay time
  //                                     if(parseInt(rr) == 0){
  //                                       fq.showErrMessage = false
  //                                       setTimeout(() => {
  //                                           // this.questionMap[qq.columnName].value = null;
  //                                           this.errorColor1(fq.columnName)
  //                                           fq.showErrMessage = true
  //                                           this.checkRelevance(fq);
  //                                           this.clearFeatureFilters(fq);
  //                                           this.compute(fq);
  //                                           this.validateBeginRepeat(fq.columnName);
  //                                           this.calculateScore(fq)
  //                                       }, 500)
  //                                     }else{
  //                                       this.errorColor1(fq.columnName)
  //                                       fq.showErrMessage = false
  //                                     }
  //                                 }
  //                             }
  //                             break;
  //                     }
  //                 }
  //             }
  //         }
  //     }
  // }

  checkConstraints(fq: IQuestionModel) {
    // alert(fq.label)
    let ccd = this.constraintsArray[fq.columnName as any]
    if (ccd) {
      console.log("consttiants", ccd)
      for (let qq of ccd) {

        if ((qq.controlType == 'textbox' || qq.controlType == 'cell') && (qq.constraints != null) && (qq.constraints.includes('exp:'))) {
          for (let c of qq.constraints.split('@AND')) {

            switch (c.split(":")[0].replace(/\s/g, '')) {
              case 'exp':
                {
                  let exp = c.split(":")[1]
                  // alert(exp)
                  let rr = this.constraintTokenizer.resolveExpression(exp, this.questionMap, "constraint")
                  setTimeout(() => {
                    console.log("waiting time")
                  }, 100)
                  // alert(rr)
                  if (rr != null && rr != NaN && rr != "null" && rr != "NaN") {
                    // if in some devices data is not clear make sure to increase delay time
                    if (parseInt(rr) == 0) {
                      setTimeout(() => {
                        // this.questionMap[qq.columnName].value = null;
                        // alert(fq.label)
                        this.errorColor1(fq.columnName)
                        this.errorStatus = true;
                        fq.showErrMessage = true

                      }, 500)
                    } else {
                      this.removeColor(fq.columnName)
                      fq.showErrMessage = false
                      this.errorStatus = false
                    }
                  }
                }
                break;
            }
          }
        }
      }

    }

    this.checkRelevance(fq);
    this.clearFeatureFilters(fq);
    this.compute(fq);
    this.validateBeginRepeat(fq);
    this.calculateScore(fq)


  }
  removeColor(key: any) {
    if (this.tempCaryId != null) {
      let temp = document.getElementById(this.tempCaryId + "")
      if (temp != null && temp != undefined) {
        temp.style.removeProperty("border-bottom-width");
        temp.style.removeProperty("border-bottom-style");
        temp.style.removeProperty("border-bottom-color");
        this.tempCaryId = null;
      }
    }
    if (key != null && key != '' && key != undefined) {
      let temp = document.getElementById(key + "")
      if (temp != null && temp != undefined) {
        temp.style.removeProperty("border-bottom-width");
        temp.style.removeProperty("border-bottom-style");
        temp.style.removeProperty("border-bottom-color");
        this.tempCaryId = key;
      }
    }

  };

  errorColor(key?: any, beginErr?: any) {
    // alert(key)
    if (this.tempCaryId != null)
      this.removeColor(this.tempCaryId);

    if (key != null && key != '' && key != undefined) {
      setTimeout(() => {

        let eleId = document.getElementById(key + "");

        if (eleId != null) {
          let box = eleId.parentNode.parentElement.getBoundingClientRect();

          let body = document.body;
          let docEl = document.documentElement;
          let scrollTop = window.pageYOffset || docEl.scrollTop || body.scrollTop;
          let clientTop = docEl.clientTop || body.clientTop || 0;
          let top = box.top + scrollTop - clientTop;
          let cDim = this.content.getContentDimensions();
          let scrollOffset = Math.round(top) + cDim.scrollTop - cDim.contentTop;
          this.content.scrollTo(0, scrollOffset, 500);
          if (beginErr == undefined) {
            eleId.style.setProperty("border-bottom", "#FF0000 solid 1px", "important");
          }
          setTimeout(() => {
            $(document.getElementById(key + "")).focus().trigger("click");
          }, 600)
          let ddHeight = scrollOffset + 50
          let cols = document.getElementsByClassName('popover-content');
          for (let i = 0; i < cols.length; i++) {
            cols[i].setAttribute('top', 'ddHeight');
          }
        } else {
          this.content.scrollToTop(300);
        }
      }, 50)

    } else {
      this.content.scrollToTop(300);
    }

  }
  errorColorNoScroll(key?: any) {
    if (this.tempCaryId != null)
      this.removeColor(this.tempCaryId);

    if (key != null && key != '' && key != undefined) {
      setTimeout(() => {

        let eleId = document.getElementById(key + "");

        if (eleId != null) {

          eleId.style.setProperty("border-bottom", "#FF0000 solid 1px", "important");
        }
      }, 50)

    }

  }
  calculateScore(question: IQuestionModel) {
    if (this.scoreKeyMapper[question.columnName]) {
      for (let impactedScoreHolders of this.scoreKeyMapper[question.columnName]) {
        let result = this.commonsEngineProvider.calculateScore(this.questionMap[impactedScoreHolders], this.questionMap)
        this.questionMap[impactedScoreHolders].value = result
      }
    }
  }

  drawScoreDependencyGraph(question: IQuestionModel) {
    let scoreExpChars = question.scoreExp.split("");
    for (let i = 0; i < scoreExpChars.length; i++) {
      let ch: string = scoreExpChars[i] as string;
      if (ch == '$') {
        let qName = "";
        for (let j = i + 2; j < scoreExpChars.length; j++) {
          if (scoreExpChars[j] == "}") {
            i = j;
            break;
          }
          qName = qName + (scoreExpChars[j]);
        }
        if (this.scoreKeyMapper[qName])
          this.scoreKeyMapper[qName].push(question.columnName)
        else {
          this.scoreKeyMapper[qName] = []
          this.scoreKeyMapper[qName].push(question.columnName)
        }
      }
    }

  }

  // temp: number;
  checkSaveMandatory() {
    this.errorStatus = false;
    for (let index = 0; index < Object.keys(this.data).length; index++) {
      this.sectionMap.set(Object.keys(this.data)[index], this.data[Object.keys(this.data)[index]])
      for (let j = 0; j < this.data[Object.keys(this.data)[index]].length; j++) {
        let subSections = this.data[Object.keys(this.data)[index]][0]
        for (let qs = 0; qs < Object.keys(subSections).length; qs++) {
          for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
            let question: IQuestionModel = subSections[Object.keys(subSections)[qs]][q]
            //     formId = question.formId

            if (question.tempSaveMandatory == true) {
              switch (question.controlType) {

                case "geolocation":
                  if (question.value == null || question.value == "") {
                    this.errorColorNoScroll(question.columnName);
                  }
                  break;
                case "camera":
                  if (question.value == null || question.value == "") {
                    this.errorColorNoScroll(question.columnName);
                  }
                  break;
                case 'dropdown':
                  for (let i = 0; i < question.options.length; i++) {
                    if (question.value == null || question.value == "") {
                      this.errorColorNoScroll(question.columnName);
                    }
                  }
                  break;
                case 'textbox':
                  if (question.value == null || question.value == "") {
                    this.errorColorNoScroll(question.columnName);
                  }
                  break;
                case 'Time Widget':
                  if (question.value == null || question.value == "") {
                    this.errorColorNoScroll(question.columnName);
                  }
                  break;
                case 'Date Widget':
                  if (question.value == null || question.value == '') {
                    this.errorColorNoScroll(question.columnName);
                  }
                  break;
                case 'checkbox':
                  if (question.value == null || question.value == "") {
                    this.errorColorNoScroll(question.columnName);
                  }
                  break;
                case 'tableWithRowWiseArithmetic':
                  let tableData = question.tableModel
                  for (let i = 0; i < tableData.length; i++) {
                    for (let j = 0; j < Object.keys(tableData[i]).length; j++) {
                      let cell = (tableData[i])[Object.keys(tableData[i])[j]]
                      if (typeof cell == 'object') {
                        if (cell.dependecy == false && cell.mandatory == 'yes' && (cell.value == null || (cell.value as string).trim() == "")) {
                          this.errorColorNoScroll(cell.columnName);
                        } else if (cell.dependecy == true && cell.mandatory == 'yes' && cell.typeDetailIdOfDependencyType == this.questionMap[cell.dependentColumn.split(',')[0] as any].value &&
                          (cell.value == null || (cell.value as string).trim() == "")) {
                          this.errorColorNoScroll(cell.columnName);
                        }
                      }
                    }
                  }
                  break;
                case 'textarea':
                  if (question.value == null || question.value == "") {
                    this.errorColorNoScroll(question.columnName);
                  }
                  break;
                case 'checkbox':
                  if (question.value == null || question.value == "") {
                    this.errorColorNoScroll(question.columnName);
                  }
                  break
              }
            }
          }
        }
      }
    }
  }
  ionViewDidEnter() {
    this.initializeBackButtonCustomHandler();
    this.navBar.backButtonClick = () => {
      if (this.segment) {
        this.initializeNavBackButton();
      } else {
        this.navCtrl.pop()
      }
    };
  }
  async initializeNavBackButton() {




    if (!(this.navParams.get('submission') == undefined)) {


      let tempData = await this.storage.get(ConstantProvider.dbKeyNames.form + "-" + this.userService.user.username);
      // alert(tempData)
      let tempSubmissions = tempData[this.formId as any]
      let submission = tempSubmissions[(this.navParams.get("submission") as IDbFormModel).uniqueId as any] as any

      this.backButtonCheckData = submission.formData


    } else {
      this.saveType = 'new'

      await this.questionService.getQuestionBank(this.formId, null,ConstantProvider.lastUpdatedDate).then((data) => {
        let formData = data
        this.backButtonCheckData = formData

      });
    }

    //begins

    loop1: for (let index = 0; index < Object.keys(this.backButtonCheckData).length; index++) {
      this.sectionMap.set(Object.keys(this.backButtonCheckData)[index], this.backButtonCheckData[Object.keys(this.backButtonCheckData)[index]])
      for (let j = 0; j < this.backButtonCheckData[Object.keys(this.backButtonCheckData)[index]].length; j++) {
        let subSections = this.backButtonCheckData[Object.keys(this.backButtonCheckData)[index]][0]
        for (let qs = 0; qs < Object.keys(subSections).length; qs++) {
          for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
            let question: IQuestionModel = subSections[Object.keys(subSections)[qs]][q]

            if (this.setBackButtonFlag(question) == false) {
              this.backButtonFlag = false;
              break loop1;
            }

          }
        }
      }
    }

    //ends
    // alert(this.backButtonFlag)

    if (this.backButtonFlag == false) {
      let confirm = this.alertCtrl.create({
        enableBackdropDismiss: false,
        title: 'Warning',
        message: "Do you want to save this data? ",
        buttons: [{
          text: 'No',
          handler: () => {
            this.navCtrl.pop()
          }
        },
        {
          text: 'Yes',
          handler: () => {
            if (this.isWeb) {
              this.customComponent.onSave('save')
            } else {
              this.onSave('save')
            }
          }
        }
        ]
      });
      confirm.present();
    }
    else {
      this.navCtrl.pop()
    }
  }
  public initializeBackButtonCustomHandler(): void {
    this.unregisterBackButtonAction = this.platform.registerBackButtonAction(() => {
      this.customHandleBackButton();
    }, 10);
  }
  private customHandleBackButton(): void {
    const overlayView = this.app._overlayPortal._views[0];
    if (overlayView && overlayView.dismiss) {
      overlayView.dismiss();
    } else {


      if (this.segment) {
        this.initializeNavBackButton();
      } else {
        this.navCtrl.pop()
      }
    }
  }

  ionViewWillLeave() {
    // Unregister the custom back button action for this page
    this.unregisterBackButtonAction && this.unregisterBackButtonAction();
  }
  setBackButtonFlag(tempQuestion): boolean {
    //save function starts



    //****** */
    let flg: boolean;
    loop1: for (let index = 0; index < Object.keys(this.data).length; index++) {
      this.sectionMap.set(Object.keys(this.data)[index], this.data[Object.keys(this.data)[index]])
      for (let j = 0; j < this.data[Object.keys(this.data)[index]].length; j++) {
        let subSections = this.data[Object.keys(this.data)[index]][0]
        for (let qs = 0; qs < Object.keys(subSections).length; qs++) {
          for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
            let question: IQuestionModel = subSections[Object.keys(subSections)[qs]][q]

            if (question.columnName == tempQuestion.columnName) {

              if (question.value != tempQuestion.value) {
                // console.log(question.value," ******************* ",tempQuestion.value)
                flg = false
                break loop1;
              }
              else {
                flg = true
              }

            }



          }
        }
      }
    }




    return flg;
  }

  checkQuestionSizeBasedOnRelevance(questions: IQuestionModel[]) {
    for (let q of questions) {
      if (q.displayComponent == true) {
        return true
      }
    }
    return false
  }

  errorColor1(key: any) {

    if (this.tempCaryId != null)
      this.removeColor(this.tempCaryId);
    if (key != null && key != "" && key != undefined) {
      setTimeout(() => {
        let eleId = document.getElementById(key + "");
        if (eleId != null) {
          let toscrl = eleId.parentNode.parentElement.offsetTop;
          // eleId.style.setProperty("border-bottom", "#FF0000 solid 1px", "important");
          // document
          //   .getElementsByClassName("scroll-content")[3]

          this.tempCaryId = key;
        } else {
          // document.getElementsByClassName("scroll-content")[3].scrollTop;
        }
      }, 50);
    } else {
      // document.getElementsByClassName("scroll-content")[3].scrollTop = 0;
    }
  }


  checkConstraintsFinalized(fq: IQuestionModel) {
    let ccd = this.constraintsArray[fq.columnName as any]
    if (ccd) {
      for (let qq of ccd) {
        if ((qq.controlType == 'textbox' || qq.controlType == 'cell') && (qq.constraints != null) && (qq.constraints.includes('exp:'))) {
          for (let c of qq.constraints.split('@AND')) {
            switch (c.split(":")[0].replace(/\s/g, '')) {
              case 'exp':
                {
                  let exp = c.split(":")[1]
                  let rr = this.constraintTokenizer.resolveExpression(exp, this.questionMap, "constraint")
                  if (rr != null && rr != NaN && rr != "null" && rr != "NaN") {
                    // if in some devices data is not clear make sure to increase delay time
                    if (parseInt(rr) == 0) {
                      if (parseInt(rr) == 0) {
                        fq.showErrMessage = false
                        // setTimeout(() => {
                        // this.questionMap[qq.columnName].value = null;
                        this.errorColor1(fq.columnName)
                        fq.showErrMessage = true
                        this.checkRelevance(fq);
                        this.clearFeatureFilters(fq);
                        this.compute(fq);
                        this.validateBeginRepeat(fq.columnName);
                        this.calculateScore(fq)
                        // }, 500)
                      }
                    }
                    else {
                      this.errorColor1(fq.columnName)
                      fq.showErrMessage = false
                    }
                  }
                }
                break;
            }
          }
        }
      }
    }

  }

  onSegmentChanged(event) {
    this.cf.detectChanges();
  }

  checkFinalizedConstraints(): boolean {
    let wasConstraintFailed = false;
    for (let index = 0; index < Object.keys(this.data).length; index++) {
      this.sectionMap.set(Object.keys(this.data)[index], this.data[Object.keys(this.data)[index]]);
      for (let j = 0; j < this.data[Object.keys(this.data)[index]].length; j++) {
        let subSections = this.data[Object.keys(this.data)[index]][0];
        for (let qs = 0; qs < Object.keys(subSections).length; qs++) {
          for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
            let fq: IQuestionModel = subSections[Object.keys(subSections)[qs]][q];
            if (fq.controlType == 'tableWithRowWiseArithmetic' || fq.controlType == 'table') {
              {
                let tableData = fq.tableModel;
                for (let i = 0; i < tableData.length; i++) {
                  for (let j = 0; j < Object.keys(tableData[i]).length; j++) {
                    let cell = tableData[i][Object.keys(tableData[i])[j]];
                    if (typeof cell == "object") {
                      let ccd = this.constraintsArray[cell.columnName as any]
                      if (ccd) {
                        for (let qq of ccd) {
                          if (qq.finalizeMandatory == true && (qq.controlType == 'textbox' || qq.controlType == 'cell') && (qq.constraints != null) && (qq.constraints.includes('exp:'))) {
                            // console.log("question" + qq.columnName)
                            for (let c of qq.constraints.split('@AND')) {
                              switch (c.split(":")[0].replace(/\s/g, '')) {
                                case 'exp':
                                  {
                                    let exp = c.split(":")[1]

                                    let rr = this.constraintTokenizer.resolveExpression(exp, this.questionMap, "constraint")
                                    if (rr != null && rr != NaN && rr != "null" && rr != "NaN") {
                                      // if in some devices data is not clear make sure to increase delay time
                                      if (parseInt(rr) == 0) {
                                        setTimeout(() => {
                                          cell.showErrMessage = true
                                        }, 500)
                                      } else {
                                        setTimeout(() => {
                                          cell.showErrMessage = false
                                        }, 500)
                                      }
                                      // console.log(qq, exp, rr)
                                    }
                                  }
                                  break;
                              }
                            }
                          }
                        }
                        // }
                      } else if (cell.finalizeMandatory == true && (cell.controlType == 'textbox' || cell.controlType == 'cell') && cell.constraints == null && cell.cmsg != null && (cell.value == null || cell.value == "")) {
                        cell.showErrMessage = true
                      } else {
                        cell.showErrMessage = false
                      }
                    }
                  }
                }
              }
            } else if (fq.controlType == 'beginrepeat') {
              for (let bgindex = 0; bgindex < fq.beginRepeat.length; bgindex++) {
                let beginRepeatQuestions: IQuestionModel[] = fq.beginRepeat[bgindex];
                for (let beginRepeatQuestion of beginRepeatQuestions) {
                  let ccd = this.constraintsArray[beginRepeatQuestion.columnName as any]
                  if (ccd) {
                    for (let qq of ccd) {
                      if (qq.finalizeMandatory == true && (qq.controlType == 'textbox' || qq.controlType == 'cell') && (qq.constraints != null) && (qq.constraints.includes('exp:'))) {
                        for (let c of qq.constraints.split('@AND')) {
                          switch (c.split(":")[0].replace(/\s/g, '')) {
                            case 'exp':
                              {
                                let exp = c.split(":")[1]
                                let rr = this.constraintTokenizer.resolveExpression(exp, this.questionMap, "constraint")
                                if (rr != null && rr != NaN && rr != "null" && rr != "NaN") {
                                  // if in some devices data is not clear make sure to increase delay time
                                  if (parseInt(rr) == 0) {
                                    setTimeout(() => {
                                      beginRepeatQuestion.showErrMessage = true
                                    }, 500)
                                  } else {
                                    setTimeout(() => {
                                      beginRepeatQuestion.showErrMessage = false
                                    }, 500)
                                  }
                                }
                              }
                              break;
                          }
                        }
                      }
                    }
                  }
                }
              }
            } else {
              let ccd = this.constraintsArray[fq.columnName as any]
              if (ccd) {
                for (let qq of ccd) {
                  if (qq.finalizeMandatory == true && (qq.controlType == 'textbox' || qq.controlType == 'cell') && (qq.constraints != null) && (qq.constraints.includes('exp:'))) {
                    for (let c of qq.constraints.split('@AND')) {
                      switch (c.split(":")[0].replace(/\s/g, '')) {
                        case 'exp':
                          {
                            let exp = c.split(":")[1]
                            let rr = this.constraintTokenizer.resolveExpression(exp, this.questionMap, "constraint")
                            if (rr != null && rr != NaN && rr != "null" && rr != "NaN") {
                              // if in some devices data is not clear make sure to increase delay time
                              if (parseInt(rr) == 0) {
                                setTimeout(() => {
                                  fq.showErrMessage = true
                                }, 500)
                              } else {
                                setTimeout(() => {
                                  fq.showErrMessage = false
                                }, 500)
                              }
                            }
                          }
                          break;
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return wasConstraintFailed;
  }

}
