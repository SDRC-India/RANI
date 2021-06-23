package org.sdrc.datum19.repository;

import org.sdrc.datum19.document.CumulativeData;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CumulativeDataRepository extends MongoRepository<CumulativeData, String>{

}
