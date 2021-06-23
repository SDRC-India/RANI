package org.sdrc.datum19.repository;

import java.util.List;

import org.sdrc.datum19.document.RelatedIndicators;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RelatedIndicatorsRepository extends MongoRepository<RelatedIndicators, String> {

	List<RelatedIndicators> findTop10ByGroupId(Integer groupId);

	RelatedIndicators findTop1ByIndicatorId(Integer indicatorId);

	List<RelatedIndicators> findTop5ByGroupId(Integer groupId);

}
