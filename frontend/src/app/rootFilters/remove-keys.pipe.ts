import { Pipe, PipeTransform } from '@angular/core';
/**
 * Remove the keys of object which we don't want to show
 */
@Pipe({
  name: 'removeKeys'
})
export class RemoveKeysPipe implements PipeTransform {

  transform(items: any, args?: any): any {
    if(items){
     const filteredItems = items.filter(item => !args.includes(item))  
     return filteredItems;
    }
   }
}
