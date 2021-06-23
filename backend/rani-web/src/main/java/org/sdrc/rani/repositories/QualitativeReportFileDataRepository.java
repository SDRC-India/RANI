package org.sdrc.rani.repositories;

import java.util.List;

import org.sdrc.rani.document.QualitativeReportFileData;
import org.sdrc.rani.document.TimePeriod;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author subham
 *
 */
public interface QualitativeReportFileDataRepository extends MongoRepository<QualitativeReportFileData, String>{

	List<QualitativeReportFileData> findAllByOrderByCreatedDateDesc();

	QualitativeReportFileData findById(String id);

	QualitativeReportFileData findByTimePeriod(TimePeriod currentTimePeriod);

}
