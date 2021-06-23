

import {
  HttpClient,
  HttpHeaders
} from '@angular/common/http';
import {
  Injectable
} from '@angular/core';
import {
  Storage
} from '@ionic/storage'
import {
  ConstantProvider
} from '../constant/constant';
import {
  MessageServiceProvider
} from '../message-service/message-service';
import {
  UserServiceProvider
} from '../user-service/user-service';
import {
  Events, SelectPopover
} from 'ionic-angular';
import {
  DatePipe
} from '@angular/common';
import { File, IWriteOptions } from '@ionic-native/file';
import { ApplicationDetailsProvider } from '../application/appdetails.provider.';
import { CommonsEngineProvider } from '../commons-engine/commons-engine';
declare var window;
/*
  Generated class for the SyncServiceProvider provider.
 
  See https://angular.io/guide/dependency-injection for more info on providers
  and Angular DI.
*/
@Injectable()
export class SyncServiceProvider {

  username: string = ''

  constructor(public http: HttpClient, public storage: Storage, public constantService: ConstantProvider, public events: Events,
    public messageService: MessageServiceProvider, public userService: UserServiceProvider, private raniProvider: ApplicationDetailsProvider,
    private datePipe: DatePipe, private file: File, private applicationDetailsProvider: ApplicationDetailsProvider,
    private commonEngine: CommonsEngineProvider) { }

  b64toBlob(b64Data, contentType, sliceSize?) {
    contentType = contentType || '';
    sliceSize = sliceSize || 512;

    var byteCharacters = atob(b64Data);
    var byteArrays = [];

    for (var offset = 0; offset < byteCharacters.length; offset += sliceSize) {
      var slice = byteCharacters.slice(offset, offset + sliceSize);

      var byteNumbers = new Array(slice.length);
      for (var i = 0; i < slice.length; i++) {
        byteNumbers[i] = slice.charCodeAt(i);
      }

      var byteArray = new Uint8Array(byteNumbers);

      byteArrays.push(byteArray);
    }

    var blob = new Blob(byteArrays, { type: contentType });
    return blob;
  }
  savebase64AsImageFile(folderpath, filename, content, contentType) {
    // Convert the base64 string in a Blob
    var DataBlob = this.b64toBlob(content, contentType);

    console.log("Starting to write the file :3");

    window.resolveLocalFileSystemURL(folderpath, function (dir) {
      console.log("Access to the directory granted succesfully");
      dir.getFile(filename, { create: true }, function (file) {
        console.log("File created succesfully.");
        file.createWriter(function (fileWriter) {
          console.log("Writing content to file");
          fileWriter.write(DataBlob);
        }, function () {
          alert('Unable to save file in path ' + folderpath);
        });
      });
    });
  }
  async getUsrName() {
    let userAndForm = await this.userService.getUserAndForm()
    this.username = userAndForm['user'].username
  }

