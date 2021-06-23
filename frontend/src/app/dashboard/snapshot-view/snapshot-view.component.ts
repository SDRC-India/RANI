import { Component, OnInit } from '@angular/core';
import { DashboardService } from '../services/dashboard.service';
import html2canvas from 'html2canvas';
import saveAs from 'save-as';
import { NgxSpinnerService } from 'ngx-spinner';
import * as d3 from "d3v4";
declare var $: any;

@Component({
  selector: 'rmncha-snapshot-view',
  templateUrl: './snapshot-view.component.html',
  styleUrls: ['./snapshot-view.component.scss']
})
export class SnapshotViewComponent implements OnInit {

  selectedStateId: number;
  selectedDistrictId: number;
  selectedVillageId: number;
  selectedBlockId: number;
  snapShotData: any;
  boxData: any;
  subsectors: any;
  groupIndList: any;
  checkListName: string;
  dashboardService: DashboardService;
  // windowScrolled: boolean;
  sections: any;
  subSectors: any;
  selectedSecId: number;
  indicators: any;
  formId: number;
  indicatorsData: boolean = false;
  tabMode: boolean = false;
  viewportWidth: number;
  sectorName: any;
  selectedChart: boolean = true;
  toggleChart: boolean = false;
  stackedBarRawData: any;
  stackedBarFormattedData: any;
  stackLegends: any;
  chartType: any = 'threeDBar';
  selectedAreaLevelId: number;
  clusterId: any;

  constructor(private dashboardServiceprovider: DashboardService, private spinner: NgxSpinnerService) {
    this.dashboardService = dashboardServiceprovider;
  }
  ngOnInit() {
    this.viewportWidth = $(window).width();
    if (this.viewportWidth < 995) {
      $(".chart-right").removeClass("offset-md-2");
      $(".chart-right").removeClass("col-md-10").addClass("col-md-12");
      this.tabMode = true
    } else {
      this.tabMode = false;
    }

    this.dashboardService.snapshotView = {};

    this.dashboardService.getAreaDetails().subscribe(data => {
      this.dashboardService.areaDetails = data;
      this.dashboardService.snapshotView.selectedStateId = this.dashboardService.areaDetails['STATE'][0].areaId;
      this.dashboardService.snapshotView.selectedDistrictId = this.dashboardService.areaDetails['DISTRICT'][0].areaId;
      this.dashboardService.selectedStateName = this.dashboardService.areaDetails['STATE'][0].areaName;
      this.dashboardService.selectedDistrictName = this.dashboardService.areaDetails['DISTRICT'][0].areaName;
      // console.log(" area details");
    })

    // this.dashboardService.getUserRoles().subscribe(data => {
    //   this.dashboardService.areaLevels = data;
    //   this.dashboardService.snapshotView.selectedAreaLevelId = this.dashboardService.areaLevels[0].areaLevelId;
    //   console.log(" area level details");
    // })

    //  this.dashboardService.getCheckListData().subscribe(res => {
    //   this.dashboardService.checkListDetails = res;
    //   this.dashboardService.checkLists = Object.keys(this.dashboardService.checkListDetails);
    //   this.checkListName = this.dashboardService.checkLists[0];    

    //   this.dashboardService.getAllTimeperiods().subscribe(res => {
    //     this.dashboardService.timeperiodLists = res;
    //     this.dashboardService.snapshotView.timeperiodId = this.dashboardService.timeperiodLists[0].tpId;
    //     this.dashboardService.snapshotView.timeperiodName = this.dashboardService.timeperiodLists[this.dashboardService.timeperiodLists.length - 1].tpName;       
    //     this.selectSectors(this.checkListName);
    //   })     
    // })    

    this.dashboardService.getMediaVideoList().subscribe(data => {
      this.dashboardService.videoLists = data;
    })
    this.getDetailAreaLevel();
  }
  async getDetailAreaLevel() {
    let data = await this.dashboardService.getUserRoles().toPromise();
    this.dashboardService.areaLevels = data;
    // console.log("area level details");
    this.dashboardService.snapshotView.selectedAreaLevelId = this.dashboardService.areaLevels[0].areaLevelId;
    this.getCheckListData();
  }
  getCheckListData() {
    this.dashboardService.getCheckListData().toPromise().then(res => {
      this.dashboardService.checkListDetails = res;
      // console.log(" checklist details");
      this.dashboardService.checkLists = Object.keys(this.dashboardService.checkListDetails);
      this.checkListName = this.dashboardService.checkLists[0];
      this.getTimePeriodList();
    });
  }
  getTimePeriodList() {
    this.dashboardService.getAllTimeperiods().subscribe(res => {
      this.dashboardService.timeperiodLists = res;
      // console.log(" time details");
      this.dashboardService.snapshotView.timeperiodId = this.dashboardService.timeperiodLists[0].tpId;
      this.dashboardService.snapshotView.timeperiodName = this.dashboardService.timeperiodLists[this.dashboardService.timeperiodLists.length - 1].tpName;
      this.selectSectors(this.checkListName);
    })
  }

