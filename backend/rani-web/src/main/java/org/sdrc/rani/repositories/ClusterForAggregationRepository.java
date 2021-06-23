package org.sdrc.rani.repositories;

import java.util.List;

import org.sdrc.rani.document.ClusterForAggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ClusterForAggregationRepository  extends MongoRepository<ClusterForAggregation, String> {

	List<ClusterForAggregation> findByDistrict(Integer areaId);

	List<ClusterForAggregation> findByBlock(Integer areaId);

	List<ClusterForAggregation> findByClusterNumber(Integer areaId);

}
