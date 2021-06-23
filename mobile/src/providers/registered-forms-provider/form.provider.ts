import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ConstantProvider } from '../constant/constant';
import { Storage } from '@ionic/storage';
import { UserServiceProvider } from '../user-service/user-service';
import { FormSearchPipe } from '../../pipes/form-search/form-search';

/*
  Generated class for the RegisteredAnganwadiFormProvider provider.

  See https://angular.io/guide/dependency-injection for more info on providers
  and Angular DI.
*/
@Injectable()
export class FormProvider {

  formList: any;
  constructor(public http: HttpClient, public storage: Storage, public constantService: ConstantProvider, public userService: UserServiceProvider,
    private formSearch: FormSearchPipe) {
  }

  async findAllFroms(formId):Promise<IDbFormModel[]>{

   let d = await this.userService.getUserAndForm()

    // console.log(d)

    let promise: Promise<IDbFormModel[]> = new Promise((resolve, reject)=>{
      this.storage.get(ConstantProvider.dbKeyNames.form+"-"+(d['user'].username))
      .then(data=>{
        if(data != null){
          this.formList = data[formId];
          resolve(this.formList)
        }else{
          resolve([])
        }
      })
      .catch(err=>{
        console.log(err)
        reject(err.message)
      })
    })
    return promise;
  }
  changeStatusOfRecord(record: IDbFormModel, formId: any, status:any){
    let mainFormsDataforSave: {} = {}
    let dataModel: IDbFormModel[] = [];
    let promise: Promise<IDbFormModel> = new Promise((resolve, reject)=>{
      this.storage.get(ConstantProvider.dbKeyNames.form+"-"+this.userService.user.username)
      .then(async data=>{
        if(data){
          mainFormsDataforSave = data
          dataModel = data[formId]
          for(let i =0; i< Object.keys(dataModel).length;i++){
            if(dataModel[Object.keys(dataModel)[i]].uniqueId === record.uniqueId){
              //record found, need to splice and enter new
              dataModel[Object.keys(dataModel)[i]].formStatus = status
            }
          }
          mainFormsDataforSave[formId] = dataModel
          await this.storage.set(ConstantProvider.dbKeyNames.form+"-"+this.userService.user.username,mainFormsDataforSave)
          resolve(data)
        }
      })
      .catch(err=>{
        reject(err.message)
      })
    })
    return promise;
  }
  async deleteSingleRecord(record: IDbFormModel, formId: any){
    let mainFormsDataforSave: {} = {}
    let dataModel: IDbFormModel[] = [];
   
      let promise: Promise<IDbFormModel> = new Promise((resolve, reject)=>{
        this.storage.get(ConstantProvider.dbKeyNames.form+"-"+this.userService.user.username)
        .then(data=>{
          if(data){
            mainFormsDataforSave = data
            dataModel = data[formId]
            for(let i =0; i< Object.keys(dataModel).length;i++){
              if(dataModel[Object.keys(dataModel)[i]].uniqueId === record.uniqueId){
                //record found, need to splice and enter new
                delete dataModel[Object.keys(dataModel)[i]]
              }
            }
            mainFormsDataforSave[formId] = dataModel
            this.storage.set(ConstantProvider.dbKeyNames.form+"-"+this.userService.user.username,mainFormsDataforSave)
            resolve(data)
          }
        })
        .catch(err=>{
          reject(err.message)
        })
      })
      return promise;
   
   
  }

  /**
   * This method will help us getting searched patients
   * @param patients The whole patient list from which we have search
   * @param searchTerm The string to which we will search
   * @author Ratikanta
   * @since 0.0.1
  */
  getSearchFormList(formSubmissions ,searchTerm: string): IDbFormModel[]{

    return this.formSearch.transform(formSubmissions, searchTerm,'')
  }

}
