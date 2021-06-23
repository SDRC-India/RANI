// for line chart post request

interface DashboardLineChartSelectionRequestModel {
    startTimePeriodModel:any;
	
    endTimePeriodModel:any;
  
    indicatorId:number;
  
    areaCode:string;

    financialYearId:number;

    preodicity:number;

    cumulative:boolean;
}