package org.sdrc.rani.repositories;

import java.util.List;

import org.sdrc.rani.document.AreaLevel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author subham
 *
 */
@Repository
public interface AreaLevelRepository extends MongoRepository<AreaLevel, String> {

	AreaLevel findByAreaLevelId(Integer areaLevelId);

	AreaLevel findByAreaLevelName(String string);

	 List<AreaLevel> findByAreaLevelIdIn(List<Integer> asList);

}
