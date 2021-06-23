import { Pipe, PipeTransform } from '@angular/core';

/**
 * Filters the child area depends upon the parent area from arealist
 */

@Pipe({
  name: 'areaFilter'
})
export class AreaFilterPipe implements PipeTransform {

  transform(areas: any, areaLevel: number, parentAreaId: number[]): IArea[] {
    
    if(areas != undefined && areas != null && areaLevel != undefined && areaLevel != null && parentAreaId != undefined && parentAreaId != null ){      
    
      switch(areaLevel){       
        case 2:
          return areas.DISTRICT.filter(area => area.parentAreaId == parentAreaId)
        case 3:
          return areas.BLOCK.filter(area => area.parentAreaId == parentAreaId)
        case 4:
          return areas.VILLAGE.filter(area => parentAreaId.indexOf(area.parentAreaId) !=-1)  
      }      
    }
    else {
      return [];
    }
  }

}
