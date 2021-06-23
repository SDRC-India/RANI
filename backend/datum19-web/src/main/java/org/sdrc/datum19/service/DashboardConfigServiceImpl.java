package org.sdrc.datum19.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.sdrc.datum19.document.Area;
import org.sdrc.datum19.document.AreaLevel;
import org.sdrc.datum19.document.DashboardIndicator;
import org.sdrc.datum19.document.EnginesForm;
import org.sdrc.datum19.document.Indicator;
import org.sdrc.datum19.document.Sector;
import org.sdrc.datum19.document.ThematicFileData;
import org.sdrc.datum19.model.DashboardIndicatorGroupModel;
import org.sdrc.datum19.model.FormSectorModel;
import org.sdrc.datum19.model.TypeModel;
import org.sdrc.datum19.repository.AreaLevelRepository;
import org.sdrc.datum19.repository.AreaRepository;
import org.sdrc.datum19.repository.DashboardIndicatorRepository;
import org.sdrc.datum19.repository.EnginesFormRepository;
import org.sdrc.datum19.repository.IndicatorRepository;
import org.sdrc.datum19.repository.SectorRepository;
import org.sdrc.datum19.repository.ThematicFileDataRepository;
import org.sdrc.datum19.repository.TypeRepository;
import org.sdrc.datum19.util.Alignments;
import org.sdrc.datum19.util.ChartType;
import org.sdrc.datum19.util.Constants;
import org.sdrc.datum19.util.Unit;
import org.sdrc.datum19.util.ValueObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author subrata
 *
 */
@Service
@Slf4j
public class DashboardConfigServiceImpl implements DashboardConfigService {

	@Autowired
	private TypeRepository typeRepository;

	@Autowired
	private SectorRepository sectorRepository;

	@Autowired
	private ConfigurableEnvironment env;

	@Autowired
	private DashboardIndicatorRepository dashboardIndicatorRepository;

	@Autowired
	private EnginesFormRepository enginesFormRepository;

	@Autowired
	private AreaLevelRepository areaLevelRepository;

	@Autowired
	private AreaRepository areaRepository;

	@Autowired
	private ThematicFileDataRepository thematicFileDataRepository;

	private Path mapPathLocation;

	@Value("${map.file.path}")
	private String mapfilepath;

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private IndicatorRepository indicatorRepository;

	/**
	 * Returning the chart types
	 */
	@Override
	public List<ValueObject> getChartTypes() {
		return Arrays.asList(ChartType.values()).stream().map(v -> {
			ValueObject object = new ValueObject();
			object.setKey(v.getId());
			object.setValue(v.getChartType());
			object.setName(v.toString().contains("_") ? (v.toString()).replace("_", " ").trim() : v.toString());
			return object;
		}).sorted(Comparator.comparing(v -> v.getValue())).collect(Collectors.toList());
	}

	/**
	 * Returning the Units
	 */
	@Override
	public List<ValueObject> getUnits() {
		return Arrays.asList(Unit.values()).stream().map(v -> {
			ValueObject object = new ValueObject();
			object.setKey(v.getId());
			object.setValue(v.getUnit());
			object.setName(v.toString());
			return object;
		}).sorted(Comparator.comparing(v -> v.getKey())).collect(Collectors.toList());
	}

	/**
	 * Returning the alignments
	 */
	@Override
	public List<ValueObject> getAlignments() {
		return Arrays.asList(Alignments.values()).stream().map(v -> {
			ValueObject object = new ValueObject();
			object.setKey(v.getId());
			object.setValue(v.getAlign());
			object.setName(v.toString().contains("_") ? (v.toString()).replace("_", " ").trim() : v.toString());
			return object;
		}).sorted(Comparator.comparing(v -> v.getKey())).collect(Collectors.toList());
	}

