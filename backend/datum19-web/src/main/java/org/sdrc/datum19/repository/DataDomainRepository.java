package org.sdrc.datum19.repository;

import java.util.List;

import org.sdrc.datum19.document.DataValue;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DataDomainRepository extends MongoRepository<DataValue, String> {

	List<DataValue> findByDatumIdAndTpIn(Integer blockId, List<Integer> asList);

	List<DataValue> findByDatumIdAndInidAndTpIn(Integer blockId, Integer indicatorId, List<Integer> asList);

	List<DataValue> findTop4ByDatumIdAndInidOrderByTpDesc(Integer areaId, Integer indicatorId);

	List<DataValue> findByInidAndDatumId(Integer indicatorId, Integer datumId);

	List<DataValue> findByDatumIdAndTpInAndDatumId(int parseInt, List<Integer> asList, Integer areaId);

	List<DataValue> findByInidAndTpInAndDatumIdIn(int parseInt, List<Integer> asList, List<Integer> listOfAreaIds);

	List<DataValue> findByInidInAndTpInAndDatumIdInAndDatumtype(List<Integer> indicatorIds, List<Integer> asList,
			List<Integer> areaListAvailable, String areaType);

	List<DataValue> findByInidInAndTpIn(List<Integer> indicatorIds, List<Integer> asList);

	List<DataValue> findByInidIn(List<Integer> indicatorIds);
}
