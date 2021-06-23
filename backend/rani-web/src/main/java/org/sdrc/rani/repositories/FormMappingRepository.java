package org.sdrc.rani.repositories;

import org.sdrc.rani.document.FormMapping;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author subham
 *
 */
public interface FormMappingRepository extends MongoRepository<FormMapping, String> {

	FormMapping findByCfFormId(Integer formId);

	FormMapping findBySupervisorFormId(Integer formId);

}
