package org.sdrc.rani.repositories;

import java.util.List;

import org.sdrc.rani.document.GroupIndicator;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GroupIndicatorRepository extends MongoRepository<GroupIndicator, String>{

	List<GroupIndicator> findBySector(String sector);
	
	List<GroupIndicator> findBySectorIn(List<String> sectorIds);
}
