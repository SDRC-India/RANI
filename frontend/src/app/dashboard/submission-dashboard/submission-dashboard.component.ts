import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { DomSanitizer } from '@angular/platform-browser';
import { DashboardService } from '../services/dashboard.service';
import html2canvas from 'html2canvas';
import saveAs from 'save-as';
import { ThrowStmt } from '@angular/compiler';

declare var $: any;

@Component({
  selector: 'app-submission-dashboard',
  templateUrl: './submission-dashboard.component.html',
  styleUrls: ['./submission-dashboard.component.scss']
})
export class SubmissionDashboardComponent implements OnInit {
  tableData:  any=[];
  tableColumns : any ;
  lineChartData: any;
  lineChartVisible:boolean=false;
  selectedDropdown:any;
  performancedashboardNgModel: any = {};
  userTableData:any;
  filterData:any;
  tableDataCommunity:any=[] ;
  dataKeys:any;
  selectedFormName: string;
  selectedDesgName: string;

  constructor(public dashboardService: DashboardService, private router: Router, private http: HttpClient, private dom: DomSanitizer) {
  }

  ngOnInit() {
     this.dashboardService.performanceDashtype().subscribe(type=>{
      this.dashboardService.performanceDashboard = type;
      this.performancedashboardNgModel.type= this.dashboardService.performanceDashboard[0].id;
      this.selectedDesgName = 'Supervisor';
      
      this.getsupervisor();
    })
  }
  /**
   * Show line chart on row click
   * @param event 
   */
  showLineChart(event){
    this.selectedFormName = event.table.form;
    this.dashboardService.getSubmissionLineChartData(event.table.formId,this.performancedashboardNgModel.type).subscribe(data=>{
      this.lineChartData = data;       
      if(this.lineChartData.lineChart.length>0){
        $('.container.qualitative-form').css('opacity', '.5');         
        this.lineChartVisible=true;
      }else{
        this.lineChartVisible=false;
      }
      $("#previewModal").modal('show');
    })     
  }
  /**
   * Close line chart on close button click
   */
  closeViz(){
    $('.container.qualitative-form').css('opacity', ''); 
    this.lineChartVisible=false;
  }
  /**
   *  Get selected desigantion wise data on tablar format
   */
  getsupervisor(){
    this.lineChartVisible=false;
    this.dashboardService.performancedashboard(this.performancedashboardNgModel.type).subscribe(userlist=>{
          this.userTableData = userlist;
          this.dataKeys=Object.keys(userlist)
          this.tableData=userlist[this.dataKeys]['tableData'];
          this.tableColumns=userlist[this.dataKeys]['tableColumns'];
    })
  }
  /**
   * Call on tab selection
   * @param event 
   */
  detailsShow(event){
    if(event.index==0){
      this.selectedDesgName = 'Supervisor';
      this.lineChartVisible=false;
      this.getsupervisor();
    }
    if(event.index==1){
      this.selectedDesgName = 'Community Facilitator';
      this.lineChartVisible=false;
      this.getsupervisor();
    }
  }
  /**
   * Download charts to image
   * @param el 
   * @param id 
   * @param IndicatorName 
   */
  downloadChartToImage(el, id, IndicatorName) {
    let position:any = $(window).scrollTop();
    window.scrollTo(0,0);            
    $('.chart-head').css('display', 'none');
    $('.trend-close').css('display', 'none');

    html2canvas(document.getElementById(id), { logging: false }).then((canvas) => {
      canvas.toBlob((blob) => {       
        saveAs(blob, IndicatorName + ".jpg");            
        $('.chart-head').css('display', 'block');
        $('.trend-close').css('display', 'block');
      });
      }).catch(err=> {               
        $('.chart-head').css('display', 'block');
        $('.trend-close').css('display', 'block');
      });
      window.scrollTo(0, position);
  }
}
