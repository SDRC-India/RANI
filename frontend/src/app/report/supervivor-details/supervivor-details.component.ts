import { Component, OnInit, ViewContainerRef } from '@angular/core';
import { ReportServiceService } from '../report-service.service';
import { Router } from '@angular/router';
import { CommonsEngineProvider } from '../engine/commons-engine';
import { EngineUtilsProvider } from '../engine/engine-utils.service';
import { DatePipe } from '@angular/common';
import { MessageServiceProvider } from '../engine/message-service/message-service';
import { DataSharingServiceProvider } from '../engine/data-sharing-service/data-sharing-service';
import { FormService } from 'src/app/service/form-service.service';
import { ToastsManager } from 'ng6-toastr/ng2-toastr';
import { NgForm } from '@angular/forms';
declare var $;

@Component({
  selector: 'app-supervivor-details',
  templateUrl: './supervivor-details.component.html',
  styleUrls: ['./supervivor-details.component.scss']
})
export class SupervivorDetailsComponent implements OnInit {
  reportService: ReportServiceService;
  selectedSubmissonDate: string;
  section: String;
  dataSharingService: DataSharingServiceProvider;
  repeatSubSection: Map<Number, IQuestionModel> = new Map();
  sectionNames = [];
  subSections: Array<Map<String, Array<IQuestionModel>>>;
  sectionMap: Map<String, Array<Map<String, Array<IQuestionModel>>>> = new Map();
  data: Map<String, Array<Map<String, Array<IQuestionModel>>>> = new Map();
  sectionHeading: any;
  questionMap: {} = {};

  mandatoryQuestion: {} = {};
  disableStatus: boolean = false;
  questionDependencyArray: {} = {};
  questionFeaturesArray: {} = {};
  constraintsArray: {} = {};
  beginRepeatArray: {} = {};
  fullDate: any;    
  scoreKeyMapper: {} = [];
  form: NgForm;
  collapseableHeading: any;

  constructor(private commonsEngineProvider: CommonsEngineProvider,
    public messageService: MessageServiceProvider,
    public datepipe: DatePipe,
    public formService: FormService,
    private router: Router,
    private reportServices: ReportServiceService,
    private engineUtilsProvider: EngineUtilsProvider, private dataSharingProvider: DataSharingServiceProvider
    , public toastr: ToastsManager, vcr: ViewContainerRef
  ) {
    this.dataSharingService = dataSharingProvider;
    this.toastr.setRootViewContainerRef(vcr);
    this.reportService = reportServices
  }

  async ngOnInit() {
    this.collapseableHeading = this.reportService.getSuperviorDetails();
    this.fullDate = this.datepipe.transform(new Date(), "yyyy-MM-dd").split("-");

    this.disableStatus = true;
    if(this.collapseableHeading.length>0)
    this.getData(this.collapseableHeading[0])   
  }
  getData(d){
    this.data =  d.formData;
    this.loadQuestionBankIntoUI(this.data);
  }
  sectionSelected(sectionHeading: any) {
    this.sectionHeading = sectionHeading;
    this.subSections = this.sectionMap.get(sectionHeading);
  }
  
  /**
   * This function is use to set the options in the date picker like dateFormat,disableSince,editableDateField,showTodayBtn,showClearDateBtn     
   */
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
              case "textarea":
              case "heading":
              case "Time Widget":
              case "cell":
              case "uuid":
              case "file":
              case "mfile":
              case 'geolocation':
              case 'camera':
              case 'segment':
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
    this.sectionNames = Array.from(this.sectionMap.keys());
    this.section = this.sectionNames[0];
    this.sectionSelected(this.section);
  }

  checkRelevance(question: IQuestionModel) {
    // console.log(question)
    if (this.questionDependencyArray[question.columnName + ":" + question.key + ":" + question.controlType + ":" + question.label] != null)
      for (let q of this.questionDependencyArray[question.columnName + ":" + question.key + ":" + question.controlType + ":" + question.label]) {

        if (q.relevance) {
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
   * This method will return date in dd-MM-yyyy format  
   * @param {{date: {day: number, month: number, year: number}}} date Input date from the object
   */
  getDateValue(date: { date: { day: number, month: number, year: number } }): string {
    if (date != undefined && date != null) {
      this.selectedSubmissonDate = `${date.date.day}-${date.date.month}-${date.date.year}`;
      return this.selectedSubmissonDate;
    } else {
      return 'N/A';
    }
  }
  /**
   * This method will return the selected values from dropdown
   * @param options 
   * @param selectedKey 
   */
  getDropdownValue(options: IOption[], selectedKey: any): string {
    try {
      if (typeof (selectedKey) === 'number') {
        // code for single select
        return options.filter(d => d.key === selectedKey)[0].value
      } else if (typeof (selectedKey) === 'object') {
        // code for multi select
        let selectedOptions: string = ""
        selectedKey.forEach(key => {
          selectedOptions += (options.filter(d => d.key === key)[0].value + ',')
        });
        selectedOptions = selectedOptions.substring(0, selectedOptions.length - 1)
        return selectedOptions
      } else {
        return 'N/A';
      }
    } catch (err) {
      return 'N/A';
    }
  }
}