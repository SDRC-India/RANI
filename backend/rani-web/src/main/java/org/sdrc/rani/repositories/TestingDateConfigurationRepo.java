package org.sdrc.rani.repositories;

import org.sdrc.rani.document.TestingDateConfiguration;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author subham
 *
 */
public interface TestingDateConfigurationRepo extends MongoRepository<TestingDateConfiguration, String> {

	TestingDateConfiguration findAllBySlugId(int i);

}
