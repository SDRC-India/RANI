import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'areaFilter'
})
export class AreaFilterPipe implements PipeTransform {
  areas: any[] = [];

  transform(areas: any, areaLevel: number, areaLevelId: number): any {
    if(areas != undefined && areas != null && areaLevel != undefined && areaLevel != null && areaLevelId != undefined && areaLevelId != null ){      
      switch(areaLevel){
        case 1:
          this.areas= areas.STATE.filter(area => area.parentAreaId === areaLevelId);
          break;
        case 2:
        this.areas = areas.DISTRICT.filter(area => area.parentAreaId == areaLevelId);
        break;
        case 3:
        this.areas =  areas.BLOCK.filter(area => area.parentAreaId == areaLevelId);
        break;
        case 4:
          this.areas =  areas.VILLAGE.filter(area => area.parentAreaId == areaLevelId);
        break;       
      }     
      this.areas= this.areas.sort((area1: any, area2: any) => {
        var area1Name = area1.areaName.toLowerCase(), area2Name = area2.areaName.toLowerCase()
        if (area1Name < area2Name) //sort string ascending
          return -1
        if (area1Name > area2Name)
          return 1
        return 0 //default return value (no sorting)
      })
      return this.areas; 
    }
    else {
      return [];
    }
  }

}
