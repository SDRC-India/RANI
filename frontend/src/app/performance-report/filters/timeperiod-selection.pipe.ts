import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'timeperiodSelection'
})
export class TimeperiodSelectionPipe implements PipeTransform {

  transform(timeperiodList: any, fromtimeperiodId?: number): any {
    if (fromtimeperiodId)
      return timeperiodList.filter(datas => datas.tpId >= fromtimeperiodId)
  }
}