	/**
	 * Returning the types
	 */
	@Override
	public List<TypeModel> getTypes() {

		return typeRepository.findAll().stream().map(v -> {
			TypeModel tm = new TypeModel();
			tm.setTypeId(v.getSlugId());
			tm.setFormId(v.getFormId());
			tm.setTypeName(v.getTypeName());
			tm.setDescription(v.getDescription());

			return tm;
		}).sorted(Comparator.comparing(v -> v.getTypeId())).collect(Collectors.toList());
	}

	/**
	 * Saving the Dashboard indicator configuration
	 */
	@Override
	public ResponseEntity<String> saveDashboardIndicatorConfig(DashboardIndicatorGroupModel indgr) {
		DashboardIndicator latestEntrdGroup = null;
		DashboardIndicator dashboardIndicator = null;

		try {
			/**
			 * Checking the Id from the DashboardIndicatorGroupModel model. If not found,
			 * then we are creating a new DashboardIndicator else getting from the database
			 * for update.
			 */
			if (indgr.getId() == null) {
				latestEntrdGroup = dashboardIndicatorRepository.findTop1ByOrderByCreatedDateDesc();
				dashboardIndicator = new DashboardIndicator();
			} else {
				dashboardIndicator = dashboardIndicatorRepository.findByDashboardIdAndChartGroup(indgr.getDashboardId(),
						indgr.getChartGroup());
			}

			/**
			 * setting all the values in the DashboardIndicator
			 */
			dashboardIndicator.setAlign(indgr.getAlign());

			dashboardIndicator.setChartHeader(indgr.getChartHeader());
			dashboardIndicator
					.setChartIndicators(indgr.getChartIndicators() != null && !indgr.getChartIndicators().isEmpty()
							? (indgr.getChartIndicators().stream().map(l -> {
								return l.stream().map(v -> Integer.valueOf(v)).collect(Collectors.toList());
							}).collect(Collectors.toList()))
							: null);
			dashboardIndicator.setChartIndicatorNames(indgr.getChartIndicatorNames());
			dashboardIndicator.setChartLegends(
					indgr.getChartLegends() != null && !indgr.getChartLegends().isEmpty() ? indgr.getChartLegends()
							: null);
			dashboardIndicator.setChartType(indgr.getChartType());
			dashboardIndicator.setColorLegends(
					indgr.getColorLegends() != null && !indgr.getColorLegends().isEmpty() ? indgr.getColorLegends()
							: null);
			dashboardIndicator.setExtraInfo(indgr.getExtraInfo() != null ? indgr.getExtraInfo() : null);

			dashboardIndicator.setKpiChartHeader(indgr.getKpiChartHeader() != null ? indgr.getKpiChartHeader() : null);
			dashboardIndicator.setKpiIndicator(indgr.getKpiIndicator() != null ? indgr.getKpiIndicator() : null);
			dashboardIndicator.setSector(indgr.getSector().toUpperCase().trim());
			dashboardIndicator.setSectorId(indgr.getSectorId().toString());
			dashboardIndicator.setSubSector(indgr.getSubSector().toUpperCase().trim());
			dashboardIndicator.setSubSectorId(indgr.getSubSectorId().toString());
			dashboardIndicator.setUnit(indgr.getUnit());
			dashboardIndicator.setValueFrom(indgr.getValueFrom() != null ? indgr.getValueFrom() : null);

			dashboardIndicator.setCreatedDate(indgr.getId() == null ? new Date() : dashboardIndicator.getCreatedDate());
			dashboardIndicator.setUpdatedDate(indgr.getId() == null ? null : new Date());
			dashboardIndicator.setDashboardId(indgr.getDashboardId());

			String chartGroup = indgr.getId() == null
					? (latestEntrdGroup != null
							? (latestEntrdGroup.getChartGroup().substring(0, 2) + (Integer.valueOf(latestEntrdGroup
									.getChartGroup().substring(2, latestEntrdGroup.getChartGroup().length())) + 1))
							: Constants.INITIAL_GROUP_NAME)
					: dashboardIndicator.getChartGroup();

			dashboardIndicator.setChartGroup(chartGroup);
			dashboardIndicator
					.setIndicatorGroup((indgr.getIndicatorGroup() != null && !indgr.getIndicatorGroup().equals(""))
							? indgr.getIndicatorGroup()
							: chartGroup);
			dashboardIndicator.setFormId(indgr.getFormId());
			dashboardIndicator.setThematicFileDataSlugId(
					indgr.getThematicFileDataSlugId() != null ? indgr.getThematicFileDataSlugId() : null);
			dashboardIndicator.setAreaCode(indgr.getAreaCode() != null ? indgr.getAreaCode() : null);
			dashboardIndicator.setRange(indgr.getRange() == null ? null : indgr.getRange());
			dashboardIndicator.setHeading(indgr.getHeading() == null ? null : indgr.getHeading());
			dashboardIndicator.setSubheading(indgr.getSubheading() == null ? null : indgr.getSubheading());
			dashboardIndicator.setGeolegends(indgr.getGeolegends());
			dashboardIndicator
					.setMulIndOrMulArea(indgr.getMulIndOrMulArea() == null ? null : indgr.getMulIndOrMulArea());
			dashboardIndicator.setAreaIdsAreaNames(indgr.getAreaIdsAreaNames().get(0) == null ? null
					: Arrays.asList(indgr.getAreaIdsAreaNames().get(0)));
			dashboardIndicator.setMultipleAreasName(indgr.getMultipleAreasName().get(0) == null ? null
					: Arrays.asList(indgr.getMultipleAreasName().get(0)));
			dashboardIndicator
			.setTableHeaders(indgr.getTableHeaders() == null ? null : indgr.getTableHeaders());
			dashboardIndicator
			.setRowTitles(indgr.getRowTitles() == null ? null : indgr.getRowTitles());
			dashboardIndicatorRepository.save(dashboardIndicator);

			return ResponseEntity.status(HttpStatus.OK)
					.body(indgr.getSaveOrEdit().equals(Constants.SAVE) ? env.getProperty("ind.gr.added.success")
							: env.getProperty("ind.gr.updated.success"));
		} catch (Exception ex) {
			log.error("Unable to update with error : ", ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(env.getProperty("msg.failure"));
		}
	}

	@Override
	public List<DashboardIndicator> getDashboardIndicatorConfigGr(String dashboardId) {

		return dashboardIndicatorRepository.findAllByDashboardIdOrderByCreatedDateDesc(dashboardId);
	}

	/**
	 * Returning the sector details
	 */
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

		Map<String, List<FormSectorModel>> map = list.stream()
				.collect(Collectors.groupingBy(FormSectorModel::getSectorName, LinkedHashMap::new,
						Collectors.mapping(Function.identity(), Collectors.toList())));
		return map;

	}

