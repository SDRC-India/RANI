package org.sdrc.rani.repositories;

import java.util.Date;
import java.util.List;

import org.sdrc.rani.document.CFInputFormData;
import org.sdrc.rani.document.TimePeriod;
import org.sdrc.rani.models.SubmissionStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

/**
 * @author subham
 *
 */
public interface CFInputFormDataRepository extends MongoRepository<CFInputFormData, String> {

	CFInputFormData findById(String submissionId);

	List<CFInputFormData> findAllByRejectedFalseAndIsValidTrueAndSyncDateBetween(Date startDate, Date endDate);

	List<CFInputFormData> findByIdIn(List<String> rejectionList);

	CFInputFormData findByIdAndFormId(String submissionId, Integer formId);

	List<CFInputFormData> findByIdInAndFormId(List<String> submissionIds, Integer formId);

	@Query("{ $and: [ {'data.F1Q3' : ?0},{'data.F1Q7' : ?1},{'timePeriod' : ?2},{'formId' : ?3}]}")
	List<CFInputFormData> findByVillageComponentNameTimePeriodAndFormId(Integer village, Integer componentName,
			TimePeriod timePeriod, int formId);

	List<CFInputFormData> findAllBySubmissionCompleteStatusAndFormIdAndSyncDateBetween(SubmissionStatus c,
			Integer formId, Date startDate, Date endDate);

	@Query("{$and:[{'data.F7Q3':?0},{'data.F7QDN1':?1},{'data.F7Q7':?2},{'formId':?3}]}")
	List<CFInputFormData> getT4DataByVillageDateOfVisitComponentNameAndFormId(Integer villageId, String dateOfVisit,
			Integer slugId, int formId);

	@Query("{$and:[{'data.F8Q3':?0},{'data.F8QDN1':?1},{'data.F8Q7':?2},{'formId':?3}]}")
	List<CFInputFormData> getCEMdataByVillageDateOfVisitComponentNameAndFormId(Integer villageId, String dateOfVisit,
			Integer slugId, int formId);

	@Query("{$and:[{'data.F9Q3':?0},{'data.F9QDN1':?1},{'formId':?2}]}")
	List<CFInputFormData> getMediaDataByVillageDateOfVisitAndFormId(Integer villageId, String dateOfVisit, int formId);

	@Query("{$and:[{'data.F10Q3':?0},{'data.F10QDN1':?1},{'formId':?2}]}")
	List<CFInputFormData> getHemocueDataByVillageDateOfVisitAndFormId(Integer villageId, String dateOfVisit,
			int formId);

	@Query("{$and:[{'data.F11Q3':?0},{'data.F11QDN1':?1},{'formId':?2}]}")
	List<CFInputFormData> getSMSdataByVillageDateOfVisitAndFormId(Integer villageId, String dateOfVisit, int formId);

	List<CFInputFormData> findAllByFormIdAndAndIsDeletedFalseAndSyncDateBetween(Integer formId, Date startDate, Date endDate);

	List<CFInputFormData> findByFormIdAndUniqueIdAndRejectedFalse(Integer formId, String uniqueId);

	@Query("{ $and: [ {'data.F1Q3' : ?0},{'data.F1Q7' : ?1},{'formId' : ?2}]}")
	List<CFInputFormData> findByVillageComponentNameAndFormId(Integer village, Integer componentName,int formId);


}
