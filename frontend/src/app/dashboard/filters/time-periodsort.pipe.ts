import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'timePeriodsort'
})
export class TimePeriodsortPipe implements PipeTransform {

  transform(timeperiodList: any, args: string): any {
    if (timeperiodList) {
      timeperiodList.sort((a: any, b: any) => {
        if (a.tpId < b.tpId) { return -1; }
        else if (a > b) {
          return 1;
        }
        else {
          return 0;
        }
      });
      return timeperiodList;
    }
  }
}
