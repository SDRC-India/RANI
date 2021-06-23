package org.sdrc.datum19.repository;

import java.util.List;

import org.sdrc.datum19.document.DataSearch;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface DataSearchRepository extends MongoRepository<DataSearch, String> {

	@Query("{indicator: { $regex: ?0}}, {}")
	List<DataSearch> findDataSearchByRegex(String string);

	List<DataSearch> findByInidAndDatumId(Integer i, Integer datumId);

	List<DataSearch> findTop10ByInidAndTp(Integer i, Integer tp);

	List<DataSearch> findTop10ByInidAndTpOrderByDataValueAsc(String indicatorId, Integer tp);

	List<DataSearch> findTop5ByInidInAndDatumIdAndTp(List<Integer> relatedIndiactors, Integer areaId, Integer tp);

	List<DataSearch> findTop10ByInidAndTpAndDatumIdIn(int indicatorId, Integer tp, List<Integer> areaIds);

	List<DataSearch> findTop5ByInidAndTp(String string, Integer tp);

	List<DataSearch> findTop5ByInidAndTpAndDatumIdInOrderByDataValueAsc(Integer string, Integer tp, List<Integer> areaIds);

	List<DataSearch> findTop5ByInidAndTpAndDatumIdInOrderByDataValueDesc(Integer string, Integer tp, List<Integer> areaIds);

}
