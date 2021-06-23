import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'formOnDesgination'
})
export class FormOnDesginationPipe implements PipeTransform {

  transform(formList: any, desgSelection: string): any {
    let desgVal: string='';
    if (desgSelection == "003"){
      desgVal= "COMMUNITY FACILITATOR";
    }else if(desgSelection == "002"){
      desgVal= "SUPERVISOR";
    }
    if(desgSelection && formList)
    return formList.filter(datas => datas.designation == desgVal)
  }
}
