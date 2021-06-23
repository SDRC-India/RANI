import {
  HttpClient
} from '@angular/common/http';
import {
  Injectable
} from '@angular/core';
import { EngineUtilsProvider } from './engine-utils.service';


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
      return this.engineUtilsProvider.resolveExpression(question.scoreExp, questionMap)
    }
    return null;
  }

  renameRelevanceAndFeaturesAndConstraintsAndScoreExpression(question: IQuestionModel, questionMap, repeatQuestion: IQuestionModel, size: Number): IQuestionModel {
    question = this.renameRelevance(question,questionMap,repeatQuestion,size)
    question = this.renameFeatures(question,repeatQuestion,size)
    question = this.renameConstraints(question,repeatQuestion,size)
    question = this.renameScoreExpression(question,repeatQuestion,size)
    return question
  }

  renameRelevance(question: IQuestionModel, questionMap, repeatQuestion: IQuestionModel, size: Number): IQuestionModel {
    if (question.relevance != null) {
      let relevanceString = "";
      for (let rel of question.relevance.split(":")) {
        if (questionMap[rel] != undefined && questionMap[rel].parentColumnName && questionMap[rel].controlType !='cell') {
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
                  {}
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
    if(question.constraints!=null){
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
          if(qName.includes("-")) {
            let depColName = qName.split("-")[3];
            let depColIndex = qName.split("-")[2];
            qName = repeatQuestion.columnName + "-" + size + "-" + depColIndex + "-" + depColName;
          }
          alteredConstraint = alteredConstraint + "${" + qName + "}"
        }
      }else{
        alteredConstraint = alteredConstraint + ch
      }
    }
    question.constraints = alteredConstraint
  }
    return question
  }

  renameScoreExpression(question: IQuestionModel, repeatQuestion: IQuestionModel, size: Number): IQuestionModel {
    if(question.scoreExp!=null){
      let expression = question.scoreExp.replace(" ", "");
      let str: String[] = expression.split("");
      let alteredExpression= ""
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
          if(qName.includes("-")) {
            let depColName = qName.split("-")[3];
            let depColIndex = qName.split("-")[2];
            qName = repeatQuestion.columnName + "-" + size + "-" + depColIndex + "-" + depColName;
          }
          alteredExpression = alteredExpression + "${" + qName + "}"
        }else{
          alteredExpression = alteredExpression + ch
        }
      }
      question.scoreExp = alteredExpression
    }

    return question
  }



}