	/**
	 * According to the group name, returning the indicator configuration for edit
	 */
	@Override
	public ResponseEntity<DashboardIndicatorGroupModel> getIndicatorConfigEdit(String dashboardId, String groupName) {
		/**
		 * According to the group name, getting the DashboardIndicator group from
		 * database.
		 */
		DashboardIndicator editGroup = dashboardIndicatorRepository.findByDashboardIdAndIndicatorGroup(dashboardId,	groupName);

		DashboardIndicatorGroupModel groupModel = null;
		
		List<List<Integer>> chartIndicators = editGroup.getChartIndicators();

		List<String> indicatorIds = new ArrayList<>();
		if (chartIndicators != null) {
			for (List<Integer> indIds : chartIndicators) {
				indicatorIds.addAll(indIds.stream().map(v -> v.toString()).collect(Collectors.toList()));
			}
			}else {
				indicatorIds.add(editGroup.getKpiIndicator().toString());
			}
		List<Indicator> indicatorsList=indicatorRepository.findByIndNidIn(indicatorIds);
		Map<Integer,Indicator> nidIndMap=new HashMap<>();
		for (Indicator indicator : indicatorsList) {
			nidIndMap.put(Integer.parseInt((String)indicator.getIndicatorDataMap().get("indicatorNid")), indicator);
		}
		if (editGroup != null) {
			/**
			 * If groupName is found, then returning groupModel
			 */
			groupModel = new DashboardIndicatorGroupModel();

			groupModel.setAlign(editGroup.getAlign());
			groupModel.setChartGroup(editGroup.getChartGroup());
			groupModel.setChartHeader(editGroup.getChartHeader());

			groupModel.setChartIndicators(
					editGroup.getChartIndicators() != null && !editGroup.getChartIndicators().isEmpty()
							? (editGroup.getChartIndicators().stream().map(l -> {
								return l.stream().map(v -> String.valueOf(v)).collect(Collectors.toList());
							}).collect(Collectors.toList()))
							: null);
			groupModel.setChartIndicatorNames(editGroup.getChartIndicatorNames());
			groupModel.setChartLegends(editGroup.getChartLegends());
			groupModel.setChartType(editGroup.getChartType());
			groupModel.setColorLegends(editGroup.getColorLegends());
			groupModel.setExtraInfo(editGroup.getExtraInfo());
			groupModel.setFormId(editGroup.getFormId());
			groupModel.setIndicatorGroup(editGroup.getIndicatorGroup());
			groupModel.setKpiChartHeader(editGroup.getKpiChartHeader());
			groupModel.setKpiIndicator(editGroup.getKpiIndicator());
			groupModel.setSectorId(Integer.valueOf(editGroup.getSectorId()));
			groupModel.setSector(editGroup.getSector());
			groupModel.setSubSector(editGroup.getSubSector());
			groupModel.setSubSectorId(Integer.valueOf(editGroup.getSubSectorId()));
			groupModel.setUnit(editGroup.getUnit());
			groupModel.setValueFrom(editGroup.getValueFrom());
			groupModel.setId(editGroup.getId());
			groupModel.setHeading(editGroup.getHeading() == null ? null : editGroup.getHeading());
			groupModel.setSubheading(editGroup.getSubheading() == null ? null : editGroup.getSubheading());
			groupModel.setRange(editGroup.getRange() == null ? null : editGroup.getRange());
			groupModel.setAreaIdsAreaNames(
					editGroup.getAreaIdsAreaNames() == null ? null : editGroup.getAreaIdsAreaNames());
			groupModel
					.setMulIndOrMulArea(editGroup.getMulIndOrMulArea() == null ? null : editGroup.getMulIndOrMulArea());
			groupModel.setMultipleAreasName(editGroup.getMultipleAreasName().get(0) == null ? null
					: Arrays.asList(editGroup.getMultipleAreasName().get(0)));
			groupModel
			.setTableHeaders(editGroup.getTableHeaders() == null ? null : editGroup.getTableHeaders());
			groupModel
			.setRowTitles(editGroup.getRowTitles() == null ? null : editGroup.getRowTitles());
			
			List<List<Indicator>> indicatorListGroupWise=new LinkedList<>();
			List<Indicator> indicatorList=new LinkedList<>();
			if(chartIndicators!=null) {
			for (List<Integer> indNidList : chartIndicators) {
				indicatorList=new LinkedList<>();
				for (Integer indNid : indNidList) {
					indicatorList.add(nidIndMap.get(indNid));
				}
				indicatorListGroupWise.add(indicatorList);
			}
		}else {
			indicatorList.add(nidIndMap.get(editGroup.getKpiIndicator()));
			indicatorListGroupWise.add(indicatorList);
		}
			groupModel.setIndicators(indicatorListGroupWise);
			if (editGroup.getThematicFileDataSlugId() != null) {
				ThematicFileData thematicFileData = thematicFileDataRepository
						.findBySlugId(editGroup.getThematicFileDataSlugId());
				if (thematicFileData != null) {
					groupModel.setShapePath(thematicFileData.getShapeJSON());
					groupModel.setFileName(thematicFileData.getFileName());
					groupModel.setThematicFileDataSlugId(editGroup.getThematicFileDataSlugId());
				}
				Area areaThematic = null;

				if (thematicFileData.getDistrictId() != null) {
					areaThematic = areaRepository.findByAreaId(thematicFileData.getDistrictId());
					groupModel.setAreaCode(areaThematic.getAreaCode());
					groupModel.setAreaName(areaThematic.getAreaName());
					groupModel.setAreaLevelId(areaThematic.getAreaLevel().getAreaLevelId());
					groupModel.setAreaLevelName(areaThematic.getAreaLevel().getAreaLevelName());
					groupModel.setDistrict(areaThematic);
					groupModel.setState(areaRepository.findByAreaId(areaThematic.getParentAreaId()));
				} else if (thematicFileData.getStateId() != null) {
					areaThematic = areaRepository.findByAreaId(thematicFileData.getStateId());
					groupModel.setAreaCode(areaThematic.getAreaCode());
					groupModel.setAreaName(areaThematic.getAreaName());
					groupModel.setAreaLevelId(areaThematic.getAreaLevel().getAreaLevelId());
					groupModel.setAreaLevelName(areaThematic.getAreaLevel().getAreaLevelName());
					groupModel.setState(areaThematic);
				} else if (thematicFileData.getCountryId() != null) {
					areaThematic = areaRepository.findByAreaId(thematicFileData.getCountryId());
					groupModel.setAreaCode(areaThematic.getAreaCode());
					groupModel.setAreaName(areaThematic.getAreaName());
					groupModel.setAreaLevelId(areaThematic.getAreaLevel().getAreaLevelId());
					groupModel.setAreaLevelName(areaThematic.getAreaLevel().getAreaLevelName());
				}
			}

			return ResponseEntity.status(HttpStatus.OK).body(groupModel);
		} else {
			/**
			 * If groupName is not found, then returning null
			 */
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(groupModel);
		}
	}

