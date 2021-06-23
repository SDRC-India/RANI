package org.sdrc.rani.repositories;

import java.util.List;

import org.sdrc.usermgmt.mongodb.domain.Account;
import org.sdrc.usermgmt.mongodb.domain.AssignedDesignations;
import org.sdrc.usermgmt.mongodb.repository.AccountRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * @author subham
 *
 */
@Repository("customeAccountRepository")
public interface CustomAccountRepository extends AccountRepository {

	List<Account> findByAssignedDesignationsDesignationIds(String roleId);

	List<Account> findByAssignedDesignationsDesignationIdsIn(List<String> desgIds);

	List<Account> findByAssignedDesignationsDesignationIdsInAndEnabledTrue(List<String> desgIds);

//	@Query("{$and:[{'mappedAreaIds':{$in:?0}},{'userDetails.isIFAuser':?1},{'assignedDesignations':{$in:?2}}]}")
	@Query("{$and:[{'mappedAreaIds':{$in:?0}},{'assignedDesignations':{$in:?2}}]}")
	List<Account> findByAreaIdAndIsIFAuserFalseAndDesignation(List<Integer> list, boolean b,
			List<AssignedDesignations> assignedDesignations);

}
