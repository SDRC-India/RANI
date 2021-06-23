package org.sdrc.rani.repositories;

import java.util.List;

import org.sdrc.rani.document.UserAreaMap;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserAreaMapRepository extends MongoRepository<UserAreaMap, String> {
	public List<UserAreaMap> findByUsernameAndStatusAndTp(String username, String status, Integer tp);
}
