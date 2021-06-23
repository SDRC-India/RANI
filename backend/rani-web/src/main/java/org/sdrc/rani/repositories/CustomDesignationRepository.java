package org.sdrc.rani.repositories;

import java.util.List;

import org.sdrc.usermgmt.mongodb.domain.Designation;
import org.sdrc.usermgmt.mongodb.repository.DesignationRepository;
import org.springframework.stereotype.Component;

/**
 * @author subham
 *
 */
@Component("customMongoDesignationRepository")
public interface CustomDesignationRepository extends DesignationRepository {

	Designation findByName(String desgName);

	List<Designation> findByCodeIn(List<String> asList);

}
