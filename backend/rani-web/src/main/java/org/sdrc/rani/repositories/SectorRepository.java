package org.sdrc.rani.repositories;

import java.util.List;

import org.sdrc.rani.document.Sector;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SectorRepository extends MongoRepository<Sector, String> {

	Sector findTopByOrderByIdDesc();

	List<Sector> findAllByOrderByOrderAsc();

}
