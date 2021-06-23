package org.sdrc.datum19.repository;

import org.sdrc.datum19.document.UserIndicatorSearch;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserIndicatorSearchRepository extends MongoRepository<UserIndicatorSearch, String> {

	UserIndicatorSearch findByUsernameAndIndicatorId(String username, Integer indicatorId);

}