	@Override
	public void deleteChart(String id) {
		// TODO Auto-generated method stub
		dashboardIndicatorRepository.deleteById(id);
	}

	@Override
	public List<AreaLevel> getAreaLevel() {
		// TODO Auto-generated method stub
		return areaLevelRepository.findAll();
	}

	@Override
	public List<Area> getAreaByAreaLevelId(Integer areaLevelId, Integer parentAreaId) {
		return areaRepository.findByParentAreaIdAndAreaLevelAreaLevelId(parentAreaId, areaLevelId);
	}

	@Override
	public ResponseEntity<String> uploadFile(MultipartFile uploadFileModel, String filePathLocation) {

		mapPathLocation = Paths.get(mapfilepath + filePathLocation + "/");

		String filePath = null;
		if (!new File(mapfilepath + filePathLocation).exists()) {
			new File(mapfilepath + filePathLocation).mkdirs();
		}
		if (uploadFileModel != null) {
			try {
				String fileNameWithDateTime = FilenameUtils.getBaseName(uploadFileModel.getOriginalFilename()) + "_"
						+ new Date().getTime() + "."
						+ FilenameUtils.getExtension(uploadFileModel.getOriginalFilename());

				filePath = mapfilepath + filePathLocation + "/" + fileNameWithDateTime;

				Files.copy(uploadFileModel.getInputStream(), this.mapPathLocation.resolve(fileNameWithDateTime),
						StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new ResponseEntity<String>(filePath, HttpStatus.OK);
	}

	@Override
	public List<Indicator> getSearchedIndicatorForDbTblWithGid(String indicatorGid, String subgroupType) {
		List<Indicator> indicators = new ArrayList<>();
		Query query = new Query();
		query.addCriteria(Criteria.where("indicatorDataMap.indicatorGid").is(indicatorGid)
				.and("indicatorDataMap.subgroupType").is(subgroupType));
		indicators = mongoTemplate.find(query, Indicator.class);
		return indicators;
	}

	@Override
	public List<Indicator> getSearchedIndicatorForDbTbl(List<String> subgroupTypeList) {
		List<Indicator> indicators = new ArrayList<>();
		Query query = new Query();
		query.addCriteria(Criteria.where("indicatorDataMap.subgroupType").in(subgroupTypeList));
		indicators = mongoTemplate.find(query, Indicator.class);
		return indicators;
	}

}
