package org.sdrc.datum19.repository;

import java.util.List;

import org.sdrc.datum19.document.Sector;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SectorRepository extends MongoRepository<Sector, String> {

	Sector findTopByOrderByIdDesc();
	
	List<Sector> findAllByOrderByOrderAsc();

	Sector findBySectorId(Integer sectorId);

}
