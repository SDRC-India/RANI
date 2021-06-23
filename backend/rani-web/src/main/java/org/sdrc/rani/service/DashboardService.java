package org.sdrc.rani.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.sdrc.rani.document.AreaLevel;
import org.sdrc.rani.models.FormSectorModel;
import org.sdrc.rani.models.IndicatorModel;
import org.sdrc.rani.models.PerformanceData;
import org.sdrc.rani.models.SectorModel;
import org.sdrc.rani.models.ThematicDashboardDataModel;
import org.sdrc.rani.models.TimePeriodModel;
import org.springframework.http.ResponseEntity;

/**
 * @author Debiprasad Parida Created Date: 17-04-2019
 *
 */

public interface DashboardService {

	public String pushIndicatorGroupData();

	Map<String, List<FormSectorModel>> getAllChecklistSectors();

	List<SectorModel> getDashboardData(Integer areaLevel, Integer areaId, String sectorName, Integer tpId,
			Integer formId, String dashboardType);

	public List<AreaLevel> getAreaLevels();

	public List<TimePeriodModel> getAllTimeperiods();

	public Map<String, List<FormSectorModel>> getFormSectorMappingData();

	public List<IndicatorModel> getIndicators(Integer formId);

	public Map<String, Object> getThematicViewData(Integer indicatorId, Integer tpId, Integer areaLevel, Integer areaId,
			String sectorName);

	public Map<String,PerformanceData> getOntimeData();

	public Map<String,PerformanceData> getPlanningdata();

	public ResponseEntity<String> getThematicViewDownload(ThematicDashboardDataModel thematicDashboardDataModel,
			HttpServletRequest request);
	
	public Map<String, Object> getPerformanceTrend(Integer formId);
	
	public void saveAchievemaneData(Integer timePeriodId);
	
	public Map<String, Object> getAchievementData(Integer formId);
	
	public void mapUsers();

	public void updateUserMap(String username, List<Integer> areaIds);

	public ResponseEntity<String> getThematicViewDownloadExcel(ThematicDashboardDataModel thematicDashboardDataModel,
			HttpServletRequest request);

}