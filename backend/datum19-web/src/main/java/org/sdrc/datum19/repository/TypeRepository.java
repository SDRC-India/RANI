package org.sdrc.datum19.repository;

import org.sdrc.datum19.document.Type;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TypeRepository extends MongoRepository<Type, String> {

}
