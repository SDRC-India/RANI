package org.sdrc.rani.service;

import static org.springframework.data.mongodb.core.aggregation.ConditionalOperators.when;
import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.commons.lang.time.DateUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdrc.rani.document.AchievementData;
import org.sdrc.rani.document.Area;
import org.sdrc.rani.document.AreaLevel;
import org.sdrc.rani.document.CFInputFormData;
import org.sdrc.rani.document.ClusterDataValue;
import org.sdrc.rani.document.ClusterForAggregation;
import org.sdrc.rani.document.DataValue;
import org.sdrc.rani.document.GroupIndicator;
import org.sdrc.rani.document.Indicator;
import org.sdrc.rani.document.PlanningData;
import org.sdrc.rani.document.Sector;
import org.sdrc.rani.document.TimePeriod;
import org.sdrc.rani.document.UserAreaMap;
import org.sdrc.rani.models.AreaModel;
import org.sdrc.rani.models.ChartDataModel;
import org.sdrc.rani.models.DateModel;
import org.sdrc.rani.models.FormSectorModel;
import org.sdrc.rani.models.GroupChartDataModel;
import org.sdrc.rani.models.IndicatorGroupModel;
import org.sdrc.rani.models.IndicatorModel;
import org.sdrc.rani.models.LegendModel;
import org.sdrc.rani.models.PerformanceData;
import org.sdrc.rani.models.SectorModel;
import org.sdrc.rani.models.SubSectorModel;
import org.sdrc.rani.models.ThematicDashboardDataModel;
import org.sdrc.rani.models.TimePeriodModel;
import org.sdrc.rani.repositories.AchievementDataRepository;
import org.sdrc.rani.repositories.AreaLevelRepository;
import org.sdrc.rani.repositories.AreaRepository;
import org.sdrc.rani.repositories.ClusterDataValueRepository;
import org.sdrc.rani.repositories.ClusterForAggregationRepository;
import org.sdrc.rani.repositories.CumulativeDataRepository;
import org.sdrc.rani.repositories.DataDomainRepository;
import org.sdrc.rani.repositories.EnginesFormRepository;
import org.sdrc.rani.repositories.GroupIndicatorRepository;
import org.sdrc.rani.repositories.IndicatorCssClassGrouprepository;
import org.sdrc.rani.repositories.IndicatorRepository;
import org.sdrc.rani.repositories.SectorRepository;
import org.sdrc.rani.repositories.TimePeriodRepository;
import org.sdrc.rani.repositories.UserAreaMapRepository;
import org.sdrc.rani.util.Constants;
import org.sdrc.rani.util.ExcelStyleSheet;
import org.sdrc.rani.util.HeaderFooter;
import org.sdrc.usermgmt.model.Mail;
import org.sdrc.usermgmt.mongodb.domain.Account;
import org.sdrc.usermgmt.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.html.WebColors;
import com.itextpdf.text.pdf.GrayColor;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import in.co.sdrc.sdrcdatacollector.document.EnginesForm;


/**
 * @author Biswabhusan Pradhan
 * 		   Subrata Pradhan
 *
 */
@Service
public class DashboardServiceImpl implements DashboardService {
	
	@Autowired
	private TimePeriodRepository timePeriodRepository;
	
	@Autowired
	private ConfigurableEnvironment configurableEnvironment;

	@Autowired
	GroupIndicatorRepository groupIndicatorRepository;
	
	@Autowired
	IndicatorCssClassGrouprepository indicatorCssClassGrouprepository;
	
	@Autowired
	private IndicatorRepository indicatorRepository;
	
	@Autowired
	private SectorRepository sectorRepository;
	
	@Autowired
	private DataDomainRepository dataValueRepository;
	
	@Autowired
	private ClusterDataValueRepository clusterDataValueRepository;
	
	@Autowired
	private EnginesFormRepository enginesFormRepository;
	
	@Autowired
	private AreaLevelRepository areaLevelRepository;
	
	@Autowired
	private AreaRepository areaRepository;
	
	@Autowired
	private ClusterForAggregationRepository clusterForAggregationRepository;
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private AchievementDataRepository achievementDataRepository;
	
	@Autowired
	private UserAreaMapRepository userAreaMapRepository;

	@Autowired
	private MailService mailService;
	
	@Autowired
	private CumulativeDataRepository cumulativeDataRepository;

	private ArrayList<Element> v;
	
	@Override
	public String pushIndicatorGroupData() {

//		FormSectorMapping formSectorMapping = null;
		try {
			List < GroupIndicator > indicatorModels = new LinkedList <> ();

			GroupIndicator groupIndicatorModel = null;

			 File file = new File(ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX+configurableEnvironment.getProperty("indicator.group.template.uri")).getAbsolutePath());
//			FileInputStream excelfile = new FileInputStream(
//					new File(configurableEnvironment.getProperty("indicator.group.template.uri")));

			XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file));
			XSSFSheet sheet = workbook.getSheet("Sheet1");

			XSSFRow row = null;
			XSSFCell cell = null;
			for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {

				groupIndicatorModel = new GroupIndicator();

				int colNum = 0;

				row = sheet.getRow(rowNum);
				cell = row.getCell(colNum);
				groupIndicatorModel.setIndicatorGroup(cell.getStringCellValue());

//				indicatorGroup	kpiIndicator	chartType	chartIndicators	sector	sectorId	subSector	

				colNum++;
				cell = row.getCell(colNum);
				if(cell!=null && cell.getCellTypeEnum() == CellType.NUMERIC) {
					groupIndicatorModel.setKpiIndicator((int)cell.getNumericCellValue());
				}
				colNum++;
				cell = row.getCell(colNum);
				groupIndicatorModel.setChartType(cell!=null && cell.getCellTypeEnum() != CellType.BLANK ? new ArrayList <  > (Arrays.asList(cell.getStringCellValue())): Arrays.asList(""));

				
				colNum++;
				cell = row.getCell(colNum);
				if(cell!=null)
				groupIndicatorModel.setChartIndicators(getBarChartIndicators(cell.getStringCellValue()));
				
				colNum++;
				cell = row.getCell(colNum);
				groupIndicatorModel.setSector(cell.getStringCellValue());
				
				colNum++;
				cell = row.getCell(colNum);
				
				if(cell!=null && cell.getCellTypeEnum() == CellType.NUMERIC) {
					groupIndicatorModel.setSectorId(String.valueOf((int) cell.getNumericCellValue()));
				}else if(cell!=null && cell.getCellTypeEnum() == CellType.STRING) {
					groupIndicatorModel.setSectorId(cell.getStringCellValue());
				}
				
//				groupIndicatorModel.setSectorId(cell.getStringCellValue());
				
				colNum++;
				cell = row.getCell(colNum);
				groupIndicatorModel.setSubSector(cell.getStringCellValue());

				
//				kpiChartHeader	chartHeader	
				
				colNum++;
				cell = row.getCell(colNum);
				if(cell!=null)
				groupIndicatorModel.setKpiChartHeader(cell.getStringCellValue());
				
				colNum++;
				cell = row.getCell(colNum);
				if(cell!=null)
				groupIndicatorModel.setChartHeader(cell.getStringCellValue());
				
//				cardType	chartLegends	colorLegends	
				colNum++;
				cell = row.getCell(colNum);
				if(cell!=null)
				groupIndicatorModel.setCardType(cell.getStringCellValue());
				
				colNum++;
				cell = row.getCell(colNum);
				if(cell!=null)
				groupIndicatorModel.setChartLegends(cell.getStringCellValue());
				
				colNum++;
				cell = row.getCell(colNum);
				if(cell!=null)
				groupIndicatorModel.setColorLegends(cell.getStringCellValue());
				
//				align	valueFrom	unit	chartGroup
				colNum++;
				cell = row.getCell(colNum);
				groupIndicatorModel.setAlign(cell.getStringCellValue());
				
				colNum++;
				cell = row.getCell(colNum);
				
				if(cell!=null && cell.getCellTypeEnum() == CellType.NUMERIC) {
					groupIndicatorModel.setValueFrom(String.valueOf((int) cell.getNumericCellValue()));
				}else if(cell!=null && cell.getCellTypeEnum() == CellType.STRING) {
					groupIndicatorModel.setValueFrom(cell.getStringCellValue());
				}
				
				
				colNum++;
				cell = row.getCell(colNum);
				groupIndicatorModel.setUnit(cell.getStringCellValue());

				colNum++;
				cell = row.getCell(colNum);
				groupIndicatorModel.setChartGroup(cell.getStringCellValue());
				
				colNum++;
				cell = row.getCell(colNum);
				groupIndicatorModel.setExtraInfo(cell!=null ? cell.getStringCellValue() : "");

				indicatorModels.add(groupIndicatorModel);

			}
			/*sheet = null;
			sheet = workbook.getSheet("Sheet2");

			row = null;
			cell = null;
			List < FormSectorMapping > formSectorMappingList = new ArrayList <  > ();
			for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
				formSectorMapping = new FormSectorMapping();
				int colNum = 0;

				row = sheet.getRow(rowNum);
				cell = row.getCell(colNum);
				formSectorMapping.setFormId((int)cell.getNumericCellValue());

				colNum++;
				cell = row.getCell(colNum);
				formSectorMapping.setSectorName(cell.getStringCellValue());
				
				colNum++;
				cell = row.getCell(colNum);
				formSectorMapping.setSectorId((int)cell.getNumericCellValue());

				formSectorMappingList.add(formSectorMapping);

			}
			*/

			workbook.close();
			
			groupIndicatorRepository.save(indicatorModels);