  async getAllDataFromDb(): Promise<any> {

    await this.getUsrName();
    let isWeb = this.raniProvider.getPlatform().isWebPWA
    let dd: {} = {}

    let sendToServerData: any[] = []
    let sendIMageToServerData: any[] = []

    let dataModelArr: IDbFormModel[] = [];
    let facilityIdArr: any[] = [];
    let formIdArr: any[] = [];
    let formIdNameArr: any[] = [];
    let createdDateArr: any[] = [];
    let updatedDateArr: any[] = [];
    let uniqueDateArr: any[] = [];
    let uniqueNameArr: any[] = [];
    let indexServerData = 0;
    let finalizedCount: number = 0;
    let sentCount: number = 0;
    await  this.storage.get(ConstantProvider.dbKeyNames.form + "-" + this.userService.user.username).then(async (d) => {
      if (d) {
        let forms = d
        for (let form = 0; form < Object.keys(forms).length; form++) {
          let submittedForms = forms[Object.keys(forms)[form]]
          for (let submittedForm = 0; submittedForm < Object.keys(submittedForms).length; submittedForm++) {

            let facilityId = Object.keys(submittedForms)[submittedForm]

            let data = submittedForms[facilityId];
            let serverData = new Map()
            let serverImageData = new Map();

            let sectionMap = data.formData;

            if (data.formStatus == 'finalized') {
              for (let index = 0; index < Object.keys(sectionMap).length; index++) {
                for (let j = 0; j < sectionMap[Object.keys(sectionMap)[index]].length; j++) {
                  let subSections = sectionMap[Object.keys(sectionMap)[index]][0]
                  for (let qs = 0; qs < Object.keys(subSections).length; qs++) {
                    for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
                      let question: IQuestionModel = subSections[Object.keys(subSections)[qs]][q]
                      switch (question.controlType) {
                        case "geolocation":
                          serverData.set(question.columnName, question.value);
                          break;
                        case "camera":

                          let cameraData: any[] = []
                          if (question.value) {
                            for (let i = 0; i < question.value.length; i++) {

                              if (question.value[i].split(':')[0] == 'file') {
                                cameraData.push(question.value[i]);
                              }
                              else {
                                let myBaseString = question.value[i];

                                // Split the base64 string in data and contentType
                                let block = myBaseString.split(";");
                                // Get the content type
                                let dataType = block[0].split(":")[1];// In this case "image/png"
                                // get the real base64 content of the file
                                let realData = block[1].split(",")[1];// In this case "iVBORw0KGg...."

                                // The path where the file will be created
                                // let folderpath = "file:///storage/emulated/0/";
                                let folderpath = this.file.externalCacheDirectory
                                // The name of your file, note that you need to know if is .png,.jpeg etc
                                let currentTime = +new Date();
                                let random = Math.random()
                                let trends = random + ""
                                let filename = this.username + '_' + trends.replace('.', '') + '_' + i + '_' + currentTime + ".jpg";

                                this.savebase64AsImageFile(folderpath, filename, realData, dataType);

                                cameraData.push(folderpath + "" + filename);
                                console.log(folderpath + "" + filename);
                              }

                            }
                            serverImageData.set(question.columnName, cameraData);
                          }

                          break;
                        case "textbox":
                          if (question.type == 'tel' && question.value != null) {
                            serverData.set(question.columnName, Number(question.value))
                          } else {
                            serverData.set(question.columnName, question.value)
                          }
                          break;
                        case "textarea":
                          serverData.set(question.columnName, question.value)
                          break;
                        case "dropdown":
                          serverData.set(question.columnName, question.value)
                          break;
                        case "segment":
                          serverData.set(question.columnName, question.value)
                          break;
                        case "checkbox":
                          serverData.set(question.columnName, question.value)
                          break;
                        case "Time Widget":
                          serverData.set(question.columnName, question.value)
                          break;
                        case "Date Widget":
                          if (isWeb && question.value != null) {
                            if (isWeb && question.value.date != null) {
                              let dateValue = question.value.date.day + "-" + question.value.date.month + "-" + question.value.date.year
                              serverData.set(question.columnName, dateValue)
                            } else {
                              serverData.set(question.columnName, question.value)
                            }
                          } else if (isWeb && question.value == null) {
                            serverData.set(question.columnName, question.value)
                          } else if (!isWeb && question.value != null) {
                            if (question.value.split("-")[0].length > 0) {
                              serverData.set(question.columnName, this.datePipe.transform(question.value, "dd-MM-yyyy"))
                            } else {
                              serverData.set(question.columnName, question.value)
                            }
                          } else {
                            serverData.set(question.columnName, question.value)
                          }
                          break;
                        case 'tableWithRowWiseArithmetic':
                          {
                            let tableData = question.tableModel
                            let tableArray: any[] = []
                            for (let i = 0; i < tableData.length; i++) {
                              let tableRow: {} = {}
                              for (let j = 0; j < Object.keys(tableData[i]).length; j++) {
                                let cell = (tableData[i])[Object.keys(tableData[i])[j]]
                                if (typeof cell != 'string' && cell.value != null) {
                                  tableRow[cell.columnName] = Number(cell.value)
                                } else if (typeof cell != 'string') {
                                  tableRow[cell.columnName] = cell.value
                                }
                              }
                              tableArray.push(tableRow)
                            }
                            serverData.set(question.columnName, tableArray)
                          }
                          break;

                        case 'tableWithRowAndColumnWiseArithmetic':
                          {
                            let tableData = question.tableModel
                            let tableArray: any[] = []
                            for (let i = 0; i < tableData.length; i++) {
                              let tableRow: {} = {}
                              for (let j = 0; j < Object.keys(tableData[i]).length; j++) {
                                let cell = (tableData[i])[Object.keys(tableData[i])[j]]
                                if (typeof cell != 'string' && cell.value != null) {
                                  tableRow[cell.columnName] = Number(cell.value)
                                } else if (typeof cell != 'string') {
                                  tableRow[cell.columnName] = cell.value
                                }
                              }
                              tableArray.push(tableRow)
                            }
                            serverData.set(question.columnName, tableArray)
                          }
                          break;
                        case "table":
                          let tableData = question.tableModel
                          let tableArray: any[] = []
                          for (let i = 0; i < tableData.length; i++) {
                            let tableRow: {} = {}
                            for (let j = 0; j < Object.keys(tableData[i]).length; j++) {
                              let cell = (tableData[i])[Object.keys(tableData[i])[j]]
                              if (typeof cell != 'string' && cell.value != null) {
                                tableRow[cell.columnName] = Number(cell.value)
                              } else if (typeof cell != 'string') {
                                tableRow[cell.columnName] = cell.value
                              }
                            }
                            tableArray.push(tableRow)
                          }
                          serverData.set(question.columnName, tableArray)
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
                                if (isWeb && beginrepeat[i][j].value != null) {
                                  if (isWeb && beginrepeat[i][j].value.date != null) {
                                    let dateValue = beginrepeat[i][j].value.date.day + "-" + beginrepeat[i][j].value.date.month + "-" + beginrepeat[i][j].value.date.year
                                    beginrepeatMap[colName] = dateValue
                                  } else {
                                    beginrepeatMap[colName] = beginrepeat[i][j].value
                                  }
                                } else if (isWeb && beginrepeat[i][j].value == null) {
                                  beginrepeatMap[colName] = beginrepeat[i][j].value
                                } else if (!isWeb && beginrepeat[i][j].value != null) {
                                  if (beginrepeat[i][j].value.split("-")[0].length > 0) {
                                    beginrepeatMap[colName] = this.datePipe.transform(beginrepeat[i][j].value, "dd-MM-yyyy")
                                  } else {
                                    beginrepeatMap[colName] = beginrepeat[i][j].value
                                  }
                                } else {
                                  beginrepeatMap[colName] = beginrepeat[i][j].value
                                }
                              } else if (beginrepeat[i][j].controlType == 'textbox' && beginrepeat[i][j].type == 'tel') {
                                beginrepeatMap[colName] = Number(beginrepeat[i][j].value)
                              } else if (beginrepeat[i][j].controlType == 'textbox' && beginrepeat[i][j].type != 'tel') {
                                beginrepeatMap[colName] = beginrepeat[i][j].value
                              } else if (beginrepeat[i][j].controlType != 'heading') {
                                beginrepeatMap[colName] = beginrepeat[i][j].value
                              }
                            }
                            beginrepeatArray.push(beginrepeatMap)
                          }
                          serverData.set(question.columnName, beginrepeatArray)
                          break;
                      }
                    }
                  }
                }
              }
            }
            // setTimeout(() => {
            if ((data as IDbFormModel).formStatus == 'finalized') {

              facilityIdArr.push(facilityId)
              dataModelArr.push(data)
              formIdArr.push(Object.keys(forms)[form].split("_")[0])
              formIdNameArr.push(Object.keys(forms)[form])
              createdDateArr.push((data as IDbFormModel).createdDate)
              updatedDateArr.push((data as IDbFormModel).updatedDate)
              uniqueDateArr.push((data as IDbFormModel).uniqueId)
              finalizedCount++
              sendToServerData.push(serverData)
              if (serverImageData) {
                sendIMageToServerData.push(serverImageData)
              }
              else {
                sendIMageToServerData.push(null)
              }

            }
          }
        }



