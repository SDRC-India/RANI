import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'sortPipe'
})
export class SortPipePipe implements PipeTransform {

    transform(rowData: any[], args: any): any {
        if (rowData != undefined && rowData != null) {
            let sortedData = rowData.sort(function (a, b) {
                let rowdataA = parseFloat(a[args.property]);
                let rowdataB = parseFloat(b[args.property]);
                if (isNaN(rowdataA) && isNaN(rowdataB)) {
                    if (a[args.property] < b[args.property]) {
                        return -1 * args.direction;
                    }
                    else if (a[args.property] > b[args.property]) {
                        return 1 * args.direction;
                    }
                    else {
                        return 0;
                    }
                } else if (!isNaN(rowdataA) && !isNaN(rowdataB)) {
                    if (rowdataA < rowdataB) {
                        return -1 * args.direction;
                    }
                    else if (rowdataA > rowdataB) {
                        return 1 * args.direction;
                    }
                    else {
                        return 0;
                    }
                }
            });
            return sortedData;
        }
    };
}
