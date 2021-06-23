package org.sdrc.datum19.repository;

import java.util.List;

import org.sdrc.datum19.document.DashboardIndicator;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DashboardIndicatorRepository extends MongoRepository<DashboardIndicator, String> {

	DashboardIndicator findTop1ByOrderByCreatedDateDesc();

	List<DashboardIndicator> findAllByDashboardIdOrderByCreatedDateDesc(String dashboardId);

	List<DashboardIndicator> findByDashboardIdAndSectorIn(String dashboardId, List<String> asList);

	DashboardIndicator findByDashboardIdAndChartGroup(String dashboardId, String groupName);

	DashboardIndicator findByIndicatorGroup(String indicatorGroup);

	List<DashboardIndicator> findByDashboardIdAndSectorIgnoreCase(String dashboardId, String sectorName);

	DashboardIndicator findByDashboardIdAndIndicatorGroup(String dashboardId, String groupName);

}
