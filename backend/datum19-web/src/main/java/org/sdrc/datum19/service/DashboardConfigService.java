package org.sdrc.datum19.service;

import java.util.List;
import java.util.Map;

import org.sdrc.datum19.document.Area;
import org.sdrc.datum19.document.AreaLevel;
import org.sdrc.datum19.document.DashboardIndicator;
import org.sdrc.datum19.document.Indicator;
import org.sdrc.datum19.model.DashboardIndicatorGroupModel;
import org.sdrc.datum19.model.FormSectorModel;
import org.sdrc.datum19.model.TypeModel;
import org.sdrc.datum19.util.ValueObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface DashboardConfigService {

	List<ValueObject> getChartTypes();

	List<ValueObject> getUnits();

	List<ValueObject> getAlignments();

	List<TypeModel> getTypes();

	ResponseEntity<String> saveDashboardIndicatorConfig(DashboardIndicatorGroupModel indicatorGroupModel);

	List<DashboardIndicator> getDashboardIndicatorConfigGr(String dashboardId);

	Map<String, List<FormSectorModel>> getAllChecklistSectors();

	ResponseEntity<DashboardIndicatorGroupModel> getIndicatorConfigEdit(String dashboardId,String groupName);

	void deleteChart(String id);
	
	List<AreaLevel> getAreaLevel();
	
	List<Area>  getAreaByAreaLevelId(Integer areaLevelId, Integer parentAreaId);

	ResponseEntity<String> uploadFile(MultipartFile uploadFileModel,String filePaths);

	List<Indicator> getSearchedIndicatorForDbTblWithGid(String indicatorGid, String subgroupType);

	List<Indicator> getSearchedIndicatorForDbTbl(List<String> subgroupTypeList);

}
