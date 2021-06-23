import { Component, OnInit, Input, OnChanges, Output, EventEmitter } from '@angular/core';
import { FormGroup, NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import save from 'save-as';
declare var $: any;

@Component({
  selector: 'sdrc-table',
  templateUrl: './table.component.html',
  styleUrls: ['./table.component.scss']
})
export class TableComponent implements OnInit, OnChanges {
  isDesc: any;
  direction: number;
  fixTableConfig: any = {};
  checkStatus: boolean = false;
  disableDeleteBtn: boolean = true;
  columns: any;
  columnValues: any[];
  userToken: any;

  p: number = 1;
  searchFilter: string;
  uploadReport: string;
  currentUrl: any;
  @Input() id: string;
  @Input() rowData: any[];
  @Input() columnData: any[];
  @Input() maxTableHeight: string;
  @Input() isFileUpload: boolean = false;

  //on 3dmode
  @Input() isThreeDimension: boolean = false;
  @Input() threeDColumnData: any[];

  //keys for sorting and exclude sorting column from table-sorting.directive
  @Input() sorting: boolean;
  @Input() sortExcludeColumn: string[];

  //keys for paginate and itemsPerpage from table-paginate.directive
  @Input() isPaginate: boolean = false;
  @Input() itemsPerPage: number;

  //isHeaderFixed from table-headerfix.directive
  @Input() headerFixed: boolean = false;

  //searchBox from table-searchBox.directive.ts
  @Input() searchBox: boolean = false;

  //download Pdf and Excel table-downloadPdf.directive and table-downloadExcel.directive
  @Input() downloadPdf: boolean = false;
  @Input() pdfName: string;
  @Input() downloadExcel: boolean = false;
  @Input() excelName: string;
  @Input() excelHeadings: any[];
  @Input() isTotalAvailable: boolean;

  //download pdf and excel by server side
  @Input() downloadPdfByServer: boolean = false;
  @Input() downloadExcelByServer: boolean = false;

  @Output() onActionButtonClicked: EventEmitter<any> = new EventEmitter<any>();
  @Output() onDownloadPdfByServerClicked: EventEmitter<any> = new EventEmitter<any>();
  @Output() onDownloadExcelByServerClicked: EventEmitter<any> = new EventEmitter<any>();
  @Output() onPreviewUserClicked: EventEmitter<any> = new EventEmitter<any>();
  @Output() onUploadButtonClicked: EventEmitter<any> = new EventEmitter<any>();
  @Output() onEditPlanButtonClicked: EventEmitter<any> = new EventEmitter<any>();

  constructor(private router: Router) { }

  ngOnInit() { }

  ngOnChanges() {
    if (localStorage.getItem('user_details')) {
      this.userToken = JSON.parse(localStorage.getItem('user_details'));
    }
    this.currentUrl = this.router.url;    
    if (this.rowData && !this.isPaginate)
      this.itemsPerPage = this.rowData.length;
    this.searchFilter = null;
    setTimeout(() => {
      if (this.headerFixed)
        this.setDuplicateFixedTable();
      if (this.maxTableHeight) {
        $('#table-fixed-container' + this.id).css("max-height", this.maxTableHeight)
      }
    })
    if (this.columnData) {
      /** Convert column names to camelcase */
      this.columnValues = JSON.parse(JSON.stringify(this.columnData));
      for (let index = 0; index < this.columnValues.length; index++) {
        if (this.columnValues[index] == 'reportingMonth') {
          this.columnValues[index] = 'Reporting month';
        }
        if (this.columnValues[index] == 'dateOfCreation') {
          this.columnValues[index] = 'Date of creation';
        }
        if (this.columnValues[index] == 'supervisorName') {
          this.columnValues[index] = 'Supervisor name';
        }
        if(this.columnValues[index] == 'formName'){
          this.columnValues[index] = 'Activities';
        }
        if(this.columnValues[index] == 'target'){
          this.columnValues[index] = 'Target';
        }
        if(this.columnValues[index] == 'dateOfModification'){
          this.columnValues[index] = 'Date of modification';
        }
      }
    }
  }
  appendHttp(link: string) {
    if (!link.startsWith('http')) {
      return 'http://' + link;
    }
    else {
      return link;
    }
  }
  sort(property) {
    if (property == 'Reporting month') {
      property = 'reportingMonth';
    }
    if (property == 'Date of creation') {
      property = 'dateOfCreation';
    }
    if (property == 'Supervisor name') {
      property = 'supervisorName';
    }
    this.isDesc = !this.isDesc; //change the direction    
    this.columns = property;
    this.direction = this.isDesc ? 1 : -1;
  };

  setDuplicateFixedTable() {
    this.fixTableConfig.headerHeight = $("#" + this.id + " > thead").height();
    this.fixTableConfig.width = $('#' + this.id).closest(".parent-tabl-container").width();
    $("#header-fixed" + this.id).height(this.fixTableConfig.headerHeight)
    $('#' + this.id + 'fixedcontainer').height(this.fixTableConfig.headerHeight)
    if ($(window).width() <= 768) {
      $("#header-fixed" + this.id).width(this.fixTableConfig.width)
    }
    else {
      $("#header-fixed" + this.id).width(this.fixTableConfig.width - 16)
    }
    //$('#' + this.id + 'fixedcontainer').width($("#" + this.id).parent()[0].scrollWidth)

    setTimeout(() => {
      let tempThis = this;
      $("#header-fixed" + this.id + " th").each(function (index) {
        $(this).width($($("#" + tempThis.id + " th")[index]).width())
        $($("#" + tempThis.id + " th")[index]).width($($("#" + tempThis.id + " th")[index]).width())
      })
    }, 1000)
  }

  fixTableHeader(event) {
    var offset = $(event.target).scrollTop();
    $("#header-fixed" + this.id).scrollLeft($(event.target).scrollLeft())
    if (offset >= this.fixTableConfig.headerHeight) {
      $("#header-fixed" + this.id).css("display", "block");
    }
    else if (offset < this.fixTableConfig.headerHeight) {
      $("#header-fixed" + this.id).hide();
    }
  }
  tableToExcel(id) {
    let itemsPerPage = this.itemsPerPage;
    this.itemsPerPage = this.rowData.length;
    setTimeout(() => {
      var htmls = "";
      var uri = 'data:application/vnd.ms-excel;base64,';
      var template = '<html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns="http://www.w3.org/TR/REC-html40"><head><!--[if gte mso 9]><xml><x:ExcelWorkbook><x:ExcelWorksheets><x:ExcelWorksheet><x:Name>{worksheet}</x:Name><x:WorksheetOptions><x:DisplayGridlines/></x:WorksheetOptions></x:ExcelWorksheet></x:ExcelWorksheets></x:ExcelWorkbook></xml><![endif]--></head><body><table>{table}</table></body></html>';
      var base64 = function (s) {
        return window.btoa(unescape(encodeURIComponent(s)));
      };

      var format = function (s, c) {
        return s.replace(/{(\w+)}/g, function (m, p) {
          return c[p];
        });
      };

      if (this.excelHeadings) {
        var tab_text = "";
        for (let i = 0; i < this.excelHeadings.length; i++) {
          const element = this.excelHeadings[i];
          tab_text = tab_text + "<div style='text-align: left; font-size: 20px; font-weight: bold'>" + Object.keys(element)[0] + ": " + Object.values(element)[0] + "</div>";
        }
        tab_text += "<table border='2px'>";
      }
      else
        var tab_text = "<table border='2px'>";
      var textRange; var j = 0;
      let tab = document.getElementById(id); // id of table

      for (j = 0; j < tab['rows'].length; j++) {
        if (j == 0)
          tab_text = tab_text + "<tr style='background-color: #00837F; color: #FFF; font-weight: bold' valign='top'>" + tab['rows'][j].innerHTML + "</tr>";
        else
          tab_text = tab_text + "<tr valign='top'>" + tab['rows'][j].innerHTML + "</tr>";
      }

      tab_text = tab_text + "</table>";
      tab_text = tab_text.replace(/<A[^>]*>|<\/A>/g, "");//remove if u want links in your table
      tab_text = tab_text.replace(/<img[^>]*>/gi, ""); // remove if u want images in your table
      tab_text = tab_text.replace(/<input[^>]*>|<\/input>/gi, ""); // reomves input params

      var ctx = {
        worksheet: 'Worksheet',
        table: tab_text
      };
      save(new Blob([tab_text], { type: "application/vnd.ms-excel" }), this.excelName + ".xls");
      setTimeout(() => {
        this.itemsPerPage = itemsPerPage;
      }, 500)
    }, 200)
  }
  getType(x) {
    return typeof x;
  }
  clearSearchText() {
    this.searchFilter = "";
  }
  createPdf(target, containerId) {
    let tempHeight = $("kendo-pdf-export").find("#" + containerId).css("max-height");
    let itemsPerPage = this.itemsPerPage;
    let sorting = this.sorting;
    this.itemsPerPage = this.rowData.length;
    this.sorting = false;
    $("kendo-pdf-export").find("#" + containerId).scrollTop(0)
    setTimeout(() => {
      $("kendo-pdf-export").find("#" + containerId).css("max-height", "none")
      target.saveAs(this.pdfName + ".pdf")
      setTimeout(() => {
        this.itemsPerPage = itemsPerPage;
        this.sorting = sorting;
        $("kendo-pdf-export").find("#" + containerId).css("max-height", tempHeight)
      }, 1000);
    }, 200)
  }
  public downloadPdfByServerClicked(tableId, tableData): void {
    let emittedData: any = { table: tableId, tableData: tableData };
    this.onDownloadPdfByServerClicked.emit(emittedData);
  }
  public downloadExcelByServerClicked(tableId, tableData): void {
    let emittedData: any = { table: tableId, tableData: tableData };
    this.onDownloadExcelByServerClicked.emit(emittedData);
  }
  public previewByClicked(tableRow) {
    let emittedData: any = tableRow;
    this.onPreviewUserClicked.emit(emittedData);
  }
  public submitUploadForm(event, fileName) {
    let emittedData: any = { fileName: fileName, event: event };
    this.onUploadButtonClicked.emit(emittedData);
  }
  public editPlanByClicked(tableRow){
    let emittedData: any = tableRow;
    this.onEditPlanButtonClicked.emit(emittedData);
  }
}
