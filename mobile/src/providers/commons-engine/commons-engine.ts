import {
  HttpClient
} from '@angular/common/http';
import {
  Injectable
} from '@angular/core';
import {
  EngineUtilsProvider
} from '../engine-utils/engine-utils';
import { ConstantProvider } from '../constant/constant';

/*
  Generated class for the CommonsEngineProvider provider.

  See https://angular.io/guide/dependency-injection for more info on providers
  and Angular DI.
*/

@Injectable()
export class CommonsEngineProvider {

  constructor(public http: HttpClient, private engineUtilsProvider: EngineUtilsProvider) {

  }

  calculateScore(question: IQuestionModel, questionMap) {
    if (question.scoreExp) {
      return this.engineUtilsProvider.resolveExpression(question.scoreExp, questionMap, "score")
    }
    return null;
  }

  renameRelevanceAndFeaturesAndConstraintsAndScoreExpression(question: IQuestionModel, questionMap, repeatQuestion: IQuestionModel, size: Number): IQuestionModel {
    question = this.renameRelevance(question, questionMap, repeatQuestion, size)
    question = this.renameFeatures(question, repeatQuestion, size)
    question = this.renameConstraints(question, repeatQuestion, size)
    question = this.renameScoreExpression(question, repeatQuestion, size)
    return question
  }

  renameRelevance(question: IQuestionModel, questionMap, repeatQuestion: IQuestionModel, size: Number): IQuestionModel {
    if (question.relevance != null) {
      let relevanceString = "";
      for (let rel of question.relevance.split(":")) {
        if (questionMap[rel] != undefined && questionMap[rel].parentColumnName && questionMap[rel].controlType != 'cell') {
          let depColNames = "";
          let depColName = rel.split("-")[3];
          let depColIndex = rel.split("-")[2];
          depColNames = depColNames + repeatQuestion.columnName + "-" + size + "-" + depColIndex + "-" + depColName;
          relevanceString = relevanceString + depColNames + ":";
        } else {
          relevanceString = relevanceString + rel + ":";
        }
      }
      relevanceString = relevanceString.substr(0, relevanceString.length - 1);
      question.relevance = relevanceString;
    }
    return question;
  }


  renameFeatures(question: IQuestionModel, repeatQuestion: IQuestionModel, size: Number): IQuestionModel {
    if (question.features != null) {
      for (let feature of question.features.split("@AND")) {
        switch (feature.split(":")[0]) {
          case "exp":
            { }
            break;
          case "date_sync":
            {
              let rColNames;
              for (let colName of feature.split(":")[1].split("&")) {
                if (colName.includes("-")) {
                  let depColName = colName.split("-")[3];
                  let depColIndex = colName.split("-")[2];
                  rColNames = repeatQuestion.columnName + "-" + size + "-" + depColIndex + "-" + depColName;
                  question.features = question.features.replace(colName, rColNames);
                }
              }
            }
            break;
          case "area_group":
          case "filter_single":
          case "filter_multiple":
            {
              let rColNames;
              let areaColName = feature.split(":")[1];
              if (areaColName.includes("-")) {
                let depColName = areaColName.split("-")[3];
                let depColIndex = areaColName.split("-")[2];
                rColNames = repeatQuestion.columnName + "-" + size + "-" + depColIndex + "-" + depColName;
                question.features = question.features.replace(areaColName, rColNames);
              }
            }
            break;
        }
      }
    }
    return question;
  }

  renameConstraints(question: IQuestionModel, repeatQuestion: IQuestionModel, size: Number): IQuestionModel {
    if (question.constraints != null) {
      let constraints = question.constraints.replace(" ", "");
      let str: String[] = constraints.split("");
      let alteredConstraint = ""
      for (let i = 0; i < str.length; i++) {

        let ch: string = str[i] as string;
        if (ch == '$') {
          let qName = "";
          for (let j = i + 2; j < str.length; j++) {
            if (str[j] == "}") {
              i = j;
              break;
            }
            qName = qName + (str[j]);
            if (qName.includes("-")) {
              let depColName = qName.split("-")[3];
              let depColIndex = qName.split("-")[2];
              qName = repeatQuestion.columnName + "-" + size + "-" + depColIndex + "-" + depColName;
            }
            alteredConstraint = alteredConstraint + "${" + qName + "}"
          }
        } else {
          alteredConstraint = alteredConstraint + ch
        }
      }
      question.constraints = alteredConstraint
    }
    return question
  }

