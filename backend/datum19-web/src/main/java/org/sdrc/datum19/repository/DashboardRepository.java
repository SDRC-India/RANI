package org.sdrc.datum19.repository;

import java.util.List;

import org.sdrc.datum19.document.Dashboard;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DashboardRepository extends MongoRepository<Dashboard, String> {

	List<Dashboard> findByUsername(String username);

}
