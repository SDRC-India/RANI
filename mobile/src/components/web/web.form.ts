import {
  Component
}
from "@angular/core";
import {
  ConstantProvider
}
from "../../providers/constant/constant";
import {
  MessageServiceProvider
}
from "../../providers/message-service/message-service";
import {
  QuestionServiceProvider
}
from "../../providers/question-service/question-service";
import {
  NavController,
  ViewController,
  NavParams,
  ModalController,
}
from "ionic-angular";
import {
  FormServiceProvider
}
from "../../providers/form-service/form-service";
import {
  DataSharingServiceProvider
}
from "../../providers/data-sharing-service/data-sharing-service";
import {
  DatePipe
}
from "@angular/common";
import {
  DatePickerProvider
}
from "ionic2-date-picker";
import {
  EngineUtilsProvider
}
from "../../providers/engine-utils/engine-utils";
import {
  UUID
}
from "angular2-uuid";
import {
  IMyDpOptions, IMyDateModel
}
from "mydatepicker";
import {
  AmazingTimePickerService
}
from "amazing-time-picker";
import {
  Geolocation
}
from "@ionic-native/geolocation";
import {
  ApplicationDetailsProvider
}
from "../../providers/application/appdetails.provider.";
import { CommonsEngineProvider } from "../../providers/commons-engine/commons-engine";
declare var $: any;
/** 
* Generated class for the AnganwadiQuestionSectionComponent component.
*
* See https://angular.io/api/core/Component for more info on Angular
* Components.
*/
@
Component({
  selector: "web-form",
  templateUrl: "web.form.html"
})
export class WebFormComponent {
  isWeb: boolean = false;
  section: String;
  dataSharingService: DataSharingServiceProvider;
  repeatSubSection: Map < Number, IQuestionModel > = new Map();
  sectionNames = [];
  subSections: Array < Map < String, Array < IQuestionModel >>> ;
  sectionMap: Map < String, Array < Map < String, Array < IQuestionModel >>> > = new Map();
  data: Map < String, Array < Map < String, Array < IQuestionModel >>> > = new Map();
  dbFormModel: IDbFormModel;
  sectionHeading: any;
  questionMap: {} = {};
  formId: Number;
  formTitle: String;
  formModel: {} = {};
  dataModel: {} = {};
  maxDate: any;
  errorStatus: boolean = false;
  mandatoryQuestion: {} = {};
  disableStatus: boolean = false;
  disablePrimaryStatus: boolean = false;
  saveType: any;
  questionDependencyArray: {} = {};
  questionFeaturesArray: {} = {};
  constraintsArray: {} = {};
  beginRepeatArray: {} = {};
  uniqueId: String;
  checkFieldContainsAnyvalueStatus: boolean = false;
  fullDate: any; 
  createdDate: String = "";
  updatedDate: String = "";
  updatedTime: String = "";
  base64Image: any;
  currentLocation: any;
  options = {
      enableHighAccuracy: true
  };
  scoreKeyMapper : {} = [];
  constructor(private commonsEngineProvider : CommonsEngineProvider,private applicationDetailsProvider: ApplicationDetailsProvider, public questionService: QuestionServiceProvider, public messageService: MessageServiceProvider, private navCtrl: NavController, public viewCtrl: ViewController, public datepipe: DatePipe, public formService: FormServiceProvider, public navParams: NavParams, private dataSharingProvider: DataSharingServiceProvider, public modalCtrl: ModalController, private datePickerProvider: DatePickerProvider, private engineUtilsProvider: EngineUtilsProvider, private atp: AmazingTimePickerService, private geolocation: Geolocation) {
      this.dataSharingService = dataSharingProvider;
  }
  async ngOnInit() {
      this.isWeb = this.applicationDetailsProvider.getPlatform().isWebPWA;
      this.formId = this.navParams.get("formId");
      this.maxDate = this.datepipe.transform(new Date(), "yyyy-MM-dd");
      this.fullDate = this.datepipe.transform(new Date(), "yyyy-MM-dd").split("-");
      if (this.isWeb) {
          if (this.navParams) {
              if (!(this.navParams.get("submission") == undefined)) {
                  this.saveType = "old";
                  this.data = (this.navParams.get("submission") as IDbFormModel).formData;
                  this.disableStatus = (this.navParams.get("submission") as IDbFormModel).formStatus == "save" || (this.navParams.get("submission") as IDbFormModel).formStatus == "rejected" ? false : true;
                  this.uniqueId = (this.navParams.get("submission") as IDbFormModel).uniqueId;
                  this.createdDate = (this.navParams.get("submission") as IDbFormModel).createdDate;
                  this.updatedDate = (this.navParams.get("submission") as IDbFormModel).updatedDate;
                  this.updatedTime = (this.navParams.get("submission") as IDbFormModel).updatedTime;
                  this.disablePrimaryStatus = true;
                  await this.loadQuestionBankIntoUI(this.data);
              } else {
                  this.saveType = "new";
                  this.uniqueId = UUID.UUID();
                  if (this.formId) {
                      await this.questionService.getQuestionBank(this.formId, null,ConstantProvider.lastUpdatedDate).then(data => {
                          if (data) {
                              let formData = data;
                              this.loadQuestionBankIntoUI(formData);
                          } else {
                              this.navCtrl.setRoot("LoginPage");
                          }
                      });
                  } else {
                      this.navCtrl.setRoot("LoginPage");
                  }
              }
          } else {
              this.navCtrl.setRoot("LoginPage");
          }
      }
  }
  sectionSelected(sectionHeading: any) {
      // document.getElementsByClassName("scroll-content")[3].scrollTo(0, 0);
      this.sectionHeading = sectionHeading;
      this.subSections = this.sectionMap.get(sectionHeading);
      // this.scroll(); 
  }
  getGeoLocation(question) {
      this.geolocation.getCurrentPosition(this.options).then(resp => {
          this.questionMap[question.columnName].value = "Latitude :" + resp.coords.latitude + " Longitude :" + resp.coords.longitude;
      }).catch(error => {
          console.log("Error getting location", error);
      });
  }
  openCamera(question) {
      document.getElementById(question.columnName + "file-input").click();
  }
  onCameraFileChange($event, question) {
      let files = $event.target.files;
      let file = files[0];
      if ((file.name.split(".")[(file.name.split(".") as string[]).length - 1] as String).toLocaleLowerCase() === "png" || (file.name.split(".")[(file.name.split(".") as string[]).length - 1] as String).toLocaleLowerCase() === "jpg" || (file.name.split(".")[(file.name.split(".") as string[]).length - 1] as String).toLocaleLowerCase() === "jpeg") {
          let reader = new FileReader();
          reader.onload = this._handleReaderLoaded.bind(this);
          this.base64Image = question;
          reader.readAsBinaryString(file);
      } else {
          this.messageService.showErrorToast("Please select a image");
      }
  }
  _handleReaderLoaded(readerEvt) {
          let binaryString = readerEvt.target.result;
          this.geolocation.getCurrentPosition(this.options).then(resp => {
              this.questionMap[this.base64Image.columnName].value = {
                  src: "data:image/jpeg;base64," + btoa(binaryString),
                  meta_info: "Latitude :" + resp.coords.latitude + "; Longitude :" + resp.coords.longitude + "; Accuracy :" + resp.coords.accuracy
              };
          }).catch(error => {
              this.questionMap[this.base64Image.columnName].value = {
                  src: "data:image/jpeg;base64," + btoa(binaryString)
              };
          });
      }
      /**
       * This function is use to set the options in the date picker like dateFormat,disableSince,editableDateField,showTodayBtn,showClearDateBtn
       *
       * @author Jagat Bandhu (jagat@sdrc.co.in)
       */
  public myDatePickerOptions: IMyDpOptions = {
      // other options...
      dateFormat: "dd-mm-yyyy",
      disableSince: {
          year: Number(this.datepipe.transform(new Date(), "yyyy-MM-dd").split("-")[0]),
          month: Number(this.datepipe.transform(new Date(), "yyyy-MM-dd").split("-")[1]),
          day: Number(this.datepipe.transform(new Date(), "yyyy-MM-dd").split("-")[2]) + 1
      },
      editableDateField: false,
      showTodayBtn: false,
      showClearDateBtn: false
  };
  loadQuestionBankIntoUI(data) {
      this.data = data;
      for (let index = 0; index < Object.keys(data).length; index++) {
          this.sectionMap.set(Object.keys(data)[index], data[Object.keys(data)[index]]);
          for (let j = 0; j < data[Object.keys(data)[index]].length; j++) {
              let subSections = data[Object.keys(data)[index]][0];
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
                                          cell.dependecy = cell.relevance != null ? true : false;
                                          cell.displayComponent = cell.relevance == null ? true : false;
                                          this.questionMap[cell.columnName] = cell;
                                          cell = this.setupDefaultSettingsAndConstraintsAndFeatureGraph(cell);
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
                              this.repeatSubSection.set(question.key, question)
                              question = this.setupDefaultSettingsAndConstraintsAndFeatureGraph(question)
                              question.beginRepeatMinusDisable = false
                              if (question.beginRepeat.length == 1) {
                                  question.beginRepeatMinusDisable = true
                              }
                              this.questionMap[question.columnName] = question;
                              for (let index = 0; index < question.beginRepeat.length; index++) {
                                  let beginRepeatQuestions: IQuestionModel[] = question.beginRepeat[index];
                                  for (let beginRepeatQuestion of beginRepeatQuestions) {
                                      beginRepeatQuestion.dependecy = beginRepeatQuestion.relevance != null ? true : false;
                                      beginRepeatQuestion.displayComponent = beginRepeatQuestion.relevance == null ? true : false;
                                      this.questionMap[beginRepeatQuestion.columnName] = beginRepeatQuestion;
                                      beginRepeatQuestion = this.setupDefaultSettingsAndConstraintsAndFeatureGraph(beginRepeatQuestion);
                                      beginRepeatQuestion.relevance != null ? this.drawDependencyGraph(beginRepeatQuestion.relevance, beginRepeatQuestion) : null;
                                      this.mandatoryQuestion[beginRepeatQuestion.columnName] = beginRepeatQuestion.finalizeMandatory;
                                   
                                    }
                              }
                              break;
                          case "dropdown":
                          case "textbox":
                          case "Time Widget":
                          case "cell":
                         
                          case "uuid":
                          case "file":
                          case "mfile":
                          case 'geolocation':
                          case 'camera':
                          case 'segment':

                        //  if(selected(${b_10_2_i},'yes'),'1','0') + if(selected(${b_10_2_iii},'yes'),'1','0') 
                        //  if(${e_1_3}>0,"1","2")

                              question.dependecy = question.relevance != null ? true : false;
                              question.displayComponent = question.relevance == null ? true : false;
                              question = this.setupDefaultSettingsAndConstraintsAndFeatureGraph(question)
                              this.questionMap[question.columnName] = question;
                              question.relevance != null ? this.drawDependencyGraph(question.relevance, question) : null;
                             
                              this.mandatoryQuestion[question.columnName] = question.finalizeMandatory;
                              break;
                          case "Date Widget":
                              question.dependecy = question.relevance != null ? true : false;
                              question.displayComponent = question.relevance == null ? true : false;
                              question = this.setupDefaultSettingsAndConstraintsAndFeatureGraph(question);
                              this.questionMap[question.columnName] = question;
                              this.mandatoryQuestion[question.columnName] = question.finalizeMandatory;
                              question.relevance != null ? this.drawDependencyGraph(question.relevance, question) : null;
                           
                              break;
                          case "checkbox":
                              question.dependecy = question.relevance != null ? true : false;
                              question.displayComponent = question.relevance == null ? true : false;
                              question = this.setupDefaultSettingsAndConstraintsAndFeatureGraph(question);
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
      this.checkRelevanceForEachQuestion()
      for (let questionKey of this.dataSharingService.getKeys(this.beginRepeatArray)) {
          let question = this.questionMap[questionKey];
          let bgQuestion = this.beginRepeatArray[questionKey];
          if (question.value == null || question.value == 0) {
              // bgQuestion[0]['beginRepeat'].beginrepeatDisableStatus = true
              // this.beginRepeatArray[questionKey][0].beginrepeatDisableStatus = true
              bgQuestion.beginrepeatDisableStatus = true;
          }
      }
      console.log(this.questionMap);
      console.log("features array: ", this.questionFeaturesArray);
      console.log("dependencies array: ", this.questionDependencyArray);
      console.log("beginRepeatArray", this.beginRepeatArray);
      console.log("repeat Subsection", this.repeatSubSection);
      console.log('constraints array', this.constraintsArray)
      console.log('scoreKeyMapper array', this.scoreKeyMapper)
      this.sectionNames = Array.from(this.sectionMap.keys());
      this.section = this.sectionNames[0];
      this.sectionSelected(this.section);
  }
  addAnotherWorker(key: Number) {
          let beginRepeatParent: IQuestionModel = this.repeatSubSection.get(key);
          let beginRepeatQuestionList: IQuestionModel[] = beginRepeatParent.beginRepeat[beginRepeatParent.beginRepeat.length - 1];
          let size = beginRepeatParent.beginRepeat.length;
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
              clonedQuestion[index].dependecy =  clonedQuestion[index].relevance != null ? true : false;
              clonedQuestion[index].columnName = beginRepeatParent.columnName + "-" + size + "-" + colIndex + "-" + colName;
              clonedQuestion[index] = this.commonsEngineProvider.renameRelevanceAndFeaturesAndConstraintsAndScoreExpression(clonedQuestion[index],this.questionMap,beginRepeatParent,size);

              //setting up default setting and added to dependency array and feature array
              clonedQuestion[index].displayComponent = clonedQuestion[index].relevance == null ? true : false;
              this.questionMap[clonedQuestion[index].columnName] = clonedQuestion[index];
              clonedQuestion[index] = this.setupDefaultSettingsAndConstraintsAndFeatureGraph(clonedQuestion[index]);
            //  clonedQuestion[index].displayComponent = true
          }

          for (let index = 0; index < clonedQuestion.length; index++) {
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
         
      }
      /**
       * This method is called, when clicks on the delete icon to delete the beginRepeat section and clear the value of fields inside beginRepeat.
       *
       * @author Azhar (azaruddin@sdrc.co.in)
       * @param key
       */
  deleteLastWorker(key: Number, bgquestion: IQuestionModel) {

      let beginRepeatParent: IQuestionModel = this.repeatSubSection.get(key);
      let clonedQuestion: IQuestionModel[] = beginRepeatParent.beginRepeat[beginRepeatParent.beginRepeat.length - 1];
      for (let index = 0; index < clonedQuestion.length; index++) {
        clonedQuestion[index].relevance !=null ? this.removeFromDependencyGraph(clonedQuestion[index].relevance,clonedQuestion[index]) : null
      }
    
    if (bgquestion.beginRepeat.length > 1) {
              bgquestion.beginRepeat.pop();
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
      //Biswa
  tempCaryId: any;
  
      //Biswa
  async onSave(type: String) {
      // let uniqueName;
      let formId;
    //   let headerData: Map < string, string | number | any[] > = new Map();
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
                          if (question.type == 'number') {
                              question.value = Number(question.value);
                          }
                          if (question.saveMandatory == true) {
                              switch (question.controlType) {
                          
                                  case "geolocation":
                                      if (question.displayComponent == true && (question.value == null || question.value == "")) {
                                          this.errorStatus = true;
                                          this.sectionSelected(question.columnName);
                                          this.errorColor(Object.keys(this.data)[index], question.columnName);
                                          this.messageService.showErrorToast("Please enter " + question.label);
                                          break loop1;
                                      }
                                      break;
                                  case "camera":
                                      if (question.displayComponent == true && (question.value == null || question.value == "")) {
                                          this.errorStatus = true;
                                          this.sectionSelected(question.columnName);
                                          this.errorColor(Object.keys(this.data)[index], question.columnName);
                                          this.messageService.showErrorToast("Please enter " + question.label);
                                          break loop1;
                                      }
                                      break;
                                  case 'dropdown':
                                      for (let i = 0; i < question.options.length; i++) {
                                          if (question.displayComponent == true && question.value == null) {
                                              this.errorStatus = true
                                                  // uniqueName = question.options[i]['value']
                                              this.sectionSelected(question.columnName)
                                              this.errorColor(Object.keys(this.data)[index], question.columnName);
                                              this.messageService.showErrorToast("Please select " + question.label)
                                              break loop1
                                          }
                                      }
                                      break;
                                  case 'textbox':
                                      if (question.displayComponent == true && (question.value == null || question.value == '')) {
                                          this.errorStatus = true
                                              // uniqueName = question.value
                                          this.sectionSelected(question.columnName)
                                          this.errorColor(Object.keys(this.data)[index], question.columnName);
                                          this.messageService.showErrorToast("Please select " + question.label)
                                          break loop1
                                      }
                                      break;
                                  case "Date Widget":
                                      if (question.displayComponent == true && (question.value == null || question.value == "")) {
                                          this.errorStatus = true;
                                          this.sectionSelected(question.columnName);
                                          this.errorColor(Object.keys(this.data)[index], question.columnName);
                                          this.messageService.showErrorToast("Please select " + question.label);
                                          break loop1;
                                      }
                                  case "Time Widget":
                                      if (question.displayComponent == true && (question.value == null || question.value == "")) {
                                          this.errorStatus = true;
                                          this.sectionSelected(question.columnName);
                                          this.errorColor(Object.keys(this.data)[index], question.columnName);
                                          this.messageService.showErrorToast("Please select " + question.label);
                                          break loop1;
                                      }
                                      break;
                                  case 'checkbox':
                                      if (question.displayComponent == true && (question.value == null || question.value == '')) {
                                          this.errorStatus = true
                                              // uniqueName = question.value
                                          this.sectionSelected(question.columnName)
                                          this.messageService.showErrorToast("Please enter " + question.label)
                                          break loop1
                                      }
                                      break;
                                  case 'tableWithRowWiseArithmetic':
                                      let tableData = question.tableModel
                                      for (let i = 0; i < tableData.length; i++) {
                                          for (let j = 0; j < Object.keys(tableData[i]).length; j++) {
                                              let cell = (tableData[i])[Object.keys(tableData[i])[j]]
                                              if (typeof cell == 'object') {
                                                  if ((cell.value == null || (cell.value as string).trim() == "")) {
                                                      this.errorStatus = true
                                                      this.sectionSelected(Object.keys(this.data)[index])
                                                      this.messageService.showErrorToast("Please enter " + question.label + " " + (cell.label).replace('@@split@@', ''))
                                                      break loop1;
                                                  }
                                                  // else if (cell.typeDetailIdOfDependencyType == this.questionMap[cell.dependentColumn.split(',')[0] as any].value &&
                                                  //   (cell.value == null || (cell.value as string).trim() == "")) {
                                                  //   this.errorStatus = true
                                                  //   this.sectionSelected(Object.keys(this.data)[index])
                                                  //   this.messageService.showErrorToast("Please enter " + question.label + " " + (cell.label).replace('@@split@@', ''))
                                                  //   break loop1;
                                                  // }
                                              }
                                          }
                                      }
                                      break;
                                  case 'textarea':
                                      if (question.displayComponent == true && (question.value == null || question.value == '')) {
                                          this.errorStatus = true
                                          this.sectionSelected(question.columnName)
                                          this.messageService.showErrorToast("Please enter " + question.label)
                                          break loop1
                                      }
                                      break;
                              }
                             
                          }
                        if (question.reviewHeader) {
                            switch (question.controlType) {
                                case 'dropdown':
                                    // let json : any = {}
                                    headerData[question.reviewHeader] = question.options && question.options.length ? question.options.filter(d => d.key === question.value)[0].value : question.value;
                                    // headerData.push(question.reviewHeader, question.options && question.options.length ? question.options.filter(d => d.key === question.value)[0].value : question.value);
                                    // headerData(json)
                                    break;
                                case 'textbox':
                                    headerData[question.reviewHeader] =  question.value;
                                    break;
                                case 'camera':
                                    headerData[question.reviewHeader] =  question.value.src;
                                    image = question.value.src;
                                    break;
                            }
                        }
                          if (question.type == 'number') {
                              question.value = Number(question.value);
                          }
                      }
                  }
              }
          }
      }
      if (type == 'finalized') {
          this.errorStatus = false;
          loop1: for (let index = 0; index < Object.keys(this.data).length; index++) {
              this.sectionMap.set(Object.keys(this.data)[index], this.data[Object.keys(this.data)[index]])
              for (let j = 0; j < this.data[Object.keys(this.data)[index]].length; j++) {
                  let subSections = this.data[Object.keys(this.data)[index]][0]
                  for (let qs = 0; qs < Object.keys(subSections).length; qs++) {
                      for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
                          let question: IQuestionModel = subSections[Object.keys(subSections)[qs]][q]
                          formId = question.formId
                          if (question.type == 'number') {
                              question.value = Number(question.value);
                          }
                          if (question.finalizeMandatory == true) {
                              switch (question.controlType) {
                                  case "geolocation":
                                      if (question.displayComponent == true && (question.value == null || question.value == "")) {
                                          this.errorStatus = true;
                                          this.errorColor(Object.keys(this.data)[index], question.columnName);
                                          this.messageService.showErrorToast("Please enter " + question.label);
                                          break loop1;
                                      }
                                      break;
                                  case "camera":
                                      if (question.displayComponent == true && (question.value == null || question.value == "")) {
                                          this.errorStatus = true;
                                          this.errorColor(Object.keys(this.data)[index], question.columnName);
                                          this.messageService.showErrorToast("Please enter " + question.label);
                                          break loop1;
                                      }
                                      break;
                                  case 'dropdown':
                                      for (let i = 0; i < question.options.length; i++) {
                                          if (question.displayComponent == true && question.value == null) {
                                              this.errorStatus = true
                                              this.errorColor(Object.keys(this.data)[index], question.columnName);
                                              this.messageService.showErrorToast("Please select " + question.label)
                                              break loop1
                                          } else if (question.value == null && (question.displayComponent == true || question.displayComponent == null)) {
                                              this.errorStatus = true
                                              this.errorColor(Object.keys(this.data)[index], question.columnName);
                                              this.messageService.showErrorToast("Please select " + question.label)
                                              break loop1
                                          }
                                      }
                                      break;
                                  case 'textbox':
                                      if (question.displayComponent == true && (question.value == null || question.value == '')) {
                                          this.errorStatus = true
                                          this.errorColor(Object.keys(this.data)[index], question.columnName);
                                          this.messageService.showErrorToast("Please enter a valid " + question.label)
                                          break loop1
                                      }
                                      break;
                                  case 'Time Widget':
                                      if (question.relevance != null && question.displayComponent == true && (question.value == null || question.value == '')) {
                                          this.errorStatus = true
                                          this.errorColor(Object.keys(this.data)[index], question.columnName);
                                          this.messageService.showErrorToast("Please select " + question.label)
                                          break loop1
                                      }
                                      break;
                                  case 'Date Widget':
                                      if (question.relevance != null && question.displayComponent == true && (question.value == null || question.value == '')) {
                                          this.errorStatus = true
                                          this.errorColor(Object.keys(this.data)[index], question.columnName);
                                          this.messageService.showErrorToast("Please select " + question.label)
                                          break loop1
                                      }
                                      break;
                                  case 'checkbox':
                                      if (question.relevance != null && question.displayComponent == true && (question.value == null || question.value == '')) {
                                          this.errorStatus = true
                                          this.errorColor(Object.keys(this.data)[index], question.columnName);
                                          this.messageService.showErrorToast("Please enter " + question.label)
                                          break loop1
                                      }
                                      break;
                                  case 'tableWithRowWiseArithmetic':
                                      let tableData = question.tableModel
                                      for (let i = 0; i < tableData.length; i++) {
                                          for (let j = 0; j < Object.keys(tableData[i]).length; j++) {
                                              let cell = (tableData[i])[Object.keys(tableData[i])[j]]
                                              if (typeof cell == 'object') {
                                                  if (cell.dependecy == false && cell.mandatory == 'yes' && (cell.value == null || (cell.value as string).trim() == "")) {
                                                      this.errorStatus = true
                                                      this.errorColor(Object.keys(this.data)[index], cell.columnName);
                                                      this.messageService.showErrorToast("Please enter " + question.label + " " + (cell.label).replace('@@split@@', ''))
                                                      break loop1;
                                                  }
                                                  //  else if (cell.dependecy == true && cell.mandatory == 'yes' && cell.typeDetailIdOfDependencyType == this.questionMap[cell.dependentColumn.split(',')[0] as any].value &&
                                                  //   (cell.value == null || (cell.value as string).trim() == "")) {
                                                  //   this.errorStatus = true
                                                  //   this.sectionSelected(Object.keys(this.data)[index])
                                                  //   this.messageService.showErrorToast("Please enter " + question.label + " " + (cell.label).replace('@@split@@', ''))
                                                  //   break loop1;
                                                  // }
                                              }
                                          }
                                      }
                                      break;
                                  case 'textarea':
                                      if (question.relevance != null && question.displayComponent == true && (question.value == null || question.value == '')) {
                                          this.errorStatus = true
                                          this.errorColor(Object.keys(this.data)[index], question.columnName);
                                          this.messageService.showErrorToast("Please enter " + question.label)
                                          break loop1
                                      }
                                      break;
                                      case 'beginrepeat':
                                         
                                          for (let bgindex = 0; bgindex < question.beginRepeat.length; bgindex++) {
                                              let beginRepeatQuestions: IQuestionModel[] = question.beginRepeat[bgindex];
                                              for (let beginRepeatQuestion of beginRepeatQuestions) {
                                                if (beginRepeatQuestion.relevance != null && beginRepeatQuestion.displayComponent == true && (beginRepeatQuestion.value == null || beginRepeatQuestion.value == '')) {
                                                  this.errorStatus = true
                                                  this.errorColor(Object.keys(this.data)[index], beginRepeatQuestion.columnName);
                                                  this.messageService.showErrorToast("Please select " + beginRepeatQuestion.label)
                                                  break loop1
                                              }
                                              
                                                }
                                          }
                              break;
                              }
                        
                          }
                        //   if (question.reviewHeader) {
                        //     switch (question.controlType) {
                        //         case 'dropdown':
                        //             headerData.set(question.reviewHeader, question.options && question.options.length ? question.options.filter(d => d.key === question.value)[0].value : question.value);
                        //             break;
                        //         case 'textbox':
                        //             headerData.set(question.reviewHeader, question.value);
                        //             break;
                        //         case 'camera':
                        //             headerData.set(question.reviewHeader, question.value.src);
                        //             image = question.value.src;
                        //             break;
                        //     }
                        // }
                        if (question.reviewHeader) {
                            switch (question.controlType) {
                                case 'dropdown':
                                    // let json : any = {}
                                    headerData[question.reviewHeader] = question.options && question.options.length ? question.options.filter(d => d.key === question.value)[0].value : question.value;
                                    // headerData.push(question.reviewHeader, question.options && question.options.length ? question.options.filter(d => d.key === question.value)[0].value : question.value);
                                    // headerData(json)
                                    break;
                                case 'textbox':
                                    headerData[question.reviewHeader] =  question.value;
                                    break;
                                case 'camera':
                                    headerData[question.reviewHeader] =  question.value.src;
                                    image = question.value.src;
                                    break;
                            }
                        }
                          if (question.type == 'number') {
                              question.value = Number(question.value);
                          }
                      }
                  }
              }
          }
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
      } else {
          if (!this.errorStatus) {
              this.dbFormModel = {
                  createdDate: this.createdDate,
                  updatedDate: this.updatedDate,
                  updatedTime: this.updatedTime,
                  formStatus: type == 'save' ? 'save' : 'finalized',
                  extraKeys:null,
                  formData: this.data,
                  formSubmissionId: formId,
                  uniqueId: this.uniqueId,
                  formDataHead: headerData,
                  image: image,
                  attachmentCount: 0
              }
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
  checkNumber(e, type, question) {
      if (type == 'tel' && question.controlType == 'Date Widget') {
          if (Number(question.value) <= Number(this.datepipe.transform(new Date, 'yyyy'))) {
              return e.keyCode == 101 || e.keyCode == 69 ? false : true
          } else {
              question.value = this.datepipe.transform(new Date(), "yyyy");
              return false;
          }
      } else {
          return e.keyCode == 101 || e.keyCode == 69 ? false : true;
      }
  }
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
                                  let result = this.engineUtilsProvider.resolveExpression(arithmeticExpression, this.questionMap,"default");
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
                                    let rr = this.engineUtilsProvider.resolveExpression(exp, this.questionMap,"constraint")
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
  numberInput(event, question) {
      if (question.type == "tel") {
          let pass = /[4][8-9]{1}/.test(event.charCode) || /[5][0-7]{1}/.test(event.charCode);
          if (!pass) {
              return false;
          }
      } else {
          // return event.charCode >= 97 && event.charCode <= 122 || event.charCode >= 65 && event.charCode <= 90 || event.charCode == 32
          return true;
      }
  }
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
                                  let result = this.engineUtilsProvider.resolveExpression(expression, this.questionMap,"default");
                                  if (result != null && result != "NaN") question.value = String(result);
                                  else question.value = null;
                                  break;
                          }
                      }
                  }
              }
              if (this.beginRepeatArray[focusedQuestion.columnName]) this.validateBeginRepeat(focusedQuestion);
          }
      }
  }
  validateBeginRepeat(focusedQuestion) {
      if (this.beginRepeatArray[focusedQuestion.columnName]) {
          let bgQuestion = this.beginRepeatArray[focusedQuestion.columnName];
          let dependentQuestion = this.questionMap[focusedQuestion.columnName];
          if (dependentQuestion.value != null && dependentQuestion.value > 0) {
              bgQuestion.beginrepeatDisableStatus = false;
              this.setRenderDefault(true, bgQuestion);
          }
          if (dependentQuestion.value == null || dependentQuestion.value == 0) {
              while (bgQuestion.beginRepeat.length > 1) {
                  bgQuestion.beginRepeat.pop();
              }
              for (let i = 0; i < bgQuestion.beginRepeat.length; i++) {
                  for (let j = 0; j < bgQuestion.beginRepeat[i].length; j++) {
                      bgQuestion.beginRepeat[i][j].value = null;
                  }
              }
              bgQuestion.beginrepeatDisableStatus = true;
          } else {
              let diff = bgQuestion.beginRepeat.length - dependentQuestion.value;
              while (diff >= 1) {
                  bgQuestion.beginRepeat.pop();
                  diff--;
              }
          }
          if (bgQuestion.beginRepeat.length == 1) {
              bgQuestion.beginRepeatMinusDisable = true;
          } else {
              bgQuestion.beginRepeatMinusDisable = false;
          }
      }
  }
  setRenderDefault(status: boolean, bgquestion: IQuestionModel) {
      let beginrepeat = bgquestion.beginRepeat;
      for (let i = 0; i < beginrepeat.length; i++) {
          for (let j = 0; j < beginrepeat[i].length; j++) {
              if (beginrepeat[i][j].controlType == "dropdown") {
                  if (beginrepeat[i][j].features && beginrepeat[i][j].features.includes("render_default")) {
                      if (status) {
                          beginrepeat[i][j].value = Number(beginrepeat[i][j].defaultValue);
                      } else {
                          beginrepeat[i][j].value = null;
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

    console.log(question)
     
      if (this.questionDependencyArray[question.columnName + ":" + question.key + ":" + question.controlType + ":" + question.label] != null)
          for (let q of this.questionDependencyArray[question.columnName + ":" + question.key + ":" + question.controlType + ":" + question.label]) {
            
              if(q.relevance){
                let arithmeticExpression: String = this.engineUtilsProvider.expressionToArithmeticExpressionTransfomerForRelevance(q.relevance, this.questionMap);
                let rpn: String[] = this.engineUtilsProvider.transformInfixToReversePolishNotationForRelevance(arithmeticExpression.split(" "));
                let isRelevant = this.engineUtilsProvider.arithmeticExpressionResolverForRelevance(rpn);
                q.tempFinalizedMandatory = false;
                q.tempSaveMandatory = false;
             
                if (isRelevant) {
                    q.displayComponent = isRelevant;
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
          console.log(this.questionDependencyArray)
  }
  clearFeatureFilters(question: IQuestionModel) {
      if (this.questionFeaturesArray[question.columnName + ":" + question.key + ":" + question.controlType + ":" + question.label] != null)
          for (let q of this.questionFeaturesArray[question.columnName + ":" + question.key + ":" + question.controlType + ":" + question.label]) {
              for (let feature of q.features.split("@AND")) {
                  switch (feature) {
                      case "area_group":
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
                                      // if (parentQuestion == null)
                                      //   groupQuestion = this.questionMap[qcolname] as IQuestionModel
                                      // else if (parentQuestion.controlType == 'beginrepeat') {
                                      //   let rowIndexOfQuestion = question.columnName.split('-')[1]
                                      //   let questions: IQuestionModel[] = parentQuestion.beginRepeat[rowIndexOfQuestion]
                                      //   for (let ques of questions) {
                                      //     if (ques.columnName == qcolname) {
                                      //       groupQuestion = this.questionMap[ques.columnName] as IQuestionModel
                                      //       break;
                                      //     }
                                      //   }
                                      // }
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
                  {}
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
                  let subSections = this.data[Object.keys(this.data)[index]][0];
                  for (let qs = 0; qs < Object.keys(subSections).length; qs++) {
                      for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
                          let question: IQuestionModel = subSections[Object.keys(subSections)[qs]][q];
                          console.log(question.label);
                          switch (question.controlType) {
                              case "textbox":
                                  if (question.value != null && (question.value as string).trim() != "") return true;
                                  break;
                              case "segment":
                              case "dropdown":
                                  if (question.value != null && question.value != "") return true;
                                  break;
                              case "Time Widget":
                                  if (question.value != null && question.value != "") return true;
                                  break;
                              case "Date Widget":
                                  if (question.value != null && question.value != "") return true;
                                  break;
                              case "checkbox":
                                  if (question.value != null && question.value != "") return true;
                                  break;
                              case "tableWithRowWiseArithmetic":
                                  {
                                      let tableData = question.tableModel;
                                      for (let i = 0; i < tableData.length; i++) {
                                          for (let j = 0; j < Object.keys(tableData[i]).length; j++) {
                                              let cell = tableData[i][Object.keys(tableData[i])[j]];
                                              if (typeof cell == "object") {
                                                  if (cell.value != null && cell.value.trim() != "") return true;
                                                  break;
                                              }
                                          }
                                      }
                                  }
                                  break;
                              case "beginrepeat":
                                  let beginrepeat = question.beginRepeat;
                                  let beginrepeatArray: any[] = [];
                                  let beginrepeatMap: {} = {};
                                  for (let i = 0; i < beginrepeat.length; i++) {
                                      beginrepeatMap = {};
                                      for (let j = 0; j < beginrepeat[i].length; j++) {
                                          let colName = (beginrepeat[i][j].columnName as String).split("-")[3];
                                          beginrepeatMap[colName] = beginrepeat[i][j].value;
                                          console.log("begin-repeat", beginrepeat[i][j]);
                                          switch (beginrepeat[i][j].controlType) {
                                              case "textbox":
                                                  if (beginrepeat[i][j].value != null && (beginrepeat[i][j].value as string).trim() != "") return true;
                                                  break;
                                              case "dropdown":
                                              case "segment":
                                                  if (beginrepeat[i][j].value != null && beginrepeat[i][j].valu != "") return true;
                                                  break;
                                              case "Time Widget":
                                                  if (beginrepeat[i][j].value != null && beginrepeat[i][j].value != "") return true;
                                                  break;
                                              case "Date Widget":
                                                  if (beginrepeat[i][j].value != null && beginrepeat[i][j].value != "") return true;
                                                  break;
                                              case "checkbox":
                                                  if (beginrepeat[i][j].value != null && beginrepeat[i][j].value != "") return true;
                                                  break;
                                          }
                                      }
                                      beginrepeatArray.push(beginrepeatMap);
                                  }
                                  break;
                          }
                      }
                  }
              }
          }
          return false;
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
                              for(let dps of a){
                                  if(dps.columnName == question.columnName)
                                  keyFound = true
                              }
                              if(!keyFound){
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
                              for(let dps of a){
                                  if(dps.columnName == question.columnName)
                                  keyFound = true
                              }
                              if(!keyFound){
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
                          if (settings.split(":")[1] == "current_date") {
                              if (question.value != null) {
                                  if (question.value.date == null) {
                                      let fullDate = question.value.split('-')
                                      question.value = {
                                          date: {
                                              year: Number(fullDate[0]),
                                              month: Number(fullDate[1]),
                                              day: Number(fullDate[2])
                                          }
                                      }
                                  }
                              } else if (question.value == null) {
                                  let fullDate = this.datepipe.transform(new Date(), "yyyy-MM-dd").split('-')
                                  question.value = {
                                      date: {
                                          year: Number(fullDate[0]),
                                          month: Number(fullDate[1]),
                                          day: Number(fullDate[2])
                                      }
                                  }
                              } else if (question.value.date == null) {
                                  let fullDate = question.value.split('-')
                                  question.value = {
                                      date: {
                                          year: Number(fullDate[0]),
                                          month: Number(fullDate[1]),
                                          day: Number(fullDate[2])
                                      }
                                  }
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
      ///resolver methods to handle Relevance using Djisktra Shunting Algorithm
  onDateChanged(question1: any, question2: any, event: IMyDateModel) {
      question1.value = event.formatted;
      this.syncGroup(question1, question2, null);
  }
  open(question: any) {
      if (!this.disableStatus) {
          const amazingTimePicker = this.atp.open();
          amazingTimePicker.afterClose().subscribe(time => {
              question.value = time;
              if (question.constraints != null && question.constraints != "" && question.controlType == "Time Widget") {
                  for (let settings of question.constraints.split("@AND")) {
                      switch (settings.split(":")[0]) {
                          case "lessThan":
                          case "greaterThan":
                              {
                                  if (question.value != null && this.questionMap[question.constraints.split(":")[1]].value != null) {
                                      this.questionMap[question.constraints.split(":")[1]].value = null
                                  }
                              }
                      }
                  }
              }
          });
      }
  }
  checkMinMax(question1, question2) {
      if (question1.value != null && question1.constraints != "" && question1.controlType == "textbox") {
          if (question1.maxValue != null && Number(question1.value) >= question1.maxValue) {
              question1.value = null;
              this.syncGroup(question1, question2, null);
              return (question1.value = null);
          } else if (question1.maxValue != null && Number(question1.value) <= question1.minValue) {
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
//   scoreCalulate(question:IQuestionModel, totalArray) {
//       if (question.displayScore) {
//           let disScrAdd = 0;
//           let scoreQuestion = null;
//           totalArray.forEach((item, index) => {
//               if (item.displayScore == true && item.scoreValue != undefined) {
//                   disScrAdd = disScrAdd + Number(item.scoreValue - 0);
//               }
//               if (item.controlType == "sub-score-keeper") scoreQuestion = item;
//           });
//           scoreQuestion.value = disScrAdd;
//       }
//       if (question.displayScore && question.value != null) {
//           for (let opt of question.options) {
//               if (opt.key == question.value) {
//                   question.scoreValue = opt.score;
//                   break;
//               }
//           }
//       }
//   }
  // updateCheckboxSelection(opt, question) {
  //     let tempValues = null;
  //     for (let option of question.options) {
  //         if (option.isSelected) {
  //             if (tempValues) {
  //                 tempValues = tempValues + option.key + ",";
  //             } else {
  //                 tempValues = option.key + ",";
  //             }
  //         }
  //     }
  //     if (tempValues) {
  //         tempValues = tempValues.substr(0, tempValues.length - 1);
  //     }
  //     question.value = tempValues;
  //     console.log(question);
  // }
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
                  // $(event.target).parent().parent().find('input').val("");
                  // }
                  // else{
                  //   question.fileSizeExceeds = true;
                  // $(event.target).parent().parent().find('input').val("");
                  // }
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
          question.attachedFiles.splice(fIndex, 1);
          console.log(question.attachedFiles);
          question.errorMsg = null;
      }
      // checkBRLimitSize(question) {}
      checkConstraints(fq: IQuestionModel) {
        let ccd = this.constraintsArray[fq.columnName as any]
        if (ccd) {
            for (let qq of ccd) {
                if ((qq.controlType == 'textbox' || qq.controlType == 'cell') && (qq.constraints != null) && (qq.constraints.includes('exp:'))) {
                    for (let c of qq.constraints.split('@AND')) {
                        switch (c.split(":")[0]) {
                            case 'exp':
                                {
                                    let exp = c.split(":")[1]
                                    let rr = this.engineUtilsProvider.resolveExpression(exp, this.questionMap,"constraint")
                                    if (parseInt(rr) == 0) {
                                        // if in some devices data is not clear make sure to increase delay time
                                        setTimeout(() => {
                                            this.questionMap[fq.columnName].value = null;
                                            this.checkRelevance(fq);
                                            this.clearFeatureFilters(fq);
                                            this.compute(fq);
                                            this.validateBeginRepeat(fq.columnName);
                                            this.calculateScore(fq)
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

  removeColor(key: any) {
    if (this.tempCaryId != null) {
        let temp = document.getElementById(this.tempCaryId + "");
        if (temp != null && temp != undefined) document.getElementById(this.tempCaryId + "").style.removeProperty("outline");
        this.tempCaryId = null;
    }
    if (key != null && key != "" && key != undefined) {
        let temp = document.getElementById(key + "");
        if (temp != null && temp != undefined) document.getElementById(key + "").style.removeProperty("outline");
        this.tempCaryId = key;
    }
}
errorColor(sectionHeading: any, key: any) {
        this.sectionHeading = sectionHeading;
        this.subSections = this.sectionMap.get(sectionHeading);
        if (this.tempCaryId != null) this.removeColor(this.tempCaryId);
        if (key != null && key != "" && key != undefined) {
            setTimeout(() => {
                let eleId = document.getElementById(key + "");
                if (eleId != null) {
                    let toscrl = eleId.parentNode.parentElement.offsetTop;
                    eleId.style.setProperty("outline", "#FF0000 double 1px", "important");
                    document
                      .getElementsByClassName("scroll-content")[3]
                      .scrollTo(0, toscrl);
                    this.tempCaryId = key;
                } else {
                    document.getElementsByClassName("scroll-content")[3].scrollTop;
                }
            }, 50);
        } else {
            document.getElementsByClassName("scroll-content")[3].scrollTop = 0;
        }
    }

    checkQuestionSizeBasedOnRelevance(questions:IQuestionModel[]){
                for(let q of questions){
                    if(q.displayComponent == true){
                        return true
                    }
                }
                return false
    }

    calculateScore(question:IQuestionModel){
        if(this.scoreKeyMapper[question.columnName]){
            for(let impactedScoreHolders of this.scoreKeyMapper[question.columnName]){
                let result =  this.commonsEngineProvider.calculateScore(this.questionMap[impactedScoreHolders],this.questionMap)
            
                this.questionMap[impactedScoreHolders].value = result
            }
        }
    }

    drawScoreDependencyGraph(question : IQuestionModel){
            let scoreExpChars = question.scoreExp.split("");
            for(let i = 0 ; i < scoreExpChars.length;i++){
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
                    if(this.scoreKeyMapper[qName])
                         this.scoreKeyMapper[qName].push(question.columnName)
                    else{
                        this.scoreKeyMapper[qName] = []
                        this.scoreKeyMapper[qName].push(question.columnName)
                         }
                  }
            }
    
}


checkRelevanceForEachQuestion(){
    for (let questionKey of this.dataSharingService.getKeys(this.questionMap)) {
      let question = this.questionMap[questionKey];

      switch (question.controlType) {
        case "sub-score-keeper":
        case "score-keeper":
        case "score-holder":
            question.scoreExp != null ? this.drawScoreDependencyGraph(question) : null ;     
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

  removeFromDependencyGraph(expression: String, question: IQuestionModel) {
    if(this.questionMap[question.parentColumnName] && this.questionMap[question.parentColumnName].controlType == 'beginrepeat'){
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
                            for(let i=0;a.length;i++){
                                if(a[i].columnName == question.columnName){
                                    // remove the object from dependent key. if array size is 1, remove the key itself
                                    if(a.length == 1){
                                        delete this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label]
                                    }else{
                                        keyFoundIndex = i
                                        break
                                    }
                                }
                        
                            }
                            if(keyFoundIndex>-1){
                                a.splice(keyFoundIndex,1)
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
                            for(let dps of a){
                                if(dps.columnName == question.columnName)
                                keyFound = true
                            }
                            if(!keyFound){
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
}
// ({when:f3bo4:optionEquals:Gender : Female :@AND:  when:f3bo7:optionEquals:Marital Status : Married}):@AND:({when:f3bo5:greaterThanEquals:13})