  renameScoreExpression(question: IQuestionModel, repeatQuestion: IQuestionModel, size: Number): IQuestionModel {
    if (question.scoreExp != null) {
      let expression = question.scoreExp.replace(" ", "");
      let str: String[] = expression.split("");
      let alteredExpression = ""
      for (let i = 0; i < str.length; i++) {

        let ch: string = str[i] as string;
        if (ch == '$') {
          let qName = "";
          for (let j = i + 2; j < str.length; j++) {
            if (str[j] == "}") {
              i = j;
              break;
            }
            qName = qName + (str[j]);
          }
          if (qName.includes("-")) {
            let depColName = qName.split("-")[3];
            let depColIndex = qName.split("-")[2];
            qName = repeatQuestion.columnName + "-" + size + "-" + depColIndex + "-" + depColName;
          }
          alteredExpression = alteredExpression + "${" + qName + "}"
        } else {
          alteredExpression = alteredExpression + ch
        }
      }
      question.scoreExp = alteredExpression
    }

    return question
  }

  generateConstraintGraph(constraintExpression, question: IQuestionModel, constraintsArray, questionMap) {

    let str: String[] = constraintExpression.split("");
    let alteredConstraint = ""
    for (let i = 0; i < str.length; i++) {

      let ch: string = str[i] as string;
      if (ch == '$') {
        let qName = "";
        for (let j = i + 2; j < str.length; j++) {
          if (str[j] == "}") {
            i = j;
            break;
          }
          qName = qName + str[j];
        }
        if (constraintsArray[question.columnName] == undefined) {
          // constraintsArray[question.columnName] = [questionMap[qName]] ; 
          constraintsArray[question.columnName] = [question];
        }
        else {
          let exist = false
          let c = constraintsArray[question.columnName];
          // for(let e of c){
          //   // if(e == undefined){
          //   //     console.log(question,c,qName)
          //   // }
          //   // if(e.columnName == qName){
          //   //   exist = true
          //   //   break;
          //   // }
          // }
          if (!exist) {
            c.push(questionMap[qName]);
            constraintsArray[question.columnName] = c;
          }

        }
      }
    }

  }
  public getContentType(base64Data: any) {
    let block = base64Data.split(";");
    let contentType = block[0].split(":")[1];
    return contentType;
  }


  async dataURItoBlob(dataURI, type) {
    // convert base64 to raw binary data held in a string
    var byteString = atob(dataURI.split(',')[1]);

    // separate out the mime component
    // var mimeString = dataURI.split(',')[0].split(':')[1].split(';')[0]

    // write the bytes of the string to an ArrayBuffer
    var ab = new ArrayBuffer(byteString.length);
    var ia = new Uint8Array(ab);
    for (var i = 0; i < byteString.length; i++) {
      ia[i] = byteString.charCodeAt(i);
    }

    // write the ArrayBuffer to a blob, and you're done
    var bb = new Blob([ab], {
      type: type
    });
    return bb;
  }
  createFoldersInMobileDevice(formId, uniqueId, file, message): Promise<any> {

    return new Promise((resolve, reject) => {
      //checking folder existance
      file.checkDir(file.externalRootDirectory, ConstantProvider.appFolderName).then(() => {
        file.createDir(file.externalRootDirectory + ConstantProvider.appFolderName, formId, false).then(() => {
          file.createDir(file.externalRootDirectory + ConstantProvider.appFolderName + "/" + formId, uniqueId as string, false).then(() => {
            resolve(true)
          }).catch(err => {
            resolve(true)
          });
        }).catch(err => {
          file.createDir(file.externalRootDirectory + ConstantProvider.appFolderName + "/" + formId, uniqueId as string, false).then(() => {
            resolve(true)
          }).catch(err => {
            resolve(true)
          });
        });
      }).catch(err => {
        if (err.code === 1) {
          message.stopLoader()
          message.showErrorToast("The application folder has been deleted from memory. Please re-install the application to continue data entry.")
        }
      })
    })

  }
}