//			formSectorMappingRepository.save(formSectorMappingList);
			
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			
		}
		
		return "done";
		}


	private List<Integer> getValue(String stringCellValue) {
		List<Integer> indicators = new ArrayList<>();
		if (!stringCellValue.equals("")) {
			for (int i = 0; i < stringCellValue.split(",").length; i++) {
				if (!stringCellValue.split(",")[i].equals("")) {
					indicators.add(Integer.valueOf(stringCellValue.split(",")[i].trim()));
				}
			}
		}
		return indicators;
	}


	 List<List<Integer>> getBarChartIndicators(String stringCellValue) {
		List<List<Integer>> barChartIndicators = new ArrayList<>();
		if (!stringCellValue.equals("")) {
			for (int i = 0; i < stringCellValue.split("@").length; i++) {
				barChartIndicators.add(getValue(stringCellValue.split("@")[i]));
			}
		}
		return barChartIndicators;
	}
	
	@Override
	public Map<String, List<FormSectorModel>> getAllChecklistSectors() {

		Map<Integer, String> enginesFormsMap = enginesFormRepository.findAll().stream()
				.collect(Collectors.toMap(EnginesForm::getFormId, EnginesForm::getName));

		List<FormSectorModel> list = new ArrayList<>();

		List<Sector> sectors = sectorRepository.findAllByOrderByOrderAsc();
		for (Sector v : sectors) {
			FormSectorModel formSectorModel = new FormSectorModel();

			formSectorModel.setFormId(v.getFormId());
			formSectorModel.setSectorId(v.getSectorId());
			formSectorModel.setSectorName(v.getSectorName());
			formSectorModel.setFormName(enginesFormsMap.get(v.getFormId()));
			list.add(formSectorModel);

		}
		
		Map<String, List<FormSectorModel>> map = list.stream().collect(Collectors.groupingBy(FormSectorModel::getSectorName, LinkedHashMap::new,Collectors.mapping(Function.identity(), Collectors.toList())));
		return map;

	}

	@Override
	public List<SectorModel> getDashboardData(Integer areaLevel, Integer areaId, String sectorName, Integer tpId, Integer formId, String dashboardType) {
		List<SectorModel> sectorModels = new LinkedList<>();
		
		Map<String, Map<String, List<IndicatorGroupModel>>> map = new LinkedHashMap<>();
		
		try {
			
			String lastAggregatedTime = "";
			List<GroupIndicator> groupIndicatorModels = groupIndicatorRepository.findBySectorIn(Arrays.asList(sectorName));
			
			List<Indicator> indicatorList = null;
			if(sectorName.equals("Overview")) {
				List<String> valueFromExtraIndicators = groupIndicatorModels.stream().filter(v->v.getValueFrom()!=null)
						.map(v->String.valueOf(v.getValueFrom())).distinct().collect(Collectors.toList());
				
				List<String> kpiIndicators = groupIndicatorModels.stream().filter(v->v.getKpiIndicator()!=null)
						.map(v->String.valueOf(v.getKpiIndicator())).distinct().collect(Collectors.toList());
				
				List<String> chartIndicators = groupIndicatorModels.stream().filter(v->v.getChartIndicators()!=null && !v.getChartIndicators().isEmpty())
						.flatMap(v->v.getChartIndicators().stream()).flatMap(v->v.stream()).map(v->String.valueOf(v)).distinct().collect(Collectors.toList());
				
				kpiIndicators.addAll(valueFromExtraIndicators);
				kpiIndicators.addAll(chartIndicators);
				indicatorList = indicatorRepository.getIndicatiorsIn(kpiIndicators);
				
			}else if(sectorName.equals("T4 Approach")) {
				sectorName = Constants.T4;
				indicatorList = indicatorRepository.getIndicatorBySectors(Arrays.asList(sectorName));
			}else {
				indicatorList = indicatorRepository.getIndicatorBySectors(Arrays.asList(sectorName));
			}
			List<Integer> indicatorIds = new LinkedList<>() ;
			Map<Integer, String> indicatorNameMap = new HashMap<>();
			
			for (Indicator indicator : indicatorList) {
				indicatorIds.add(Integer.valueOf((String)indicator.getIndicatorDataMap().get("indicatorNid")));
				indicatorNameMap.put(Integer.valueOf((String)indicator.getIndicatorDataMap().get("indicatorNid")),
						(String) indicator.getIndicatorDataMap().get("indicatorName"));
			}
			
			List<GroupIndicator> trendGroupIndicators = groupIndicatorModels.stream().filter(ind -> ind.getChartType().contains("trend")).collect(Collectors.toList());
			
			List<Integer> trendGroupIndicatorIds = trendGroupIndicators.stream()
					.flatMap(v -> v.getChartIndicators().stream())
					.collect(Collectors.collectingAndThen(Collectors.toList(), l -> {
						return l.stream().flatMap(list -> list.stream()).collect(Collectors.toList());
					}));
			
			List<DataValue> dataValues = new ArrayList<>();
//			Map<Integer, DataValue> mapData = null;
			Map<Integer, DataValue> allMapData = new LinkedHashMap <>();
			Map<Integer,Map<Integer, DataValue>> trendData = new LinkedHashMap <>();
			
			
			Map<Integer,TimePeriod > timePeriodIdMap = new LinkedHashMap<>();
			
			List<TimePeriod> utTimeperiodList = null;
			
			if(!trendGroupIndicatorIds.isEmpty()) {
				// get all tp for trend
//				utTimeperiodList = timePeriodRepository.findTop6ByPeriodicityOrderByTimePeriodIdDesc("1");
				utTimeperiodList = timePeriodRepository.findTop6ByPeriodicityAndTimePeriodIdLessThanEqualOrderByTimePeriodIdDesc("1",tpId);
//				utTimeperiodList.remove(0);
				Collections.reverse(utTimeperiodList); 
				if(sectorName.equals(Constants.IFA_SUPPLY_AND_DEMAND_MONITORING)) {
					dataValues = dataValueRepository.findTop12ByDatumtypeAndInidInOrderByTpDesc(Constants.IFA, trendGroupIndicatorIds);
				}else {
					if(areaLevel==1 || areaLevel==3 || areaLevel == 4 || areaId == 0) {
						dataValues = mongoTemplate.aggregate(getAggregationResults(areaId.intValue()==0 ? 3 :areaId, trendGroupIndicatorIds, 
								utTimeperiodList.stream().map(v->v.getTimePeriodId()).collect(Collectors.toList()), "data"),
								DataValue.class, DataValue.class).getMappedResults();
					}else if(areaLevel==2) {
						
						List<ClusterDataValue> clusterDataValues = mongoTemplate.aggregate(getAggregationResults(areaId, trendGroupIndicatorIds, 
								utTimeperiodList.stream().map(v->v.getTimePeriodId()).collect(Collectors.toList()), "cluster"),
								ClusterDataValue.class, ClusterDataValue.class).getMappedResults();
							
							dataValues = clusterDataValues.stream().map(v->{
							DataValue dataValue = new DataValue();
							dataValue.setId(v.getId());
							dataValue.setDataValue(v.getDataValue());
							dataValue.setDatumId(v.getAreaId());
							dataValue.setTp(v.getTp());
							dataValue.set_case(v.get_case());
							dataValue.setInid(v.getInid());
							dataValue.setNumerator(v.getNumerator() != null ? v.getNumerator().toString() : null);
							dataValue.setDenominator(v.getDenominator() != null ? v.getDenominator().toString() : null);
							
							return dataValue;
						}).collect(Collectors.toList());
					}
				}
				
				for (DataValue dataValue : dataValues) {

					if (!trendData.containsKey(dataValue.getInid())) {
						Map<Integer, DataValue> newMapData = new LinkedHashMap<>();
						newMapData.put(dataValue.getTp(), dataValue);
						trendData.put(dataValue.getInid(), newMapData);
					} else {
						allMapData = trendData.get(dataValue.getInid());
						if (!allMapData.containsKey(dataValue.getTp())) {
							allMapData.put(dataValue.getTp(), dataValue);
						}
					}
				}

				utTimeperiodList.forEach(timePeriod -> {
					timePeriodIdMap.put(timePeriod.getTimePeriodId(), timePeriod);
				});

			} 
				
			if(sectorName.equals(Constants.IFA_SUPPLY_AND_DEMAND_MONITORING)) {
				dataValues = dataValueRepository.findByTpAndInidIn(tpId, indicatorIds);
			}else if(sectorName.equals("Overview")){
				dataValues=cumulativeDataRepository.findByDatumIdAndInidIn(areaId.intValue()==0 ? 3 :areaId,indicatorIds).stream().map(v->{
					DataValue dataValue=new DataValue();
					dataValue.setDatumId(v.getDatumId());
					dataValue.setInid(v.getInid());
					dataValue.setDataValue(v.getDataValue());
					dataValue.setNumerator(v.getNumerator()!=null?String.valueOf(v.getNumerator()):null);
					dataValue.setDenominator(v.getDenominator()!=null?String.valueOf(v.getDenominator()):null);
					
					return dataValue;
				}).collect(Collectors.toList());
			}else {
				if(areaLevel==1 || areaLevel==3 || areaLevel == 4 || areaId.intValue()==0) {
					dataValues = dataValueRepository.findByDatumIdAndTpAndInidIn(areaId.intValue()==0 ? 3 :areaId, tpId, indicatorIds);
				}else if(areaLevel==2) {
					List<Integer> villageList=clusterForAggregationRepository.findByClusterNumber(areaId).stream().map(v->v.getVillage())
							.collect(Collectors.toList());
					List<ClusterDataValue> clusterDataValues = clusterDataValueRepository.findByAreaIdAndTpAndInidIn(areaId, tpId, indicatorIds);
					List<DataValue> villageData=dataValueRepository.findByDatumIdInAndTpAndInidIn(villageList, tpId, indicatorIds);
	
					dataValues = clusterDataValues.stream().map(v->{
						DataValue dataValue = new DataValue();
						dataValue.setId(v.getId());
						dataValue.setDataValue(v.getDataValue());
						dataValue.setDatumId(v.getAreaId());
						dataValue.setTp(v.getTp());
						dataValue.set_case(v.get_case());
						dataValue.setInid(v.getInid());
						dataValue.setNumerator(v.getNumerator() != null ? v.getNumerator().toString() : null);
						dataValue.setDenominator(v.getDenominator() != null ? v.getDenominator().toString() : null);
						
						return dataValue;
					}).collect(Collectors.toList());
					if(areaId==0){
						dataValues=dataValueRepository.findByDatumIdIsAndTpAndInidIn(3, tpId, indicatorIds);
					}
				} else {
					dataValues=dataValueRepository.findByDatumIdIsAndTpAndInidIn(areaId, tpId, indicatorIds);
				}
			}
			
			for (DataValue dataValue : dataValues) {
				allMapData.put(dataValue.getInid(), dataValue);
			}
			
			for (GroupIndicator groupIndicatorModel : groupIndicatorModels) {

				IndicatorGroupModel indicatorGroupModel = new IndicatorGroupModel();

				List<GroupChartDataModel> listOfGroupChartData = null;
				GroupChartDataModel chartDataModel = null;
				List<LegendModel> legendModels = null;
				
				//set static indicator value
				if(groupIndicatorModel.getCardType() != null && groupIndicatorModel.getCardType().equals("static")) {
					indicatorGroupModel.setIndicatorValue(groupIndicatorModel.getKpiIndicator().toString());
				}else {
					if (allMapData != null) {
						indicatorGroupModel
						.setIndicatorValue(allMapData.get(groupIndicatorModel.getKpiIndicator()) == null ? null
								: allMapData.get(groupIndicatorModel.getKpiIndicator()).getDataValue() == null ? null :
								groupIndicatorModel.getUnit().equalsIgnoreCase("percentage") || groupIndicatorModel.getUnit().equalsIgnoreCase("Average")? 
								String.valueOf(Math.round((allMapData.get(groupIndicatorModel.getKpiIndicator()).getDataValue())* 10.0) / 10.0) : 
									String.valueOf(allMapData.get(groupIndicatorModel.getKpiIndicator()).getDataValue().intValue()));
//						indicatorGroupModel
//								.setIndicatorValue(allMapData.get(groupIndicatorModel.getKpiIndicator()) == null ? null
//										: String.valueOf(allMapData.get(groupIndicatorModel.getKpiIndicator())
//														.getDataValue().intValue()));
					}
				}

				
//				indicatorGroupModel.setTimeperiod(tp.getTimePeriod() + ", " + tp.getYear());
//				indicatorGroupModel.setTimeperiodId(tp.getTimePeriodId());
//				indicatorGroupModel.setPeriodicity(tp.getPeriodicity());

				String kpiInd = null;
				
				if(groupIndicatorModel.getValueFrom()!=null && groupIndicatorModel.getValueFrom().contains("=")) {
					String[] arrs = groupIndicatorModel.getValueFrom().split(",");
					for(String ar: arrs) {
						
						String[] each = ar.split("=");
						
						if(each[0].equals(areaLevel.toString())) {
							kpiInd = each[1];
						}
					}
					
				}else if(groupIndicatorModel.getValueFrom()!=null) {
					kpiInd = groupIndicatorModel.getValueFrom();
				}
				
					
				if(!groupIndicatorModel.getChartType().get(0).contains("card") && kpiInd!=null && !kpiInd.equals("")){
					indicatorGroupModel
							.setIndicatorValue(allMapData.get(Integer.parseInt(kpiInd))!=null ?
									String.valueOf(allMapData.get(Integer.parseInt(kpiInd)).getDataValue().intValue()) : null);
				}
//				groupIndicatorModel.getValueFrom().contains("=")? groupIndicatorModel.getValueFrom();
//				Number of aspirational district === is aggregated in state level,
				//to show district level data put value as 1

				String kpiIndName = groupIndicatorModel.getKpiChartHeader()!=null ?
						groupIndicatorModel.getKpiChartHeader().contains("@")
						? (groupIndicatorModel.getKpiChartHeader().split("@")[0]
								+ (allMapData.get(Integer.parseInt(kpiInd))!=null ? allMapData.get(Integer.parseInt(kpiInd))
										.getDataValue().intValue() : 1)
								+ groupIndicatorModel.getKpiChartHeader().split("@")[1])
						: groupIndicatorModel.getKpiChartHeader() : "";

				indicatorGroupModel.setIndicatorName(kpiIndName);
				
				indicatorGroupModel.setIndicatorId(groupIndicatorModel.getKpiIndicator());
				indicatorGroupModel.setChartsAvailable(groupIndicatorModel.getChartType());
				indicatorGroupModel.setAlign(groupIndicatorModel.getAlign());
				indicatorGroupModel.setCardType(groupIndicatorModel.getCardType());
				indicatorGroupModel.setIndicatorGroupName(groupIndicatorModel.getIndicatorGroup());
				indicatorGroupModel.setUnit(groupIndicatorModel.getUnit());
				indicatorGroupModel.setChartAlign(groupIndicatorModel.getChartAlign());
				indicatorGroupModel.setChartGroup(groupIndicatorModel.getChartGroup());
				indicatorGroupModel.setExtraInfo(groupIndicatorModel.getExtraInfo());
				
				//new code 04-06-2019
				
				
				//for trend chart indicators
				if (groupIndicatorModel.getChartType().get(0).contains("trend") && groupIndicatorModel.getChartIndicators()!=null &&
						groupIndicatorModel.getChartIndicators().size() > 0 && trendData != null && !trendData.isEmpty()) {
					listOfGroupChartData = new ArrayList<GroupChartDataModel>();
					chartDataModel = new GroupChartDataModel();

					String indName = groupIndicatorModel.getChartHeader().contains("@")
							? (groupIndicatorModel.getChartHeader().split("@")[0]
									+ allMapData.get(Integer.parseInt(kpiInd))
											.getDataValue().intValue()
									+ groupIndicatorModel.getChartHeader().split("@")[1])
							: groupIndicatorModel.getChartHeader();

					chartDataModel.setHeaderIndicatorName(indName);
//					chartDataModel.setHeaderIndicatorValue(
//							trendData.get(groupIndicatorModel.getChartIndicators().get(0).get(0)) == null ? null :
//											 trendData.get(groupIndicatorModel.getChartIndicators().get(0).get(0))
//													.get(groupIndicatorModel.getHeaderIndicator()).getDataValue()
//													.intValue());
					chartDataModel.setChartDataValue(getChartDataValue(groupIndicatorModel,
							null, indicatorNameMap, "trend", null, timePeriodIdMap, utTimeperiodList, 
							groupIndicatorModel.getUnit(),dashboardType, trendData));

					listOfGroupChartData.add(chartDataModel);
					indicatorGroupModel.setChartData(listOfGroupChartData);
					
					if (groupIndicatorModel.getColorLegends()!=null &&
							groupIndicatorModel.getColorLegends().length() > 0 && allMapData!=null) {
						legendModels = new ArrayList<>();
						String[] legendsList = groupIndicatorModel.getColorLegends().split(",");
						
						for (String string : legendsList) {
							LegendModel legendModel = new LegendModel();
							legendModel.setCssClass(string.split("_")[0]);
							legendModel.setValue(string.split("_")[1]);
							legendModels.add(legendModel);
						}
						chartDataModel.setLegends(legendModels);
					}

				}else if(groupIndicatorModel.getChartIndicators()!=null &&
						groupIndicatorModel.getChartIndicators().size() > 0 && allMapData!=null && !allMapData.isEmpty()){ //for all other chart types
					listOfGroupChartData = new ArrayList<GroupChartDataModel>();
					chartDataModel = new GroupChartDataModel();

					String indName = groupIndicatorModel.getChartHeader().contains("@")
							? (groupIndicatorModel.getChartHeader().split("@")[0]
									+ allMapData.get(Integer.parseInt(kpiInd))
											.getDataValue().intValue()
									+ groupIndicatorModel.getChartHeader().split("@")[1])
							: groupIndicatorModel.getChartHeader();

					chartDataModel.setHeaderIndicatorName(indName);
					
//					chartDataModel.setHeaderIndicatorValue(allMapData.get(groupIndicatorModel.getHeaderIndicator()) == null ? null
//									: allMapData.get(groupIndicatorModel.getHeaderIndicator()).getDataValue().intValue());
					
					chartDataModel.setChartDataValue(getChartDataValue(groupIndicatorModel, allMapData, indicatorNameMap, groupIndicatorModel.getChartType().get(0)
							, null, timePeriodIdMap, null, groupIndicatorModel.getUnit(),dashboardType, null));
					
					listOfGroupChartData.add(chartDataModel);
					indicatorGroupModel.setChartData(listOfGroupChartData);
					
					if (groupIndicatorModel.getColorLegends()!=null &&
							groupIndicatorModel.getColorLegends().length() > 0 && allMapData!=null) {
						legendModels = new ArrayList<>();
						String[] legendsList = groupIndicatorModel.getColorLegends().split(",");
						
						for (String string : legendsList) {
							LegendModel legendModel = new LegendModel();
							legendModel.setCssClass(string.split("_")[0]);
							legendModel.setValue(string.split("_")[1]);
							legendModels.add(legendModel);
						}
						chartDataModel.setLegends(legendModels);
					}
					
				}
				
				//end
				

				if (!map.containsKey(groupIndicatorModel.getSector())) {

					Map<String, List<IndicatorGroupModel>> subsectorGrMapModel = new LinkedHashMap<>();

					List<IndicatorGroupModel> sectorNewIndicators = new LinkedList<>();
					sectorNewIndicators.add(indicatorGroupModel);

					subsectorGrMapModel.put(groupIndicatorModel.getSubSector(), sectorNewIndicators);
					
					map.put(groupIndicatorModel.getSector(), subsectorGrMapModel);
				}
				else {
					
					if(!map.get(groupIndicatorModel.getSector()).containsKey(groupIndicatorModel.getSubSector())) {
						
						List<IndicatorGroupModel> newIndicators = new LinkedList<>();
						newIndicators.add(indicatorGroupModel);

						map.get(groupIndicatorModel.getSector()).put(groupIndicatorModel.getSubSector(), newIndicators);
					}else {
						map.get(groupIndicatorModel.getSector()).get(groupIndicatorModel.getSubSector()).add(indicatorGroupModel);
					}
				}
			}
			
			for (Entry<String, Map<String, List<IndicatorGroupModel>>> entry : map.entrySet()) {
				SectorModel sectorModel = new SectorModel();
				sectorModel.setSectorName(entry.getKey());
				
				List<SubSectorModel> listOfSubsector = new ArrayList<>();
			
				for (Entry<String, List<IndicatorGroupModel>> entry2 : entry.getValue().entrySet()) {
					SubSectorModel subSectorModel = new SubSectorModel();
					subSectorModel.setSubSectorName(entry2.getKey());
					listOfSubsector.add(subSectorModel);
					subSectorModel.setIndicators(entry2.getValue());
				}
				sectorModel.setSubSectors(listOfSubsector);
				sectorModel.setTimePeriod(lastAggregatedTime);
				sectorModels.add(sectorModel);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return sectorModels;
	}

	private Aggregation getAggregationResults(Integer areaId, List<Integer> trendGroupIndicatorIds,	List<Integer> tpIds, String type) {
		
		MatchOperation match = null;
		if(type.equals("data")) {
			match = Aggregation.match(Criteria.where("datumId").is(areaId)
				.and("inid").in(trendGroupIndicatorIds)
				.and("tp").in(tpIds));
		}else {
			match = Aggregation.match(Criteria.where("areaId").is(areaId)
					.and("inid").in(trendGroupIndicatorIds)
					.and("tp").in(tpIds));
		}
		
		return Aggregation.newAggregation(match);
	}


	private List<List<ChartDataModel>> getChartDataValue(GroupIndicator groupIndicatorModel,
			Map<Integer, DataValue> mapData, Map<Integer, String> indicatorNameMap, String chartType,
			Map<Integer, String> cssGroupMap, Map<Integer, TimePeriod> timePeriodIdMap,
			List<TimePeriod> utTimeperiodList, String unit, String dashboardType, Map<Integer, Map<Integer, DataValue>> trendData) {
		
		List<List<Integer>> chartIndicators = null;
		
		String[] axisList = null ;
		chartIndicators = new ArrayList<>();

		chartIndicators = groupIndicatorModel.getChartIndicators();
		
		if(groupIndicatorModel.getChartLegends()!=null)
			axisList = groupIndicatorModel.getChartLegends().split(",");
		
		if(chartType.equals("pie") || chartType.equals("donut"))
			axisList = groupIndicatorModel.getColorLegends().split(",");
				
		List<List<ChartDataModel>> listChartDataModels = new LinkedList<>();
		List<ChartDataModel> chartDataModels = null;
		
		if(chartType.equals("trend")) {
			for (List<Integer> indList : chartIndicators) {
				chartDataModels = new LinkedList<>();
				
				for (int i=0; i< indList.size(); i++) {
					
					if(null!=utTimeperiodList) {
						for(TimePeriod timePeriod :utTimeperiodList) {
							String axis = null;
							axis = trendData ==null || trendData.get(timePeriod.getTimePeriodId()) == null ? timePeriod.getTimePeriodDuration() + "-" +timePeriod.getYear()  :
								timePeriodIdMap.get(timePeriod.getTimePeriodId()).getTimePeriodDuration() + "-"+
									timePeriodIdMap.get(timePeriod.getTimePeriodId()).getYear();
						
							getChartDataModel(indicatorNameMap, indList.get(i), trendData.get(indList.get(i)),"trend", 
									chartDataModels, cssGroupMap, axis, timePeriod.getTimePeriodId(), unit, 
									dashboardType,(axisList!= null? axisList[i].split("_")[1] : null));
						};
					}
				}
				listChartDataModels.add(chartDataModels);
			}
					
					
		}else {
			mapData.get(groupIndicatorModel.getChartIndicators().get(0).get(0));
			
			int grCount = 0;
			for (List<Integer> indList : chartIndicators) {
					chartDataModels = new LinkedList<>();
					
					for (int i=0; i< indList.size(); i++) {
						
						String axis = null;
						axis = axisList!= null? axisList[i].split("_")[1] : null;
						
						String label = null;
						
						if(chartType.equals("stack"))
							label = groupIndicatorModel.getColorLegends()!= null? groupIndicatorModel.getColorLegends().split(",")[grCount].split("_")[1] : null;
						
					
						getChartDataModel(indicatorNameMap, indList.get(i), mapData,"all", chartDataModels, cssGroupMap, axis, null, unit, dashboardType, label);
					}
						listChartDataModels.add(chartDataModels);
						
						grCount++;
				}
			
			
		}
		
		return listChartDataModels;
	}


	private List<ChartDataModel> getChartDataModel(Map<Integer, String> indicatorNameMap, Integer indiId,
			Map<Integer, DataValue> mapData, String numeDeno, List<ChartDataModel> chartDataModels,
			Map<Integer, String> cssGroupMap, String axis, Integer timePeriodId, String unit, String dashboardType, String label) {
		
		ChartDataModel chartDataModel = new ChartDataModel();
		chartDataModel.setAxis(axis.trim());
		chartDataModel.setLabel(label != null ? label.trim() : null);
		if(numeDeno.equals("all")) {
			
			if(dashboardType.equals("COVERAGE")) { //in coverage dashboard put 0 instead of null
				chartDataModel.setValue(
						(mapData == null || mapData.get(indiId) == null || mapData.get(indiId).getDataValue() == null) ? "0"
								: unit.equalsIgnoreCase("percentage")
										? String.valueOf(Math.round((mapData.get(indiId).getDataValue()) * 10.0) / 10.0)
										: String.valueOf(mapData.get(indiId).getDataValue().intValue()));
			}else {
				chartDataModel.setValue(
						(mapData == null || mapData.get(indiId) == null || mapData.get(indiId).getDataValue() == null) ? null
								: unit.equalsIgnoreCase("percentage")
										? String.valueOf(Math.round((mapData.get(indiId).getDataValue()) * 10.0) / 10.0)
										: String.valueOf(mapData.get(indiId).getDataValue().intValue()));
			}
			

			chartDataModel.setNumerator(
					(mapData == null || mapData.get(indiId) == null || mapData.get(indiId).getNumerator() == null)
							? null
							: String.valueOf(Math.round(Double.parseDouble ( mapData.get(indiId).getNumerator()))));
			chartDataModel.setDenominator(
					(mapData == null || mapData.get(indiId) == null || mapData.get(indiId).getDenominator() == null)
							? null
							: String.valueOf(Math.round(Double.parseDouble ( mapData.get(indiId).getDenominator()))));
		}else { //trend chart
			
			if(dashboardType.equals("COVERAGE")) { //in coverage dashboard put 0 instead of null
				chartDataModel
				.setValue((mapData == null || mapData.get(timePeriodId) == null
						|| mapData.get(timePeriodId).getDataValue() == null)
								? "0"
								: unit.equalsIgnoreCase("percentage") ? String.valueOf(
										Math.round((mapData.get(timePeriodId).getDataValue()) * 10.0) / 10.0)
										: String.valueOf(mapData.get(timePeriodId).getDataValue().intValue()));
			}else {
				chartDataModel
				.setValue((mapData == null || mapData.get(timePeriodId) == null
						|| mapData.get(timePeriodId).getDataValue() == null)
								? null
								: unit.equalsIgnoreCase("percentage") ? String.valueOf(
										Math.round((mapData.get(timePeriodId).getDataValue()) * 10.0) / 10.0)
										: String.valueOf(mapData.get(timePeriodId).getDataValue().intValue()));
			}
			

			chartDataModel.setNumerator((mapData == null || mapData.get(timePeriodId) == null
					|| mapData.get(timePeriodId).getNumerator() == null) ? null
							: String.valueOf(Math.round(Double.parseDouble(mapData.get(timePeriodId).getNumerator()))));
			chartDataModel.setDenominator((mapData == null || mapData.get(timePeriodId) == null
					|| mapData.get(timePeriodId).getDenominator() == null) ? null
							: String.valueOf(
									Math.round(Double.parseDouble(mapData.get(timePeriodId).getDenominator()))));
		}
		chartDataModel.setLegend(axis.trim());
		chartDataModel.setId(indiId);
		chartDataModel.setUnit(unit);
		chartDataModel.setKey(chartDataModel.getLabel() != null ? chartDataModel.getLabel() : null);
		chartDataModels.add(chartDataModel);
		
		return chartDataModels;
	}

	@Override
	public List<AreaLevel> getAreaLevels() {
		return areaLevelRepository.findAll();
	}
	
	@Override
	public List<TimePeriodModel> getAllTimeperiods() {
		
		Date date = new Date();
		
		TimePeriod currentTimePeriod = timePeriodRepository.getCurrentTimePeriod(date, "1");
		
		DateModel dates = getDatesForTimePeriod(date);
		
		List<TimePeriod> timePeriods = timePeriodRepository.findAllByOrderByIdDesc();
		
		timePeriods=timePeriods.stream().filter(tp->!tp.getId().equals(currentTimePeriod.getId())).collect(Collectors.toList());
		
		//if current date is in between 1 to 10 than hide last month time-period as aggregation date is scheduled to 10th of every month at night
		if ((DateUtils.isSameDay(date, dates.getStartDate())|| (date.after(dates.getStartDate())) && (DateUtils
						.isSameDay(date, dates.getEndDate())|| date.before(dates.getEndDate())))) {
			//last month date
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.MONTH, -1);
			date = cal.getTime();
			
			TimePeriod lastMonthTimePeriod = timePeriodRepository.getCurrentTimePeriod(date, "1");
			
			timePeriods=timePeriods.stream().filter(tp->!tp.getId().equals(lastMonthTimePeriod.getId())).collect(Collectors.toList());
			
		}
		
		return timePeriods.stream().map(v->{
			TimePeriodModel timePeriodModel = new TimePeriodModel();
//			timePeriodModel.setId(v.getId());
			timePeriodModel.setTpName(v.getTimePeriodDuration()+"'"+v.getYear());
			timePeriodModel.setTpId(v.getTimePeriodId());
			return timePeriodModel;
		}).collect(Collectors.toList());
	}
	
	private DateModel getDatesForTimePeriod(Date date) {
		DateModel model = new DateModel();

		Calendar cal = Calendar.getInstance();

		cal.setTime(date);

		cal.set(Calendar.DATE, cal.getActualMinimum(Calendar.DATE));

		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MILLISECOND, 0);
		model.setStartDate(cal.getTime());


		cal.set(Calendar.DATE, 10);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 59);

		model.setEndDate(cal.getTime());

		return model;
	}


	@Override
	public Map<String, List<FormSectorModel>> getFormSectorMappingData() {
		
		Map<Integer, String> enginesFormsMap = enginesFormRepository.findAll().stream()
				.collect(Collectors.toMap(EnginesForm::getFormId, EnginesForm::getDisplayName));
		
		return sectorRepository.findAll().stream().filter(k->!k.getSectorName().equals("Overview"))
				.map(v -> {
					FormSectorModel formSectorModel = new FormSectorModel();

					formSectorModel.setFormId(v.getFormId());
					formSectorModel.setSectorId(v.getSectorId());
					formSectorModel.setSectorName(v.getSectorName());
					formSectorModel.setFormName(enginesFormsMap.get(v.getFormId()));

					return formSectorModel;
				}).collect(Collectors.groupingBy(FormSectorModel::getFormName));
	}

	@Override
	public List<IndicatorModel> getIndicators(Integer formId) {
		
		return indicatorRepository.getIndicatorByFormId(formId.toString()).stream().map(v->{
			IndicatorModel indicatorModel = new IndicatorModel();
			indicatorModel.setIndicatorId(Integer.valueOf((String)v.getIndicatorDataMap().get("indicatorNid")));
			indicatorModel.setIndicatorName((String)v.getIndicatorDataMap().get("indicatorName")+"("+(String)v.getIndicatorDataMap().get("unit")+")");
			indicatorModel.setUnit((String)v.getIndicatorDataMap().get("unit"));
			return indicatorModel;
		}).collect(Collectors.toList());
	}

	@Override
	public Map<String, Object> getThematicViewData(Integer indicatorId, Integer tpId, Integer areaLevel, Integer areaId, String sectorName) {
		
		List<ClusterForAggregation> areaLists = null;
		
		if(areaId == Constants.DISTRICT_AREA_ID) {
			areaLists = clusterForAggregationRepository.findByDistrict(areaId);
		} else if(areaLevel==1) {
			areaLists = clusterForAggregationRepository.findByBlock(areaId);
		} else if(areaLevel==2) {
			areaLists = clusterForAggregationRepository.findByClusterNumber(areaId);
		}
		
		List<Integer>  areaIds = areaLists.stream().map(v->v.getVillage()).collect(Collectors.toList());
		
		areaIds.add(areaId);
		
		List<ClusterDataValue> clusterDataValueList=new ArrayList<>();
		List<ClusterDataValue> villageDataValueList=new ArrayList<>();
		List<DataValue> dataValues=new ArrayList<>();
		Map<Integer, ClusterDataValue> allMapData = new LinkedHashMap <>();
		
		if(sectorName.equals(Constants.IFA_SUPPLY_AND_DEMAND_MONITORING)) {
		}else {
			if(areaLevel==1) {
				List<DataValue> blockData=dataValueRepository.findByDatumIdInAndTpAndInidIs(areaIds, tpId, indicatorId);
				
				clusterDataValueList = blockData.stream().map(v->{
					ClusterDataValue dataValue = new ClusterDataValue();
					dataValue.setId(v.getId());
					dataValue.setDataValue(v.getDataValue());
					dataValue.setAreaId(v.getDatumId());
					dataValue.setTp(v.getTp());
					dataValue.set_case(v.get_case());
					dataValue.setInid(v.getInid());
					dataValue.setNumerator(v.getNumerator() != null ? Double.parseDouble(v.getNumerator()) : null);
					dataValue.setDenominator(v.getDenominator() != null ? Double.parseDouble(v.getDenominator()) : null);
					
					return dataValue;
				}).collect(Collectors.toList());
			}else if(areaLevel==2) {
				List<ClusterDataValue> clusterDataValues = clusterDataValueRepository.findByAreaIdIsAndTpAndInidIn(areaId, tpId, Arrays.asList(indicatorId));
				List<DataValue> villageData=dataValueRepository.findByDatumIdInAndTpAndInid(areaIds, tpId, indicatorId);

				villageDataValueList=villageData.stream().map(v->{
					ClusterDataValue dataValue = new ClusterDataValue();
					dataValue.setId(v.getId());
					dataValue.setDataValue(v.getDataValue());
					dataValue.setAreaId(v.getDatumId());
					dataValue.setTp(v.getTp());
					dataValue.set_case(v.get_case());
					dataValue.setInid(v.getInid());
					dataValue.setNumerator(v.getNumerator() != null ? Double.parseDouble(v.getNumerator()) : null);
					dataValue.setDenominator(v.getDenominator() != null ? Double.parseDouble(v.getDenominator()) : null);
					
					return dataValue;
				}).collect(Collectors.toList());
				clusterDataValueList = clusterDataValues.stream().map(v->{
					ClusterDataValue dataValue = new ClusterDataValue();
					dataValue.setId(v.getId());
					dataValue.setDataValue(v.getDataValue());
					dataValue.setAreaId(v.getAreaId());
					dataValue.setTp(v.getTp());
					dataValue.set_case(v.get_case());
					dataValue.setInid(v.getInid());
					dataValue.setNumerator(v.getNumerator() != null ? v.getNumerator() : null);
					dataValue.setDenominator(v.getDenominator() != null ? v.getDenominator() : null);
					
					return dataValue;
				}).collect(Collectors.toList());
				
				clusterDataValueList.addAll(villageDataValueList);
			}	
		}
		for (ClusterDataValue dataValue : clusterDataValueList) {
			allMapData.put(dataValue.getAreaId(), dataValue);
		}
		
		ClusterDataValue maxValue = Collections.max(clusterDataValueList.stream().filter(v -> v.getAreaId()!=3).collect(Collectors.toList()), Comparator.comparing(v -> v.getDataValue()));
		
		List<Area>  areaList = areaRepository.findByAreaIdIn(areaIds);

		List<AreaModel> areaModels = areaList.stream()
				.filter(v-> v != null && allMapData.get(v.getAreaId())!= null)
				.filter(v -> v.getAreaId() != areaId)
				.map(v->{
					AreaModel areaModel = new AreaModel();
					areaModel.setAreaName(v.getAreaName());
					areaModel.setAreaCode(v.getAreaCode());
					areaModel.setAreaId(v.getAreaId());
					areaModel.setAreaLevel(v.getAreaLevel().getAreaLevelName());
					areaModel.setValue(allMapData.get(v.getAreaId()).getDataValue() != null 
							? allMapData.get(v.getAreaId()).getDataValue().toString() : null);
					areaModel.setCssColor(getThematicDataColor(allMapData.get(v.getAreaId()).getDataValue(),maxValue.getDataValue()));
					
					return areaModel;
				}).collect(Collectors.toList());
		
		Map<String, Object> thematicMapData = new LinkedHashMap<>();
		thematicMapData.put("thematicMapDataModels", areaModels);
		List<LegendModel> legendModels = getLegendModel(maxValue.getDataValue());
		thematicMapData.put("thematicMapLegendModels", legendModels);
		Map<Integer, Area> areasMap = areaList.stream().collect(Collectors.toMap(Area::getAreaId, v->v));
		thematicMapData.put("DistrictName", areasMap.get(areaId).getAreaName());
		thematicMapData.put("DistrictValue", allMapData.isEmpty() ? "N/A" : allMapData.get(areaId).getDataValue());
		return thematicMapData;
	}

	/*
	 * @author Biswabhusan Pradhan
	 * 
	 */
	private String getThematicDataColor(Double datavalue, Double maxValue) {
		String dataColor=null;
//		if(maxValue>0) {
		if(datavalue>=0.0 && datavalue<=(maxValue/4))
			dataColor="#d7191c";
		else if(datavalue>=(maxValue/4)+0.1 && datavalue<=(maxValue/2))
			dataColor="#fdae61";
		else if(datavalue>=(maxValue/2)+0.1 && datavalue<=(maxValue/1.42))
			dataColor="#a6d96a";
		else 
			dataColor="#1a9641";
//		}
		return dataColor;
	}

	private List<LegendModel> getLegendModel(Double maxValue) {
		List<LegendModel> legendModels = new ArrayList<>();
		if(maxValue>0) {
		legendModels.add(setLegendModel("#d7191c", "0.0-"+(maxValue/4), 0.0, (maxValue/4)));
		legendModels.add(setLegendModel("#fdae61", String.format("%.1f", (maxValue/4)+0.1)+"-"+String.format("%.1f",(maxValue/2)), (maxValue/4)+0.1, (maxValue/2)));
		legendModels.add(setLegendModel("#a6d96a", String.format("%.1f",(maxValue/2)+0.1)+"-"+String.format("%.1f",(maxValue/1.42)), (maxValue/2)+0.1, (maxValue/1.42)));
		legendModels.add(setLegendModel("#1a9641", String.format("%.1f",(maxValue/1.42)+0.1)+"-"+String.format("%.1f",maxValue)+"(Max)", (maxValue/1.42)+0.1, maxValue));
			}
		else {
			legendModels.add(setLegendModel("#d7191c", "0.0-"+(maxValue/4), 0.0, (maxValue/4)));
			legendModels.add(setLegendModel("#fdae61", "0.0-0.0", (maxValue/4)+0.1, (maxValue/2)));
			legendModels.add(setLegendModel("#a6d96a", "0.0-0.0", (maxValue/2)+0.1, (maxValue/1.42)));
			legendModels.add(setLegendModel("#1a9641", "0.0-0.0", (maxValue/1.42)+0.1, maxValue));
		}
		return legendModels;
	}


	private LegendModel setLegendModel(String color, String range, double startRange, double endRange) {
		LegendModel legendModel = new LegendModel();
		legendModel.setColor(color);
		legendModel.setRange(range);
		legendModel.setStartRange(startRange);
		legendModel.setEndRange(endRange);
		
		return legendModel;
	}
	
	/*
	 * @author Biswabhusan Pradhan
	 * 
	 */
	public Map<String,PerformanceData> getOntimeData() {
		
		//get last month date from current date
		Date currentDate = new Date();
		Calendar cal=Calendar.getInstance();
	    cal.setTime(currentDate);
	    cal.add(Calendar.MONTH, -1);
	    currentDate = cal.getTime();
		TimePeriod tp = timePeriodRepository.getCurrentTimePeriod(currentDate, "1");
		
		PerformanceData performanceData=new PerformanceData();
		List<String> columnHead=new ArrayList<>();
		columnHead.addAll(Arrays.asList(configurableEnvironment.getProperty("ontime_dashboard_head").split(",")));
		performanceData.setTableColumns(columnHead);
		MatchOperation mop=Aggregation.match(Criteria.where("timePeriod.timePeriodId").is(tp.getTimePeriodId()));
		ProjectionOperation pop=Aggregation.project().and("formId").as("formId").and(when(where("submissionStatus")
				.is("ONTIMESUBMISSION")).then(1).otherwise(0)).as("ontime")
				.and(when(where("submissionStatus").is("LATESUBMISSION")).then(1).otherwise(0)).as("delayed")
				.and(when(where("submissionStatus").is("INVALIDSUBMISSION")).then(1).otherwise(0)).as("invalid");
		
		GroupOperation gop=Aggregation.group("formId").sum("ontime").as("ontime").sum("delayed").as("delayed").sum("invalid").as("invalid");
		String lop2="{$lookup:{from:'designationFormMapping',let : {formId:'$_id'},pipeline:[{$match:{$expr : {$and:[{$eq : ['$form.formId','$$formId']},{$eq : ['$accessType' , 'DATA_ENTRY']}]}}},{$project:{form:'$form.name',formId:'$form.formId',ontime:'$ontime,delayed:$delayed',invalid:'$invalid',role:'$designation.name',_id:0}}],as : 'des'}}";
		String uops="{$unwind : {path : '$des'}}";
		String pops="{$project:{formId:'$des.formId',form:'$des.form',ontime:'$ontime',delayed:'$delayed',invalid:'$invalid',role:'$des.role',_id:0}}";
		UnwindOperation uop2=Aggregation.unwind("des");
		ProjectionOperation pop2=Aggregation.project("ontime","delayed","invalid").and("des.form.name").as("form").and("des.designation.name").as("role").andExclude("_id");
		Aggregation resultQuery=Aggregation.newAggregation(mop,pop,gop,new CustomProjectAggregationOperation(lop2),
				new CustomProjectAggregationOperation(uops),new CustomProjectAggregationOperation(pops));
		List<Map> dataMap=mongoTemplate.aggregate(resultQuery,CFInputFormData.class, Map.class).getMappedResults();
		performanceData.setTableData(dataMap);
		
		Map<String,PerformanceData> map = new HashMap<>();
		String date = new SimpleDateFormat("MMM-yyyy").format(tp.getStartDate());
		map.put(date, performanceData);
		return map;
	}
	
	/*
	 * @author Biswabhusan Pradhan
	 * 
	 */
	public Map<String,PerformanceData> getPlanningdata() {
		
		//get last month date from current date
		Date currentDate = new Date();
//		Calendar cal=Calendar.getInstance();
//	    cal.setTime(currentDate);
//	    cal.add(Calendar.MONTH, -1);
//	    currentDate = cal.getTime();
	    
	    List<TimePeriod> tpList = timePeriodRepository.findAllByPeriodicityOrderByCreatedDateAsc("1");
	    
	    /**
	     * if current date is between 1 to 10 both inclusive than take the time period as last month
	     * if date crosses to 10 than take time period to current month.
	     */
		TimePeriod tp=null;
		
	    DateModel dates = getDatesForTimePeriod(currentDate);
	    
	    if ((DateUtils.isSameDay(currentDate, dates.getStartDate())|| (currentDate.after(dates.getStartDate())) && (DateUtils
				.isSameDay(currentDate, dates.getEndDate())|| currentDate.before(dates.getEndDate())))) {
	    	tp = tpList.get(tpList.size()-2);
	    }else {
	    	tp = tpList.get(tpList.size()-1);
	    }
	    	
	    Integer monthKey=tp.getEndDate().getMonth();
		String month = configurableEnvironment.getProperty(String.valueOf(monthKey));
		
		PerformanceData performanceData=new PerformanceData();
		List<String> columnHead=new ArrayList<>();
		columnHead.addAll(Arrays.asList(configurableEnvironment.getProperty("planning_dashboard_head").split(",")));
		performanceData.setTableColumns(columnHead);
		
		String mop = "{ $match : {month : "+monthKey+"}}";
		String lop1 = "{ $lookup:{ from:'timePeriod', let:{month:'"+month+"', year:'$year'}, pipeline:[{ $match:{ $expr:{$and:[{$eq:['$timePeriodDuration','$$month']},{$eq:['$year','$$year']}]} } }], as:'timeperiod' } }";
		String uop1= "{ $unwind:{ path:'$timeperiod' } }";
		String pop2 = "{ $project : {formId : '$formId', timePeriodId : '$timeperiod.timePeriodId', target : '$target'}}";
		String gop1= "{ $group:{ _id:{formId:'$formId',tp:'$timePeriodId'}, target : {$sum:'$target'} } }";
		String lop2 = "{ $lookup:{ from:'enginesForm', localField:'_id.formId', foreignField:'formId', as : 'form' } }";
		String uop2="{ $unwind:{ path : '$form' } }";
		String lop3="{ $lookup:{ from : 'cFInputFormData', let : {formId:'$_id.formId', tp : '$_id.tp'}, pipeline:[{ $match : { $expr : {$and : [{$eq:['$formId','$$formId']}, "
				+ "{$eq:['$timePeriod.timePeriodId', '$$tp']}]} } }, { $group:{ _id : {formId:'$formId'}, achieved : {$sum:{$cond:[{$eq:['$formId',4]},'$data.F4QT1',{$cond:[{$eq:['$formId',3]},"
				+ "'$data.F3NET11',{$cond:[{$eq:['$formId',9]},'$data.F9NET11',1]}]}]}} } }, "
				+ "{ $project:{ _id :0, achievecount : '$achieved' } } ], as : 'achieved' } }";
		String uop3="{ $unwind:{ path : \"$achieved\" } }";
		String lop4 = "{$lookup:{ from:'designationFormMapping', let : {formId:'$_id.formId'}, pipeline:[ { $match:{ $expr : {$and:[{$eq : ['$form.formId','$$formId']},"
				+ "{$eq:[\"$accessType\" , \"DATA_ENTRY\"]}]} } } ], as : 'des' } }";
		String uop4 = "{ $unwind : { path : \"$des\" } }";
		String pop1="{ $project:{ formId: \"$_id.formId\", tp: \"$_id.tp\", role: \"$des.designation.name\", form : \"$des.form.name\", planned : \"$target\", "
				+ "achieved : \"$achieved.achievecount\",percentage:{$cond : {if : { $eq : ['$target',0] }, then : 100, else : { $multiply: [ { $divide: ['$achieved.achievecount', '$target'] }, 100 ] }}}, _id:0 } }";
		
		Aggregation resultQuery=Aggregation.newAggregation(new CustomProjectAggregationOperation(mop),new CustomProjectAggregationOperation(lop1),new CustomProjectAggregationOperation(uop1),
				new CustomProjectAggregationOperation(pop2),new CustomProjectAggregationOperation(gop1),
				new CustomProjectAggregationOperation(lop3),new CustomProjectAggregationOperation(uop3),
				new CustomProjectAggregationOperation(lop4), new CustomProjectAggregationOperation(uop4),new CustomProjectAggregationOperation(pop1));
		
		List<Map> dataMap=mongoTemplate.aggregate(resultQuery,PlanningData.class, Map.class).getMappedResults();
		dataMap.stream().forEach(v -> {
			v.replace("percentage", Double.parseDouble(new DecimalFormat("##.#").format(v.get("percentage"))));
			v.replace("form",configurableEnvironment.getProperty("performance_"+v.get("formId")));
			
		});
		performanceData.setTableData(dataMap);
		
		Map<String,PerformanceData> map = new HashMap<>();
//		DateModel dates = getDatesForTimePeriod(currentDate);
		String date = new SimpleDateFormat("MMM-yyyy").format(tp.getStartDate());
		
//		if ((DateUtils.isSameDay(currentDate, dates.getStartDate())|| (currentDate.after(dates.getStartDate())) && (DateUtils
//				.isSameDay(currentDate, dates.getEndDate())|| currentDate.before(dates.getEndDate())))) {
//			
//				cal=Calendar.getInstance();
//			    cal.setTime(currentDate);
//			    cal.add(Calendar.MONTH, -1);
//			    currentDate = cal.getTime();
//			    date = new SimpleDateFormat("MMM-yyyy").format(currentDate);
//		}
		
		map.put(date, performanceData);
		return map;
	}


	@Override
	public ResponseEntity<String> getThematicViewDownload(ThematicDashboardDataModel thematicDashboardDataModel,
			HttpServletRequest request) {

//		String areaLevelName = thematicDashboardDataModel.getAreaLevelId();
//		String areaName = areaRepository.findByAreaId(thematicDashboardDataModel.getAreaId()).getAreaName();
		
		String formName = thematicDashboardDataModel.getFormId();									
		Map<String, Object> indMap = indicatorRepository.getIndicatiorsIn(Arrays.asList(thematicDashboardDataModel.getIndicatorId())).get(0)
				.getIndicatorDataMap();
		String indicatorName = (String) indMap.get("indicatorName");
		TimePeriod tp = timePeriodRepository.findByTimePeriodId(thematicDashboardDataModel.getTimePeriodId());
		String timePeriod = tp.getTimePeriodDuration().concat("'").concat(String.valueOf(tp.getYear()));

		try {

			String dir = configurableEnvironment.getProperty("report.path");
			String fileName = configurableEnvironment.getProperty("thematic.dashboard.filename");

			File file = new File(dir);

			/*
			 * make directory if doesn't exist
			 */
			if (!file.exists())
				file.mkdirs();

			String name = fileName + "_" + new SimpleDateFormat("ddMMyyyyHHmmss").format(new Date()) + ".pdf";
			String path = dir + "" + name;

			FileOutputStream fos = new FileOutputStream(new File(path));

			Rectangle layout = new Rectangle(PageSize.A4.rotate());
			layout.setBackgroundColor(new BaseColor(221, 221, 221));

			Document document = new Document(PageSize.A4.rotate());
			PdfWriter writer = PdfWriter.getInstance(document, fos);

			String uri = request.getRequestURI();
			String url = request.getRequestURL().toString();
			url = url.replaceFirst(uri, "");

			HeaderFooter headerFooter = new HeaderFooter(url, "dashboard");
			writer.setPageEvent(headerFooter);

			document.open();

//			float yAxis = document.getPageSize().getHeight() - document.topMargin() - document.bottomMargin();
			float yAxis=10;
			float xAxis = 10;
			
			Font fontHeader = new Font(FontFamily.HELVETICA, 16, Font.NORMAL, BaseColor.BLACK);
			Font indFont = new Font(FontFamily.TIMES_ROMAN, 12, Font.NORMAL, GrayColor.DARK_GRAY);
			Font legendFont = new Font(FontFamily.TIMES_ROMAN, 9, Font.NORMAL, GrayColor.DARK_GRAY);
			Font formHeader = new Font(FontFamily.HELVETICA, 16, Font.NORMAL, BaseColor.RED);
			Font indHeader = new Font(FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.RED);
			
			BaseColor headerBgColor = WebColors.getRGBColor("#C5D9F1");
			BaseColor evenColColor = WebColors.getRGBColor("#C0C0C0");
			
			Chunk headLineText = new Chunk("Thematic View", fontHeader);
			Paragraph para1 = new Paragraph(headLineText);
			para1.setAlignment(Element.ALIGN_CENTER);
			para1.setSpacingBefore(15);
			para1.setSpacingAfter(10);
			document.add(para1);

			Paragraph areaParagraph = new Paragraph();
			areaParagraph.setAlignment(Element.ALIGN_CENTER);
			areaParagraph.setSpacingBefore(15);
			areaParagraph.setSpacingAfter(10);
			
			String chunkName=null;
			
//			if(thematicDashboardDataModel.getClusterName()==null) {
//				chunkName =  "Form : " + formName
//						+ ",  Sector : " + thematicDashboardDataModel.getSector() + ", Indicator : " + indicatorName + ", Time Period : " + timePeriod;
//			}else {
//				chunkName = "Area Level : " + StringUtils.capitalise(areaLevelName.toLowerCase()) + ", Cluster : " + thematicDashboardDataModel.getClusterName() + ", Form : " + formName
//						+ ",  Sector : " + thematicDashboardDataModel.getSector() + ", Indicator : " + indicatorName + ", Time Period : " + timePeriod;
//			}

			chunkName =  "Form : " + formName
					+ ",  Sector : " + thematicDashboardDataModel.getSector() + ", Indicator : " + indicatorName + ", Time Period : " + timePeriod;
			
			Chunk areaChunk = new Chunk(chunkName,indFont);
			areaParagraph.add(areaChunk);
			
			document.add(areaParagraph);
			
			Chunk formChunk = new Chunk(indicatorName.concat("(").concat(thematicDashboardDataModel.getUnit()).concat(")"), indHeader);
			Paragraph para2 = new Paragraph(formChunk);
			para1.setAlignment(Element.ALIGN_CENTER);
			para1.setSpacingBefore(15);
			para1.setSpacingAfter(10);
			document.add(para2);
			
			/**
			 * write Map--image
			 */
			String rbpath = configurableEnvironment.getProperty("thematic.dashboard.imagename")+ new SimpleDateFormat("ddMMyyyyHHmmss").format(new Date()) + ".svg";
			rbpath = dir + "" + rbpath;
			
			File svgFile = new File(rbpath);
			FileOutputStream fop = new FileOutputStream(svgFile);
			byte[] contentbytes = thematicDashboardDataModel.getSvg().getBytes();
			fop.write(contentbytes);
			
			String jpgFilePath = createMapImgFromFile(rbpath);
			Image jpgImage = Image.getInstance(jpgFilePath);

			//set legend
			PdfContentByte canvas = writer.getDirectContent();
			
			List<Map<String, String>> legentMap = thematicDashboardDataModel.getLegend();
			
			Map<String, String> lgnd = new HashMap<>();
			lgnd.put("range", "Non Intervention");
			lgnd.put("color", "#888888");
			lgnd.put("rgbColor", "rgba(136,136,136)");
			legentMap.add(lgnd);
			
			
			String css="";
			String leg = "";
			int i =120;
			int c=50;
			for(Map<String, String> legend : legentMap) {
				
				css = legend.get("color");
				leg = legend.get("range");
				
				int color = (int) Long.parseLong(css.split("#")[1], 16);
				int r = (color >> 16) & 0xFF;
				int g = (color >> 8) & 0xFF;
				int b = (color >> 0) & 0xFF;
				
				canvas.rectangle(xAxis+600, yAxis + i + 130 -c, 7, 7);
//				System.out.println(leg+" yvalue ="+ (yAxis + i + 70-c));
				canvas.setColorFill(new BaseColor(r, g, b));
				canvas.fill();
				
				PdfPCell legendCell = new PdfPCell();
				Paragraph legendPara = new Paragraph(leg, legendFont);
				legendPara.setAlignment(Element.ALIGN_LEFT);
				legendPara.setSpacingBefore(1);
				legendPara.setSpacingAfter(3);
				legendCell.addElement(legendPara);
				legendCell.setBorder(Rectangle.NO_BORDER);

				PdfPTable legendTable = new PdfPTable(1);
				legendTable.addCell(legendCell);
				legendTable.setTotalWidth(document.getPageSize().getWidth());
				legendTable.writeSelectedRows(-1, -1, xAxis +630, yAxis + 146 + i -c,
						writer.getDirectContent());

				i -= 40;
				c -=10;
			}
			
			
			//write data value in table
			
            List<Map<String, String>> tableData = thematicDashboardDataModel.getTableData();
            PdfPTable table = new PdfPTable(2);
            if(!tableData.isEmpty()) {
    			
    			PdfPCell header1 = new PdfPCell();
    			Paragraph legendPara = new Paragraph("Area", indFont);
    			legendPara.setAlignment(Element.ALIGN_LEFT);
    			legendPara.setSpacingBefore(1);
    			legendPara.setSpacingAfter(3);
    			header1.setBackgroundColor(headerBgColor);
    			header1.addElement(legendPara);
    			table.addCell(header1);
    			
    			PdfPCell header2 = new PdfPCell();
    			Paragraph headerPara = new Paragraph("Value (in "+ thematicDashboardDataModel.getUnit()+")", indFont);
    			legendPara.setAlignment(Element.ALIGN_LEFT);
    			legendPara.setSpacingBefore(1);
    			legendPara.setSpacingAfter(3);
    			header2.setBackgroundColor(headerBgColor);
    			header2.addElement(headerPara);
    			table.addCell(header2);
    			
    			int index=1;
    			for(Map<String,String> data: tableData){
    			
    				PdfPCell area = new PdfPCell();
        			Paragraph areaPara = new Paragraph(data.get("areaName"), indFont);
        			areaPara.setAlignment(Element.ALIGN_LEFT);
        			areaPara.setSpacingBefore(1);
        			areaPara.setSpacingAfter(3);
        			if(index%2==0)
        				area.setBackgroundColor(evenColColor);
        			area.addElement(areaPara);
        			table.addCell(area);
        			
        			
        			PdfPCell valueCell = new PdfPCell();
        			Paragraph value = new Paragraph(data.get("value"), indFont);
        			value.setAlignment(Element.ALIGN_LEFT);
        			value.setSpacingBefore(1);
        			value.setSpacingAfter(3);
        			if(index%2==0)
        				valueCell.setBackgroundColor(evenColColor);
        			valueCell.addElement(value);
        			table.addCell(valueCell);
    				
        			index++;
    			}
    			
            }
			
			document.add(jpgImage);
			
			if(!tableData.isEmpty()) 
				 document.add(table);
			
			document.close();
			return new ResponseEntity<String>(path,HttpStatus.OK);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}


	private String createMapImgFromFile(String path) {
		
		try {
			
		
		String filePath = configurableEnvironment.getProperty("report.path") + "svg/" ;

		File filePathDirect = new File(filePath);
		if (!filePathDirect.exists())
			filePathDirect.mkdir();

		String fileName = "";

		fileName = filePath + "thematic_map_image" + new SimpleDateFormat("ddMMyyyyHHmmss").format(new Date()) + ".jpg";
		
		JPEGTranscoder t = new JPEGTranscoder();

		// Set the transcoding hints.
		t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(.8));
		t.addTranscodingHint(JPEGTranscoder.KEY_WIDTH, new Float(900));
		t.addTranscodingHint(JPEGTranscoder.KEY_HEIGHT, new Float(380));
		
		// Create the transcoder input.
		String svgURI = new File(path).toURI().toURL().toString();

		TranscoderInput input = new TranscoderInput(svgURI);

		OutputStream ostream = new FileOutputStream(fileName);
		TranscoderOutput output = new TranscoderOutput(ostream);

		// Save the image.

		t.transcode(input, output);
		ostream.flush();
		ostream.close();
		
		return fileName;
		
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getPerformanceTrend(Integer formId){
		List<Integer> utTimeperiodList = new ArrayList<>();
		timePeriodRepository.findTop6ByPeriodicityOrderByTimePeriodIdDesc("1").stream()
				.forEach(tp -> {
					utTimeperiodList.add(tp.getTimePeriodId());
				});
		utTimeperiodList.remove(0);
		MatchOperation mop = Aggregation.match(Criteria.where("timePeriod.timePeriodId").in(utTimeperiodList).and("formId").is(formId));
		GroupOperation gop = Aggregation.group("timePeriod.timePeriodId","submissionStatus").count().as("value");
		LookupOperation lop = Aggregation.lookup("timePeriod", "_id.timePeriodId", "timePeriodId", "tp");
		UnwindOperation uop=Aggregation.unwind("tp");
		SortOperation sop = Aggregation.sort(Sort.Direction.ASC, "tp.timePeriodId");
		ProjectionOperation pop = Aggregation.project().and("submissionStatus").as("key").and("value").as("value").and("tp.timePeriodDuration").as("axis");
		
		Aggregation resultQuery = Aggregation.newAggregation(mop,gop,lop,uop,sop,pop);
		List<Map> dataMap=mongoTemplate.aggregate(resultQuery,CFInputFormData.class, Map.class).getMappedResults();
		dataMap.forEach(o -> {
			if(String.valueOf(o.get("key")).equals("ONTIMESUBMISSION"))
				o.put("color", "#1a9641");
			else if(String.valueOf(o.get("key")).equals("LATESUBMISSION"))
				o.put("color", "#a6d96a");
			else 
				o.put("color", "#fdae61");
		});
		
		List<LegendModel> legendModels = new ArrayList<>();
		legendModels.add(setLegendModel("#1a9641", "Ontime", 70.1, 100.0));
		legendModels.add(setLegendModel("#689435", "Delayed", 50.1, 70.0));
		legendModels.add(setLegendModel("#fdae61", "Invalid", 30.1, 50.0));
		
		Map<String, Object> _pmap=new HashMap<>();
		_pmap.put("lineChart", dataMap);
		_pmap.put("legends", legendModels);
		return _pmap;
	}
	
	public void saveAchievemaneData(Integer timePeriodId) {
		TimePeriod tp = timePeriodRepository.findByTimePeriodId(timePeriodId);
		
		String mop = "{ $match : {month : "+tp.getEndDate().getMonth()+"}}";
		String lop1 = "{ $lookup:{ from:'timePeriod', let:{month:'"+tp.getTimePeriodDuration()+"', year:"+tp.getYear()+"}, pipeline:[{ $match:{ $expr:{$and:[{$eq:['$timePeriodDuration','$$month']},{$eq:['$year','$$year']}]} } }], as:'timeperiod' } }";
		String uop1= "{ $unwind:{ path:'$timeperiod' } }";
		String gop1= "{ $group:{ _id:{formId:'$formId',tp:'$timeperiod.timePeriodId', month : '$timeperiod.timePeriodDuration', year : '$timeperiod.year'}, target : {$sum:'$target'} } }";
		String lop3="{ $lookup:{ from : 'cFInputFormData', let : {formId:'$_id.formId', tp : '$_id.tp'}, pipeline:[{ $match : { $expr : {$and : [{$eq:['$formId','$$formId']}, {$eq:['$timePeriod.timePeriodId', '$$tp']}]} } }, { $group:{ _id : {formId:'$formId'}, "
				+ "achieved : {$sum:{$cond:[{$eq:['$formId',4]},'$data.F4QT1',{$cond:[{$eq:['$formId',3]},'$data.F3NET11',{$cond:[{$eq:['$formId',9]},'$data.F9NET11',1]}]}]}} } }, { $project:{ _id :0, achievecount : '$achieved' } } ], as : 'achieved' } }";
		String uop3="{ $unwind:{ path : \"$achieved\" } }";
		String lop4 = "{$lookup:{ from:'designationFormMapping', let : {formId:'$_id.formId'}, pipeline:[ { $match:{ $expr : {$and:[{$eq : ['$form.formId','$$formId']},{$eq:[\"$accessType\" , \"DATA_ENTRY\"]}]} } } ], as : 'des' } }";
		String uop4 = "{ $unwind : { path : \"$des\" } }";
		String pop1="{ $project:{ formId: \"$_id.formId\", tp: \"$_id.tp\",month : '$_id.month', year : '$_id.year', role: \"$des.designation.name\", form : \"$des.form.name\", "
				+ "planned : \"$target\", achieved : \"$achieved.achievecount\","
				+ "percentage:{$cond : {if : { $eq : ['$target',0] }, then : 100, else : { $multiply: [ { $divide: ['$achieved.achievecount', '$target'] }, 100 ] }}}, _id:0 } }";
		
		Aggregation resultQuery=Aggregation.newAggregation(new CustomProjectAggregationOperation(mop),new CustomProjectAggregationOperation(lop1),new CustomProjectAggregationOperation(uop1),
				new CustomProjectAggregationOperation(gop1),new CustomProjectAggregationOperation(lop3),
				new CustomProjectAggregationOperation(uop3),new CustomProjectAggregationOperation(lop4),
				new CustomProjectAggregationOperation(uop4),new CustomProjectAggregationOperation(pop1));
		
		List<Map> dataMap=mongoTemplate.aggregate(resultQuery,PlanningData.class, Map.class).getMappedResults();
		System.out.println(dataMap);
		
		List<AchievementData> _dataMap=new ArrayList<>();
		dataMap.forEach(v -> {
			AchievementData data=new AchievementData();
			data.setDataValue(v);
			
			_dataMap.add(data);
		});
		
		achievementDataRepository.save(_dataMap);
	}
	
	public Map<String, Object> getAchievementData(Integer formId){
		List<AchievementData> data=achievementDataRepository.getAchievementData(formId);
		List<Map> _mlist=new ArrayList<>();
		data.forEach(v -> {
			Map<String, String> m = new HashMap<>();
			m.put("axis", String.valueOf(v.getDataValue().get("month"))+"-"+String.valueOf(v.getDataValue().get("year")));
			m.put("key", "Achievement");
			m.put("value", String.valueOf(new DecimalFormat("##.#").format(Double.parseDouble(String.valueOf(v.getDataValue().get("percentage"))))));
			m.put("unit", "percent");
			
			_mlist.add(m);
		});
		Map<String, Object> _amap=new HashMap<>();
		_amap.put("lineChart", _mlist);
		return _amap;
	}
	
	public void mapUsers() {
		TimePeriod tp = timePeriodRepository.findTop1ByPeriodicityOrderByTimePeriodIdDesc("1");
		UnwindOperation uop = Aggregation.unwind("mappedAreaIds");
		GroupOperation gop = Aggregation.group("mappedAreaIds", "userName");
		String pop = "{$project : {username : '$_id.userName',areaId : '$_id.mappedAreaIds',status : 'active', _id : 0}}";
		
		Aggregation resultQuery = Aggregation.newAggregation(uop, gop, new CustomProjectAggregationOperation(pop));
		
		List<Map> data=mongoTemplate.aggregate(resultQuery, Account.class, Map.class).getMappedResults();
		List<UserAreaMap> uams=new ArrayList<>();
		data.forEach(v -> {
			UserAreaMap uam = new UserAreaMap();
			uam.setAreaId(Integer.parseInt(String.valueOf(v.get("areaId"))));
			uam.setTp(tp.getTimePeriodId());
			uam.setUsername(String.valueOf(v.get("username")));
			uam.setStartDate(tp.getStartDate());
			uam.setStatus("active");
			uams.add(uam);
		});
		System.out.println(data);
		userAreaMapRepository.insert(uams);
	}
	
	public void updateUserMap(String username, List<Integer> areaIds) {
		
		try {

			TimePeriod tp = timePeriodRepository.findTop1ByPeriodicityOrderByTimePeriodIdDesc("1");
			
			Query query = new Query();
			query.addCriteria(Criteria.where("username").is(username).and("status").is("active").and("tp").is(tp.getTimePeriodId()).and("areaId").nin(areaIds));
			Update update = new Update();
			update.set("status", "inactive");
			update.set("endDate", new Date());
			mongoTemplate.updateMulti(query, update, UserAreaMap.class);
			
			List<UserAreaMap> uams = userAreaMapRepository.findByUsernameAndStatusAndTp(username, "active", tp.getTimePeriodId());
			uams.forEach(u -> {
				if (areaIds.contains(u.getAreaId())) {
					areaIds.remove(u.getAreaId());
				}
			});
			
			areaIds.forEach(a -> {
				UserAreaMap uam = new UserAreaMap();
				uam.setAreaId(a);
				uam.setUsername(username);
				uam.setStartDate(new Date());
				uam.setTp(tp.getTimePeriodId());
				uam.setStatus("active");
				
				userAreaMapRepository.save(uam);
			});
		
		}catch(Exception e) {
			Mail mail = new Mail();
			List<String> emailId = new ArrayList<String>();
			emailId.add(configurableEnvironment.getProperty("emailId.send.mail"));
			mail.setToEmailIds(emailId);
			mail.setToUserName("Biswa");
			mail.setSubject("Error While writing in a document UserAreaMap");
			mail.setMessage(configurableEnvironment.getProperty("Error While writing in a document UserAreaMap with payload {} ") + username +"  "+areaIds);
			mail.setFromUserName("Administrator");
			mailService.sendMail(mail);
			throw new RuntimeException(e);
		}
	}


	@Override
	public ResponseEntity<String> getThematicViewDownloadExcel(ThematicDashboardDataModel thematicDashboardDataModel,
			HttpServletRequest request) {

//		String areaLevelName = thematicDashboardDataModel.getAreaLevelId();
//		String areaName = areaRepository.findByAreaId(thematicDashboardDataModel.getAreaId()).getAreaName();
		String formName = thematicDashboardDataModel.getFormId();
		Map<String, Object> indMap = indicatorRepository.getIndicatiorsIn(Arrays.asList(thematicDashboardDataModel.getIndicatorId())).get(0)
				.getIndicatorDataMap();
		String indicatorName = (String) indMap.get("indicatorName");
		TimePeriod tp = timePeriodRepository.findByTimePeriodId(thematicDashboardDataModel.getTimePeriodId());
		String timePeriod = tp.getTimePeriodDuration().concat("'").concat(String.valueOf(tp.getYear()));

		try {

			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("thematic_data");
			Row row;
			Cell cell;
			
			Integer rowCount=5;
			Integer colCount=0;
			
			CellStyle styleForHeadingWithoutBorder = ExcelStyleSheet.getStyleForHeadingWithoutBorder(workbook);
			CellStyle styleForHeadingWithBorder = ExcelStyleSheet.getStyleForHeadingWithBorder(workbook,true);
			CellStyle styleHeading = ExcelStyleSheet.getStyleForHeadingWithBorder(workbook,false);
			
			row = sheet.createRow(1);
			cell = row.createCell(colCount);
			sheet.setHorizontallyCenter(true);
			cell.setCellStyle(styleHeading);
			cell.setCellValue("Thematic View");
			row.setHeight((short) 500);
			sheet.setColumnWidth(cell.getColumnIndex(), 1000);
			sheet = ExcelStyleSheet.doMergeThematicView(1, 1, 0,13, sheet,true);
			
			row = sheet.createRow(2);
			cell = row.createCell(1);
			sheet.setHorizontallyCenter(true);
			cell.setCellStyle(ExcelStyleSheet.getStyleForHeadingWithBorder(workbook,null));
			cell.setCellValue(indicatorName+"("+thematicDashboardDataModel.getUnit()+")");
			row.setHeight((short) 500);
			sheet.setColumnWidth(cell.getColumnIndex(), 1000);
			sheet = ExcelStyleSheet.doMergeThematicView(2, 2, 1,13, sheet,false);
			
//			row = sheet.createRow(rowCount);
//			cell = row.createCell(colCount);
//			sheet.setHorizontallyCenter(true);
//			row.setHeight((short) 300);
//			sheet.setColumnWidth(cell.getColumnIndex(), 15800);
//			cell.setCellStyle(styleForHeadingWithoutBorder);
//			cell.setCellValue("Select Area Level");
//			rowCount++;
//			
//			row = sheet.createRow(rowCount);
//			cell = row.createCell(colCount);
//			sheet.setHorizontallyCenter(true);
//			row.setHeight((short) 300);
//			sheet.setColumnWidth(cell.getColumnIndex(), 15800);
//			cell.setCellStyle(styleForHeadingWithBorder);
//			cell.setCellValue(areaLevelName);
//			rowCount++;
			
//			if(thematicDashboardDataModel.getClusterName()==null) {
//				row = sheet.createRow(rowCount);
//				row.setHeight((short) 1000);
//				cell = row.createCell(colCount);
//				sheet.setHorizontallyCenter(true);
//				cell.setCellStyle(styleForHeadingWithoutBorder);
//				row.setHeight((short) 350);
//				sheet.setColumnWidth(cell.getColumnIndex(), 15800);
//				cell.setCellValue("Select Block");
//				rowCount++;
//				
//				row = sheet.createRow(rowCount);
//				row.setHeight((short) 1000);
//				cell = row.createCell(colCount);
//				sheet.setHorizontallyCenter(true);
//				cell.setCellStyle(styleForHeadingWithBorder);
//				row.setHeight((short) 350);
//				sheet.setColumnWidth(cell.getColumnIndex(), 15800);
//				cell.setCellValue(areaName);
//				rowCount++;
//			}else {
//				
//				row = sheet.createRow(rowCount);
//				cell = row.createCell(colCount);
//				sheet.setHorizontallyCenter(true);
//				cell.setCellStyle(styleForHeadingWithoutBorder);
//				row.setHeight((short) 350);
//				sheet.setColumnWidth(cell.getColumnIndex(), 15800);
//				cell.setCellValue("Select Cluster");
//				rowCount++;
//				
//				row = sheet.createRow(rowCount);
//				cell = row.createCell(colCount);
//				sheet.setHorizontallyCenter(true);
//				cell.setCellStyle(styleForHeadingWithBorder);
//				cell.setCellValue(thematicDashboardDataModel.getClusterName());
//				row.setHeight((short) 350);
//				sheet.setColumnWidth(cell.getColumnIndex(), 15800);
//				rowCount++;
//			}
			
			row = sheet.createRow(rowCount);
			cell = row.createCell(colCount);
			sheet.setHorizontallyCenter(true);
			cell.setCellStyle(styleForHeadingWithoutBorder);
			row.setHeight((short) 350);
			sheet.setColumnWidth(cell.getColumnIndex(), 15800);
			cell.setCellValue("Select Form");
			rowCount++;
			
			row = sheet.createRow(rowCount);
			cell = row.createCell(colCount);
			sheet.setHorizontallyCenter(true);
			row.setHeight((short) 350);
			sheet.setColumnWidth(cell.getColumnIndex(), 15800);
			cell.setCellStyle(styleForHeadingWithBorder);
			cell.setCellValue(formName);
			rowCount++;
			
			row = sheet.createRow(rowCount);
			cell = row.createCell(colCount);
			sheet.setHorizontallyCenter(true);
			row.setHeight((short) 350);
			sheet.setColumnWidth(cell.getColumnIndex(), 15800);
			cell.setCellStyle(styleForHeadingWithoutBorder);
			cell.setCellValue("Select Sector");
			rowCount++;
			
			row = sheet.createRow(rowCount);
			cell = row.createCell(colCount);
			sheet.setHorizontallyCenter(true);
			cell.setCellStyle(styleForHeadingWithBorder);
			row.setHeight((short) 350);
			sheet.setColumnWidth(cell.getColumnIndex(), 15800);
			cell.setCellValue(thematicDashboardDataModel.getSector());
			rowCount++;
			
			row = sheet.createRow(rowCount);
			cell = row.createCell(colCount);
			sheet.setHorizontallyCenter(true);
			row.setHeight((short) 350);
			sheet.setColumnWidth(cell.getColumnIndex(), 15800);
			cell.setCellStyle(styleForHeadingWithoutBorder);
			cell.setCellValue("Select Indicator");
			rowCount++;
			
			row = sheet.createRow(rowCount);
			cell = row.createCell(colCount);
			sheet.setHorizontallyCenter(true);
			cell.setCellStyle(styleForHeadingWithBorder);
			row.setHeight((short) 350);
			sheet.setColumnWidth(cell.getColumnIndex(), 15800);
			cell.setCellValue(indicatorName);
			rowCount++;
			
			row = sheet.createRow(rowCount);
			cell = row.createCell(colCount);
			sheet.setHorizontallyCenter(true);
			row.setHeight((short) 350);
			sheet.setColumnWidth(cell.getColumnIndex(), 15800);
			cell.setCellStyle(styleForHeadingWithoutBorder);
			cell.setCellValue("Time Period");
			rowCount++;
			
			row = sheet.createRow(rowCount);
			cell = row.createCell(colCount);
			sheet.setHorizontallyCenter(true);
			row.setHeight((short) 350);
			sheet.setColumnWidth(cell.getColumnIndex(), 15800);
			cell.setCellStyle(styleForHeadingWithBorder);
			cell.setCellValue(timePeriod);
			rowCount++;
			
			
			
			String dir = configurableEnvironment.getProperty("report.path");
			String fileName = configurableEnvironment.getProperty("thematic.dashboard.filename");

			File file = new File(dir);

			/*
			 * make directory if doesn't exist
			 */
			if (!file.exists())
				file.mkdirs();

			/**
			 * write Map--image
			 */
			String rbpath = configurableEnvironment.getProperty("thematic.dashboard.imagename")+ new SimpleDateFormat("ddMMyyyyHHmmss").format(new Date()) + ".svg";
			rbpath = dir + "" + rbpath;
			
			File svgFile = new File(rbpath);
			FileOutputStream fop = new FileOutputStream(svgFile);
			byte[] contentbytes = thematicDashboardDataModel.getSvg().getBytes();
			fop.write(contentbytes);

			String jpgFilePath = createMapImgFromFile(rbpath);
			
			BufferedImage bImage = ImageIO.read(new File(jpgFilePath));
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(bImage, "jpg", bos );
			byte [] data = bos.toByteArray();
			insertimage(data, workbook, sheet);
			
			rowCount=12;
			colCount=7;
			//insert legends
			List<Map<String, String>> legendsz = thematicDashboardDataModel.getLegend();
			Map<String, String> lgnd = new HashMap<>();
			lgnd.put("range", "Non Intervention");
			lgnd.put("color", "#888");
			lgnd.put("rgbColor", "rgba(136,136,136)");
			legendsz.add(lgnd);
			
			for(int i =0;i<legendsz.size();i++) {
				
				Map<String, String> legendMap = thematicDashboardDataModel.getLegend().get(i);
				String r=null;
				String g=null;
				String b=null;
				
				
				if(legendMap.get("rgbColor")!=null) {
					r = legendMap.get("rgbColor").split("\\(")[1].split(",")[0];
					g = legendMap.get("rgbColor").split("\\(")[1].split(",")[1];
					b = legendMap.get("rgbColor").split("\\(")[1].split(",")[2].split("\\)")[0];
				}else {
					r="204";
					g="204";
					b="204";
				}
				
				colCount=14;
				
				CellStyle styleForThematicDownload = ExcelStyleSheet.getStyleForThematicDownload(workbook, Integer.valueOf(r), Integer.valueOf(g), Integer.valueOf(b));
				
				if(rowCount!=12)
					row=sheet.createRow(rowCount);
				cell = row.createCell(colCount);
				sheet.setHorizontallyCenter(true);
				row.setHeight((short) 120);
				cell.setCellStyle(styleForThematicDownload);
//				cell.setCellValue(legendMap.get("range"));
				colCount++;
				
				cell = row.createCell(colCount);
				sheet.setHorizontallyCenter(true);
				row.setHeight((short) 350);
//				cell.setCellStyle(styleForThematicDownload);
				if(legendMap.get("range")!=null)
					cell.setCellValue(legendMap.get("range"));
				else
				cell.setCellValue("Not Available");
				rowCount++;
				
			}
			
			
			//write data value in table
            List<Map<String, String>> tableData = thematicDashboardDataModel.getTableData();
            if(!tableData.isEmpty()) {
            	
            	CellStyle styleForEvenCell = ExcelStyleSheet.getStyleForEvenCell(workbook, true);
            	CellStyle styleForOddCell = ExcelStyleSheet.getStyleForOddCell(workbook, true);
            	CellStyle styleForColorHeader= ExcelStyleSheet.getStyleForHeading(workbook);
            	
            	rowCount=23;
            	colCount=6;
            	
            	row = sheet.createRow(rowCount);
    			cell = row.createCell(colCount);
    			sheet.setHorizontallyCenter(true);
    			cell.setCellStyle(styleForColorHeader);
    			row.setHeight((short) 350);
    			sheet.setColumnWidth(cell.getColumnIndex(), 5000);
    			cell.setCellValue("Area");
    			colCount++;
    			
    			cell = row.createCell(colCount);
    			sheet.setHorizontallyCenter(true);
    			cell.setCellStyle(styleForColorHeader);
    			row.setHeight((short) 350);
    			sheet.setColumnWidth(cell.getColumnIndex(), 5000);
    			cell.setCellValue("Value (in "+ thematicDashboardDataModel.getUnit()+")");
    			rowCount++;
            	
    			rowCount=24;
    			colCount=6;
    			for(int i=0;i<tableData.size();i++){
        			
    				Map<String, String> dataz = tableData.get(i);
    				
    				row = sheet.createRow(rowCount);
    				cell = row.createCell(colCount);
    				sheet.setHorizontallyCenter(true);
    				row.setHeight((short) 350);
    				sheet.setColumnWidth(cell.getColumnIndex(), 6000);
    				cell.setCellStyle(i % 2 == 0 ? styleForEvenCell : styleForOddCell);
    				cell.setCellValue(dataz.get("areaName").toString().split("\\(")[0]);
    				colCount++;
    				
    				cell = row.createCell(colCount);
    				sheet.setHorizontallyCenter(true);
    				row.setHeight((short) 350);
    				sheet.setColumnWidth(cell.getColumnIndex(), 6000);
    				cell.setCellStyle(i % 2 == 0 ? styleForEvenCell : styleForOddCell);
    				cell.setCellValue(Double.parseDouble(dataz.get("value").toString()));
    				
    				rowCount++;
    				colCount=6;
    				
    			}
            	
            }
			
			
			String name = fileName + "_" + new SimpleDateFormat("ddMMyyyyHHmmss").format(new Date()) + ".xlsx";
			String path = dir + "" + name;

			FileOutputStream fos = new FileOutputStream(new File(path));
			
			workbook.write(fos);
			fos.close();
			workbook.close();
			
			return new ResponseEntity<String>(path,HttpStatus.OK);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
	
	// insert image in excel file
	private void insertimage(byte [] imageBytes, XSSFWorkbook xssfWorkbook, XSSFSheet sheet) {
		
		try {
			int pictureIdx = xssfWorkbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG);
			CreationHelper helper = xssfWorkbook.getCreationHelper();
			Drawing<?> drawing = sheet.createDrawingPatriarch();
			ClientAnchor anchor = helper.createClientAnchor();
			anchor.setCol1(2);
			anchor.setCol2(9);
			anchor.setRow1(4);
			anchor.setRow2(13);
			Picture pict = drawing.createPicture(anchor, pictureIdx);
			if(pict.getImageDimension().getHeight()<=100) {
				anchor.setCol2(13);
				anchor.setRow2(4);
			}else if(pict.getImageDimension().getHeight()<150){
				pict.resize(1.2,0.5);
			} else if(pict.getImageDimension().getHeight()>150 && pict.getImageDimension().getHeight()<300) {
				pict.resize(1.2,1.0);
			} else if (pict.getImageDimension().getHeight()>300) {
				pict.resize(1.65);
			}

		} catch (Exception e) {
		}
	}
}