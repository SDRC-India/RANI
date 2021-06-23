package org.sdrc.datum19.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.sdrc.datum19.document.DataValue;
import org.sdrc.datum19.document.Indicator;
import org.sdrc.datum19.document.TimePeriod;
import org.sdrc.datum19.model.AreaDataModel;
import org.sdrc.datum19.model.ChartData;
import org.sdrc.datum19.model.ChartDataModel;
import org.sdrc.datum19.model.GroupChartDataModel;
import org.sdrc.datum19.model.IndicatorGroupModel;
import org.sdrc.datum19.model.LegendModel;
import org.sdrc.datum19.model.SearchDataRequestModel;
import org.sdrc.datum19.model.Top5Bot5;
import org.sdrc.datum19.repository.AreaRepository;
import org.sdrc.datum19.repository.DataDomainRepository;
import org.sdrc.datum19.repository.DataSearchRepository;
import org.sdrc.datum19.repository.IndicatorRepository;
import org.sdrc.datum19.repository.TimePeriodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SearchDataServiceImpl implements SearchDataService {

	@Autowired
	private TimePeriodRepository timePeriodRepository;

	@Autowired
	private DataDomainRepository dataDomainRepository;

	@Autowired
	private IndicatorRepository indicatorRepository;

	@Autowired
	private AreaRepository arearepo;

	@Autowired
	private DataSearchRepository dataSearchRepository;

	@Override
	public Map<String, Object> getChartDataForSearchedIndicator(SearchDataRequestModel model) {

		Map<String, Object> searchDataMap = new LinkedHashMap<>();

		searchDataMap.put("doughnutChart", getDoughnutChartData(model));
		searchDataMap.put("trendChart", getDataSearchLegend(model));
		searchDataMap.put("elseWhere",
				getAreaData(Integer.parseInt(model.getIndicatorId()), model.getDatumId(), model.getTp()));

		return searchDataMap;
	}

	public List<IndicatorGroupModel> getDoughnutChartData(SearchDataRequestModel model) {

		Indicator requestedInd = indicatorRepository.findByIndNid(model.getIndicatorId());
		List<Indicator> indicatorList = indicatorRepository.findAllByIndGidAndSubGroupTypeAndUnit(
				(String) requestedInd.getIndicatorDataMap().get("indicatorGid"),
				(String) requestedInd.getIndicatorDataMap().get("subgroupType"),
				(String) requestedInd.getIndicatorDataMap().get("unit"));

		List<Integer> indIdList = new ArrayList<>();
		Map<Integer, String> indicatorIdNameMap = new HashMap<Integer, String>();
		for (Indicator ind : indicatorList) {
			indIdList.add(Integer.parseInt((String) ind.getIndicatorDataMap().get("indicatorNid")));
			indicatorIdNameMap.put(Integer.parseInt((String) ind.getIndicatorDataMap().get("indicatorNid")),
					(String) ind.getIndicatorDataMap().get("indicatorName"));
		}

		List<DataValue> listOfDataValue = new ArrayList<>();
		Map<String, Double> indValueMap = new HashMap<>();

		listOfDataValue = dataDomainRepository.findByInidInAndTpInAndDatumIdInAndDatumtype(indIdList,
				Arrays.asList(model.getTp()), Arrays.asList(model.getDatumId()), "area");
		for (DataValue dataValue : listOfDataValue) {
			indValueMap.put(String.valueOf(dataValue.getInid()), dataValue.getDataValue());
		}

		List<IndicatorGroupModel> IndicatorGroupModelList = new ArrayList<>();

		List<GroupChartDataModel> listOfGroupChartData = null;
		GroupChartDataModel groupChartDataModel = null;
		List<List<ChartDataModel>> chartDataValue = new LinkedList<>();
		List<LegendModel> legends = new LinkedList<>();
		List<ChartDataModel> chartDataModels = new LinkedList<>();

		listOfGroupChartData = new ArrayList<GroupChartDataModel>();
		groupChartDataModel = new GroupChartDataModel();

		IndicatorGroupModel indicatorGroupModel = new IndicatorGroupModel();
		String chartType = ((String) requestedInd.getIndicatorDataMap().get("unit")).equalsIgnoreCase("number") ? "bar"
				: "doughnut";

		indicatorGroupModel.setChartsAvailable(Arrays.asList(chartType));
		indicatorGroupModel.setIndicatorName((String) requestedInd.getIndicatorDataMap().get("indicatorName"));
		indicatorGroupModel.setAlign("col-md-6");
		List<String> hexColorCodes = Arrays.asList("#af4448", "#c85a54", "#FF5733", "#FC33FF", "#F42954", "#F4292C",
				"#EB8B17", "#AA0707", "#F03D3D", "#D65A13");
		int colorIndex = 0;
		for (Indicator indicator : indicatorList) {

			/**
			 * getting the indicator name
			 */
			String indName = "";

			groupChartDataModel.setHeaderIndicatorName((String) indicator.getIndicatorDataMap().get("indicatorName"));

			groupChartDataModel
					.setHeaderIndicatorValue(Integer.valueOf(String.valueOf((Math.round(Math.random() * 100)))));
			/**
			 * getting the ChartDataValue
			 */

			String dataValue = "";
			dataValue = String.valueOf(indValueMap.get((String) indicator.getIndicatorDataMap().get("indicatorNid")));
			String axis = chartType.equalsIgnoreCase("BAR") ? (String) indicator.getIndicatorDataMap().get("subgroup")
					: hexColorCodes.get(colorIndex) + "_" + (String) indicator.getIndicatorDataMap().get("subgroup");

			if (!((String) indicator.getIndicatorDataMap().get("subgroup")).equalsIgnoreCase("Total")) {

				getChartDataModel(Integer.parseInt((String) indicator.getIndicatorDataMap().get("indicatorNid")),
						chartType, chartDataModels, null, axis, null,
						(String) indicator.getIndicatorDataMap().get("unit"), null, dataValue, null, null);

				/**
				 * Setting the Legends in ChartDataModel
				 */
				LegendModel legendModel = new LegendModel();
				legendModel.setCssClass(hexColorCodes.get(colorIndex));
				legendModel.setValue((String) indicator.getIndicatorDataMap().get("subgroup"));
				legends.add(legendModel);
			} else {
				ChartDataModel totalSubChartData = getTotalSubTypeChartData(
						Integer.parseInt((String) indicator.getIndicatorDataMap().get("indicatorNid")), chartType,
						chartDataModels, null, axis, null, (String) indicator.getIndicatorDataMap().get("unit"), null,
						dataValue, null, null);

				groupChartDataModel.setTotalSubChartData(totalSubChartData);
			}

			groupChartDataModel.setLegends(legends);
			++colorIndex;

		}
		chartDataValue.add(chartDataModels);
		groupChartDataModel.setChartDataValue(chartDataValue);

		listOfGroupChartData.add(groupChartDataModel);

		indicatorGroupModel.setChartData(listOfGroupChartData);
		IndicatorGroupModelList.add(indicatorGroupModel);
		return IndicatorGroupModelList;

	}

	public GroupChartDataModel getDataSearchLegend(SearchDataRequestModel model) {
		GroupChartDataModel groupChartDataModel = new GroupChartDataModel();
		Indicator indicator = indicatorRepository.getIndicatorsByDatumId(model.getIndicatorId());

		groupChartDataModel.setHeaderIndicatorName(indicator.getIndicatorDataMap().get("indicatorName").toString());
		groupChartDataModel.setUnit(indicator.getIndicatorDataMap().get("unit").toString());
		groupChartDataModel.setChartsAvailable(Arrays.asList("trend"));

		ChartData chartData = new ChartData();
		chartData.setHeaderIndicatorName(indicator.getIndicatorDataMap().get("indicatorName").toString());

		List<List<ChartDataModel>> listOfListChartDataValues = new ArrayList<List<ChartDataModel>>();
		List<ChartDataModel> listOfChartDataValue = new ArrayList<>();
		ChartDataModel chartDataModel = null;
		List<TimePeriod> listOfTimePeriod = timePeriodRepository.findTop6ByPeriodicityOrderByCreatedDateDesc("12");
		List<Integer> timeperiodIds = new ArrayList<>();
		Map<Integer, String> timeperiodIdAndNameMap = new HashMap<>();
		for (TimePeriod timePeriod : listOfTimePeriod) {
			timeperiodIds.add(timePeriod.getTimePeriodId());
			timeperiodIdAndNameMap.put(timePeriod.getTimePeriodId(), timePeriod.getTimePeriodDuration());

		}
		List<DataValue> listOfDataValue = dataDomainRepository.findByDatumIdAndInidAndTpIn(model.getDatumId(),
				Integer.parseInt(model.getIndicatorId()), timeperiodIds);

		for (DataValue dataValue : listOfDataValue) {
			chartDataModel = new ChartDataModel();
			chartDataModel.setAxis(timeperiodIdAndNameMap.get(dataValue.getTp()));
			chartDataModel.setValue(dataValue.getDataValue().toString());
			listOfChartDataValue.add(chartDataModel);

		}
		chartData.setChartDataValue(Arrays.asList(listOfChartDataValue));
		groupChartDataModel.setChartData(chartData);

		return groupChartDataModel;
	}

	private List<ChartDataModel> getChartDataModel(Integer indiId, String chartType,
			List<ChartDataModel> chartDataModels, Map<Integer, String> cssGroupMap, String axis, TimePeriod timePeriod,
			String unit, String label, String value, String key, String mulIndOrMulArea) {

		ChartDataModel chartDataModel = new ChartDataModel();

		chartDataModel.setAxis(axis.trim());

		chartDataModel.setLabel(label != null ? label.trim() : null);
		if (chartType.equalsIgnoreCase("pie") || chartType.equalsIgnoreCase("doughnut")
				|| chartType.equalsIgnoreCase("stack") || chartType.equalsIgnoreCase("BAR")
				|| chartType.equalsIgnoreCase("column") || chartType.equalsIgnoreCase("trend")) {
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

	public List<Top5Bot5> getAreaData(Integer indicatorId, Integer areaId, Integer tp) {
		List<Top5Bot5> top5Bot5Data = new ArrayList<>();
		Integer parentAreaId = arearepo.findByAreaId(areaId).getParentAreaId();
		Map<Integer, String> areaMap = new HashMap<>();
		List<Integer> areaIds = arearepo.findByParentAreaIdOrderByAreaName(parentAreaId).stream().map(a -> {
			areaMap.put(a.getAreaId(), a.getAreaName());
			return a.getAreaId();
		}).collect(Collectors.toList());
		Top5Bot5 top5 = new Top5Bot5();
		List<AreaDataModel> top5DataModels = new ArrayList<>();
		top5.setLabel("top5");
		dataSearchRepository.findTop5ByInidAndTpAndDatumIdInOrderByDataValueDesc(indicatorId, tp, areaIds)
				.forEach(d -> {
					AreaDataModel top5DataModel = new AreaDataModel();
					top5DataModel.setArea(areaMap.get(d.getDatumId()));
					top5DataModel.setValue(d.getDataValue());

					top5DataModels.add(top5DataModel);
				});
		top5.setAreaDatamodel(top5DataModels);
		top5Bot5Data.add(top5);

		Top5Bot5 bottom5 = new Top5Bot5();
		bottom5.setLabel("bottom5");
		List<AreaDataModel> bottom5DataModels = new ArrayList<>();
		dataSearchRepository.findTop5ByInidAndTpAndDatumIdInOrderByDataValueAsc(indicatorId, tp, areaIds).forEach(d -> {
			AreaDataModel bottom5DataModel = new AreaDataModel();
			bottom5DataModel.setArea(areaMap.get(d.getDatumId()));
			bottom5DataModel.setValue(d.getDataValue());

			bottom5DataModels.add(bottom5DataModel);
		});
		bottom5.setAreaDatamodel(bottom5DataModels);
		top5Bot5Data.add(bottom5);

		return top5Bot5Data;
	}

	private ChartDataModel getTotalSubTypeChartData(Integer indiId, String chartType,
			List<ChartDataModel> chartDataModels, Map<Integer, String> cssGroupMap, String axis, TimePeriod timePeriod,
			String unit, String label, String value, String key, String mulIndOrMulArea) {

		ChartDataModel chartDataModel = new ChartDataModel();

		chartDataModel.setAxis(axis.trim());

		chartDataModel.setLabel(label != null ? label.trim() : null);
		chartDataModel.setValue(String.valueOf(value));

		chartDataModel.setNumerator(String.valueOf((Math.round(Math.random() * 100))));
		chartDataModel.setDenominator(String.valueOf((Math.round(Math.random() * 100))));
		chartDataModel.setLegend(axis.trim());
		chartDataModel.setId(indiId);
		chartDataModel.setUnit(unit);
		chartDataModel.setKey(key);
		return chartDataModel;
	}

}
