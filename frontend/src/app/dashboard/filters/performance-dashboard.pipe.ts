import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'performanceDashboard'
})
export class PerformanceDashboardPipe implements PipeTransform {

  transform(data: any, args?: any): any {
   return data.filter(datas => datas.role == args)
  }
  }


