package org.sdrc.rani.repositories;

import org.sdrc.rani.document.Area;
import org.sdrc.rani.document.ClusterMapping;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author subham
 *
 */
public interface ClusterMappingRepository extends MongoRepository<ClusterMapping, String> {

	ClusterMapping findByVillage(Area area);

}
