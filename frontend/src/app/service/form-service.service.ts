import { Injectable } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Constants } from '../constants';
import { Observable } from 'rxjs';
import { ReviewPageModel } from '../interface/reviewpagemodel';

@Injectable({
  providedIn: 'root'
})
export class FormService {
  formDataTransfer: Map < String, Array < Map < String, Array < IQuestionModel >>> > = new Map();
  uniquueIdForNewRecord: String;

  draftData:any = null;

  constructor(private datePipe: DatePipe,private http:HttpClient) {}

  /**
   *
   * This method  will  save the from data in local database
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @since 0.0.1
   */
  saveData(formId: any, dataModel: any) {
      let formModel: {} = {}
      dataModel.createdDate = this.datePipe.transform(new Date(), 'dd-MM-yyyy')
      dataModel.updatedDate = this.datePipe.transform(new Date(), 'dd-MM-yyyy')
      dataModel.updatedTime = this.datePipe.transform(new Date(), 'HH:mm:ss')
      let data = this.prepareKeyValueModel(dataModel)
      dataModel.submissionData = data
      formModel[dataModel.uniqueId] = dataModel
      console.log(formModel)
      return this.http.post(Constants.HOME_URL+"saveData",dataModel,{responseType:'text'})
    
  }

  getDrafts(formId:any):Observable<ReviewPageModel> {
   
    return this.http.get<ReviewPageModel>(Constants.HOME_URL+"getDataForReview?formId="+formId);
  
}

  prepareKeyValueModel(data){
    let serverData:{} ={}
    let serverImageData = new Map();

    let sectionMap = data.formData;

    for (let index = 0; index < Object.keys(sectionMap).length; index++) {
      for (let j = 0; j < sectionMap[Object.keys(sectionMap)[index]].length; j++) {
        let subSections = sectionMap[Object.keys(sectionMap)[index]][0]
        for (let qs = 0; qs < Object.keys(subSections).length; qs++) {
          for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
            let question: IQuestionModel = subSections[Object.keys(subSections)[qs]][q]
            switch (question.controlType) {
              case "geolocation":
                serverData[question.columnName]=question.value
                break;
              case "camera":

                let cameraData: any[] = []
                if (question.value) {
                  for (let i = 0; i < question.value.length; i++) {

                    cameraData.push(question.value[i]);
                  }
                  serverImageData.set(question.columnName, cameraData);
                }

                break;
              case "textbox":
              serverData[question.columnName]=question.value
                break;
              case "textarea":
                serverData[question.columnName]=question.value
                break;
              case "dropdown":
              serverData[question.columnName]=question.value
                break;
              case "checkbox":
              serverData[question.columnName]=question.value
                break;
              case "Time Widget":
              serverData[question.columnName]=question.value
                break;
              case "Date Widget":
                if (question.value != null) {
                  if (question.value.date != null) {
                    let dateValue = question.value.date.day + "-" + question.value.date.month + "-" + question.value.date.year
                    serverData[question.columnName]= dateValue
                  } else {
                    serverData[question.columnName]=  question.value
                  }
                } else if (question.value == null) {
                  serverData[question.columnName]=  question.value
                }  else {
                  serverData[question.columnName]=  question.value
                }
                break;
              case 'tableWithRowWiseArithmetic':
              case 'tableWithRowAndColumnWiseArithmetic':
              case "table":
                {
                  let tableData = question.tableModel
                  let tableArray: any[] = []
                  for (let i = 0; i < tableData.length; i++) {
                    let tableRow: {} = {}
                    for (let j = 0; j < Object.keys(tableData[i]).length; j++) {
                      let cell = (tableData[i])[Object.keys(tableData[i])[j]]
                      if (typeof cell != 'string') {
                        tableRow[cell.columnName] = cell.value
                      }

                    }
                    tableArray.push(tableRow)
                  }
                 
                  serverData[question.columnName]=tableArray
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
                    if (beginrepeat[i][j].controlType == 'Date Widget') {
                      if (beginrepeat[i][j].value != null) {
                        if (beginrepeat[i][j].value.date != null) {
                          let dateValue = beginrepeat[i][j].value.date.day + "-" + beginrepeat[i][j].value.date.month + "-" + beginrepeat[i][j].value.date.year
                          beginrepeatMap[colName] = dateValue
                        } else {
                          beginrepeatMap[colName] = beginrepeat[i][j].value
                        }
                      } else if (beginrepeat[i][j].value == null) {
                        beginrepeatMap[colName] = beginrepeat[i][j].value
                      } else if (beginrepeat[i][j].value != null) {
                        if (beginrepeat[i][j].value.split("-")[0].length > 0) {
                          beginrepeatMap[colName] = this.datePipe.transform(beginrepeat[i][j].value, "dd-MM-yyyy")
                        } else {
                          beginrepeatMap[colName] = beginrepeat[i][j].value
                        }
                      } else {
                        beginrepeatMap[colName] = beginrepeat[i][j].value
                      }
                    } else if (beginrepeat[i][j].controlType != 'heading') {
                      beginrepeatMap[colName] = beginrepeat[i][j].value
                    }
                  }
                  beginrepeatArray.push(beginrepeatMap)
                }
               
                serverData[question.columnName]=beginrepeatArray
                break;
            }
          }
        }
      }
    }
    return serverData
  }

  /**
   * This method will check whether we have the record with given patient id, date and time.
   * If all the attribute value will match, this will splice that record and append incoming record.
   * Because it has come for an update.
   *
   * If record does not match, this will just push the input record with existing once
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @since 0.0.1
   * @param formsDataforSave All the existing record
   * @param formDataforSave incoming record
   * @returns IDbFormModel[] modified record
   */
  private validateNewEntryAndUpdate(
    formsDataforSave: {} = {},
    formDataforSave: any,
    formModel: {},
    formId: any,
    facilityName: any
  ) {
    for (let i = 0; i < Object.keys(formsDataforSave[formId]).length; i++) {
      if (Object.keys(formsDataforSave[formId])[i] == formDataforSave.facilityName) {
        //record found, need to splice and enter new
        delete formsDataforSave[i]
        delete formModel[i]
      }
    }
    formModel[facilityName] = formDataforSave
    formsDataforSave[formId] = formModel
    return formsDataforSave;
  }

  getBlankQuestion():Observable<any>{
    return this.http.get<any>(Constants.HOME_URL +"getQuestion");
  }

  setDraftData(data){
    this.draftData = data
  }

  getDraftData(){
    return this.draftData
  }
}
