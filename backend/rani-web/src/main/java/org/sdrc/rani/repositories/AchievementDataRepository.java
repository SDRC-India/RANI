package org.sdrc.rani.repositories;

import java.util.List;

import org.sdrc.rani.document.AchievementData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface AchievementDataRepository extends MongoRepository<AchievementData, String> {

	@Query("{'dataValue.formId' : ?0}")
	List<AchievementData> getAchievementData(Integer formId);
	
}
