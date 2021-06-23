package org.sdrc.rani.repositories;

import java.util.List;

import org.sdrc.rani.document.DesignationIFAMapping;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author subham
 *
 */
public interface DesignationIFAMappingRepository extends MongoRepository<DesignationIFAMapping, String> {

	List<DesignationIFAMapping> findByDesgId(String id);

	DesignationIFAMapping findByIfaSuppyName(String dependentValue);

	List<DesignationIFAMapping> findByIdIn(List<String> list);

}
