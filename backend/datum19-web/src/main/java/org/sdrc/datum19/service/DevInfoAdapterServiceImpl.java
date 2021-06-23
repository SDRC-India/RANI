package org.sdrc.datum19.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdrc.datum19.document.Area;
import org.sdrc.datum19.document.DataValue;
import org.sdrc.datum19.document.Indicator;
import org.sdrc.datum19.document.TimePeriod;
import org.sdrc.datum19.repository.AreaLevelRepository;
import org.sdrc.datum19.repository.AreaRepository;
import org.sdrc.datum19.repository.DataDomainRepository;
import org.sdrc.datum19.repository.IndicatorRepository;
import org.sdrc.datum19.repository.TimePeriodRepository;
import org.sdrc.datum19.util.CellOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DevInfoAdapterServiceImpl implements DevInfoAdapterService {

	@Autowired
	private AreaRepository areaRepository;
	
	@Autowired
	private AreaLevelRepository areaLevelRepository;
	
	@Autowired
	public IndicatorRepository indicatorRepository;
	
	@Autowired
	private DataDomainRepository dataDomainRepository;
	
	@Autowired
	private TimePeriodRepository timePeriodRepository;
	
	String filepath = "E:\\SDRC Info\\census_households\\";
	@Override
	public String importDevinfoData(Integer sourceCode, String sector, String subsector, String sourcepath) 
			throws InvalidFormatException, IOException {
		// TODO Auto-generated method stub
		filepath = sourcepath;
		Map<Integer, Integer> oldNewAreaMap = importArea();
//		Map<Integer, Integer> iuspMap = importIndicators(oldNewAreaMap, sourceCode, sector, subsector);
//		saveTimePeriods(sourceCode);
//		importData(oldNewAreaMap, iuspMap);
		return "success";
	}
	
	private Map<Integer, Integer> importArea() throws InvalidFormatException, IOException {
		XSSFWorkbook areaBook = new XSSFWorkbook(new File(filepath+"UT_Area_en.xlsx"));
		XSSFSheet areaSheet = areaBook.getSheet("UT_Area_en");
		Map<Integer, Integer> oldNewAreaMap = new HashMap<>();
		for (int rowNum = 1; rowNum <= areaSheet.getLastRowNum(); rowNum++) {
			XSSFRow row=areaSheet.getRow(rowNum);
			Area area = areaRepository.findByAreaCode(row.getCell(2).getStringCellValue());
			if(area==null) {
				area = new Area();
				area.setAreaCode(row.getCell(2).getStringCellValue());
				area.setAreaId((int)row.getCell(0).getNumericCellValue());
				area.setAreaName(row.getCell(3).getStringCellValue());
				area.setAreaLevel(areaLevelRepository.findByAreaLevelId((int)row.getCell(5).getNumericCellValue()));
				area.setParentAreaId((int)row.getCell(1).getNumericCellValue());
				areaRepository.save(area);
			} else {
				oldNewAreaMap.put((int)row.getCell(0).getNumericCellValue(), area.getAreaId());
			}
		}
		areaBook.close();
		return oldNewAreaMap;
	}

	private Map<Integer, Integer> importIndicators(Map<Integer, Integer> oldNewAreaMap, Integer sourceCode, String sector, String subsector) 
			throws InvalidFormatException, IOException {
		Map<Integer, String> unitMap = new HashMap<>();
		
//		unitMap.put(1, "number");
//		unitMap.put(1, "Per 1000 population");
//		unitMap.put(2, "Per 1000 women");
//		unitMap.put(3, "Percent");
//		unitMap.put(4, "Deaths per 1000 live births");
//		unitMap.put(5, "Deaths per 100,000 live births");
//		unitMap.put(6, "Years");
//		unitMap.put(7, "Girls per 1000 boys");
//		unitMap.put(8, "Per 1000 births");
//		unitMap.put(9, "Live births per woman");
		
		unitMap.put(1, "Per 1000 population");
		unitMap.put(2, "Live births per woman");
		unitMap.put(3, "Per 1000 population");
		unitMap.put(4, "Per woman");
		unitMap.put(5, "Number");
		unitMap.put(6, "Rupees");
		unitMap.put(7, "Girls per 1000 boys");
		unitMap.put(8, "Per 1000 births");
		
		XSSFWorkbook indicatorBook = new XSSFWorkbook(new File(filepath+"UT_Indicator_en.xlsx"));
		XSSFSheet indicatorSheet = indicatorBook.getSheet("UT_Indicator_en");
		Map<Integer, String> indicatorMap = new HashMap<>();
		Map<Integer, String> indicatorGroupMap = new HashMap<>();
		for (int rowNum = 1; rowNum <= indicatorSheet.getLastRowNum(); rowNum++) {
			XSSFRow row=indicatorSheet.getRow(rowNum);
			indicatorMap.put((int)row.getCell(0).getNumericCellValue(), row.getCell(1).getStringCellValue());
			indicatorGroupMap.put((int)row.getCell(0).getNumericCellValue(), sourceCode+indicatorGroupMap.get((int)row.getCell(0).getNumericCellValue()));
		}
		Map<Integer, String> highIsGoodMap = new HashMap<>();
		for (int rowNum = 1; rowNum <= indicatorSheet.getLastRowNum(); rowNum++) {
			XSSFRow row=indicatorSheet.getRow(rowNum);
			highIsGoodMap.put((int)row.getCell(0).getNumericCellValue(), String.valueOf(row.getCell(9).getBooleanCellValue()));
		}
		indicatorBook.close();
		

		XSSFWorkbook subgroupBook = new XSSFWorkbook(new File(filepath+"UT_Subgroup_en.xlsx"));
		XSSFSheet subgroupSheet = subgroupBook.getSheet("UT_Subgroup_en");
		Map<Integer, String> subgroupMap = new HashMap<>();
		Map<Integer, String> subgroupTypeMap = new HashMap<>();
		for (int rowNum = 1; rowNum <= subgroupSheet.getLastRowNum(); rowNum++) {
			XSSFRow row=subgroupSheet.getRow(rowNum);
			subgroupMap.put((int)row.getCell(0).getNumericCellValue(), row.getCell(1).getStringCellValue());
			subgroupTypeMap.put((int)row.getCell(0).getNumericCellValue(), sourceCode+String.valueOf((int)row.getCell(4).getNumericCellValue()));
		}
		subgroupBook.close();
		
		long count = indicatorRepository.count();
		
		Integer indicatorNid = null;
		XSSFWorkbook iusBook = new XSSFWorkbook(new File(filepath+"UT_Indicator_Unit_Subgroup.xlsx"));
		XSSFSheet iusSheet = iusBook.getSheet("UT_Indicator_Unit_Subgroup");
		Map<String, Object> iuspMap = new HashMap<>();
		Map<Integer, Integer> iusNidMapping = new HashMap<>();
		for (int rowNum = 1; rowNum <= iusSheet.getLastRowNum(); rowNum++) {
			count = (count+1);
			Indicator indicator = new Indicator();
			XSSFRow row=iusSheet.getRow(rowNum);
			indicatorNid = Integer.parseInt(sourceCode+String.valueOf(count));
			iusNidMapping.put((int)row.getCell(0).getNumericCellValue(), indicatorNid);
			String indicatorName = indicatorMap.get((int)row.getCell(1).getNumericCellValue()) + 
					"-" + subgroupMap.get((int)row.getCell(3).getNumericCellValue());
			iuspMap.put("indicatorNid", String.valueOf(indicatorNid));
			iuspMap.put("indicatorGid", sector+"_"+String.valueOf((int)row.getCell(1).getNumericCellValue()));
			iuspMap.put("formId", sourceCode);
			iuspMap.put("indicatorName", indicatorName);
			iuspMap.put("sector", sector);
			iuspMap.put("subsector", subsector);
			iuspMap.put("subgroup", String.valueOf(subgroupMap.get((int)row.getCell(3).getNumericCellValue())));
			iuspMap.put("subgroupType", String.valueOf(subgroupTypeMap.get((int)row.getCell(3).getNumericCellValue())));
			iuspMap.put("highIsGood", String.valueOf(highIsGoodMap.get((int)row.getCell(1).getNumericCellValue())));
			iuspMap.put("unit", String.valueOf(unitMap.get((int)row.getCell(2).getNumericCellValue())).toLowerCase());
			iuspMap.put("typeDetailId", "");
			iuspMap.put("source", "secondary");
			indicator.setIndicatorDataMap(iuspMap);
			indicatorRepository.save(indicator);
		}
		iusBook.close();
		return iusNidMapping;
	}
	
	/*private Map<Integer, Integer> saveTimePeriods(Integer sourceCode) throws InvalidFormatException, IOException {
		XSSFWorkbook tpBook = new XSSFWorkbook(new File(filepath+"UT_TimePeriod.xlsx"));
		XSSFSheet tpSheet = tpBook.getSheet("UT_TimePeriod");
		
		Map<Integer, Integer> tpMap = new HashMap<>();
		TimePeriod tp = new TimePeriod();
		TimePeriod duplicateId = new TimePeriod();
		int count = 0;
		
		for (int rowNum = 1; rowNum <= tpSheet.getLastRowNum(); rowNum++) {
			XSSFRow row=tpSheet.getRow(rowNum);
			tp = timePeriodRepository.findByTimePeriodDuration(row.getCell(1).getStringCellValue());
			duplicateId = timePeriodRepository.findByTimePeriodId((int)row.getCell(0).getNumericCellValue());
			if(tp == null && duplicateId==null) {
				tp.setTimePeriodId((int)row.getCell(0).getNumericCellValue());
				tp.setTimePeriodDuration(row.getCell(1).getStringCellValue());
				tpMap.put((int)row.getCell(0).getNumericCellValue(), (int)row.getCell(0).getNumericCellValue());
				timePeriodRepository.save(tp);
			} else if (tp == null && duplicateId!=null) {
				tp.setTimePeriodId(Integer.parseInt(String.valueOf(sourceCode)+row.getCell(0).getStringCellValue()));
				tp.setTimePeriodDuration(row.getCell(1).getStringCellValue());
				tpMap.put((int)row.getCell(0).getNumericCellValue(), tp.getTimePeriodId());
				timePeriodRepository.save(tp);
			} 
		}
		tpBook.close();
		return tpMap;
	}*/
	
	private String importData(Map<Integer, Integer> oldNewAreaMap, Map<Integer, Integer> iuspMap) throws InvalidFormatException, IOException {
		System.out.println("Started importing data . . .");
		XSSFWorkbook dataBook = new XSSFWorkbook(new File(filepath+"UT_Data.xlsx"));
		XSSFSheet dataSheet = dataBook.getSheet("UT_Data");
		
		for (int rowNum = 1; rowNum <= dataSheet.getLastRowNum(); rowNum++) {
			XSSFRow row=dataSheet.getRow(rowNum);
			DataValue dataObj = new DataValue();
			dataObj.setInid(Integer.parseInt(String.valueOf(iuspMap.get((int)row.getCell(1).getNumericCellValue()))));
			dataObj.setDatumId(Integer.parseInt(String.valueOf(oldNewAreaMap.get((int)row.getCell(3).getNumericCellValue()))));
//			dataObj.setTp(Integer.parseInt(String.valueOf(tpMap.get((int)row.getCell(2).getNumericCellValue()))));
			dataObj.setTp((int)row.getCell(2).getNumericCellValue());
			dataObj.setDataValue(Double.parseDouble(row.getCell(4).getStringCellValue()));
			dataObj.set_case("secondary_source");
			dataObj.setDatumtype("area");
			
			dataDomainRepository.save(dataObj);
		}
		dataBook.close();
		return "success";
	}
}
