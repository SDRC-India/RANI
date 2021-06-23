import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { DashboardService } from '../services/dashboard.service';
import saveAs from 'save-as';
import { FormGroup, FormBuilder, FormControl, Validators } from '@angular/forms';
//import { ToastrService } from 'ngx-toastr';
import html2canvas from 'html2canvas';
import * as d3 from 'd3v4';
import { map, catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

declare var $: any;

@Component({
  selector: 'app-thematic-view',
  templateUrl: './thematic-view.component.html',
  styleUrls: ['./thematic-view.component.scss']
})
export class ThematicViewComponent implements OnInit {

  @ViewChild('screen') screen: ElementRef;
  @ViewChild('canvas') canvas: ElementRef;
  @ViewChild('downloadLink') downloadLink: ElementRef;

  selectedSector: any;
  selectedForm: any;
  sectorList: any = [];
  selectedIndicator: any;
  indicatorList: any = [];
  timePeriodList: any = [];
  selectedSectorName: string;
  selectedTimePeriod: any;
  allData: any;

  thematicData: any;
  lineChartData: any;
  dashboardSectorModels: any;
  indicatorModels: any;
  selectedFinancialYear: any;
  thematicMapDataModels: any[] = [];
  thematicdataModel;
  thematicMapLegendModels: any;

  selectedArea: any;
  chipModels: any[];
  lineChartVisible: boolean = false;
  areaSearch: any;
  selectedAreaLevel: string;
  selectedIndicatorUnit: string;
  filteredIndicator:any;

  constructor(public dashboardService: DashboardService, private fb: FormBuilder) { }

  ngOnInit() {
    this.dashboardService.getUserRoles().subscribe(data => {
      this.dashboardService.areaLevels = data;   /** Get area level details */
      this.dashboardService.snapshotView.selectedAreaLevelId = this.dashboardService.areaLevels[0].areaLevelId;
      this.selectedAreaLevel = this.dashboardService.areaLevels[0].areaLevelName;
    })
    this.dashboardService.getAreaDetails().subscribe(data => {
      this.dashboardService.areaDetails = data;  /** Get area details */
      //this.dashboardService.snapshotView.selectedBlockId = this.dashboardService.areaDetails['BLOCK'][0].areaId;
      this.dashboardService.snapshotView.selectedBlockId = this.dashboardService.areaDetails['DISTRICT'][0].areaId;
    })
    setTimeout(() => {
      this.dashboardService.getFormSectorMappingData().subscribe(res => {
        this.allData = res;         /** Get sector form mapping details */
        this.dashboardSectorModels = Object.keys(this.allData);
        this.selectedForm = this.dashboardSectorModels[0];
        this.formSelection();
      })
    }, 1000)
    this.dashboardService.getAllTimeperiods().subscribe(res => {
      this.dashboardService.timeperiodLists = res;  /** Get all timeperiods details */
      this.dashboardService.snapshotView.timeperiodId = this.dashboardService.timeperiodLists[0].tpId;
    })
  }
  /**
   * Get thematic data on section 
   */
  getThematicMapData() {
    let clusterId;
    if (this.dashboardService.snapshotView.selectedClusterId) {
      clusterId = this.dashboardService.snapshotView.selectedClusterId != 'All' ? this.dashboardService.snapshotView.selectedClusterId.split(" ")[1] : 3;
    }
    if (this.dashboardService.snapshotView.timeperiodId) {
      this.dashboardService.getThemeData(this.selectedIndicator.indicatorId, this.dashboardService.snapshotView.timeperiodId, this.dashboardService.snapshotView.selectedAreaLevelId,
        this.dashboardService.snapshotView.selectedBlockId ? this.dashboardService.snapshotView.selectedBlockId : this.dashboardService.snapshotView.selectedClusterId ? clusterId : '',
        this.selectedSector.sectorName).subscribe((res) => {
          this.thematicMapDataModels = res.thematicMapDataModels;
          this.thematicdataModel = res;
          this.thematicMapLegendModels = res.thematicMapLegendModels;
        });
    }
  }
  /**
   * Prevent drop down search box on enter click
   * @param event 
   */
  _handleKeydown(event: KeyboardEvent) {
    if (event.keyCode === 32) {
      // do not propagate spaces to MatSelect, as this would select the currently active option
      event.stopPropagation();
    }
  }
  /**
   * Select form from the list
   */
  formSelection() {
    if (this.selectedForm) {
      this.sectorList = this.allData[this.selectedForm];
      this.selectedSector = this.sectorList[0];
      this.lineChartVisible = false;
      this.sectorSelection();
    }
  }
  /**
   * Select sector from the list
   */
  sectorSelection() {
    if (this.selectedSector.formId) {
      this.dashboardService.getIndicatorsData(this.selectedSector.formId).subscribe((res) => {
        this.indicatorList = res;
        this.filteredIndicator =res;
        this.selectedIndicator = this.indicatorList[0];
        this.lineChartVisible = false;
        this.indicatorChange();
      });
    }
  }
  /**
   * select indicator from list
   */
  indicatorChange() {
    this.thematicMapDataModels = null;
    this.lineChartVisible = false;
    this.selectedIndicatorUnit = this.selectedIndicator.unit;
    this.selectedSectorName = this.selectedSector.formName;
    this.timePeriodChange();
  }
  /**
   * Select time period from list
   */
  timePeriodChange() {
    this.lineChartVisible = false;
    this.getThematicMapData();
    this.indicatorModels = [];
  }
  /**
   * Select area level from list
   * @param selectedAreaId 
   */
  getSelectedLevelAreas(selectedAreaId) {
    this.dashboardService.selectedBlockName = undefined;
    this.thematicMapDataModels = [];
    this.selectedAreaLevel = this.dashboardService.areaLevels[this.dashboardService.areaLevels.findIndex(d => d.areaLevelId === selectedAreaId)].areaLevelName
    if (selectedAreaId == 2) {
      this.dashboardService.getAllClusterAreas().subscribe(data => {
        let res = data;
        this.dashboardService.snapshotView.selectedBlockId = undefined;
        this.dashboardService.clusterAreaDetails = res;
        this.dashboardService.clusters = Object.keys(this.dashboardService.clusterAreaDetails);
        this.dashboardService.snapshotView.selectedClusterId = this.dashboardService.clusters[0];
        this.timePeriodChange();
      });
    } else {
      this.dashboardService.snapshotView.selectedBlockId = this.dashboardService.areaDetails['BLOCK'][0].areaId;
      this.timePeriodChange();
    }
  }
  /**
   * Display trend chart on map click
   * @param $event 
   */
  thematicMapClicked($event) {
    this.selectedArea = $event.selectedArea;
    this.dashboardService.getLineChartData(this.selectedIndicator.indicatorId, this.dashboardService.snapshotView.timeperiodId, this.selectedArea.areaId).subscribe(data => {
      this.lineChartData = data;
      if (data != null || data != undefined) {
        this.lineChartVisible = true;
      }
    })
  }
  /**
   * Close trend chart on click close button
   */
  closeViz() {
    this.lineChartVisible = false;
  }
  /**
   * Download charts to image
   * @param el 
   * @param id 
   * @param IndicatorName 
   * @param legend 
   */
  downloadChartToImage(el, id, IndicatorName, legend) {     
    let position:any = $(window).scrollTop();
     window.scrollTo(0, 0); 
      $('.chart-head').css('display', 'none');
      $('.trend-close').css('display', 'none');
      $('.line-chart').css('background', 'transparent');
    
    html2canvas(document.getElementById(id), { logging: false }).then((canvas) => {
      canvas.toBlob((blob) => {
        // $('.download-chart').css('display', "block");
        saveAs(blob, IndicatorName + ".jpg");      
          $('.chart-head').css('display', 'block');
          $('.trend-close').css('display', 'block');
          $('.line-chart').css('background', '#cbccce');
      });
    }).catch(err => {
        $('.chart-head').css('display', 'block');
        $('.trend-close').css('display', 'block');
        $('.line-chart').css('background', '#cbccce');
        console.log(err.message)
    });
    window.scrollTo(0, position);
  }
  /**
   * Download full page as PDF / Excel
   * @param exportType 
   */
  async downloadExcelPDF(exportType) {
    d3.selectAll("svg").attr("version", 1.1).attr("xmlns", "http://www.w3.org/2000/svg");
    let svgDocuments = document.getElementsByTagName("svg");
    let legentList: any = [];
    let tableDataList: any = [];
    let chartModel: any;
    let clusterId, clusterName;
    let svgs = [];

    for (let index = 0; index < this.thematicMapLegendModels.length; index++) {
      legentList.push({ 'range': this.thematicMapLegendModels[index].range, 'color': this.thematicMapLegendModels[index].color, 'rgbColor': this.convertHex(this.thematicMapLegendModels[index].color) })
    }
    legentList.push({ 'range': "Not Available", 'color': '#cccccc' })

    for (let j = 0; j < this.thematicMapDataModels.length; j++) {
      tableDataList.push({ 'areaName': this.thematicMapDataModels[j].areaName, 'value': this.thematicMapDataModels[j].value })
    }

    if (this.dashboardService.snapshotView.selectedClusterId) {
      clusterId = this.dashboardService.snapshotView.selectedClusterId != 'All' ? this.dashboardService.snapshotView.selectedClusterId.split(" ")[1] : 3;
      clusterName = this.dashboardService.snapshotView.selectedClusterId ? this.dashboardService.snapshotView.selectedClusterId : '';
    }

    for (let i = 0; i < svgDocuments.length; i++) {
      html2canvas(document.querySelector("#legend")).then(legendSection => {
        svgs.push(legendSection.toDataURL('image/png', 1.0));

        if (svgDocuments.item(i).id == 'thematicMap') {
          chartModel = {
            areaLevelId: this.selectedAreaLevel,
            areaId: this.dashboardService.snapshotView.selectedBlockId ? this.dashboardService.snapshotView.selectedBlockId : this.dashboardService.snapshotView.selectedClusterId ? clusterId : 3,
            formId: this.selectedForm,
            indicatorId: this.selectedIndicator.indicatorId.toString(),
            timePeriodId: this.dashboardService.snapshotView.timeperiodId,
            sector: this.selectedSector.sectorName,
            chartImageType: 'svg',
            svg: svgDocuments.item(i).outerHTML,
            legend: legentList,
            tableData: tableDataList,
            unit: this.selectedIndicatorUnit,
            clusterName: clusterName,
            thematicLegends: svgs
          }
        }

        let fileName = "thematic-view";
        this.dashboardService.exportData(chartModel, exportType).pipe(
          map((res: Blob) => res),
          catchError((res: Blob) => throwError(res))
        ).subscribe(data => {
          let ext: string = exportType == 'pdf' ? 'pdf' : 'xlsx'
          saveAs(data, fileName + "_" + new Date().getTime().toString() + "." + ext);
        },
          error => { }
        );
      });
    }
  }
  convertHex(hex) {
    hex = hex.replace('#', '');
    let r = parseInt(hex.substring(0, 2), 16);
    let g = parseInt(hex.substring(2, 4), 16);
    let b = parseInt(hex.substring(4, 6), 16);

    let result = 'rgba(' + r + ',' + g + ',' + b + ')';
    return result;
  }
}