  getIndicators(arealevelId, areaId, sector, formId) {
    window.scrollTo(0, 100);
    if (sector && formId)
      this.dashboardService.getIndicatorData(arealevelId, areaId, sector, this.dashboardService.snapshotView.timeperiodId, formId).subscribe(res => {
        this.indicators = res;
      })
  }

  selectSectors(sector) {
    this.checkListName = sector;
    this.sections = this.dashboardService.checkListDetails[sector];
    this.formId = this.sections[0].formId;
    this.selectedSecId = this.sections[0].sectorId;
    this.sectorName = sector;
    if (this.dashboardService.videoLists)
      this.dashboardService.snapshotView.videoId = this.dashboardService.videoLists[0].value;

    if (this.formId != 6) {
      this.dashboardService.snapshotView.selectedAreaLevelId = this.dashboardService.areaLevels[0].areaLevelId;
      this.dashboardService.snapshotView.selectedBlockId = this.dashboardService.snapshotView.selectedDistrictId;
    } else {
      this.dashboardService.snapshotView.selectedAreaLevelId = undefined;
    }
    this.dashboardService.snapshotView.selectedVillageId = undefined;
    this.dashboardService.snapshotView.selectedClusterId = undefined;

    //this.getIndicators(this.dashboardService.snapshotView.selectedAreaLevelId ? this.dashboardService.snapshotView.selectedAreaLevelId : 0, this.selectedVillageId ? this.selectedVillageId : this.dashboardService.snapshotView.selectedDistrictId, this.sectorName, this.formId)
    this.getSelectedLevelAreas(this.dashboardService.snapshotView.selectedAreaLevelId);
  }

  getSelectedLevelAreas(selectedAreaId) {
    this.selectedBlockId = undefined;
    this.dashboardService.snapshotView.selectedVillageId = undefined;
    this.dashboardService.selectedBlockName = undefined;
    this.dashboardService.selectedVillageName = undefined;
    if (selectedAreaId == 2) {
      this.dashboardService.getAllClusterAreas().subscribe(data => {
        let res = data;
        this.dashboardService.clusterAreaDetails = res;
        this.dashboardService.clusters = Object.keys(this.dashboardService.clusterAreaDetails);
        this.dashboardService.snapshotView.selectedClusterId = 'All';
        this.selectClusterLevel(this.dashboardService.snapshotView.selectedClusterId);
      });
    } else {
      this.dashboardService.snapshotView.selectedBlockId = 3
      this.selectBlockLevel(this.dashboardService.snapshotView.selectedBlockId);
    }
  }

  selectBlockLevel(selectedBlockId) {
    this.selectedBlockId = selectedBlockId;
    this.dashboardService.selectedVillageName = undefined;
    this.selectedVillageId = undefined;
    this.clusterId = undefined;
    if (this.selectedBlockId && this.selectedBlockId != 3) {
      this.dashboardService.selectedBlockName = this.dashboardService.areaDetails.BLOCK.filter(d => d.areaId == selectedBlockId)[0].areaName;
      this.dashboardService.snapshotView.selectedVillageId = this.selectedBlockId;
    }
    if (this.selectedSecId)
      this.getIndicators(this.dashboardService.snapshotView.selectedAreaLevelId ? this.dashboardService.snapshotView.selectedAreaLevelId : 0,
        this.selectedBlockId ? this.selectedBlockId : this.selectedVillageId ? this.selectedVillageId : this.dashboardService.snapshotView.selectedDistrictId, this.sectorName, this.formId)
  }

  selectClusterLevel(selectedClusterId) {
    //let clusterId: any;
    this.selectedBlockId = undefined;
    this.dashboardService.selectedBlockName = undefined;
    this.dashboardService.selectedVillageName = undefined;
    this.selectedVillageId = undefined;
    this.dashboardService.snapshotView.selectedVillageId = undefined;
    if (selectedClusterId == 'All') {
      this.clusterId = 0;
    } else {
      this.clusterId = parseInt(selectedClusterId.split(' ')[1]);
      this.dashboardService.snapshotView.selectedVillageId = selectedClusterId.split(' ')[1];
    }
    if (this.selectedSecId)
      this.getIndicators(this.dashboardService.snapshotView.selectedAreaLevelId ? this.dashboardService.snapshotView.selectedAreaLevelId : 0,
        this.clusterId ? this.clusterId : this.selectedVillageId ? this.selectedVillageId : 0, this.sectorName, this.formId)
  }

