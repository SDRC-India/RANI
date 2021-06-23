import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'clusterArea'
})
export class ClusterAreaPipe implements PipeTransform {
  areas: any[] = [];
  
  transform(areas: any, areaLevel: any): any {
    if(areas != undefined && areas != null && areaLevel != undefined && areaLevel != null){      
      if(areaLevel){
        this.areas = areas[areaLevel];
          //this.areas= data.filter(area => area.clusterName === areaLevel);
      }           
      return this.areas; 
    }
    else {
      return [];
    }
  }

}
