package org.sdrc.rani.repositories;

import java.util.List;
import java.util.Set;

import org.sdrc.rani.document.DesignationFormMapping;
import org.springframework.data.mongodb.repository.MongoRepository;

import in.co.sdrc.sdrcdatacollector.models.AccessType;
import in.co.sdrc.sdrcdatacollector.util.Status;

/**
 * @author Subham Ashish
 *
 */
public interface DesignationFormMappingRepository extends MongoRepository<DesignationFormMapping, String> {

	List<DesignationFormMapping> findByDesignationIdAndAccessType(String string, AccessType review);

	List<DesignationFormMapping> findByDesignationIdInAndAccessTypeAndStatus(Set<String> roleId,
			AccessType downloadRawData, Status active);

	DesignationFormMapping findByDesignationCodeAndFormFormIdAndAccessTypeAndStatus(String roleId, Integer formId,
			AccessType dataEntry, Status active);

	DesignationFormMapping findByDesignationCodeAndFormFormIdAndAccessType(String roleId, Integer formId,
			AccessType dataEntry);

	List<DesignationFormMapping> findByDesignationIdInAndAccessTypeAndStatusOrderByFormFormIdAsc(Set<String> roleIds,
			AccessType downloadRawData, Status active);

	List<DesignationFormMapping> findByDesignationIdAndAccessTypeOrderByFormFormId(String roleId, AccessType dataEntry);

}
