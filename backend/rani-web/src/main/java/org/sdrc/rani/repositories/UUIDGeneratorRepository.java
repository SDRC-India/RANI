package org.sdrc.rani.repositories;

import org.sdrc.rani.document.UUIdGenerator;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
public interface UUIDGeneratorRepository extends MongoRepository<UUIdGenerator, String> {

	UUIdGenerator findByMonthAndYearAndAccountId(int i, int j, String userId);

	UUIdGenerator findByUuidAndMonthAndYear(String uuidValue, int month, int year);

	UUIdGenerator findByMonthAndYearAndAccountIdAndDesgId(int month, int year, String accId, String roleId);

	UUIdGenerator findByUuidAndMonthAndYearAndDesgId(String uuidValue, int i, int j, String roleId);

}
