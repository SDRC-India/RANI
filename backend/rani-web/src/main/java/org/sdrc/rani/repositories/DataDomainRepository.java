package org.sdrc.rani.repositories;

import java.util.List;

import org.sdrc.rani.document.DataValue;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DataDomainRepository extends MongoRepository<DataValue, String> {

	List<DataValue> findByDatumIdAndTpIn(Integer blockId, List<Integer> asList);

	List<DataValue> findByDatumIdAndInidAndTpIn(Integer blockId, Integer indicatorId, List<Integer> asList);

	List<DataValue> findTop4ByDatumIdAndInidOrderByTpDesc(Integer areaId, Integer indicatorId);

	List<DataValue> findTop12ByDatumIdAndInidInOrderByTpDesc(Integer areaId, List<Integer> indicatorIds);
	
	List<DataValue> findByDatumIdAndTpAndInidIn(Integer areaId, Integer tp, List<Integer> indicatorId);
	
	List<DataValue> findByTp(Integer tp);

	List<DataValue> findTop12ByDatumtypeAndInidInOrderByTpDesc(String string, List<Integer> trendGroupIndicatorIds);

	List<DataValue> findByTpAndInidIn(Integer tpId, List<Integer> indicatorIds);

	List<DataValue> findByDatumIdInAndTpAndInidIn(List<Integer> areaIds, Integer tpId, List<Integer> asList);

	List<DataValue> findByDatumIdInAndTpAndInid(List<Integer> areaIds, Integer tpId, Integer indicatorId);

	List<DataValue> findByDatumIdAndTpAndInidIs(Integer areaId, Integer tpId, Integer indicatorId);

	List<DataValue> findByDatumIdIsAndTpAndInidIn(Integer areaId, Integer tpId, List<Integer> indicatorIds);

	List<DataValue> findByDatumIdInAndTpAndInidIs(List<Integer> areaId, Integer tpId, Integer indicatorId);
	
}