package org.sdrc.rani.repositories;

import java.util.List;

import org.sdrc.rani.document.CoverageData;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CoverageDataRepository  extends MongoRepository<CoverageData, String> {

	List<CoverageData> findByAreaIdAndInidIn(Integer areaId, List<Integer> indicatorIds);
}
