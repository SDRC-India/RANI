import { Pipe, PipeTransform } from '@angular/core';

/**
 *  Search for areas on dropdown arealist
 */

@Pipe({
  name: 'dropdownSearch'
})
export class DropDownSearchrPipe implements PipeTransform {

  transform(value: any[], searchText: any): any[] {
    if(!value) return [];
    if (searchText) {
      return value.filter(details => {
        return JSON.stringify(details).toLowerCase().includes(searchText);        
      });
    }
    else {
      return value;
    }
  }
}
