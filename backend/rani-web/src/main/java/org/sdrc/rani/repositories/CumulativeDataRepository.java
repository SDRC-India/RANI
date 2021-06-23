package org.sdrc.rani.repositories;

import java.util.List;

import org.sdrc.rani.document.CumulativeData;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CumulativeDataRepository extends MongoRepository<CumulativeData, String>{

	List<CumulativeData> findByDatumIdAndInidIn(Integer areaId,List<Integer> indicatorIds);

}