  selectVillageLevel(selectedVillageId) {
    this.selectedVillageId = selectedVillageId;
    if (this.dashboardService.snapshotView.selectedAreaLevelId == 2 && selectedVillageId != this.clusterId)
      this.dashboardService.selectedVillageName = this.dashboardService.clusterAreaDetails[this.dashboardService.snapshotView.selectedClusterId].filter(d => d.areaId == selectedVillageId)[0].areaName;
    else
      if ((this.selectedVillageId != 4) && (this.selectedVillageId != 5) && selectedVillageId != this.clusterId)
        this.dashboardService.selectedVillageName = this.dashboardService.areaDetails.VILLAGE.filter(d => d.areaId == selectedVillageId)[0].areaName;
      else
        if (this.dashboardService.snapshotView.selectedAreaLevelId == 2 && selectedVillageId == this.clusterId)
          this.dashboardService.selectedVillageName = 'All';
        else
          this.dashboardService.selectedVillageName = null;

    if (this.selectedSecId) {
      if (this.clusterId == (this.selectedVillageId))
        this.selectedAreaLevelId = 2;
      else
        this.selectedAreaLevelId = 4;
    }
    this.getIndicators(this.selectedAreaLevelId ? this.selectedAreaLevelId : this.dashboardService.snapshotView.selectedAreaLevelId,
      selectedVillageId ? selectedVillageId : this.dashboardService.snapshotView.selectedDistrictId, this.sectorName, this.formId)
  }

  selectTimePeriod(tp, event) {
    if (event.isUserInput == true) {
      this.dashboardService.snapshotView.timeperiodName = tp.tpName;
      this.dashboardService.snapshotView.timeperiodId = tp.tpId;

      if (this.selectedSecId)
        if (this.selectedVillageId) {
          if (this.clusterId == (this.selectedVillageId))
            this.selectedAreaLevelId = 2;
          else
            this.selectedAreaLevelId = 4;
        } else {
          this.selectedAreaLevelId = undefined;
        }
    
      this.getIndicators(this.selectedAreaLevelId ? this.selectedAreaLevelId : this.dashboardService.snapshotView.selectedAreaLevelId ? this.dashboardService.snapshotView.selectedAreaLevelId : 0,
        this.selectedVillageId ? this.selectedVillageId : this.selectedBlockId ? this.selectedBlockId : this.clusterId ? this.clusterId : 0, this.sectorName, this.formId)
    }
  }

  getStackedBar(chartData) {
    this.stackedBarRawData = chartData;
    this.stackedBarFormattedData = this.convertToStack(this.stackedBarRawData);
    return this.stackedBarFormattedData;
  }

  // convert to stacked bar chart data
  convertToStack(stack) {
    let formattedData: any[] = [];
    let stackEl: any = {};
    for (let i = 0; i < stack.length; i++) {
      const el = stack[i];
      for (let j = 0; j < el.length; j++) {
        const sEl = el[j];
        let axis = sEl.axis; //DH
        let value = sEl.value;
        let label = sEl.label; // 12
        let denominator = label + ' denominator';
        let numerator = label + ' numerator';
        let unit = label + ' unit';
        if (!stackEl[axis]) {          //sEl["DH"]
          stackEl[axis] = {};
          stackEl[axis][label] = sEl.value;
          stackEl[axis][denominator] = sEl.denominator;
          stackEl[axis][numerator] = sEl.numerator;
          stackEl[axis][unit] = sEl.unit;

        } else {
          stackEl[axis][label] = sEl.value;
          stackEl[axis].axis = axis;
          stackEl[axis].value = value;
          stackEl[axis][denominator] = sEl.denominator;
          stackEl[axis][numerator] = sEl.numerator;
          stackEl[axis][unit] = sEl.unit;
        }
      }
    }
    for (let k = 0; k < Object.keys(stackEl).length; k++) {
      const axis = Object.keys(stackEl)[k];
      formattedData.push(stackEl[axis]);
    }
    return formattedData;
  }

  selectChart(chart, chartName) {
    chart.selectedChart = chartName;
  }

