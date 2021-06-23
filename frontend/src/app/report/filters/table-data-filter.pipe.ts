import { Pipe, PipeTransform } from '@angular/core';

/**
 * Filter data based on tab selection
*/

@Pipe({
  name: 'tableDataFilter'
})
export class TableDataFilterPipe implements PipeTransform {

  transform(data: any, args?: any): any {
    return data.filter(datas => datas.rejected == args)
  }
}
