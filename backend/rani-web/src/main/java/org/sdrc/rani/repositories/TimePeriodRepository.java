package org.sdrc.rani.repositories;

import java.util.Date;
import java.util.List;

import org.sdrc.rani.document.TimePeriod;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author subham
 *
 */
public interface TimePeriodRepository extends MongoRepository<TimePeriod, String> {

	
	@Query("{'startDate':{$lte:?0},'endDate':{$gte:?0},periodicity:?1}")
	TimePeriod getCurrentTimePeriod(@Param("createdDate") Date createdDate,@Param("periodicity") String periodicity);

	List<TimePeriod> findAllByPeriodicity(String property);

	List<TimePeriod> findAllByPeriodicityOrderByCreatedDateAsc(String property);

	List<TimePeriod> findAllByOrderByIdDesc();

	List<TimePeriod> findTop6ByPeriodicityOrderByTimePeriodIdDesc(String string);
	
	@Query("{'periodicity':?0, $and:[{'timePeriodId':{$gte:?1}}, {'timePeriodId':{$lte:?2}}]}")
	List<TimePeriod> findTimePeriodRange(String periodicity,Integer startTp, Integer endTp);
	
	TimePeriod findByTimePeriodId(Integer timePeriodId);
	
	List<TimePeriod> findTop8ByPeriodicityOrderByTimePeriodIdAsc(String periodicity);
	
	TimePeriod findTop1ByPeriodicityOrderByTimePeriodIdDesc(String string);
	
	@Query("{'startDate':{'$eq':{'$date': :#{#sd}}},'endDate':{'$eq':{'$date': :#{#ed}}}}")
	public TimePeriod getTimePeriod(@Param("sd")String sd, @Param("ed")String ed);

	List<TimePeriod> findTop6ByPeriodicityAndTimePeriodIdLessThanEqualOrderByTimePeriodIdDesc(String string, Integer tpId);
}