  /**
   * Download each image on click
   * @param el 
   * @param id 
   * @param indicatorName 
   */
  downloadChartToImage(el, id, indicatorName) {
    let position: any = $(window).scrollTop();
    window.scrollTo(0, 0);
    $('.download-chart').css('display', "none");
    html2canvas(document.getElementById(id), { backgroundColor: null, logging: false }).then((canvas) => {
      canvas.toBlob((blob) => {
        $('.download-chart').css('display', "block");
        saveAs(blob, indicatorName + '_' + this.dashboardService.snapshotView.timeperiodName + ".jpg");
      });
    });
    // window.scrollTo(0, (document.getElementById(id).style.height));
    window.scrollTo(0, position);
  }

  /**
 * Download full page content as pdf / excel
 * @param districtName 
 * @param blockName 
 * @param stateName
 * @param areaLevelId
 */
  async downloadAllChartsToImage(areaLevelName, stateName, districtName, blockName, villageName, type, fileType, checkListName, timePeriod) {
    this.spinner.show();
    let barccards: any = [];
    let chartSvgs = [];
    let cdivIds = $(".chart-div:visible").map(function () {
      if (this.id) {
        return this.id;
      }
    }).get();

    d3.selectAll("svg").attr("version", 1.1).attr("xmlns", "http://www.w3.org/2000/svg");
    $('.download-chart').css('display', "none");
    for (let countCDiv = 0; countCDiv < cdivIds.length; countCDiv++) {
      $('#' + cdivIds[countCDiv]).children().each(function (index, el) {

        let chartSvg = $(this).children().html();
        if (chartSvg != "")
          chartSvgs.push(chartSvg);

        let base64Cards: any = {
          "indicatorGroupName": "",
          "svg": "",
          "chartType": "",
          "chartAlign": "",
          "showValue": "",
          "showNName": "",
          "indName": ""
        };
        base64Cards.indicatorGroupName = $('#' + cdivIds[countCDiv]).attr('grpid');
        base64Cards.chartType = $('#' + cdivIds[countCDiv]).attr('chartType');
        base64Cards.chartAlign = $('#' + cdivIds[countCDiv]).attr('align');
        base64Cards.showValue = $('#' + cdivIds[countCDiv]).attr('nVal') ? parseInt($('#' + cdivIds[countCDiv]).attr('nVal')) : $('#' + cdivIds[countCDiv]).attr('nVal');
        base64Cards.showNName = $('#' + cdivIds[countCDiv]).attr('showNName');
        base64Cards.indName = $('#' + cdivIds[countCDiv]).attr('indName');
        base64Cards.svg = chartSvg;
        barccards.push(base64Cards);
      });
    }
    this.dashboardService.downloadFullpageSvg(barccards, areaLevelName, stateName, districtName, blockName, villageName, type, checkListName, timePeriod).subscribe(res => {
      saveAs(res, (stateName != undefined && districtName != undefined && blockName != undefined) ?
        stateName + "_" + districtName + "_" + blockName + "_" + new Date().getTime().toString() + ".pdf" :
        (stateName != undefined && districtName == undefined && blockName == undefined) ?
          stateName + "_" + new Date().getTime().toString() + ".pdf" :
          (stateName != undefined && districtName != undefined && blockName == undefined) ?
            stateName + "_" + districtName + "_" + new Date().getTime().toString() + ".pdf" :
            this.dashboardService.areaLevelName + "_" + new Date().getTime().toString() + ".pdf");
      $('.download-chart').css('display', "block");
      this.spinner.hide();
    }, err => {
      $('.download-chart').css('display', "block");
      this.spinner.hide();
    });
  }
  async downloadAllChartsToImageInExcel(areaLevelName, areaLevelId, stateName, districtName,
    blockName, villageName, checkListName, sectionName, selectedSecId, timeperiod, type, fileType) {
    this.spinner.show();
    let barccards: any = [];
    let chartSvgs = [];
    let cdivIds = $(".chart-div:visible").map(function () {
      if (this.id) {
        return this.id;
      }
    }).get();
    $('.download-chart').css('display', "none");
    let paramModel: any = {
      "areaLevelId": areaLevelId ? areaLevelId : null,
      "stateName": stateName ? stateName : null,
      "districtName": districtName ? districtName : null,
      "blockName": blockName ? blockName : null,
      "villageName": villageName ? villageName : null,
      "dashboardType": type ? type : null,
      "districtId": this.dashboardService.snapshotView.selectedDistrictId ? this.dashboardService.snapshotView.selectedDistrictId : null,
      "blockId": this.selectedBlockId ? this.selectedBlockId : null,
      "stateId": this.dashboardService.snapshotView.selectedStateId ? this.dashboardService.snapshotView.selectedStateId : null,
      "villageId": this.dashboardService.snapshotView.selectedVillageId ? this.dashboardService.snapshotView.selectedVillageId : null,
      "sectorId": this.selectedSecId,
      "tpId": this.dashboardService.snapshotView.timeperiodId,
      "formId": this.formId ? this.formId : null,
      "listOfSvgs": [],
      "areaLevelName": areaLevelName ? areaLevelName : null,
      "checklistName": checkListName ? checkListName : null,
      "sectorName": sectionName ? sectionName : null,
      "timeperiod": timeperiod
    };
    // for (let i = 0; i < divIds.length; i++) {
    d3.selectAll("svg").attr("version", 1.1).attr("xmlns", "http://www.w3.org/2000/svg");
    $('.download-chart').css('display', "none");
    for (let countCDiv = 0; countCDiv < cdivIds.length; countCDiv++) {
      $('#' + cdivIds[countCDiv]).children().each(function (index, el) {

        let chartSvg = $(this).children().html();
        if (chartSvg != "")
          chartSvgs.push(chartSvg);

        let base64Cards: any = {
          "indicatorGroupName": "",
          "svg": "",
          "chartType": "",
          "chartAlign": "",
          "showValue": "",
          "showNName": "",
          "indName": ""
        };
        base64Cards.indicatorGroupName = $('#' + cdivIds[countCDiv]).attr('grpid');
        base64Cards.chartType = $('#' + cdivIds[countCDiv]).attr('chartType');
        base64Cards.chartAlign = $('#' + cdivIds[countCDiv]).attr('align');
        base64Cards.showValue = $('#' + cdivIds[countCDiv]).attr('nVal');
        base64Cards.showNName = $('#' + cdivIds[countCDiv]).attr('showNName');
        base64Cards.indName = $('#' + cdivIds[countCDiv]).attr('indName');
        base64Cards.svg = chartSvg;
        // barccards.push(base64Cards);
        paramModel.listOfSvgs.push(base64Cards);
        barccards.push(paramModel);
      });
    }
    if (fileType === 'Excel') {
      this.dashboardService.downloadFullpageExcel(paramModel).subscribe(res => {
        saveAs(res, (stateName != undefined && districtName != undefined && blockName != undefined) ?
          stateName + "_" + districtName + "_" + blockName + "_" + new Date().getTime().toString() + ".xlsx" :
          (stateName != undefined && districtName == undefined && blockName == undefined) ?
            stateName + "_" + new Date().getTime().toString() + ".xlsx" :
            (stateName != undefined && districtName != undefined && blockName == undefined) ?
              stateName + "_" + districtName + "_" + new Date().getTime().toString() + ".xlsx" :
              this.dashboardService.areaLevelName + "_" + new Date().getTime().toString() + ".xlsx");
        $('.download-chart').css('display', "block");
        this.spinner.hide();
      }, err => {
        $('.download-chart').css('display', "block");
        this.spinner.hide();
      });
    }
  }
  removeReference(data) {
    return JSON.parse(JSON.stringify(data));
  }
  selectedChartType(data) {
    return JSON.parse(JSON.stringify(data));
  }
  cardData(indicators) {
    // return (cardIndicatorVal);
    return JSON.parse(JSON.stringify(indicators));
  }
  getStackKeys(dataArr) {
    let allKeys = Object.keys(dataArr[0]);
    allKeys.forEach(element => {
      let addedKey = element;
      if (addedKey.includes('denominator') || addedKey.includes('numerator') || addedKey.includes('unit') || addedKey.includes('axis')
        || addedKey.includes('value')) {
        let index = allKeys.indexOf(addedKey);
        delete allKeys[index];
        allKeys = allKeys.filter(item => (addedKey.includes('denominator') || addedKey.includes('numerator') || addedKey.includes('unit') || addedKey.includes('axis')
          || addedKey.includes('value')))
      }
    });
    return allKeys;
  }
  showLists() {
    $(".dashboard-list").attr("style", "display: block !important");
    $('.mob-left-list').attr("style", "display: none !important");
  }
  ngAfterViewInit() {
    $('body,html').click(function (e) {
      if ((window.innerWidth) <= 992) {
        if (e.target.className == "mob-left-list") {
          return;
        } else {
          $(".dashboard-list").attr("style", "display: none !important");
          $('.mob-left-list').attr("style", "display: block !important");
        }
      }
    });
  }
}