        while (finalizedCount > 0) {
       
          finalizedCount--
          try {
            await this.sendDataToServer(sendToServerData[indexServerData], forms,
              dataModelArr[indexServerData], facilityIdArr[indexServerData], formIdArr[indexServerData],
              createdDateArr[indexServerData], updatedDateArr[indexServerData],
              uniqueDateArr[indexServerData], uniqueNameArr[indexServerData],
              formIdNameArr[indexServerData], sendIMageToServerData[indexServerData])
              sentCount = sentCount + 1;
          } catch (err) {
            throw err
          }
          indexServerData++
        }

      
      }
    })
    return sentCount
  }


  /**
   * This method will send form data to server
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @since 0.0.1
   */
  async sendDataToServer(mainSyncModel: Map<String, Object>, forms, dataModel: IDbFormModel, facilityId: any, formId: any, createdDate: any, updatedDate: any, uniqueId: any, uniqueName: any, formIdName: any, sendIMageToServerData: Map<String, Object>) {

    let formModel: {} = {}
    let dd = {}


    let imageParameter: IAttachementModel;
    let dataStr: string = '';
    let imageColumnName = ""
    if (sendIMageToServerData != null) {
      sendIMageToServerData.forEach((value, key) => {
        dataStr = value.toString();
        imageColumnName = key.toString()

      })
      let finalValue = dataStr.split(',')
      if (mainSyncModel) {
        let syncDataModel: ISyncDataModel;
        const httpOptions = {
          headers: new HttpHeaders({
            // 'Authorization': 'Bearer ' + accessToken
          })
        };
        mainSyncModel.forEach((value, key) => {
          dd[(key as any)] = value
        })
        try {
          syncDataModel = {
            formId: Number(formId),
            createdDate: createdDate,
            updatedDate: updatedDate,
            uniqueId: uniqueId,
            submissionData: dd,
            attachmentCount: finalValue.length
          }

          let submissionId = (await this.http.post(ConstantProvider.baseUrl + 'api/saveData', syncDataModel, httpOptions).retry(3).toPromise())

          if (finalValue.length > 0 && finalValue[0] != "") {
            for (let i = 0; i < finalValue.length; i++) {
              let tempFileName = finalValue[i];
              let fileName = tempFileName.toString().split('/');
              let urlForImage = tempFileName.substr(0, tempFileName.lastIndexOf('/') + 1);
              let data = await this.file.readAsArrayBuffer(urlForImage, decodeURI(fileName[fileName.length - 1]))
              let tempstr = fileName[fileName.length - 1].split('.');
              const formdata: FormData = new FormData();
              imageParameter = {
                columnName: imageColumnName,
                submissionId: submissionId,
                formId: Number(formId),
                originalName: fileName[fileName.length - 1],
                fileExtension: tempstr[tempstr.length - 1],
                localDevicePath: tempFileName
              }
              formdata.append('fileModel', JSON.stringify(imageParameter))

              let file: Blob = new Blob([data], {
                type: "image/" + tempstr[tempstr.length - 1]
              });

              formdata.append('file', file, fileName[fileName.length - 1]);
              let imageSubmission = await this.http.post(ConstantProvider.baseUrl + 'api/uploadFile', formdata, {
                reportProgress: true,
                responseType: 'text'

              }).retry(3).toPromise().catch(error => {
                throw error
              })

            }
            console.log("Setting the form status to sent")
            dataModel.formStatus = 'sent'
            formModel = (forms[formIdName])
            formModel[facilityId] = dataModel
            forms[formIdName] = formModel
            await this.storage.set(ConstantProvider.dbKeyNames.form + "-" + this.userService.user.username, forms)
          } else {
            dataModel.formStatus = 'sent'
            formModel = (forms[formIdName])
            formModel[facilityId] = dataModel
            forms[formIdName] = formModel
            await this.storage.set(ConstantProvider.dbKeyNames.form + "-" + this.userService.user.username, forms)
          }
        } catch (err) {
          throw err
        }
      }
    }
    else {
      if (mainSyncModel) {
        let syncDataModel: ISyncDataModel;
        const httpOptions = {
          headers: new HttpHeaders({
          })
        };
        mainSyncModel.forEach((value, key) => {
          dd[(key as any)] = value
        })
        try {
          syncDataModel = {
            formId: Number(formId),
            createdDate: createdDate,
            updatedDate: updatedDate,
            uniqueId: uniqueId,
            submissionData: dd,
            attachmentCount: 0
          }
          await this.sleep()
          let submissionId = (await this.http.post(ConstantProvider.baseUrl + 'api/saveData', syncDataModel, httpOptions).retry(3).toPromise())

          dataModel.formStatus = 'sent'
          formModel = (forms[formIdName])
          formModel[facilityId] = dataModel
          forms[formIdName] = formModel
          await this.storage.set(ConstantProvider.dbKeyNames.form + "-" + this.userService.user.username, forms)

        } catch (err) {
          throw err
        }
      }

    }
  }

  async getRejectedForms(): Promise<any> {
    let rejectionCount: number = 0;
    let formIdName: string;

    const httpOptions = {
      headers: new HttpHeaders({
        'Content-type': 'application/x-www-form-urlencoded; charset=utf-8'
      })
    };
    await this.sleep()
    let baseURL = ConstantProvider.baseUrl + 'api/getRejectedData';
    await this.http.get(baseURL, httpOptions).toPromise()
      .then(async data => {
        let localDbData = null
        await this.storage.get(ConstantProvider.dbKeyNames.form + "-" + this.userService.user.username)
          .then((val) => {
            localDbData = val;
          });
        for (let i = 0; i < Object.keys(data).length; i++) {
       
          formIdName = Object.keys(data)[i]
          for (let j = 0; j < data[Object.keys(data)[i]].length; j++) {

            let dbFormModel: IDbFormModel;
            let formModel: {} = {}
            let mainFormsDataforSave: {} = {};
            //push camera and attachements into db in case of WEBPWA
            if (this.applicationDetailsProvider.getPlatform().isAndroid) {
              data[Object.keys(data)[i]][j].formData = await this.pushCameraImagesAndAttachementsIntoApplicationFolder(data[Object.keys(data)[i]][j].formData, formIdName, data[Object.keys(data)[i]][j].uniqueId);
            }
            dbFormModel = {
              createdDate: data[Object.keys(data)[i]][j].createdDate,
              updatedDate: data[Object.keys(data)[i]][j].updatedDate,
              updatedTime: this.datePipe.transform(new Date(), 'HH:mm:ss'),
              formStatus: 'rejected',
              extraKeys: data[Object.keys(data)[i]][j].extraKeys,
              formData: data[Object.keys(data)[i]][j].formData,
              formSubmissionId: data[Object.keys(data)[i]][j].formId,
              uniqueId: data[Object.keys(data)[i]][j].uniqueId,
              checked: true,
              image: data[Object.keys(data)[i]][j].image,
              attachmentCount: 0,
              formDataHead: data[Object.keys(data)[i]][j].formDataHead
            }
            // await this.storage.get(ConstantProvider.dbKeyNames.form + "-" + this.userService.user.username)
            //   .then((val) => {
            if (localDbData != null) {
              mainFormsDataforSave = localDbData
              if (mainFormsDataforSave[formIdName] != undefined) {
                if (dbFormModel.uniqueId != Object.keys(mainFormsDataforSave[formIdName])[i]) {
                  formModel = mainFormsDataforSave[formIdName]
                  formModel[dbFormModel.uniqueId as any] = dbFormModel
                  mainFormsDataforSave[formIdName] = formModel
                  // } else if(dbFormModel.formStatus != mainFormsDataforSave[formIdName][Object.keys(mainFormsDataforSave[formIdName])[0]].formStatus){
                } else if (mainFormsDataforSave[formIdName][Object.keys(mainFormsDataforSave[formIdName])[i]].formStatus == 'sent') {
                  formModel = mainFormsDataforSave[formIdName]
                  formModel[dbFormModel.uniqueId as any] = dbFormModel
                  mainFormsDataforSave[formIdName] = formModel
                }
              } else {
                formModel[dbFormModel.uniqueId as any] = dbFormModel
                mainFormsDataforSave[formIdName] = formModel
              }
            } else {
              formModel[dbFormModel.uniqueId as any] = dbFormModel
              mainFormsDataforSave[formIdName] = formModel
            }
            localDbData = mainFormsDataforSave
            rejectionCount++
          }
        }
        await this.storage.set(ConstantProvider.dbKeyNames.form + "-" + this.userService.user.username, localDbData)

      }, err => {
        throw err
      })
    return rejectionCount
  }
  sleep() {
    return new Promise(resolve => setTimeout(resolve, 10000));
  }

  convertBlobToBase64 = blob => new Promise((resolve, reject) => {
    const reader = new FileReader;
    reader.onerror = reject;
    reader.onload = () => {
      resolve(reader.result);
    };
    reader.readAsDataURL(blob);
  });
  async pushCameraImagesAndAttachementsIntoApplicationFolder(data, formId, uniqueId): Promise<any> {

    const iWriteOptions: IWriteOptions = {
      replace: true
    }

    for (let index = 0; index < Object.keys(data).length; index++) {
      for (let j = 0; j < data[Object.keys(data)[index]].length; j++) {
        let subSections = data[Object.keys(data)[index]][0]
        for (let qs = 0; qs < Object.keys(subSections).length; qs++) {
          for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
            let question: IQuestionModel = subSections[Object.keys(subSections)[qs]][q]

            switch (question.controlType) {

              case "camera":
              case "file":
              case "mfile":
                {
                  if (question.attachmentsInBase64) {
                    let index = 0;
                    question.value = []
                    for (let path of question.attachmentsInBase64) {
                      let file = await this.http.get(question.controlType == 'camera' ? path : path.base64, { responseType: 'blob' }).toPromise()
                      let base64String = await this.convertBlobToBase64(file);
                      let imageBlob = await this.commonEngine.dataURItoBlob(base64String, this.commonEngine.getContentType(base64String))
                      await this.commonEngine.createFoldersInMobileDevice(formId, uniqueId, this.file, this.messageService).then((async d => {
                        let currentTime = +new Date();
                        let random = Math.random()
                        let trends = random + ""

                        if (question.controlType == "camera") {
                          let filename = trends.replace('.', '') + '_' + question.columnName + '_' + currentTime + ".jpg";
                          let writeToFilePath = this.file.externalRootDirectory + ConstantProvider.appFolderName + "/" + formId + "/" + uniqueId;
                          let writtenFile = await this.file.writeFile(writeToFilePath, filename, imageBlob, iWriteOptions)
                          question.attachmentsInBase64[index] = writtenFile.nativeURL
                          question.value[index] = writtenFile.nativeURL
                        } else {

                          let filename = trends.replace('.', '') + '_' + question.columnName + '_' + question.attachmentsInBase64[index].fileName;
                          let writeToFilePath = this.file.externalRootDirectory + ConstantProvider.appFolderName + "/" + formId + "/" + uniqueId;
                          let writtenFile = await this.file.writeFile(writeToFilePath, filename, imageBlob, iWriteOptions)
                          let f = {
                            fileType: question.attachmentsInBase64[index].fileType,
                            fileName: question.attachmentsInBase64[index].fileName,
                            fp: writtenFile.nativeURL,
                            basePath: writeToFilePath,
                            pathFileName: filename
                          };
                          question.attachmentsInBase64[index] = f
                        }
                      }))
                      index += 1;
                    }
                  }
                }
                break;
              case "beginrepeat":
                {
                  for (let bgindex = 0; bgindex < question.beginRepeat.length; bgindex++) {
                    let beginRepeatQuestions: IQuestionModel[] = question.beginRepeat[bgindex];
                    for (let beginRepeatQuestion of beginRepeatQuestions) {
                      if (beginRepeatQuestion.controlType == "file" || beginRepeatQuestion.controlType == "mfile" || beginRepeatQuestion.controlType == "camera") {
                        beginRepeatQuestion.value = []

                        let index = 0;

                        for (let path of beginRepeatQuestion.attachmentsInBase64) {
                          let file = await this.http.get(beginRepeatQuestion.controlType == 'camera' ? path : path.base64, { responseType: 'blob' }).toPromise()
                          let base64String = await this.convertBlobToBase64(file);
                          let imageBlob = await this.commonEngine.dataURItoBlob(base64String, this.commonEngine.getContentType(base64String))
                          await this.commonEngine.createFoldersInMobileDevice(formId, uniqueId, this.file, this.messageService).then((async d => {
                            let currentTime = +new Date();
                            let random = Math.random()
                            let trends = random + ""
                            let filename = trends.replace('.', '') + '_' + beginRepeatQuestion.columnName + '_' + currentTime + ".jpg";
                            let writeToFilePath = this.file.externalRootDirectory + ConstantProvider.appFolderName + "/" + formId + "/" + uniqueId;
                            let writtenFile = await this.file.writeFile(writeToFilePath, filename, imageBlob, iWriteOptions)
                            if (beginRepeatQuestion.controlType == "camera") {
                              beginRepeatQuestion.attachmentsInBase64[index] = writtenFile.nativeURL
                              beginRepeatQuestion.value[index] = writtenFile.nativeURL
                            } else {
                              let fileBasePath = writtenFile.nativeURL[0].substr(0, writtenFile.nativeURL[0].lastIndexOf('/') + 1);
                              let f = {
                                fileType: beginRepeatQuestion.attachmentsInBase64[index].fileType,
                                fileName: beginRepeatQuestion.attachmentsInBase64[index].fileName,
                                fp: writtenFile.nativeURL,
                                basePath: fileBasePath,
                                pathFileName: beginRepeatQuestion.attachmentsInBase64[index].fileName
                              };
                              beginRepeatQuestion.attachmentsInBase64[index] = f
                            }
                          }))
                          index += 1;
                        }


                      }
                    }
                  }
                }
                break;
            }
          }
        }
      }
    }
    return data
  }
}
