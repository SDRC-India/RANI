import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'mediaExtraInfo'
})
/**
 * Filter data on selected video
 */
export class MediaExtraInfoPipe implements PipeTransform {

  transform(dataList: any, formId?: number, selectedVal?: string): any {
    if(formId ==3){
      return dataList.filter(indData => indData.extraInfo === selectedVal);
    }
    else{
      return dataList
    }
  }
}
