package org.sdrc.datum19.repository;

import org.sdrc.datum19.document.EnginesForm;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface EnginesFormRepository  extends MongoRepository<EnginesForm, String> {

	EnginesForm findByFormId(Integer formId);

}