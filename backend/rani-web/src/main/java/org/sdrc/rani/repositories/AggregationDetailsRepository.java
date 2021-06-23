package org.sdrc.rani.repositories;

import org.sdrc.rani.document.AggregationDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Subham Ashish
 *
 */
public interface AggregationDetailsRepository extends MongoRepository<AggregationDetails, String>{

}
