package org.sdrc.rani.service;

import static java.util.Comparator.comparing;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.sdrc.rani.document.TimePeriod;
import org.sdrc.rani.models.PerformanceData;
import org.sdrc.rani.repositories.CustomDesignationRepository;
import org.sdrc.rani.repositories.TimePeriodRepository;
import org.sdrc.rani.util.ExcelStyleSheet;
import org.sdrc.usermgmt.mongodb.domain.Designation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import in.co.sdrc.sdrcdatacollector.document.EnginesForm;
import in.co.sdrc.sdrcdatacollector.models.MessageModel;
import in.co.sdrc.sdrcdatacollector.mongorepositories.EngineFormRepository;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
@Service
public class ReportServiceImpl implements ReportService {

	@Autowired
//	@Qualifier("mongoDesignationRepository")
	private CustomDesignationRepository designationRepository;
	
	@Autowired
	private TimePeriodRepository timePeriodRepository;
	
	@Autowired
	private ConfigurableEnvironment configurableEnvironment;
	
	@Autowired
	private PerformanceReportServiceImpl performanceReportServiceImpl;
	
	@Autowired
	private RawDataReportServiceImpl rawDataReportServiceImpl;
	
	@Autowired
	private EngineFormRepository formRepository;
	
	@Override
	public ResponseEntity<MessageModel> getSubmissionReport(Integer formId, String designation, Integer startTp, Integer endTp) {

		Designation desg = designationRepository.findByCode(designation);
		TimePeriod startTimePeriod = timePeriodRepository.findByTimePeriodId(startTp);
		TimePeriod endTimePeriod = timePeriodRepository.findByTimePeriodId(endTp);

		List<TimePeriod> timeperiods=timePeriodRepository.findTimePeriodRange("1", startTp, endTp);
	
		Collections.sort(timeperiods, comparing(TimePeriod::getTimePeriodId));
		
		List<String> tpids=timeperiods.stream().map(m->m.getTimePeriodDuration()).collect(Collectors.toList());
		
		
		List<String> tpString = timeperiods.stream().map(v->v.getTimePeriodDuration()+"-"+v.getYear()).collect(Collectors.toList());
		
		PerformanceData performanceData = performanceReportServiceImpl.getPerformanceData(formId,designation, startTp, endTp);
		
		if(performanceData.getTableData().isEmpty()) {
			MessageModel setMessageModel = rawDataReportServiceImpl.setMessageModelDataNotFound();
			return new ResponseEntity<>(setMessageModel, HttpStatus.OK);
		}
		
		int mergeValue = ((endTimePeriod.getTimePeriodId()-startTimePeriod.getTimePeriodId()) + 1) * 3;

		HSSFWorkbook workbook;
		HSSFSheet sheet;
		Row row;
		Cell cell;
		Integer rowNum = 0, colNum = 0;

		try {

			workbook = new HSSFWorkbook();
			sheet = workbook.createSheet("submissionReport");

			CellStyle colStyleOdd = ExcelStyleSheet.getStyleForOddCell(workbook, false);
			CellStyle colStyleEven = ExcelStyleSheet.getStyleForEvenCell(workbook, false);
			CellStyle styleForHeading = ExcelStyleSheet.getStyleForPlanningModuleHeading(workbook);

			row = sheet.createRow(rowNum);
			cell = row.createCell(colNum);
			sheet.setColumnWidth(cell.getColumnIndex(), 6000);
			cell.setCellStyle(styleForHeading);
			cell.setCellValue("RANI: Submission Report for " + desg.getName() + " for (" + startTimePeriod
					.getTimePeriodDuration().concat("-").concat( endTimePeriod.getTimePeriodDuration()+" "+endTimePeriod.getYear()+")"));
			sheet = ExcelStyleSheet.doMerge(0, 0, 0, mergeValue, sheet);
			rowNum++;
			
			row = sheet.createRow(rowNum);
			cell = row.createCell(colNum);
			sheet.setColumnWidth(cell.getColumnIndex(), 6000);
			cell.setCellStyle(styleForHeading);
			cell.setCellValue("Date of Report Generation : " + new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
			sheet = ExcelStyleSheet.doMerge(1, 1, 0, mergeValue, sheet);
			rowNum++;

			
			row = sheet.createRow(rowNum);
			colNum=1;
			for(int i =0;i<tpids.size();i++) {
				
				cell = row.createCell(colNum);
				cell.setCellStyle(styleForHeading);
				cell.setCellValue(tpids.get(i));
				sheet = ExcelStyleSheet.doMerge(rowNum, rowNum, colNum, colNum+2, sheet);
				colNum=colNum+3;
				
			}
			rowNum++;
			
			colNum=0;
			row = sheet.createRow(rowNum);
			cell = row.createCell(colNum);
			sheet.setColumnWidth(cell.getColumnIndex(), 4000);
			cell.setCellStyle(styleForHeading);
			cell.setCellValue("Name");
			colNum++;
			for(int i =0;i<tpids.size();i++) { 
				
				cell = row.createCell(colNum);
				cell.setCellStyle(styleForHeading);
				cell.setCellValue("Ontime");
				colNum++;
				
				cell = row.createCell(colNum);
				cell.setCellStyle(styleForHeading);
				cell.setCellValue("Delayed");
				colNum++;
				
				cell = row.createCell(colNum);
				cell.setCellStyle(styleForHeading);
				cell.setCellValue("Invalid");
				colNum++;
				
			}
			
			rowNum++;
			colNum=0;
			for(int i=0;i<performanceData.getTableData().size();i++) {
				
				row = sheet.createRow(rowNum);
				colNum=0;
				Map<String, String> dataMap = performanceData.getTableData().get(i);
				cell = row.createCell(colNum);
				cell.setCellStyle(styleForHeading);
				cell.setCellValue(dataMap.get("name"));
				cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
				colNum++;
				
				for(String tp:tpString) {
					
					cell = row.createCell(colNum);
					
					if(dataMap.get("ontime_".concat(tp))==null) {
						cell.setCellValue("NA");
					}else {
						cell.setCellValue(dataMap.get("ontime_".concat(tp)));
					}
					cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
					colNum++;
					
					cell = row.createCell(colNum);
					
					if(dataMap.get("delayed_".concat(tp))==null) {
						cell.setCellValue("NA");
					}else {
						cell.setCellValue(dataMap.get("delayed_".concat(tp)));
					}
					cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
					colNum++;
					
					cell = row.createCell(colNum);
					
					if(dataMap.get("invalid_".concat(tp))==null) {
						cell.setCellValue("NA");
					}else {
						cell.setCellValue(dataMap.get("invalid_".concat(tp)));
					}
					cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
					colNum++;
				}
				
				rowNum++;
				
			}
			
			String dir = configurableEnvironment.getProperty("report.path");

			File file = new File(dir);

			/*
			 * make directory if doesn't exist
			 */
			if (!file.exists())
				file.mkdirs();

			String name = configurableEnvironment.getProperty("submission.report.file.name") + "_"
					+ desg.getName().toLowerCase() + "_" + new SimpleDateFormat("ddMMyyyyHHmmsssss").format(new Date())
					+ ".xls";

			String path = dir + "" + name;

			FileOutputStream fos = new FileOutputStream(new File(path));
			workbook.write(fos);
			fos.close();
			workbook.close();

			MessageModel model = new MessageModel();
			model.setMessage(path);
			model.setStatusCode(200);

			return new ResponseEntity<>(model, HttpStatus.OK);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public ResponseEntity<MessageModel> getRejectionReport(Integer formId, Integer startTp, Integer endTp) {

		EnginesForm form = formRepository.findByFormId(formId);
		
		TimePeriod startTimePeriod = timePeriodRepository.findByTimePeriodId(startTp);
		TimePeriod endTimePeriod = timePeriodRepository.findByTimePeriodId(endTp);

		List<TimePeriod> timeperiods=timePeriodRepository.findTimePeriodRange("1", startTp, endTp);
	
		Collections.sort(timeperiods, comparing(TimePeriod::getTimePeriodId));
		
		List<String> tpids=timeperiods.stream().map(m->m.getTimePeriodDuration()).collect(Collectors.toList());
		
		
		List<String> tpString = timeperiods.stream().map(v->v.getTimePeriodDuration()+"-"+v.getYear()).collect(Collectors.toList());
		
		PerformanceData performanceData = performanceReportServiceImpl.getRejectionData(formId, startTp, endTp);
		if(performanceData.getTableData().isEmpty()) {
			MessageModel setMessageModel = rawDataReportServiceImpl.setMessageModelDataNotFound();
			return new ResponseEntity<>(setMessageModel, HttpStatus.OK);
		}
		
		int mergeValue = ((endTimePeriod.getTimePeriodId()-startTimePeriod.getTimePeriodId()) + 1) * 3;

		HSSFWorkbook workbook;
		HSSFSheet sheet;
		Row row;
		Cell cell;
		Integer rowNum = 0, colNum = 0;

		try {

			workbook = new HSSFWorkbook();
			sheet = workbook.createSheet("rejReport");

			CellStyle colStyleOdd = ExcelStyleSheet.getStyleForOddCell(workbook, false);
			CellStyle colStyleEven = ExcelStyleSheet.getStyleForEvenCell(workbook, false);
			CellStyle styleForHeading = ExcelStyleSheet.getStyleForPlanningModuleHeading(workbook);

			row = sheet.createRow(rowNum);
			cell = row.createCell(colNum);
			sheet.setColumnWidth(cell.getColumnIndex(), 6000);
			cell.setCellStyle(styleForHeading);
			cell.setCellValue("RANI: Rejected Submission Report for " + form.getName() + " for (" + startTimePeriod
					.getTimePeriodDuration().concat("-").concat( endTimePeriod.getTimePeriodDuration()+" "+endTimePeriod.getYear()+")"));
			sheet = ExcelStyleSheet.doMerge(0, 0, 0, mergeValue, sheet);
			rowNum++;
			
			row = sheet.createRow(rowNum);
			cell = row.createCell(colNum);
			sheet.setColumnWidth(cell.getColumnIndex(), 6000);
			cell.setCellStyle(styleForHeading);
			cell.setCellValue("Date of Report Generation : " + new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
			sheet = ExcelStyleSheet.doMerge(1, 1, 0, mergeValue, sheet);
			rowNum++;

			
			row = sheet.createRow(rowNum);
			colNum=1;
			for(int i =0;i<tpids.size();i++) {
				
				cell = row.createCell(colNum);
				cell.setCellStyle(styleForHeading);
				cell.setCellValue(tpids.get(i));
				sheet = ExcelStyleSheet.doMerge(rowNum, rowNum, colNum, colNum+2, sheet);
				colNum=colNum+3;
				
			}
			rowNum++;
			
			colNum=0;
			row = sheet.createRow(rowNum);
			cell = row.createCell(colNum);
			sheet.setColumnWidth(cell.getColumnIndex(), 4000);
			cell.setCellStyle(styleForHeading);
			cell.setCellValue("Name");
			colNum++;
			for(int i =0;i<tpids.size();i++) { 
				
				cell = row.createCell(colNum);
				cell.setCellStyle(styleForHeading);
				cell.setCellValue("Total");
				colNum++;
				
				cell = row.createCell(colNum);
				cell.setCellStyle(styleForHeading);
				cell.setCellValue("Accepted");
				colNum++;
				
				cell = row.createCell(colNum);
				cell.setCellStyle(styleForHeading);
				cell.setCellValue("Rejected");
				colNum++;
				
			}
			
			rowNum++;
			colNum=0;
			for(int i=0;i<performanceData.getTableData().size();i++) {
				
				row = sheet.createRow(rowNum);
				colNum=0;
				Map<String, String> dataMap = performanceData.getTableData().get(i);
				cell = row.createCell(colNum);
				cell.setCellStyle(styleForHeading);
				cell.setCellValue(dataMap.get("name"));
				cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
				colNum++;
				
				for(String tp:tpString) {
					
					cell = row.createCell(colNum);
					
					if(dataMap.get("total_".concat(tp))==null) {
						cell.setCellValue("NA");
					}else {
						cell.setCellValue(dataMap.get("total_".concat(tp)));
					}
					cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
					colNum++;
					
					cell = row.createCell(colNum);
					
					if(dataMap.get("accepted_".concat(tp))==null) {
						cell.setCellValue("NA");
					}else {
						cell.setCellValue(dataMap.get("accepted_".concat(tp)));
					}
					cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
					colNum++;
					
					cell = row.createCell(colNum);
					
					if(dataMap.get("rejected_".concat(tp))==null) {
						cell.setCellValue("NA");
					}else {
						cell.setCellValue(dataMap.get("rejected_".concat(tp)));
					}
					cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
					colNum++;
				}
				
				rowNum++;
				
			}
			
			String dir = configurableEnvironment.getProperty("report.path");

			File file = new File(dir);

			/*
			 * make directory if doesn't exist
			 */
			if (!file.exists())
				file.mkdirs();

			String name = configurableEnvironment.getProperty("rejection.report.file.name") + "_"
					+ form.getName().toLowerCase() + "_" + new SimpleDateFormat("ddMMyyyyHHmmsssss").format(new Date())
					+ ".xls";

			String path = dir + "" + name;

			FileOutputStream fos = new FileOutputStream(new File(path));
			workbook.write(fos);
			fos.close();
			workbook.close();

			MessageModel model = new MessageModel();
			model.setMessage(path);
			model.setStatusCode(200);

			return new ResponseEntity<>(model, HttpStatus.OK);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public ResponseEntity<MessageModel> gethemocueReport(String areaLevel, Integer startTp, Integer endTp) {

		
		TimePeriod startTimePeriod = timePeriodRepository.findByTimePeriodId(startTp);
		TimePeriod endTimePeriod = timePeriodRepository.findByTimePeriodId(endTp);

		List<TimePeriod> timeperiods=timePeriodRepository.findTimePeriodRange("1", startTp, endTp);
	
		Collections.sort(timeperiods, comparing(TimePeriod::getTimePeriodId));
		
		List<String> tpids=timeperiods.stream().map(m->m.getTimePeriodDuration()).collect(Collectors.toList());
		
		
		List<String> tpString = timeperiods.stream().map(v->v.getTimePeriodDuration()+"-"+v.getYear()).collect(Collectors.toList());
		
		PerformanceData performanceData = performanceReportServiceImpl.getHemocueData(areaLevel, startTp, endTp);
		if(performanceData.getTableData().isEmpty()) {
			MessageModel setMessageModel = rawDataReportServiceImpl.setMessageModelDataNotFound();
			return new ResponseEntity<>(setMessageModel, HttpStatus.OK);
		}
		
		int mergeValue = ((endTimePeriod.getTimePeriodId()-startTimePeriod.getTimePeriodId()) + 1) * 4;

		HSSFWorkbook workbook;
		HSSFSheet sheet;
		Row row;
		Cell cell;
		Integer rowNum = 0, colNum = 0;

		try {

			workbook = new HSSFWorkbook();
			sheet = workbook.createSheet("hemocueReport");

			CellStyle colStyleOdd = ExcelStyleSheet.getStyleForOddCell(workbook, false);
			CellStyle colStyleEven = ExcelStyleSheet.getStyleForEvenCell(workbook, false);
			CellStyle styleForHeading = ExcelStyleSheet.getStyleForPlanningModuleHeading(workbook);

			row = sheet.createRow(rowNum);
			cell = row.createCell(colNum);
			sheet.setColumnWidth(cell.getColumnIndex(), 6000);
			cell.setCellStyle(styleForHeading);
			cell.setCellValue("RANI: Number of Women diagnosed as Normal, Mild, Moderate, Severely anemic"+ " for (" + startTimePeriod
					.getTimePeriodDuration().concat("-").concat( endTimePeriod.getTimePeriodDuration()+" "+endTimePeriod.getYear()+")"));
			sheet = ExcelStyleSheet.doMerge(0, 0, 0, mergeValue, sheet);
			rowNum++;
			
			row = sheet.createRow(rowNum);
			cell = row.createCell(colNum);
			sheet.setColumnWidth(cell.getColumnIndex(), 6000);
			cell.setCellStyle(styleForHeading);
			cell.setCellValue("Date of Report Generation : " + new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
			sheet = ExcelStyleSheet.doMerge(1, 1, 0, mergeValue, sheet);
			rowNum++;

			
			row = sheet.createRow(rowNum);
			colNum=1;
			for(int i =0;i<tpids.size();i++) {
				
				cell = row.createCell(colNum);
				cell.setCellStyle(styleForHeading);
				cell.setCellValue(tpids.get(i));
				sheet = ExcelStyleSheet.doMerge(rowNum, rowNum, colNum, colNum+3, sheet);
				colNum=colNum+4;
				
			}
			rowNum++;
			
			colNum=0;
			row = sheet.createRow(rowNum);
			cell = row.createCell(colNum);
			sheet.setColumnWidth(cell.getColumnIndex(), 4000);
			cell.setCellStyle(styleForHeading);
			cell.setCellValue("area");
			colNum++;
			for(int i =0;i<tpids.size();i++) { 
				
				cell = row.createCell(colNum);
				cell.setCellStyle(styleForHeading);
				cell.setCellValue("Severe");
				colNum++;
				
				cell = row.createCell(colNum);
				cell.setCellStyle(styleForHeading);
				cell.setCellValue("Moderate");
				colNum++;
				
				cell = row.createCell(colNum);
				cell.setCellStyle(styleForHeading);
				cell.setCellValue("Mild");
				colNum++;
				
				cell = row.createCell(colNum);
				cell.setCellStyle(styleForHeading);
				cell.setCellValue("Normal");
				colNum++;
			}
			
			rowNum++;
			colNum=0;
			for(int i=0;i<performanceData.getTableData().size();i++) {
				
				row = sheet.createRow(rowNum);
				colNum=0;
				Map<String, String> dataMap = performanceData.getTableData().get(i);
				cell = row.createCell(colNum);
				cell.setCellStyle(styleForHeading);
				cell.setCellValue(dataMap.get("area"));
				cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
				colNum++;
				
				for(String tp:tpString) {
					
					cell = row.createCell(colNum);
					cell.setCellType(CellType.STRING);
					
					if(dataMap.get("severe_".concat(tp))==null) {
						cell.setCellValue("NA");
					}else {
						cell.setCellValue(dataMap.get("severe_".concat(tp)));
					}
					cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
					colNum++;
					
					cell = row.createCell(colNum);
					cell.setCellType(CellType.STRING);
					
					if(dataMap.get("moderate_".concat(tp))==null) {
						cell.setCellValue("NA");
					}else {
						cell.setCellValue(dataMap.get("moderate_".concat(tp)));
					}
					cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
					colNum++;
					
					
					cell = row.createCell(colNum);
					cell.setCellType(CellType.STRING);
					
					if(dataMap.get("mild_".concat(tp))==null) {
						cell.setCellValue("NA");
					}else {
						cell.setCellValue(dataMap.get("mild_".concat(tp)));
					}
					cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
					colNum++;
					
					cell = row.createCell(colNum);
					cell.setCellType(CellType.STRING);
					
					if(dataMap.get("normal_".concat(tp))==null) {
						cell.setCellValue("NA");
					}else {
						cell.setCellValue(dataMap.get("normal_".concat(tp)));
					}
					cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
					colNum++;
					
				}
				
				rowNum++;
				
			}
			
			String dir = configurableEnvironment.getProperty("report.path");

			File file = new File(dir);

			/*
			 * make directory if doesn't exist
			 */
			if (!file.exists())
				file.mkdirs();

			String name = configurableEnvironment.getProperty("hemocue.report.file.name") + "_"
					+ "hemocue" + "_" + new SimpleDateFormat("ddMMyyyyHHmmsssss").format(new Date())
					+ ".xls";

			String path = dir + "" + name;

			FileOutputStream fos = new FileOutputStream(new File(path));
			workbook.write(fos);
			fos.close();
			workbook.close();

			MessageModel model = new MessageModel();
			model.setMessage(path);
			model.setStatusCode(200);

			return new ResponseEntity<>(model, HttpStatus.OK);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
	
}
