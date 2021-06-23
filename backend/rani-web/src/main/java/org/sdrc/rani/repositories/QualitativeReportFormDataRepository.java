package org.sdrc.rani.repositories;

import java.util.List;

import org.sdrc.rani.document.QualitativeReportFormData;
import org.sdrc.rani.document.TimePeriod;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
public interface QualitativeReportFormDataRepository extends MongoRepository<QualitativeReportFormData, String> {

	QualitativeReportFormData findByTimePeriodAndUserId(TimePeriod timePeriod, String userId);

	List<QualitativeReportFormData> findAllByOrderByCreatedDateDesc();

	List<QualitativeReportFormData> findAllByUserNameOrderByCreatedDateDesc(String name);

	QualitativeReportFormData findById(String id);

}
