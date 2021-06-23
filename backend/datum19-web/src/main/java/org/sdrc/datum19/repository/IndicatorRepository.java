package org.sdrc.datum19.repository;

import java.util.List;

import org.sdrc.datum19.document.Indicator;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface IndicatorRepository extends MongoRepository<Indicator, String> {
	@Query("{'indicatorDataMap.periodicity':?0}")
	List<Indicator> getIndicatorByPeriodicity(String periodcity);

	@Query("{'indicatorDataMap.periodicity':?0, 'indicatorDataMap.parentType':?1}")
	List<Indicator> getPercentageIndicators(String periodicity, String indicatorType);
	
	Indicator findTopByOrderByIdDesc();
	
	@Query("{'indicatorDataMap.unit':'number'}")
	List<Indicator> getNumberIndicators();

	@Query("{'indicatorDataMap.sector' :{$in:?0 }}")
	List<Indicator> getIndicatorBySectors(List<String> sectors);

	@Query("{'indicatorDataMap.formId':?0, 'indicatorDataMap.sector':?1, 'indicatorDataMap.subsector':?2}")
	List<Indicator> getByFormIdSectorSubsector(String formId, String sector, String subsector);

	@Query(value = "{'indicatorDataMap.source' : {$ne : 'secondary'}}", sort = "{'indicatorDataMap.indicatorNid' : -1}")
	List<Indicator> getIndicatorsBySourceIsNull();
	
	@Query("{'indicatorDataMap.indicatorNid':?0 }")
	Indicator getIndicatorsByDatumId(String indecatorId);

	@Query("{'indicatorDataMap.indicatorGid':?0, 'indicatorDataMap.subgroupType':?1}")
	List<Indicator> findAllByIndGidAndSubGroupType(String indicatorGid, String subgroupType);

	@Query("{'indicatorDataMap.subgroupType':?0}")
	List<Indicator> findAllBySubGroupType(String subgroupType);
	// @Query("{'indicatorDataMap.indicatorName':?0}")
	// Indicator getIndicatorByRegex(String searchText);

	@Query("{'indicatorDataMap.indicatorNid':?0 }")
	Indicator findByIndNid(String indicatorId);
	@Query("{'indicatorDataMap.indicatorGid':?0, 'indicatorDataMap.subgroupType':?1, 'indicatorDataMap.unit':?2}")
	List<Indicator> findAllByIndGidAndSubGroupTypeAndUnit(String gid, String subgroup, String unit);

	@Query("{'indicatorDataMap.indicatorGid':?0 }")
	List<Indicator> getGroupIndicators(String indicatorGid);

	@Query("{'indicatorDataMap.indicatorNid' :{$in:?0 }}")
	List<Indicator> findByIndNidIn(List<String> indicatorIds);
	

}
