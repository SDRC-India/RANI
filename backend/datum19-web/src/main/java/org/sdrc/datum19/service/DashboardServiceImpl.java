package org.sdrc.datum19.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.sdrc.datum19.document.Area;
import org.sdrc.datum19.document.Dashboard;
import org.sdrc.datum19.document.DashboardIndicator;
import org.sdrc.datum19.document.DataValue;
import org.sdrc.datum19.document.Heading;
import org.sdrc.datum19.document.Indicator;
import org.sdrc.datum19.document.ThematicFileData;
import org.sdrc.datum19.document.TimePeriod;
import org.sdrc.datum19.model.ChartDataModel;
import org.sdrc.datum19.model.DataValueModel;
import org.sdrc.datum19.model.GeoLegends;
import org.sdrc.datum19.model.GroupChartDataModel;
import org.sdrc.datum19.model.IndicatorGroupModel;
import org.sdrc.datum19.model.IndicatorModel;
import org.sdrc.datum19.model.LegendModel;
import org.sdrc.datum19.model.SectorModel;
import org.sdrc.datum19.model.SubsectorModel;
import org.sdrc.datum19.model.TableData;
import org.sdrc.datum19.model.TableHead;
import org.sdrc.datum19.model.ThematicIndicatorModel;
import org.sdrc.datum19.repository.AreaRepository;
import org.sdrc.datum19.repository.DashboardIndicatorRepository;
import org.sdrc.datum19.repository.DashboardRepository;
import org.sdrc.datum19.repository.DataDomainRepository;
import org.sdrc.datum19.repository.EnginesFormRepository;
import org.sdrc.datum19.repository.HeadingRepository;
import org.sdrc.datum19.repository.IndicatorRepository;
import org.sdrc.datum19.repository.ThematicFileDataRepository;
import org.sdrc.datum19.repository.TimePeriodRepository;
import org.sdrc.datum19.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author subrata
 *
 */
@Service
public class DashboardServiceImpl implements DashboardService {

//	@Autowired
//	private ConfigurableEnvironment configurableEnvironment;

	@Autowired
	DashboardIndicatorRepository groupIndicatorRepository;

//	@Autowired
//	IndicatorCssClassGrouprepository indicatorCssClassGrouprepository;

	@Autowired
	private IndicatorRepository indicatorRepository;

	@Autowired
	private DashboardRepository dashboardRepository;

	@Autowired
	private DataDomainRepository dataDomainRepository;

	@Autowired
	private ThematicFileDataRepository thematicFileDataRepository;

	@Autowired
	private AreaRepository areaRepository;

	@Autowired
	private DashboardIndicatorRepository dashboardIndicatorRepository;

	@Autowired
	private TimePeriodRepository timePeriodRepository;

	@Autowired
	private EnginesFormRepository enginesFormRepository;

	@Autowired
	private HeadingRepository headingRepository;

//	public final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

	private static SimpleDateFormat simpleDateformater = new SimpleDateFormat("yyyy-MM-dd");

	private static DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("MMM");

	private static SimpleDateFormat fullDateFormat = new SimpleDateFormat("MMMM");

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy");

