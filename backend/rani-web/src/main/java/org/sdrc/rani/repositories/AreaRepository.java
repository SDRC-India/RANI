package org.sdrc.rani.repositories;

import java.util.List;

import org.sdrc.rani.document.Area;
import org.sdrc.rani.document.AreaLevel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 */
@Repository
public interface AreaRepository extends MongoRepository<Area, String> {

	Area findByAreaNameAndAreaLevel(String areaName, AreaLevel areaLevel);

	List<Area> findByParentAreaIdOrderByAreaName(Integer parentAreaId);

	List<Area> findByAreaIdOrderByAreaName(Integer parentAreaId);

	List<Area> findByAreaLevel(AreaLevel areaLevel);

	@Query(value = "{}", fields = "{areaId : 1, areaName : 1}")
	List<Area> findAreaIdAndAreaName();

	List<Area> findByAreaLevelAreaLevelIdAndBlockId(int i, Integer areaId);

	List<Area> findByAreaLevelAreaLevelIdAndDistrictId(int i, Integer areaId);

	Area findByAreaCode(String parentAreaId);

	Area findByAreaNameAndAreaLevelAreaLevelIdAndParentAreaId(String trim, int i, Integer areaId);

	Area findByAreaId(Integer areaId);

	List<Area> findByAreaLevelAreaLevelIdAndAreaId(int i, Integer areaId);

	List<Area> findByAreaLevelAreaLevelIdAndVillageId(int i, Integer areaId);

	List<Area> findByAreaIdIn(List<Integer> mappedAreaIds);

	List<Area> findAllByAreaLevelAreaLevelIdInOrderByAreaIdAsc(List<Integer> asList);

	List<Area> findByAreaLevelAndIdOrderByAreaName(Integer areaLevelVillage);

}
