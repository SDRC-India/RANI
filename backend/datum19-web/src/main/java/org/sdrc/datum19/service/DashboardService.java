package org.sdrc.datum19.service;

import java.util.List;
import java.util.Map;

import org.sdrc.datum19.document.Dashboard;
import org.sdrc.datum19.document.Heading;
import org.sdrc.datum19.document.ThematicFileData;
import org.sdrc.datum19.document.TimePeriod;
import org.sdrc.datum19.model.DataValueModel;
import org.sdrc.datum19.model.SectorModel;


public interface DashboardService {

	List<SectorModel> getDashboardData(String sectorName, String groupName, String dashboardId, List<Integer> areaList, Integer tp);

	List<SectorModel> getDashboardGroupData(String groupName, String dashboardId);
	
	Dashboard addDashboard(Dashboard dashboard);

	List<SectorModel> getThematic(String sectorName, String groupName);

	List<Dashboard> getDashboards(String username);

	Map<String, List<DataValueModel>> gethematictData(String indecatorId, String tp, String areaCode, String indicatorGroup);

	String saveThematicFileData(ThematicFileData thematicFileData);

	Boolean getThematicMapValidation(String parentAreaCode, List<String> childAreaCodes);
	
	List<TimePeriod> getListOfTimePeriod(String periodicity);
	
	Map<String, List<DataValueModel>> getGisData(String indecatorId, String tp, String areaCode, String indicatorGroup);

	Long saveHeaderAndSubheader(String title);

	List<Heading> searchHeading(String title);

	List<String> getHeadingByDashboard(String dashboardId);

}