	/**
	 * Getting the Dashboard data
	 * 
	 * @param sectotrName
	 * @param groupName
	 * 
	 * @return sectorModels
	 */
	@Override
	public List<SectorModel> getDashboardData(String sectorName, String groupName, String dashboardId,
			List<Integer> areaList, Integer tp) {

		List<DashboardIndicator> groupIndicatorModels = null;
		/**
		 * If the sector name is not null, then getting the list of DashboardIndicator
		 * from the database.
		 */
		if (sectorName != null) {
			groupIndicatorModels = groupIndicatorRepository.findByDashboardIdAndSectorIgnoreCase(dashboardId,
					sectorName);
		} else {
			/**
			 * If the group name is present, then getting the DashboardIndicator from
			 * database.
			 */
			groupIndicatorModels = Arrays
					.asList(groupIndicatorRepository.findByDashboardIdAndChartGroup(dashboardId, groupName));
			/**
			 * Getting the sector name
			 */
			sectorName = groupIndicatorModels.get(0).getSector();
		}

		List<Indicator> indicatorList = null;

		if (sectorName.equals("T4 Approach")) {
			sectorName = Constants.T4;
//			indicatorList = indicatorRepository.getIndicatorBySectors(Arrays.asList(sectorName));
		} /*
			 * else { indicatorList =
			 * indicatorRepository.getIndicatorBySectors(Arrays.asList(sectorName)); }
			 */
		/**
		 * Getting the list of indicators based on the sector name.
		 */
		indicatorList = indicatorRepository.getIndicatorBySectors(Arrays.asList(sectorName));

		Map<Integer, String> indicatorNameMap = new HashMap<Integer, String>();
		/**
		 * Iterating the indicatorList and putting in a indicatorNameMap where
		 * indicatorId as the key and indicatorName as the value
		 */
		for (Indicator indicator : indicatorList) {
			indicatorNameMap.put(Integer.parseInt(String.valueOf(indicator.getIndicatorDataMap().get("indicatorNid"))),
					(String) indicator.getIndicatorDataMap().get("indicatorName"));
		}

		String lastAggregatedTime = "";

		List<SectorModel> sectorModels = new LinkedList<SectorModel>();

		Map<String, Map<String, List<IndicatorGroupModel>>> map = new LinkedHashMap<String, Map<String, List<IndicatorGroupModel>>>();
		Map<String, String> shapeMap = new HashMap<String, String>();
		try {

			/**
			 * Iterating the groupIndicatorModels.
			 */
			for (DashboardIndicator groupIndicatorModel : groupIndicatorModels) {

				IndicatorGroupModel indicatorGroupModel = new IndicatorGroupModel();
				if (groupIndicatorModel.getChartType().get(0).equals("thematic")
						|| groupIndicatorModel.getChartType().get(0).equals("geo")) {
					/*
					 * if (!shapeMap.containsKey(groupIndicatorModel.getSector()+"@"+
					 * groupIndicatorModel.getSubSector())) {
					 * shapeMap.put(groupIndicatorModel.getSector()+"@"+groupIndicatorModel.
					 * getSubSector(), groupIndicatorModel.getThematicFileDataSlugId().toString());
					 * }
					 */

					IndicatorModel indicatorModel = null;
					List<IndicatorModel> indicatorLists = new ArrayList<IndicatorModel>();
					for (List<Integer> indicatorId : groupIndicatorModel.getChartIndicators()) {
						for (Integer indicator : indicatorId) {
							indicatorModel = new IndicatorModel();
							Indicator indic = indicatorRepository.getIndicatorsByDatumId(indicator.toString());
							indicatorModel.setIndicatorId(indicator);
							indicatorModel
									.setIndicatorName(indic.getIndicatorDataMap().get("indicatorName").toString());
							indicatorLists.add(indicatorModel);
						}

					}
					ThematicIndicatorModel thematicIndicatorModel = new ThematicIndicatorModel();
					thematicIndicatorModel.setIndicatorList(indicatorLists);

					List<ThematicIndicatorModel> thematicData = new ArrayList<ThematicIndicatorModel>();
					thematicData.add(thematicIndicatorModel);
					indicatorGroupModel.setThematicData(thematicData);
				}
				if (groupIndicatorModel.getChartType().get(0).equals("table")) {

					TableData tableData = new TableData();
					setTableData(groupIndicatorModel, tableData, areaList, tp);
					List<TableData> tableDatas = new ArrayList<TableData>();
					tableDatas.add(tableData);
					indicatorGroupModel.setTableData(tableDatas);
				}

				List<GroupChartDataModel> listOfGroupChartData = null;
				GroupChartDataModel chartDataModel = null;
				List<LegendModel> legendModels = null;
				/**
				 * Setting the indicator value.
				 */
				String dataValue = "";
				if (groupIndicatorModel.getChartType().get(0).contains("card")) {
					List<DataValue> dataValueList = dataDomainRepository.findByInidInAndTpInAndDatumIdInAndDatumtype(
							Arrays.asList(groupIndicatorModel.getKpiIndicator()), Arrays.asList(tp), areaList, "area");
					String cardDataValue = dataValueList.size() != 0
							&& String.valueOf(dataValueList.get(0).getDataValue()) != null
									? String.valueOf(dataValueList.get(0).getDataValue())
									: "";
					indicatorGroupModel.setIndicatorValue(cardDataValue);
				} else {
					indicatorGroupModel.setIndicatorValue(String.valueOf(Math.round(Math.random() * 100)));
				}
				String kpiInd = null;

				if (groupIndicatorModel.getValueFrom() != null && groupIndicatorModel.getValueFrom().contains("=")) {
//					String[] arrs = groupIndicatorModel.getValueFrom().split(",");
//					for(String ar: arrs) {
//						String[] each = ar.split("=");
//						if(each[0].equals(areaLevel.toString())) {
//							kpiInd = each[1];
//						}
//					}
				} else if (groupIndicatorModel.getValueFrom() != null) {
					/**
					 * getting the KPI value
					 */
					kpiInd = groupIndicatorModel.getValueFrom();
				}
				/**
				 * setting the indicator values other than card type
				 */
				if (!groupIndicatorModel.getChartType().get(0).contains("card") && kpiInd != null
						&& !kpiInd.equals("")) {
					indicatorGroupModel.setIndicatorValue(String.valueOf(Math.round(Math.random() * 100)));
				}

				/**
				 * Getting the KPI indicator name
				 */
				String kpiIndName = groupIndicatorModel.getKpiChartHeader() != null
						? groupIndicatorModel.getKpiChartHeader().contains("@")
								? (groupIndicatorModel.getKpiChartHeader().split("@")[0]
										+ (String.valueOf(Math.round(Math.random() * 1000)))
										+ groupIndicatorModel.getKpiChartHeader().split("@")[1])
								: groupIndicatorModel.getKpiChartHeader()
						: "";

				indicatorGroupModel.setIndicatorName(kpiIndName);

				indicatorGroupModel.setIndicatorId(groupIndicatorModel.getKpiIndicator());
				indicatorGroupModel
						.setSource(enginesFormRepository.findByFormId(groupIndicatorModel.getFormId()).getName());
				indicatorGroupModel.setChartsAvailable(groupIndicatorModel.getChartType());
				indicatorGroupModel.setAlign(groupIndicatorModel.getAlign());
				indicatorGroupModel.setIndicatorGroupName(groupIndicatorModel.getIndicatorGroup());
				indicatorGroupModel.setUnit(groupIndicatorModel.getUnit());
				indicatorGroupModel.setChartGroup(groupIndicatorModel.getChartGroup());
				indicatorGroupModel.setExtraInfo(groupIndicatorModel.getExtraInfo());
				indicatorGroupModel.setGroupName(groupIndicatorModel.getGroupName());
				indicatorGroupModel.setId(groupIndicatorModel.getId());
				indicatorGroupModel
						.setRange(groupIndicatorModel.getRange() == null ? null : groupIndicatorModel.getRange());
				if (groupIndicatorModel.getThematicFileDataSlugId() != null) {
					ThematicFileData thematicFileData = thematicFileDataRepository
							.findBySlugId(groupIndicatorModel.getThematicFileDataSlugId());
					indicatorGroupModel.setShapePath(thematicFileData.getShapeJSON());
					Area areaThematic = null;

					if (thematicFileData.getDistrictId() != null) {

						areaThematic = areaRepository.findByAreaId(thematicFileData.getDistrictId());

						indicatorGroupModel.setAreaCode(areaThematic.getAreaCode());
						indicatorGroupModel.setAreaName(areaThematic.getAreaName());
						indicatorGroupModel.setAreaLevelId(areaThematic.getAreaLevel().getAreaLevelId());
						indicatorGroupModel.setAreaLevelName(areaThematic.getAreaLevel().getAreaLevelName());

					} else if (thematicFileData.getStateId() != null) {
						areaThematic = areaRepository.findByAreaId(thematicFileData.getStateId());
						indicatorGroupModel.setAreaCode(areaThematic.getAreaCode());
						indicatorGroupModel.setAreaName(areaThematic.getAreaName());
						indicatorGroupModel.setAreaLevelId(areaThematic.getAreaLevel().getAreaLevelId());
						indicatorGroupModel.setAreaLevelName(areaThematic.getAreaLevel().getAreaLevelName());

					} else if (thematicFileData.getCountryId() != null) {
						areaThematic = areaRepository.findByAreaId(thematicFileData.getCountryId());
						indicatorGroupModel.setAreaCode(areaThematic.getAreaCode());
						indicatorGroupModel.setAreaName(areaThematic.getAreaName());
						indicatorGroupModel.setAreaLevelId(areaThematic.getAreaLevel().getAreaLevelId());
						indicatorGroupModel.setAreaLevelName(areaThematic.getAreaLevel().getAreaLevelName());

					}
				}
				// new code 04-06-2019

				/**
				 * for trend chart indicators
				 */
				if (groupIndicatorModel.getChartType().get(0).contains("trend")
						&& groupIndicatorModel.getChartIndicators() != null
						&& groupIndicatorModel.getChartIndicators().size() > 0
						|| groupIndicatorModel.getChartType().get(0).contains("thematic")
						|| groupIndicatorModel.getChartType().get(0).contains("geo")) {
					listOfGroupChartData = new ArrayList<GroupChartDataModel>();
					chartDataModel = new GroupChartDataModel();
					/**
					 * Getting the indicator name
					 */
					String indName = groupIndicatorModel.getChartHeader().contains("@")
							? (groupIndicatorModel.getChartHeader().split("@")[0]
									+ String.valueOf(Math.round(Math.random() * 100))
									+ groupIndicatorModel.getChartHeader().split("@")[1])
							: groupIndicatorModel.getChartHeader();

					chartDataModel.setHeaderIndicatorName(indName);
					chartDataModel.setHeaderIndicatorValue(
							Integer.valueOf(String.valueOf((Math.round(Math.random() * 100)))));
					/**
					 * getting the ChartDataValue for trend type
					 */
					chartDataModel.setChartDataValue(getChartDataValue(groupIndicatorModel, indicatorNameMap,
							groupIndicatorModel.getChartType().get(0), null, groupIndicatorModel.getUnit(), areaList,
							tp));

					listOfGroupChartData.add(chartDataModel);
					indicatorGroupModel.setChartData(listOfGroupChartData);

					/**
					 * Setting the Legends in ChartDataModel
					 */
					if (groupIndicatorModel.getColorLegends() != null
							&& groupIndicatorModel.getColorLegends().size() > 0) {
						legendModels = new ArrayList<LegendModel>();
//						String[] legendsList = groupIndicatorModel.getColorLegends().split(",");
						List<String> legendsList = groupIndicatorModel.getColorLegends();
						/**
						 * Iterating the legendsList and setting the values in LegendModel
						 */
						for (String string : legendsList) {
							LegendModel legendModel = new LegendModel();
							legendModel.setCssClass(string.split("_")[0]);
							legendModel.setValue(string.split("_")[1]);
							legendModels.add(legendModel);
						}
						chartDataModel.setLegends(legendModels);
					}

				} else if (groupIndicatorModel.getChartIndicators() != null
						&& groupIndicatorModel.getChartIndicators().size() > 0) {
					/**
					 * If the chart type is not trend, then this block will execute.
					 */
					listOfGroupChartData = new ArrayList<GroupChartDataModel>();
					chartDataModel = new GroupChartDataModel();

					/**
					 * getting the indicator name
					 */
					String indName = groupIndicatorModel.getChartHeader().contains("@")
							? (groupIndicatorModel.getChartHeader().split("@")[0]
									+ String.valueOf(Math.round(Math.random() * 100))
									+ groupIndicatorModel.getChartHeader().split("@")[1])
							: groupIndicatorModel.getChartHeader();

					chartDataModel.setHeaderIndicatorName(indName);

					chartDataModel.setHeaderIndicatorValue(
							Integer.valueOf(String.valueOf((Math.round(Math.random() * 100)))));
					/**
					 * getting the ChartDataValue
					 */
					if (!groupIndicatorModel.getChartType().get(0).equals("table")) {
						chartDataModel.setChartDataValue(getChartDataValue(groupIndicatorModel, indicatorNameMap,
								groupIndicatorModel.getChartType().get(0), null, groupIndicatorModel.getUnit(),
								areaList, tp));
					}
					listOfGroupChartData.add(chartDataModel);
					indicatorGroupModel.setChartData(listOfGroupChartData);

					/**
					 * Setting the Legends in ChartDataModel
					 */
					if (groupIndicatorModel.getColorLegends() != null
							&& groupIndicatorModel.getColorLegends().size() > 0) {
						legendModels = new ArrayList<LegendModel>();
//						String[] legendsList = groupIndicatorModel.getColorLegends().split(",");
						List<String> legendsList = groupIndicatorModel.getColorLegends();
						/**
						 * Iterating the legendsList and setting the values in LegendModel
						 */
						for (String string : legendsList) {
							LegendModel legendModel = new LegendModel();
							legendModel.setCssClass(string.split("_")[0]);
							legendModel.setValue(string.split("_")[1]);
							legendModels.add(legendModel);
						}
						chartDataModel.setLegends(legendModels);
					}
				}
				/**
				 * Storing sector name as the key and subsectorGrMapModel as the value in a map
				 */
				if (!map.containsKey(groupIndicatorModel.getSector())) {

					Map<String, List<IndicatorGroupModel>> subsectorGrMapModel = new LinkedHashMap<String, List<IndicatorGroupModel>>();

					List<IndicatorGroupModel> sectorNewIndicators = new LinkedList<IndicatorGroupModel>();
					sectorNewIndicators.add(indicatorGroupModel);
					subsectorGrMapModel.put(groupIndicatorModel.getSubSector(), sectorNewIndicators);
					map.put(groupIndicatorModel.getSector(), subsectorGrMapModel);
				} else {
					if (!map.get(groupIndicatorModel.getSector()).containsKey(groupIndicatorModel.getSubSector())) {
						List<IndicatorGroupModel> newIndicators = new LinkedList<IndicatorGroupModel>();
						newIndicators.add(indicatorGroupModel);

						map.get(groupIndicatorModel.getSector()).put(groupIndicatorModel.getSubSector(), newIndicators);
					} else {
						map.get(groupIndicatorModel.getSector()).get(groupIndicatorModel.getSubSector())
								.add(indicatorGroupModel);
					}
				}
			}
			/**
			 * Iterating the map and setting all the values in the sectorModel
			 */
			for (Entry<String, Map<String, List<IndicatorGroupModel>>> entry : map.entrySet()) {
				SectorModel sectorModel = new SectorModel();
				sectorModel.setSectorName(entry.getKey());

				List<SubsectorModel> listOfSubsector = new ArrayList<SubsectorModel>();

				for (Entry<String, List<IndicatorGroupModel>> entry2 : entry.getValue().entrySet()) {

					SubsectorModel subSectorModel = new SubsectorModel();
					subSectorModel.setSubsectorName(entry2.getKey());
					subSectorModel.setIndicators(entry2.getValue());

//					subSectorModel.setIndicatorsMap(entry2.getValue().stream().sorted(Comparator.comparing(v->v.getGroupName())).collect(Collectors.groupingBy(v->v.getGroupName(), Collectors.toList())));

					listOfSubsector.add(subSectorModel);
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

	/**
	 * Getting ChartDataValues
	 * 
	 * @param groupIndicatorModel
	 * @param indicatorNameMap
	 * @param chartType
	 * @param cssGroupMap
	 * @param unit
	 * @param tp
	 * @param areaList
	 * @return listChartDataModels
	 */
	private List<List<ChartDataModel>> getChartDataValue(DashboardIndicator groupIndicatorModel,
			Map<Integer, String> indicatorNameMap, String chartType, Map<Integer, String> cssGroupMap, String unit,
			List<Integer> areaList, Integer tp) {

		List<Integer> areaListAvailable = groupIndicatorModel.getMulIndOrMulArea() != null
				&& groupIndicatorModel.getMulIndOrMulArea().equals("multipleIndicators") && areaList != null ? areaList
						: groupIndicatorModel.getAreaIdsAreaNames().get(0);
		// areaListAvailable=Arrays.asList(5004,5003);
		List<Area> areasAvailble = areaRepository.findByAreaIdIn(areaListAvailable);
		List<String> axisList = null;

		List<List<Integer>> chartIndicators = groupIndicatorModel.getChartIndicators();

		List<Integer> indicatorIds = new ArrayList<>();
		for (List<Integer> indIds : chartIndicators) {
			indicatorIds.addAll(indIds);
		}
		/**
		 * finding data based on indicatorList and area list if dashboard indicators is
		 * not multipleAreaType
		 */
		List<DataValue> listOfDataValue = new ArrayList<>();
		Map<String, Double> indTpAreaValueMap = new HashMap<>();
		if (areaListAvailable != null && tp != null) {
			listOfDataValue = dataDomainRepository.findByInidInAndTpInAndDatumIdInAndDatumtype(indicatorIds,
					Arrays.asList(tp), areaListAvailable, "area");
			for (DataValue dataValue : listOfDataValue) {
				indTpAreaValueMap.put(dataValue.getInid() + "_" + dataValue.getTp() + "_" + dataValue.getDatumId(),
						dataValue.getDataValue());
			}
		} else if (areaListAvailable == null && tp != null) {
			listOfDataValue = dataDomainRepository.findByInidInAndTpIn(indicatorIds, Arrays.asList(tp));
			for (DataValue dataValue : listOfDataValue) {
				indTpAreaValueMap.put(dataValue.getInid() + "_" + dataValue.getTp(), dataValue.getDataValue());
			}
		} else {
			listOfDataValue = dataDomainRepository.findByInidIn(indicatorIds);
			for (DataValue dataValue : listOfDataValue) {
				indTpAreaValueMap.put(dataValue.getInid().toString(), dataValue.getDataValue());
			}
		}

		String mulIndOrMulArea = groupIndicatorModel.getMulIndOrMulArea();

		List<Integer> values = null;
		/**
		 * If the ChartLegends is not null, then getting the axisList
		 */
		if (groupIndicatorModel.getChartLegends() != null) {
			axisList = groupIndicatorModel.getChartLegends();
		}
		/**
		 * If the ChartType is pie/donut, then getting the axisList
		 */
		if (chartType.equals("pie") || chartType.equals("doughnut")) {
			axisList = groupIndicatorModel.getColorLegends();
		}

		List<List<ChartDataModel>> listChartDataModels = new LinkedList<>();
		List<ChartDataModel> chartDataModels = null;
		TimePeriod timeperiod = timePeriodRepository.findById(tp);
		/**
		 * If the ChartType is trend, then creating the last 6 time periods.
		 */
		if (chartType.equals("trend")) {
			/*
			 * finding last six time periods from data value
			 */
			DataValue dataValueForTp = listOfDataValue.get(0);
			TimePeriod timeP = timePeriodRepository.findByTimePeriodId(dataValueForTp.getTp());
			String periodiciyOfInd = timeP.getPeriodicity();
			List<TimePeriod> timePeriodList = timePeriodRepository
					.findTop6ByPeriodicityOrderByCreatedDateDesc(periodiciyOfInd);
			// List<TimePeriod> utTimeperiodList = getLast6Timeperiods();
			/**
			 * Iterating the chartIndicators and getting ChartDataModel
			 */
			for (List<Integer> indList : chartIndicators) {
				chartDataModels = new LinkedList<>();

				for (TimePeriod timePeriod : timePeriodList) {

					for (int i = 0; i < indList.size(); i++) {
						/**
						 * Iterating the timeperiods and according to the time period setting the
						 * ChartDataModel
						 */
						String axisName = axisList != null ? axisList.get(i) : null;

						String axis = null;
						axis = timePeriod.getTimePeriodDuration() + "-" + timePeriod.getYear();

//						getChartDataModel(indList.get(i), "trend", chartDataModels, cssGroupMap, axis, timePeriod, unit, 
//								(axisList!= null? axisList[i].split("_")[1] : null), null);
						// String axis, TimePeriod timePeriod, String unit, String label, Integer
						// value,String key
						String dataValue = "";
						int areaIndex = i;
						if (mulIndOrMulArea.equals("multipleAreas")) {
							for (String axisName1 : axisList) {
								dataValue = String.valueOf(
										indTpAreaValueMap.get(indList.get(i) + "_" + timePeriod.getTimePeriodId() + "_"
												+ groupIndicatorModel.getAreaIdsAreaNames().get(0).get(areaIndex)));
								getChartDataModel(indList.get(i), chartType, chartDataModels, cssGroupMap, axis,
										timeperiod, unit, axisName1, dataValue, axisName1, mulIndOrMulArea);
								++areaIndex;
							}
						} else {
							dataValue = String.valueOf(indTpAreaValueMap.get(indList.get(i) + "_"
									+ timePeriod.getTimePeriodId() + "_" + areasAvailble.get(0).getAreaId()));
							getChartDataModel(indList.get(i), chartType, chartDataModels, cssGroupMap, axis, timeperiod,
									unit, axisName, dataValue, axisName, mulIndOrMulArea);
						}

//						if(areasAvailble!=null & tp!=null) {
//							for (Area area : areasAvailble) {
//								
//								 dataValue = String.valueOf(indTpAreaValueMap
//										.get(indList.get(i) + "_" + timePeriod.getTimePeriodId() + "_" + area.getAreaId()));
//								 getChartDataModel(indList.get(i), "trend", chartDataModels, cssGroupMap, axis, timePeriod,
//											unit, (axisList != null ? axisList.get(i) : null), dataValue, area.getAreaName(),mulIndOrMulArea);
//							}
//						}else if(areasAvailble==null && tp!=null) {
//							 dataValue = String.valueOf(indTpAreaValueMap
//									.get(indList.get(i) + "_" + timePeriod.getTimePeriodId()));
//							 getChartDataModel(indList.get(i), "trend", chartDataModels, cssGroupMap, axis, timePeriod,
//										unit, (axisList != null ? axisList.get(i) : null), dataValue, null,mulIndOrMulArea);
//						}else {
//							 dataValue = String.valueOf(indTpAreaValueMap
//									.get(indList.get(i).toString()));
//							 getChartDataModel(indList.get(i), "trend", chartDataModels, cssGroupMap, axis, timePeriod,
//										unit, (axisList != null ? axisList.get(i) : null), dataValue, null,mulIndOrMulArea);
//						}

					}

				}
				listChartDataModels.add(chartDataModels);
			}
		} else {

			int grCount = 0;
			List<List<Integer>> stackList = null;
			/**
			 * If the ChartType is stack
			 */
			if (chartType.equals("stack")) {
				stackList = new ArrayList<>();

				int arrEleSize = chartIndicators.size();
				int noOfArr = chartIndicators.get(0).size();
				/**
				 * Generating random values for each indicators
				 */
				for (int i = 0; i < noOfArr; i++) {
					int randomNum = new Random().nextInt((100 - 50) + 1) + 50;
					stackList.add(generate(randomNum, arrEleSize));
				}
			}

			int chartIndicatorsPosition = 0;
			int indicatorPosition = 0;
			/**
			 * Iterating the chartIndicators and getting ChartDataModel
			 */
			String axis = null;
			for (List<Integer> indList : chartIndicators) {
				chartDataModels = new LinkedList<>();

				/**
				 * If the ChartType is pie/donut, then generating the values for each indicators
				 */
				if (chartType.equals("pie") || chartType.equals("doughnut"))
					values = generate(100, indList.size());

				for (int i = 0; i < indList.size(); i++) {

//					axis = axisList!= null? axisList[i].split("_")[1] : null;
					/**
					 * For each indicator getting the axis here
					 */
					axis = axisList != null ? axisList.get(i) : null;

					String label = null;
					/**
					 * If the ChartType is stack, then getting the ColorLegends for label
					 */
					if (chartType.equals("stack")) {
//						label = groupIndicatorModel.getColorLegends()!= null? groupIndicatorModel.getColorLegends().split(",")[grCount].split("_")[1] : null;
						label = groupIndicatorModel.getColorLegends() != null
								? groupIndicatorModel.getColorLegends().get(grCount)
								: null;
					}
					/**
					 * If the ChartType is pie/donut, then getting ChartDataModel here
					 */
					if (chartType.equals("pie") || chartType.equals("doughnut")) {
						String dataValue = String.valueOf(indTpAreaValueMap
								.get(indList.get(i) + "_" + tp + "_" + areasAvailble.get(0).getAreaId()));
						getChartDataModel(indList.get(i), chartType, chartDataModels, cssGroupMap, axis, null, unit,
								label, dataValue, null, mulIndOrMulArea);

						// getChartDataModel(indList.get(i), chartType, chartDataModels, cssGroupMap,
						// axis, null, unit,
						// label, String.valueOf(values.get(i)), null, mulIndOrMulArea);
					} else if (chartType.equals("stack")) {
						/**
						 * If the ChartType is stack, then getting ChartDataModel here
						 */

						String dataValue = String.valueOf(indTpAreaValueMap
								.get(indList.get(i) + "_" + tp + "_" + areasAvailble.get(0).getAreaId()));
						getChartDataModel(indList.get(i), chartType, chartDataModels, cssGroupMap, axis, null, unit,
								label, dataValue, areasAvailble.get(0).getAreaName(), mulIndOrMulArea);

						// Integer value =
						// stackList.get(indicatorPosition).get(chartIndicatorsPosition);
						// String
						// dataValue=String.valueOf(indTpAreaValueMap.get(indList.get(i)+"_"+timePeriod.getTimePeriodId()+"_"+area.getAreaId()));
						// getChartDataModel(indList.get(i), chartType, chartDataModels, cssGroupMap,
						// axis, null, unit,
						// label, String.valueOf(value), null);
						indicatorPosition++;
					} else if (chartType.equals("BAR") || chartType.equals("column")) {
						/**
						 * If the ChartType is stack, then getting ChartDataModel here
						 */
						String dataValue = "";
						int areaIndex = i;
						if (mulIndOrMulArea.equals("multipleAreas")) {
							for (String axisName : axisList) {

								dataValue = String.valueOf(indTpAreaValueMap.get(indList.get(i) + "_" + tp + "_"
										+ groupIndicatorModel.getAreaIdsAreaNames().get(0).get(areaIndex)));
								getChartDataModel(indList.get(i), chartType, chartDataModels, cssGroupMap, axisName,
										timeperiod, unit, label, dataValue,
										mulIndOrMulArea.equals("multipleAreas") ? axisName
												: areasAvailble.get(0).getAreaName(),
										mulIndOrMulArea);
								++areaIndex;
							}
						} else {
							dataValue = String.valueOf(indTpAreaValueMap
									.get(indList.get(i) + "_" + tp + "_" + areasAvailble.get(0).getAreaId()));
							getChartDataModel(indList.get(i), chartType, chartDataModels, cssGroupMap, axis, timeperiod,
									unit, label, dataValue,
									mulIndOrMulArea.equals("multipleAreas") ? axis : areasAvailble.get(0).getAreaName(),
									mulIndOrMulArea);
						}
						// Integer value =
						// stackList.get(indicatorPosition).get(chartIndicatorsPosition);
						// String
						// dataValue=String.valueOf(indTpAreaValueMap.get(indList.get(i)+"_"+timePeriod.getTimePeriodId()+"_"+area.getAreaId()));
						// getChartDataModel(indList.get(i), chartType, chartDataModels, cssGroupMap,
						// axis, null, unit,
						// label, String.valueOf(value), null);
						indicatorPosition++;
					} else {
						/**
						 * If the ChartType is not pie/donut/stack, then getting ChartDataModel here
						 */
						String dataValue = String.valueOf(indTpAreaValueMap
								.get(indList.get(i) + "_" + tp + "_" + areasAvailble.get(0).getAreaId()));
						getChartDataModel(indList.get(i), chartType, chartDataModels, cssGroupMap, axis, null, unit,
								label, dataValue, null, mulIndOrMulArea);
					}
				}

				indicatorPosition = 0;
				chartIndicatorsPosition++;
				listChartDataModels.add(chartDataModels);

				grCount++;
			}
		}
		return listChartDataModels;
	}

	/**
	 * Creating a random number.
	 * 
	 * @param min
	 * @param max
	 * @return number
	 */
	private static Integer randombetween(Integer min, Integer max) {
		return (int) Math.floor(Math.random() * (max - min + 1) + min);
	}

	/**
	 * Generating a random number
	 * 
	 * @param max
	 * @param thecount
	 * @return r i.e. List of integer
	 */
	private static List<Integer> generate(int max, int thecount) {
		List<Integer> r = new ArrayList<>();
		int currsum = 0;
		for (int i = 0; i < thecount - 1; i++) {
			int v = randombetween(1, max - (thecount - i - 1) - currsum);
			currsum += v;
			r.add(v);
		}
		r.add(max - currsum);
		return r;
	}

	/**
	 * Creating last 6 time periods.
	 * 
	 * @return liTimePeriods
	 */
	private List<TimePeriod> getLast6Timeperiods() {
		List<TimePeriod> liTimePeriods = new ArrayList<>();
		String financialYear = financialYear();
		for (int i = 5; i >= 0; i--) {
			liTimePeriods.add(insertTimeperiod(financialYear, String.valueOf(i), "1"));
		}
		return liTimePeriods;
	}

	/**
	 * Creating financialYear
	 * 
	 * @return financialYear
	 */
	public static String financialYear() {
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH);
		int preYear = 0, nextYear = 0;
		if (month > 5) {
			preYear = cal.get(Calendar.YEAR);
			cal.add(Calendar.YEAR, 1);
			nextYear = cal.get(Calendar.YEAR);
		} else {
			cal.add(Calendar.YEAR, -1);
			preYear = cal.get(Calendar.YEAR);
			cal.add(Calendar.YEAR, 1);
			nextYear = cal.get(Calendar.YEAR);
		}
		return preYear + "-" + nextYear;
	}

	/**
	 * Creating TimePeriod object
	 * 
	 * @param financialYear
	 * @param month
	 * @param periodicity
	 * @return timePeriod
	 */
	private static TimePeriod insertTimeperiod(String financialYear, String month, String periodicity) {

		Integer startMonth = Integer.valueOf(month).intValue();
		Integer endMonth = Integer.valueOf(month).intValue();
		TimePeriod timePeriod = null;
		try {
			Calendar startDateCalendar = Calendar.getInstance();
			startDateCalendar.add(Calendar.MONTH, -startMonth);
			startDateCalendar.set(Calendar.DATE, 1);
			Date strDate = startDateCalendar.getTime();
			String startDateStr = simpleDateformater.format(strDate);
			Date startDate = (Date) formatter.parse(startDateStr + " 00:00:00.000");
			Calendar endDateCalendar = Calendar.getInstance();
			endDateCalendar.add(Calendar.MONTH, -endMonth);
			endDateCalendar.set(Calendar.DATE, endDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
			Date eDate = endDateCalendar.getTime();
			String endDateStr = simpleDateformater.format(eDate);
			Date endDate = (Date) formatter.parse(endDateStr + " 00:00:00.000");

			timePeriod = new TimePeriod();
			timePeriod.setStartDate(new java.sql.Date(startDate.getTime()));
			timePeriod.setEndDate(new java.sql.Date(endDate.getTime()));
			timePeriod.setPeriodicity(periodicity);
			timePeriod.setTimePeriod(fullDateFormat.format(endDate) + "," + sdf.format(endDate));
			timePeriod.setYear(Integer.valueOf(sdf.format(endDate)));
			timePeriod.setTimePeriodDuration(dateFormat.format(startDate));
			timePeriod.setShortName(dateFormat.format(startDate) + "-" + timePeriod.getYear());
			timePeriod.setFinancialYear(financialYear);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return timePeriod;
	}

	/**
	 * Setting all the values in ChartDataModel
	 * 
	 * @param indiId
	 * @param chartType
	 * @param chartDataModels
	 * @param cssGroupMap
	 * @param axis
	 * @param timePeriod
	 * @param unit
	 * @param label
	 * @param value
	 * @return chartDataModels
	 */
	private List<ChartDataModel> getChartDataModel(Integer indiId, String chartType,
			List<ChartDataModel> chartDataModels, Map<Integer, String> cssGroupMap, String axis, TimePeriod timePeriod,
			String unit, String label, String value, String key, String mulIndOrMulArea) {

		ChartDataModel chartDataModel = new ChartDataModel();

		chartDataModel.setAxis(axis.trim());

		chartDataModel.setLabel(label != null ? label.trim() : null);
		if (chartType.equals("pie") || chartType.equals("doughnut") || chartType.equals("stack")
				|| chartType.equals("BAR") || chartType.equals("column") || chartType.equals("trend")
				|| chartType.equals("thematic")) {
			chartDataModel.setValue(String.valueOf(value));
		} else {
			chartDataModel.setValue(String.valueOf((Math.round(Math.random() * 100))));
		}
		chartDataModel.setNumerator(String.valueOf((Math.round(Math.random() * 100))));
		chartDataModel.setDenominator(String.valueOf((Math.round(Math.random() * 100))));
		chartDataModel.setLegend(axis.trim());
		chartDataModel.setId(indiId);
		chartDataModel.setUnit(unit);
		chartDataModel.setKey(key);
		chartDataModels.add(chartDataModel);

		return chartDataModels;
	}

	@Override
	public List<SectorModel> getDashboardGroupData(String groupName, String dashboardId) {
		return getDashboardData(null, groupName, dashboardId, null, null);
	}

	@Override
	public Dashboard addDashboard(Dashboard dashboard) {
		return dashboardRepository.save(dashboard);
	}

	@Override
	public List<Dashboard> getDashboards(String username) {
//		if(!username.equals(null))
//			return dashboardRepository.findByUsername(username);
//		else
		return dashboardRepository.findAll();
	}

	@Override
	public List<SectorModel> getThematic(String sectorName, String groupName) {

		List<DashboardIndicator> listOfDashboardIndicator = groupIndicatorRepository
				.findByDashboardIdAndSectorIn(groupName, Arrays.asList(sectorName));
		List<SectorModel> listOfSectorModel = new ArrayList<SectorModel>();

		Map<String, Map<String, List<IndicatorGroupModel>>> map = new LinkedHashMap<String, Map<String, List<IndicatorGroupModel>>>();

		List<Indicator> indicatorList = indicatorRepository.getIndicatorBySectors(Arrays.asList(sectorName));
		Map<Integer, String> indicatorNameMap = new HashMap<Integer, String>();
		for (Indicator indicator : indicatorList) {
			indicatorNameMap.put(Integer.parseInt(String.valueOf(indicator.getIndicatorDataMap().get("indicatorNid"))),
					(String) indicator.getIndicatorDataMap().get("indicatorName"));
		}

		Map<String, String> shapeMap = new HashMap<String, String>();

		for (DashboardIndicator groupIndicatorModel : listOfDashboardIndicator) {

			if (groupIndicatorModel.getChartType().get(0).equals("thematic")) {

				if (!shapeMap.containsKey(groupIndicatorModel.getSector() + "@" + groupIndicatorModel.getSubSector())) {
					shapeMap.put(groupIndicatorModel.getSector() + "@" + groupIndicatorModel.getSubSector(),
							groupIndicatorModel.getThematicFileDataSlugId().toString());
				}

				IndicatorGroupModel indicatorGroupModel = new IndicatorGroupModel();
				indicatorGroupModel.setIndicatorValue(String.valueOf(Math.round(Math.random() * 100)));

				String kpiIndName = groupIndicatorModel.getKpiChartHeader() != null
						? groupIndicatorModel.getKpiChartHeader().contains("@")
								? (groupIndicatorModel.getKpiChartHeader().split("@")[0]
										+ (String.valueOf(Math.round(Math.random() * 1000)))
										+ groupIndicatorModel.getKpiChartHeader().split("@")[1])
								: groupIndicatorModel.getKpiChartHeader()
						: "";

				indicatorGroupModel.setIndicatorName(kpiIndName);

				indicatorGroupModel.setIndicatorId(groupIndicatorModel.getKpiIndicator());
				indicatorGroupModel.setChartsAvailable(groupIndicatorModel.getChartType());
				indicatorGroupModel.setAlign(groupIndicatorModel.getAlign());
				indicatorGroupModel.setIndicatorGroupName(groupIndicatorModel.getIndicatorGroup());
				indicatorGroupModel.setUnit(groupIndicatorModel.getUnit());
				indicatorGroupModel.setChartGroup(groupIndicatorModel.getChartGroup());
				indicatorGroupModel.setExtraInfo(groupIndicatorModel.getExtraInfo());
				indicatorGroupModel.setGroupName(groupIndicatorModel.getGroupName());
				indicatorGroupModel.setId(groupIndicatorModel.getId());

				List<GroupChartDataModel> listOfGroupChartData = new ArrayList<GroupChartDataModel>();
				GroupChartDataModel chartDataModel = new GroupChartDataModel();

				String indName = groupIndicatorModel.getChartHeader().contains("@")
						? (groupIndicatorModel.getChartHeader().split("@")[0]
								+ String.valueOf(Math.round(Math.random() * 100))
								+ groupIndicatorModel.getChartHeader().split("@")[1])
						: groupIndicatorModel.getChartHeader();

				chartDataModel.setHeaderIndicatorName(indName);
				chartDataModel
						.setHeaderIndicatorValue(Integer.valueOf(String.valueOf((Math.round(Math.random() * 100)))));
				chartDataModel.setChartDataValue(getChartDataValue(groupIndicatorModel, indicatorNameMap, "trend", null,
						groupIndicatorModel.getUnit(), null, null));

				listOfGroupChartData.add(chartDataModel);
				indicatorGroupModel.setChartData(listOfGroupChartData);

				IndicatorModel indicatorModel = null;
				List<IndicatorModel> indicatorLists = new ArrayList<IndicatorModel>();
				for (List<Integer> indicatorId : groupIndicatorModel.getChartIndicators()) {
					for (Integer indicator : indicatorId) {
						indicatorModel = new IndicatorModel();
						Indicator indic = indicatorRepository.getIndicatorsByDatumId(indicator.toString());
						indicatorModel.setIndicatorId(indicator);
						indicatorModel.setIndicatorName(indic.getIndicatorDataMap().get("indicatorName").toString());
						indicatorLists.add(indicatorModel);
					}

				}
				ThematicIndicatorModel thematicIndicatorModel = new ThematicIndicatorModel();
				thematicIndicatorModel.setIndicatorList(indicatorLists);

				List<ThematicIndicatorModel> thematicData = new ArrayList<ThematicIndicatorModel>();
				thematicData.add(thematicIndicatorModel);
				indicatorGroupModel.setThematicData(thematicData);

				if (!map.containsKey(groupIndicatorModel.getSector())) {

					Map<String, List<IndicatorGroupModel>> subsectorGrMapModel = new LinkedHashMap<String, List<IndicatorGroupModel>>();

					List<IndicatorGroupModel> sectorNewIndicators = new LinkedList<IndicatorGroupModel>();
					sectorNewIndicators.add(indicatorGroupModel);
					subsectorGrMapModel.put(groupIndicatorModel.getSubSector(), sectorNewIndicators);
					map.put(groupIndicatorModel.getSector(), subsectorGrMapModel);
				} else {
					if (!map.get(groupIndicatorModel.getSector()).containsKey(groupIndicatorModel.getSubSector())) {
						List<IndicatorGroupModel> newIndicators = new LinkedList<IndicatorGroupModel>();
						newIndicators.add(indicatorGroupModel);

						map.get(groupIndicatorModel.getSector()).put(groupIndicatorModel.getSubSector(), newIndicators);
					} else {
						map.get(groupIndicatorModel.getSector()).get(groupIndicatorModel.getSubSector())
								.add(indicatorGroupModel);
					}
				}
			}
		}

		for (Entry<String, Map<String, List<IndicatorGroupModel>>> entry : map.entrySet()) {
			SectorModel sectorModel = new SectorModel();
			sectorModel.setSectorName(entry.getKey());

			List<SubsectorModel> listOfSubsector = new ArrayList<SubsectorModel>();

			for (Entry<String, List<IndicatorGroupModel>> entry2 : entry.getValue().entrySet()) {

				SubsectorModel subSectorModel = new SubsectorModel();
				subSectorModel.setSubsectorName(entry2.getKey());
				subSectorModel.setIndicators(entry2.getValue());
				subSectorModel.setShapeFile(thematicFileDataRepository
						.findBySlugId(Integer.parseInt(shapeMap.get(entry.getKey() + "@" + entry2.getKey())))
						.getShapeJSON());
				listOfSubsector.add(subSectorModel);
			}

			sectorModel.setSubSectors(listOfSubsector);
			sectorModel.setTimePeriod("");
			listOfSectorModel.add(sectorModel);
		}

		return listOfSectorModel;
	}

	@Override
	public Map<String, List<DataValueModel>> gethematictData(String indecatorId, String tp, String areaCode,
			String indicatorGroup) {

		Map<String, List<DataValueModel>> thematicMap = new HashMap<>();
		Indicator indicator = indicatorRepository.getIndicatorsByDatumId(indecatorId);
		Area fetchedArea = areaRepository.findByAreaCode(areaCode);
		List<Integer> listOfAreaIds = new ArrayList<>();
		List<Area> listOfChildArea = areaRepository.findByParentAreaIdOrderByAreaName(fetchedArea.getAreaId());
		DashboardIndicator dashboardIndicator = dashboardIndicatorRepository.findByIndicatorGroup(indicatorGroup);
		Map<String, String> colorLegendMap = new HashMap<>();
		if (!dashboardIndicator.getColorLegends().isEmpty()) {
			for (String colorLegend : dashboardIndicator.getColorLegends()) {
				colorLegendMap.put(colorLegend.split("_")[1], colorLegend.split("_")[0]);
			}
		}
		Map<Integer, Area> areaMap = new HashMap<>();
		for (Area area : listOfChildArea) {
			listOfAreaIds.add(area.getAreaId());

			areaMap.put(area.getAreaId(), area);
		}

		List<DataValue> listOfDataValue = dataDomainRepository.findByInidAndTpInAndDatumIdIn(
				Integer.parseInt(indecatorId), Arrays.asList(Integer.parseInt(tp)), listOfAreaIds);
		List<DataValueModel> listOfDataValueModel = new ArrayList<>();
		DataValueModel dataValueModel = null;
		for (DataValue dataValue : listOfDataValue) {
			dataValueModel = new DataValueModel();
			dataValueModel.setAreaCode(areaMap.get(dataValue.getDatumId()).getAreaCode());
			dataValueModel.setAreaId(dataValue.getDatumId());
			dataValueModel.setAreaLevelId(areaMap.get(dataValue.getDatumId()).getAreaLevel().getAreaLevelId());
			dataValueModel.setAreaName(areaMap.get(dataValue.getDatumId()).getAreaName());
			dataValueModel.setDataValue(dataValue.getDataValue());
			dataValueModel.setIndicatorId(dataValue.getInid());
			dataValueModel.setTimeperiod(dataValue.getTp());
			dataValueModel.setCssColor(getColor(colorLegendMap, dataValue.getDataValue()));

			listOfDataValueModel.add(dataValueModel);
		}
		thematicMap.put(indicator.getIndicatorDataMap().get("indicatorName").toString(), listOfDataValueModel);
		return thematicMap;
	}

	private String getColor(Map<String, String> colorLegendMap, Double dataValue) {
		String color = null;
		for (Map.Entry<String, String> entry : colorLegendMap.entrySet()) {
			if (dataValue >= Double.parseDouble(entry.getKey().split("-")[0])
					&& dataValue < Double.parseDouble(entry.getKey().split("-")[1])) {
				color = entry.getValue();
			}
		}
		/*
		 * System.out.println("Key = " + entry.getKey() + ", Value = " +
		 * entry.getValue());
		 */
		return color;
	}

	@Override
	public String saveThematicFileData(ThematicFileData thematicFileData) {
		List<ThematicFileData> listOfThematicFileData = thematicFileDataRepository.findAll();
		thematicFileData.setSlugId(listOfThematicFileData == null ? 1 : listOfThematicFileData.size() + 1);
		ThematicFileData persistData = thematicFileDataRepository.save(thematicFileData);
		return persistData.getSlugId().toString();
	}

	@Override
	public Boolean getThematicMapValidation(String parentAreaCode, List<String> childAreaCodes) {

		Area parentArea = areaRepository.findByAreaCode(parentAreaCode);
		List<Area> childByParent = areaRepository.findByParentAreaIdOrderByAreaName(parentArea.getAreaId());
		List<Area> listOfChildAreas = areaRepository.findByAreaCodeIn(childAreaCodes);

		if (childByParent.containsAll(listOfChildAreas)) {
			return true;
		} else {
			return false;
		}

	}

	@Override
	public List<TimePeriod> getListOfTimePeriod(String periodicity) {
		List<TimePeriod> listOfTimePeriod = timePeriodRepository
				.findAllByPeriodicityOrderByCreatedDateDesc(periodicity);
		return listOfTimePeriod;
	}

	@Override
	public Map<String, List<DataValueModel>> getGisData(String indecatorId, String tp, String areaCode,
			String indicatorGroup) {

		Map<String, List<DataValueModel>> thematicMap = new HashMap<>();
		Indicator indicator = indicatorRepository.getIndicatorsByDatumId(indecatorId);
		Area fetchedArea = areaRepository.findByAreaCode(areaCode);
		List<Integer> listOfAreaIds = new ArrayList<>();
		List<Area> listOfChildArea = areaRepository.findByParentAreaIdOrderByAreaName(fetchedArea.getAreaId());
		DashboardIndicator dashboardIndicator = dashboardIndicatorRepository.findByIndicatorGroup(indicatorGroup);
		Map<String, String> colorLegendMap = new HashMap<>();
		Map<String, String> colorLegendFileMap = new HashMap<>();
		if (!dashboardIndicator.getGeolegends().isEmpty()) {
			for (GeoLegends geoLegends : dashboardIndicator.getGeolegends()) {
				colorLegendMap.put(geoLegends.getStartRange() + ("_") + geoLegends.getEndRange(),
						geoLegends.getCssColor());
				colorLegendFileMap.put(geoLegends.getCssColor(), geoLegends.getIcon());
			}
		}
		Map<Integer, Area> areaMap = new HashMap<>();
		for (Area area : listOfChildArea) {
			listOfAreaIds.add(area.getAreaId());

			areaMap.put(area.getAreaId(), area);
		}

		List<DataValue> listOfDataValue = dataDomainRepository.findByInidAndTpInAndDatumIdIn(
				Integer.parseInt(indecatorId), Arrays.asList(Integer.parseInt(tp)), listOfAreaIds);
		List<DataValueModel> listOfDataValueModel = new ArrayList<>();
		DataValueModel dataValueModel = null;
		for (DataValue dataValue : listOfDataValue) {
			Area fetchedAreaLatLong = areaRepository.findByAreaId(dataValue.getDatumId());
			dataValueModel = new DataValueModel();
			dataValueModel.setAreaCode(areaMap.get(dataValue.getDatumId()).getAreaCode());
			dataValueModel.setAreaId(dataValue.getDatumId());
			dataValueModel.setAreaLevelId(areaMap.get(dataValue.getDatumId()).getAreaLevel().getAreaLevelId());
			dataValueModel.setAreaName(areaMap.get(dataValue.getDatumId()).getAreaName());
			dataValueModel.setDataValue(dataValue.getDataValue());
			dataValueModel.setIndicatorId(dataValue.getInid());
			dataValueModel.setTimeperiod(dataValue.getTp());
			dataValueModel.setCssColor(getGeoColor(colorLegendMap, dataValue.getDataValue()));
			dataValueModel.setIcon(colorLegendFileMap.get(dataValueModel.getClass()));
			if (fetchedAreaLatLong.getLocation() != null) {
				List<Double> latlongList = (List<Double>) fetchedAreaLatLong.getLocation().get("coordinates");
				dataValueModel.setLatitude(latlongList.get(0));
				dataValueModel.setLongitude(latlongList.get(1));
			}

			listOfDataValueModel.add(dataValueModel);
		}
		thematicMap.put(indicator.getIndicatorDataMap().get("indicatorName").toString(), listOfDataValueModel);
		return thematicMap;
	}

	private String getGeoColor(Map<String, String> colorLegendMap, Double dataValue) {
		String color = null;
		for (Map.Entry<String, String> entry : colorLegendMap.entrySet()) {
			if (dataValue >= Double.parseDouble(entry.getKey().split("-")[0])
					&& dataValue < Double.parseDouble(entry.getKey().split("-")[1])) {
				color = entry.getValue();
			}
		}
		return color;
	}

	@Override
	public Long saveHeaderAndSubheader(String title) {
		Heading fetchedHeading = headingRepository.findByTitle(title.toUpperCase().trim());
		if (fetchedHeading == null) {
			fetchedHeading = new Heading();
			fetchedHeading.setCreatedDate(new Date());
			fetchedHeading.setSlugId(headingRepository.count());
			fetchedHeading.setTitle(title.toUpperCase().trim());
			fetchedHeading = headingRepository.save(fetchedHeading);
			return fetchedHeading.getSlugId();
		} else {
			return fetchedHeading.getSlugId();
		}
	}

	@Override
	public List<Heading> searchHeading(String title) {
		List<Heading> listOfHeading = headingRepository.findByTitleLikeOrderByTitle(title.toUpperCase());
		return listOfHeading;
	}

	@Override
	public List<String> getHeadingByDashboard(String dashboardId) {
		List<DashboardIndicator> listOfDashboardIndicator = dashboardIndicatorRepository
				.findAllByDashboardIdOrderByCreatedDateDesc(dashboardId);
		Set<String> sectorList = new HashSet<>();
		for (DashboardIndicator dashboardIndicator : listOfDashboardIndicator) {
			sectorList.add(dashboardIndicator.getSector());
		}
		return sectorList.stream().sorted().collect(Collectors.toList());
	}

	private void setTableData(DashboardIndicator groupIndicatorModel, TableData tableData, List<Integer> areaList,
			Integer tp) {

		List<Integer> areaListAvailable = groupIndicatorModel.getMulIndOrMulArea() != null
				&& groupIndicatorModel.getMulIndOrMulArea().equals("multipleIndicators") && areaList != null ? areaList
						: groupIndicatorModel.getAreaIdsAreaNames().get(0);
		// areaListAvailable=Arrays.asList(5004,5003);
		List<Area> areasAvailble = areaRepository.findByAreaIdIn(areaListAvailable);
		List<String> axisList = null;

		List<List<Integer>> chartIndicators = groupIndicatorModel.getChartIndicators();

		List<Integer> indicatorIds = new ArrayList<>();
		for (List<Integer> indIds : chartIndicators) {
			indicatorIds.addAll(indIds);
		}
		/**
		 * finding data based on indicatorList and area list if dashboard indicators is
		 * not multipleAreaType
		 */
		List<DataValue> listOfDataValue = new ArrayList<>();
		Map<String, Double> indTpAreaValueMap = new HashMap<>();
		if (areaListAvailable != null && tp != null) {
			listOfDataValue = dataDomainRepository.findByInidInAndTpInAndDatumIdInAndDatumtype(indicatorIds,
					Arrays.asList(tp), areaListAvailable, "area");
			for (DataValue dataValue : listOfDataValue) {
				indTpAreaValueMap.put(dataValue.getInid() + "_" + dataValue.getTp() + "_" + dataValue.getDatumId(),
						dataValue.getDataValue());
			}
		} else if (areaListAvailable == null && tp != null) {
			listOfDataValue = dataDomainRepository.findByInidInAndTpIn(indicatorIds, Arrays.asList(tp));
			for (DataValue dataValue : listOfDataValue) {
				indTpAreaValueMap.put(dataValue.getInid() + "_" + dataValue.getTp(), dataValue.getDataValue());
			}
		} else {
			listOfDataValue = dataDomainRepository.findByInidIn(indicatorIds);
			for (DataValue dataValue : listOfDataValue) {
				indTpAreaValueMap.put(dataValue.getInid().toString(), dataValue.getDataValue());
			}
		}
		TimePeriod timeperiod = timePeriodRepository.findByTimePeriodId(tp);
		String mulIndOrMulArea = groupIndicatorModel.getMulIndOrMulArea();
		List<TableHead> tableHeads = new LinkedList<>();
		List<String> tableColumns = null;

		List<Map<String, String>> cellData = new LinkedList<>();
		Map<String, String> cellDataValue = new LinkedHashMap<>();
		List<String> rowTitles = groupIndicatorModel.getRowTitles();

		if (mulIndOrMulArea.equals("multipleAreasMultipleIndicators")) {
			TableHead tableHead = new TableHead();
			tableHead.setColspan("1");
			tableHead.setRowspan("1");
			// rowTitles are table headers
			// tableHead.setValue(groupIndicatorModel.getRowTitles().get(i));
			tableHeads.add(tableHead);
		}
		if (mulIndOrMulArea.equals("multipleAreas")) {
			tableData.setIndName(groupIndicatorModel.getRowTitles().get(0));
		}
		int indGrp = 0;
		tableColumns = new LinkedList<>();
		if (mulIndOrMulArea.equals("multipleAreasMultipleIndicators")) {
			List<Integer> indList = new LinkedList<>();

			for (String rowTitle : rowTitles) {

				// List<Integer> indList = groupIndicatorModel.getChartIndicators().get(indGrp);

				// int indNo = 0;
				// String trimmedIndName = groupIndicatorModel.getRowTitles().get(indNo);
				if (mulIndOrMulArea.equals("multipleAreasMultipleIndicators")) {

					indList.addAll(groupIndicatorModel.getChartIndicators().get(indGrp));

					TableHead tableHead = new TableHead();
					tableHead.setColspan(groupIndicatorModel.getTableHeaders().get(indGrp).size() - 1 + "");
					tableHead.setRowspan("1");
					// rowTitles are table headers
					tableHead.setValue(rowTitle);
					tableHeads.add(tableHead);

					List<String> indTableHeadList = mulIndOrMulArea.equals("multipleAreasMultipleIndicators")
							? groupIndicatorModel.getTableHeaders().get(indGrp)
							: groupIndicatorModel.getTableHeaders().get(0);
					if (indGrp == 0) {
						tableColumns.add(indTableHeadList.get(0));
					}
					indTableHeadList.remove(0);
					for (String tableCol : indTableHeadList) {
						tableColumns.add(tableCol);
					}
				}
				++indGrp;
			}

			for (Area area : areasAvailble) {
				indGrp = 0;
				cellDataValue = new LinkedHashMap<>();

				cellDataValue.put("area", area.getAreaName());
				for (int i = 0; i < indList.size(); i++) {
					int tableColNo = i+1;

					String dataValue = "";
					dataValue = String.valueOf(indTpAreaValueMap
							.get(indList.get(i) + "_" + timeperiod.getTimePeriodId() + "_" + area.getAreaId()));

					cellDataValue.put(tableColumns.get(tableColNo), dataValue);

				}
				cellData.add(cellDataValue);
			}
		} else {

			for (String rowTitle : rowTitles) {

				List<Integer> indList = groupIndicatorModel.getChartIndicators().get(indGrp);

				// int indNo = 0;
				// String trimmedIndName = groupIndicatorModel.getRowTitles().get(indNo);
				if (mulIndOrMulArea.equals("multipleAreasMultipleIndicators")) {
					TableHead tableHead = new TableHead();
					tableHead.setColspan(groupIndicatorModel.getTableHeaders().get(indGrp).size() - 1 + "");
					tableHead.setRowspan("1");
					// rowTitles are table headers
					tableHead.setValue(rowTitle);
					tableHeads.add(tableHead);
				}

				// tableColumns.addAll(mulIndOrMulArea.equals("multipleAreasMultipleIndicators")
				// ? groupIndicatorModel.getTableHeaders().get(indGrp)
				// : groupIndicatorModel.getTableHeaders().get(0));

				List<String> indTableHeadList = mulIndOrMulArea.equals("multipleAreasMultipleIndicators")
						? groupIndicatorModel.getTableHeaders().get(indGrp)
						: groupIndicatorModel.getTableHeaders().get(0);
				if (indGrp == 0) {
					tableColumns.add(indTableHeadList.get(0));
				}
				indTableHeadList.remove(0);
				for (String tableCol : indTableHeadList) {
					tableColumns.add(tableCol);
				}

				for (Area area : areasAvailble) {
					for (int i = 0; i < indList.size(); i++) {
						cellDataValue = new LinkedHashMap<>();

						for (String indTableHeader : indTableHeadList) {

							if (mulIndOrMulArea.equals("multipleIndicators")) {
								cellDataValue.put("indicator", rowTitle);
							} else {
								cellDataValue.put("area", area.getAreaName());
							}

							String dataValue = "";
							dataValue = String.valueOf(indTpAreaValueMap
									.get(indList.get(i) + "_" + timeperiod.getTimePeriodId() + "_" + area.getAreaId()));
							cellDataValue.put(indTableHeader, dataValue);

						}

					}

					cellData.add(cellDataValue);
				}
				++indGrp;
			}

		}
		tableData.setCellData(cellData);
		tableData.setTableHeads(tableHeads);
		tableData.setTableColumns(tableColumns);

	}

}
