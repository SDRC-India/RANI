package org.sdrc.datum19.repository;

import java.util.List;

import org.sdrc.datum19.document.IndicatorSuggestionModel;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SuggestionModelRepository extends MongoRepository<IndicatorSuggestionModel, String> {

	List<IndicatorSuggestionModel> findTop5ByUsernameOrderByPredictionAsc(String username);

}
