package org.sdrc.rani.repositories;

import java.util.List;

import org.sdrc.rani.document.PlanningData;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author subham
 *
 */
public interface PlanningDataRepository extends MongoRepository<PlanningData, String> {

	PlanningData findByFormIdAndMonthAndYearAndDesgIdAndUserName(Integer formId, int i, Integer year, String id,
			String userName);

	PlanningData findById(String planId);

	List<PlanningData> findByMonthAndYearAndDesgIdAndAccId(Integer month, Integer year, String roleId, String accId);

	List<PlanningData> findByMonthAndYearAndDesgId(Integer month, Integer year, String roleId);

}
