import { Pipe, PipeTransform } from '@angular/core';
/**
 * Filter userlist based on user role selection
 */
@Pipe({
  name: 'roleWiseUser'
})
export class RoleWiseUserPipe implements PipeTransform {

  transform(totalArray: any, args?: any): any {
    if(totalArray &&  args) {
      return totalArray[args];
    }  
  }
}
