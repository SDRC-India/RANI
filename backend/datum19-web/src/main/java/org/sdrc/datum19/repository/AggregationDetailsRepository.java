package org.sdrc.datum19.repository;

import org.sdrc.datum19.document.AggregationDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AggregationDetailsRepository extends MongoRepository<AggregationDetails, String> {

}
