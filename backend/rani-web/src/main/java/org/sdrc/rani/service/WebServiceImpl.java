package org.sdrc.rani.service;

import java.io.File;
import java.io.FileOutputStream;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang.time.DateUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.sdrc.rani.document.Area;
import org.sdrc.rani.document.AreaLevel;
import org.sdrc.rani.document.CFInputFormData;
import org.sdrc.rani.document.ClusterMapping;
import org.sdrc.rani.document.DesignationFormMapping;
import org.sdrc.rani.document.DesignationIFAMapping;
import org.sdrc.rani.document.FormMapping;
import org.sdrc.rani.document.IFAQuestionMapper;
import org.sdrc.rani.document.IFASupplyPointMapping;
import org.sdrc.rani.document.TimePeriod;
import org.sdrc.rani.document.UserDetails;
import org.sdrc.rani.models.AreaModel;
import org.sdrc.rani.models.DateModel;
import org.sdrc.rani.models.FormModel;
import org.sdrc.rani.models.IFASupplyPointType;
import org.sdrc.rani.models.IFAValueModel;
import org.sdrc.rani.models.QuickStartModel;
import org.sdrc.rani.models.UserDetailsModel;
import org.sdrc.rani.models.UserModel;
import org.sdrc.rani.repositories.AreaLevelRepository;
import org.sdrc.rani.repositories.AreaRepository;
import org.sdrc.rani.repositories.ClusterMappingRepository;
import org.sdrc.rani.repositories.CustomAccountRepository;
import org.sdrc.rani.repositories.CustomDesignationRepository;
import org.sdrc.rani.repositories.DesignationFormMappingRepository;
import org.sdrc.rani.repositories.DesignationIFAMappingRepository;
import org.sdrc.rani.repositories.FormMappingRepository;
import org.sdrc.rani.repositories.IFAQuestionMapperRepository;
import org.sdrc.rani.repositories.IFASupplyPointMappingRepository;
import org.sdrc.rani.util.TokenInfoExtracter;
import org.sdrc.usermgmt.mongodb.domain.Account;
import org.sdrc.usermgmt.mongodb.domain.AssignedDesignations;
import org.sdrc.usermgmt.mongodb.domain.Designation;
import org.sdrc.usermgmt.mongodb.repository.DesignationRepository;
import org.sdrc.usermgmt.service.MongoUserManagementServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.AggregationOptions;
import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import in.co.sdrc.sdrcdatacollector.document.EnginesForm;
import in.co.sdrc.sdrcdatacollector.document.TypeDetail;
import in.co.sdrc.sdrcdatacollector.models.AccessType;
import in.co.sdrc.sdrcdatacollector.models.DataObject;
import in.co.sdrc.sdrcdatacollector.models.ReviewPageModel;
import in.co.sdrc.sdrcdatacollector.mongorepositories.TypeDetailRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
@Service
@Slf4j
public class WebServiceImpl implements WebService {

	@Autowired
	private AreaRepository areaRepository;

	@Autowired
	@Qualifier("customeAccountRepository")
	private CustomAccountRepository accountRepository;

	@Autowired
	private AreaLevelRepository areaLevelRepository;

	@Qualifier("mongoDesignationRepository")
	@Autowired
	private DesignationRepository designationRepository;

	@Autowired
	private TokenInfoExtracter tokenInfoExtracter;

	@Autowired
	private DesignationFormMappingRepository designationFormMappingRepository;

	@Autowired
	private SubmissionServiceImpl submissionServiceImpl;

	@Autowired
	private FormMappingRepository formMappingRepository;

	@Autowired
	private IFASupplyPointMappingRepository ifaSupplyPointMappingRepository;

	@Autowired
	private IFAQuestionMapperRepository ifaQuestionMapperRepository;

	@Qualifier("customMongoDesignationRepository")
	@Autowired
	private CustomDesignationRepository customDesignationRepository;

	@Autowired
	private DesignationIFAMappingRepository designationIFAMappingRepository;

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private MongoUserManagementServiceImpl mongoUserManagementServiceImpl;
	
	@Autowired
	private TypeDetailRepository typeDetailRepository;

	@Autowired
	private ConfigurableEnvironment configurableEnvironment;
	
	@Autowired
	private ClusterMappingRepository clusterMappingRepository;
	
	@Override
	public Map<String, List<AreaModel>> getAllAreaList() {

		List<Area> areas = areaRepository.findAllByAreaLevelAreaLevelIdInOrderByAreaIdAsc(Arrays.asList(2, 3, 4, 5));

		List<AreaModel> areaModelList = new ArrayList<>();
		Map<String, List<AreaModel>> areaMap = new LinkedHashMap<>();

		// setting areas is area-model list
		for (Area area : areas) {

			AreaModel areaModel = new AreaModel();

			areaModel.setAreaCode(area.getAreaCode());
			areaModel.setAreaId(area.getAreaId());
			areaModel.setAreaLevel(area.getAreaLevel().getAreaLevelName());
			areaModel.setAreaName(area.getAreaName());
			areaModel.setParentAreaId(area.getParentAreaId());
			areaModelList.add(areaModel);

		}

		// making levelName as a key
		for (AreaModel areaModel : areaModelList) {

			if (areaMap.containsKey(areaModel.getAreaLevel())) {
				areaMap.get(areaModel.getAreaLevel()).add(areaModel);
			} else {
				areaModelList = new ArrayList<>();
				areaModelList.add(areaModel);
				areaMap.put(areaModel.getAreaLevel(), areaModelList);
			}
		}

		return areaMap;
	}
	
	@Override
	public Map<String, List<AreaModel>> getAllClusterArea() {
		List<ClusterMapping> clusterMappings = clusterMappingRepository.findAll();
		
		Map<String, List<AreaModel>> areas = clusterMappings.stream().map(v -> {
			return setAreaModel(v);
		}).sorted((o1, o2) -> o1.getAreaName().compareTo(o2.getAreaName()))
				.collect(Collectors.groupingBy(AreaModel::getClusterName, TreeMap::new, Collectors.toList()));

		areas.put("All", clusterMappings.stream().map(v -> {
			return setAreaModel(v);
		}).sorted((o1, o2) -> o1.getAreaName().compareTo(o2.getAreaName()))
				.collect(Collectors.toCollection(LinkedList::new)));

		return areas;
	}

	private AreaModel setAreaModel(ClusterMapping v) {

		AreaModel areaModel = new AreaModel();
		areaModel.setAreaCode(v.getVillage().getAreaCode());
		areaModel.setAreaId(v.getVillage().getAreaId());
		areaModel.setAreaLevel(v.getVillage().getAreaLevel().getAreaLevelName());
		areaModel.setAreaName(v.getVillage().getAreaName());
		areaModel.setParentAreaId(v.getVillage().getBlockId());
		areaModel.setClusterNumber(v.getClusterNumber());
		areaModel.setClusterName("Cluster " + String.valueOf(v.getClusterNumber()));

		return areaModel;
	}

	@Override
	public List<UserDetailsModel> getAllUsers(String roleId, List<Integer> areaId, String userName) {

		List<Account> accList = null;

		accList = accountRepository.findAll();
		accList = accList.stream().filter(c -> !"admin".equals(c.getUserName())).collect(Collectors.toList());

		List<UserDetailsModel> modelList = new ArrayList<>();

		// Area area = areaRepository.findByAreaId(areaId.get(0));

		if (accList.isEmpty() || accList == null) {
			throw new UsernameNotFoundException("User Not Found");
		}

		for (Account v : accList){
			
			UserDetailsModel model = new UserDetailsModel();
			UserDetails userDetails = (UserDetails)v.getUserDetails();
			
			model.setSiNo(modelList.size() + 1);
			model.setName(((UserDetails) v.getUserDetails()).getFullName());
			model.setUserName(v.getUserName());

			List<AssignedDesignations> assignedDesignations = v.getAssignedDesignations();
			assignedDesignations.stream().filter(d -> !"false".equals(d.getEnable())).collect(Collectors.toList());

			List<String> roles = new ArrayList<>();
			List<String> roleNames = new ArrayList<String>();

			if (!assignedDesignations.isEmpty()) {

				assignedDesignations.forEach(role -> {
					roles.add(role.getDesignationIds());
					roleNames.add(designationRepository.findById(role.getDesignationIds()).getName());
				});

			}
			model.setRoleId(roles);
			model.setRoleNames(roleNames);

			// get area name
			List<Area> area = areaRepository.findByAreaIdIn(v.getMappedAreaIds());

			List<String> areasNames = new ArrayList<>();
			area.forEach(ar -> {
				areasNames.add(ar.getAreaName());
			});
			model.setAreaName(areasNames);
			model.setAreaId(v.getMappedAreaIds());
			model.setEnable(v.isEnabled());
			model.setMobileNumber(((UserDetails) v.getUserDetails()).getMobileNumber());
			model.setUserId(v.getId());

			model.setAreaLevelId(area.get(0).getAreaLevel().getAreaLevelId());

			
			/**
			 * if role is community facilitator
			 */
			if(designationRepository.findById(model.getRoleId().get(0)).getCode().equals("003")){
			
				//use key F1,F2,F3,F4
				model.setF1(userDetails.getIfaSupplyPoints());
				model.setF2(userDetails.getAwcs());
				model.setF3(userDetails.getVhdnImmunizationPoints());
				model.setF4(userDetails.getSubCenters());
				
			}
			
			/**
			 * if role is supervisor
			 */
			if(designationRepository.findById(model.getRoleId().get(0)).getCode().equals("002")){
			
				//USE KEY F1,F2,F3,F4
				model.setF5(userDetails.getIfaSupplyPoints());
				model.setF6(userDetails.getPhcs());
				model.setF7(userDetails.getChcs());
				model.setF8(userDetails.getSdhs());
				
			}
			
			modelList.add(model);

		}

		return modelList;
	}

	@Override
	public List<AreaLevel> getAreaLevels() {

		return areaLevelRepository.findByAreaLevelIdIn(Arrays.asList(3, 4, 5));
	}

	/**
	 * get all the form associted to logged in user role for review
	 */
	@Override
	public List<FormModel> getAllForms(OAuth2Authentication auth) {

		UserModel user = tokenInfoExtracter.getUserModelInfo(auth);

		Set<String> roleIds = user.getRoleIds();

		List<FormModel> formModelList = new ArrayList<>();

		List<DesignationFormMapping> desigFormMappings = designationFormMappingRepository
				.findByDesignationIdAndAccessType(roleIds.stream().findFirst().get(), AccessType.REVIEW);

		for (DesignationFormMapping erfm : desigFormMappings) {

			FormModel model = new FormModel();
			EnginesForm form = erfm.getForm();
			model.setFormId(form.getFormId());
			model.setFormName(form.getName());
			model.setId(form.getId());
			formModelList.add(model);
		}
		return formModelList;
	}

	/**
	 * get time period value with dateOfvist date of every submission, also get
	 * current time period if both matches than data belongs to the current
	 * month and rejection button would be disabled.
	 * 
	 * if timeperiod doesnt match and current date is in between 4th-of current
	 * month to 10th of current month than rejection button would be enabled
	 */
	@Override
	public ReviewPageModel getReviewData(ReviewPageModel model) {

		Map<Integer, List<DataObject>> reviewDataMap = model.getReviewDataMap();

		Integer formId = null;
		Date currentDate = new Date();

		for (Map.Entry<Integer, List<DataObject>> map : reviewDataMap.entrySet()) {
			formId = map.getKey();
		}
		List<DataObject> dataList = null;

		if (formId != null) {
			try {

				switch (formId) {

				case 1:

					dataList = reviewDataMap.get(1);

					// filter all the rejected data from the dataList
					dataList = dataList.stream().filter(data -> data.getRejected() == false)
							.collect(Collectors.toList());

					for (DataObject data : dataList) {

						Map<String, Object> extraKeys = data.getExtraKeys();
						Date dateOfVisit = new SimpleDateFormat("dd-MM-yyyy")
								.parse(((String) extraKeys.get("dateOfVisit")));
						TimePeriod dateOfVistiTimePeriod = submissionServiceImpl.fetchTimePeriod(dateOfVisit, formId);

						TimePeriod currentTimePeriod = submissionServiceImpl.fetchTimePeriod(currentDate, formId);

						if (dateOfVistiTimePeriod.equals(currentTimePeriod)) {
							extraKeys.put("isRejectable", false);
						} else {
							/**
							 * timeperiods are not equal than check the current
							 * date is in between 4 to 10th (both inclusive)
							 */
							DateModel rejectionDates = getRejectionDates(currentDate);
							if ((DateUtils.isSameDay(currentDate, rejectionDates.getStartDate())
									|| (currentDate.after(rejectionDates.getStartDate()))
											&& (DateUtils.isSameDay(currentDate, rejectionDates.getEndDate())
													|| currentDate.before(rejectionDates.getEndDate()))))
								extraKeys.put("isRejectable", true);
							else
								extraKeys.put("isRejectable", false);
						}
					}

					break;

				case 2:
					dataList = reviewDataMap.get(2);

					// filter all the rejected data from the dataList
					dataList = dataList.stream().filter(data -> data.getRejected() == false)
							.collect(Collectors.toList());
					for (DataObject data : dataList) {

						Map<String, Object> extraKeys = data.getExtraKeys();
						Date dateOfVisit = new SimpleDateFormat("dd-MM-yyyy")
								.parse(((String) extraKeys.get("dateOfVisit")));
						TimePeriod dateOfVistiTimePeriod = submissionServiceImpl.fetchTimePeriod(dateOfVisit, formId);

						TimePeriod currentTimePeriod = submissionServiceImpl.fetchTimePeriod(currentDate, formId);

						if (dateOfVistiTimePeriod.equals(currentTimePeriod)) {
							extraKeys.put("isRejectable", false);
						} else {
							/**
							 * timeperiods are not equal than check the current
							 * date is in between 4 to 10th (both inclusive)
							 */
							DateModel rejectionDates = getRejectionDates(currentDate);
							if ((DateUtils.isSameDay(currentDate, rejectionDates.getStartDate())
									|| (currentDate.after(rejectionDates.getStartDate()))
											&& (DateUtils.isSameDay(currentDate, rejectionDates.getEndDate())
													|| currentDate.before(rejectionDates.getEndDate()))))
								extraKeys.put("isRejectable", true);
							else
								extraKeys.put("isRejectable", false);
						}
					}
					break;

				case 3:
					dataList = reviewDataMap.get(3);

					// filter all the rejected data from the dataList
					dataList = dataList.stream().filter(data -> data.getRejected() == false)
							.collect(Collectors.toList());
					for (DataObject data : dataList) {

						Map<String, Object> extraKeys = data.getExtraKeys();
						Date dateOfVisit = new SimpleDateFormat("dd-MM-yyyy")
								.parse(((String) extraKeys.get("dateOfVisit")));
						TimePeriod dateOfVistiTimePeriod = submissionServiceImpl.fetchTimePeriod(dateOfVisit, formId);

						TimePeriod currentTimePeriod = submissionServiceImpl.fetchTimePeriod(currentDate, formId);

						if (dateOfVistiTimePeriod.equals(currentTimePeriod)) {
							extraKeys.put("isRejectable", false);
						} else {
							/**
							 * timeperiods are not equal than check the current
							 * date is in between 4 to 10th (both inclusive)
							 */
							DateModel rejectionDates = getRejectionDates(currentDate);
							if ((DateUtils.isSameDay(currentDate, rejectionDates.getStartDate())
									|| (currentDate.after(rejectionDates.getStartDate()))
											&& (DateUtils.isSameDay(currentDate, rejectionDates.getEndDate())
													|| currentDate.before(rejectionDates.getEndDate()))))
								extraKeys.put("isRejectable", true);
							else
								extraKeys.put("isRejectable", false);
						}
					}
					break;

				case 4:
					dataList = reviewDataMap.get(4);

					// filter all the rejected data from the dataList
					dataList = dataList.stream().filter(data -> data.getRejected() == false)
							.collect(Collectors.toList());
					for (DataObject data : dataList) {

						Map<String, Object> extraKeys = data.getExtraKeys();
						Date dateOfVisit = new SimpleDateFormat("dd-MM-yyyy")
								.parse(((String) extraKeys.get("dateOfVisit")));
						TimePeriod dateOfVistiTimePeriod = submissionServiceImpl.fetchTimePeriod(dateOfVisit, formId);

						TimePeriod currentTimePeriod = submissionServiceImpl.fetchTimePeriod(currentDate, formId);

						if (dateOfVistiTimePeriod.equals(currentTimePeriod)) {
							extraKeys.put("isRejectable", false);
						} else {
							/**
							 * timeperiods are not equal than check the current
							 * date is in between 4 to 10th (both inclusive)
							 */
							DateModel rejectionDates = getRejectionDates(currentDate);
							if ((DateUtils.isSameDay(currentDate, rejectionDates.getStartDate())
									|| (currentDate.after(rejectionDates.getStartDate()))
											&& (DateUtils.isSameDay(currentDate, rejectionDates.getEndDate())
													|| currentDate.before(rejectionDates.getEndDate()))))
								extraKeys.put("isRejectable", true);
							else
								extraKeys.put("isRejectable", false);
						}
					}
					break;

				case 5:
					dataList = reviewDataMap.get(5);

					// filter all the rejected data from the dataList
					dataList = dataList.stream().filter(data -> data.getRejected() == false)
							.collect(Collectors.toList());
					for (DataObject data : dataList) {

						Map<String, Object> extraKeys = data.getExtraKeys();
						Date dateOfVisit = new SimpleDateFormat("dd-MM-yyyy")
								.parse(((String) extraKeys.get("dateOfVisit")));
						TimePeriod dateOfVistiTimePeriod = submissionServiceImpl.fetchTimePeriod(dateOfVisit, formId);

						TimePeriod currentTimePeriod = submissionServiceImpl.fetchTimePeriod(currentDate, formId);

						if (dateOfVistiTimePeriod.equals(currentTimePeriod)) {
							extraKeys.put("isRejectable", false);
						} else {
							/**
							 * timeperiods are not equal than check the current
							 * date is in between 4 to 10th (both inclusive)
							 */
							DateModel rejectionDates = getRejectionDates(currentDate);
							if ((DateUtils.isSameDay(currentDate, rejectionDates.getStartDate())
									|| (currentDate.after(rejectionDates.getStartDate()))
											&& (DateUtils.isSameDay(currentDate, rejectionDates.getEndDate())
													|| currentDate.before(rejectionDates.getEndDate()))))
								extraKeys.put("isRejectable", true);
							else
								extraKeys.put("isRejectable", false);
						}
					}
					break;

				case 6:
					dataList = reviewDataMap.get(6);

					// filter all the rejected data from the dataList
					dataList = dataList.stream().filter(data -> data.getRejected() == false)
							.collect(Collectors.toList());
					for (DataObject data : dataList) {

						Map<String, Object> extraKeys = data.getExtraKeys();
						Date dateOfVisit = new SimpleDateFormat("dd-MM-yyyy")
								.parse(((String) extraKeys.get("dateOfVisit")));
						TimePeriod dateOfVistiTimePeriod = submissionServiceImpl.fetchTimePeriod(dateOfVisit, formId);

						TimePeriod currentTimePeriod = submissionServiceImpl.fetchTimePeriod(currentDate, formId);

						if (dateOfVistiTimePeriod.equals(currentTimePeriod)) {
							extraKeys.put("isRejectable", false);
						} else {
							/**
							 * timeperiods are not equal than check the current
							 * date is in between 4 to 10th (both inclusive)
							 */
							DateModel rejectionDates = getRejectionDates(currentDate);
							if ((DateUtils.isSameDay(currentDate, rejectionDates.getStartDate())
									|| (currentDate.after(rejectionDates.getStartDate()))
											&& (DateUtils.isSameDay(currentDate, rejectionDates.getEndDate())
													|| currentDate.before(rejectionDates.getEndDate()))))
								extraKeys.put("isRejectable", true);
							else
								extraKeys.put("isRejectable", false);
						}
					}
					break;

				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}

		return model;
	}

	private DateModel getRejectionDates(Date date) {

		DateModel model = new DateModel();

		Calendar cal = Calendar.getInstance();

		cal.setTime(date);

		cal.set(Calendar.DATE, cal.getActualMinimum(Calendar.DATE));

		cal.set(Calendar.DATE, 4);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MILLISECOND, 0);

		model.setStartDate(cal.getTime());

		cal.set(Calendar.DATE, 10);

		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 59);

		model.setEndDate(cal.getTime());

		return model;
	}

	@Override
	public Integer getSupervisorFormId(Integer formId) {

		FormMapping formMapping = formMappingRepository.findByCfFormId(formId);

		return formMapping.getSupervisorFormId();
	}

	@Override
	public Map<String, List<IFAValueModel>> getIFASupplyMapping() {

		Map<String, List<IFAValueModel>> finalMap = new LinkedHashMap<>();

		List<IFAValueModel> modelList = new ArrayList<>();

		List<IFAQuestionMapper> ifaQuestionMapper = ifaQuestionMapperRepository.findAll();

		for (IFAQuestionMapper question : ifaQuestionMapper) {

			Designation desg = customDesignationRepository.findByName(question.getDesgName());

			if (finalMap.containsKey(desg.getId())) {

				IFAValueModel model = new IFAValueModel();
				model.setColName(question.getColName());
				model.setControlType(question.getControlType());
				model.setFieldType(question.getFieldType());
				model.setLabel(question.getLabel());
				model.setName(question.getName());
				if (question.getDropDownValue() != null) {

					List<?> list = getIFASupplyPointType(question.getDropDownValue().split("_")[1], desg.getId());

					List<IFASupplyPointMapping> supplyList;
					List<DesignationIFAMapping> desgIFAList;
					if (list != null && !list.isEmpty()) {

						List<Map<String, String>> optionsListMap = new ArrayList<>();

						if (list.get(0) instanceof IFASupplyPointMapping) {
							supplyList = (List<IFASupplyPointMapping>) list;
							for (IFASupplyPointMapping ifaMapping : supplyList) {

								Map<String, String> optionsMap = new LinkedHashMap<>();
								optionsMap.put("id", ifaMapping.getId());
								optionsMap.put("value", ifaMapping.getName());
								optionsListMap.add(optionsMap);
							}
						}

						if (list.get(0) instanceof DesignationIFAMapping) {
							desgIFAList = (List<DesignationIFAMapping>) list;
							for (DesignationIFAMapping desgIFAMapping : desgIFAList) {

								Map<String, String> optionsMap = new LinkedHashMap<>();
								optionsMap.put("id", desgIFAMapping.getId());
								optionsMap.put("value", desgIFAMapping.getIfaSuppyName());
								optionsListMap.add(optionsMap);
							}
						}

						model.setOptions(optionsListMap);
					}

				}

				if (question.getIsDependency()) {

					model.setIsDependency(question.getIsDependency());
					String dependentValue = question.getDependentCondition();
					DesignationIFAMapping desgIFAMapping = designationIFAMappingRepository
							.findByIfaSuppyName(dependentValue.split(":")[2]);
					dependentValue = dependentValue.replace(dependentValue.split(":")[2], desgIFAMapping.getId());
					model.setIsDependencyOption(dependentValue);
				}

				modelList.add(model);

			} else {

				modelList = new ArrayList<>();

				IFAValueModel model = new IFAValueModel();
				model.setColName(question.getColName());
				model.setControlType(question.getControlType());
				model.setFieldType(question.getFieldType());
				model.setLabel(question.getLabel());
				model.setIsDependency(question.getIsDependency());
				model.setName(question.getName());
				if (question.getDropDownValue() != null) {

					List<?> list = getIFASupplyPointType(question.getDropDownValue().split("_")[1], desg.getId());

					List<IFASupplyPointMapping> supplyList;
					List<DesignationIFAMapping> desgIFAList;
					if (list != null && !list.isEmpty()) {

						List<Map<String, String>> optionsListMap = new ArrayList<>();

						if (list.get(0) instanceof IFASupplyPointMapping) {
							supplyList = (List<IFASupplyPointMapping>) list;
							for (IFASupplyPointMapping ifaMapping : supplyList) {

								Map<String, String> optionsMap = new LinkedHashMap<>();
								optionsMap.put("id", ifaMapping.getId());
								optionsMap.put("value", ifaMapping.getName());
								optionsListMap.add(optionsMap);
							}
						}

						if (list.get(0) instanceof DesignationIFAMapping) {
							desgIFAList = (List<DesignationIFAMapping>) list;
							for (DesignationIFAMapping desgIFAMapping : desgIFAList) {

								Map<String, String> optionsMap = new LinkedHashMap<>();
								optionsMap.put("id", desgIFAMapping.getId());
								optionsMap.put("value", desgIFAMapping.getIfaSuppyName());
								optionsListMap.add(optionsMap);
							}
						}

						model.setOptions(optionsListMap);
					}

				}

				if (question.getIsDependency()) {

					model.setIsDependency(question.getIsDependency());
					String dependentValue = question.getDependentCondition();
					DesignationIFAMapping desgIFAMapping = designationIFAMappingRepository
							.findByIfaSuppyName(dependentValue.split(":")[2]);
					dependentValue = dependentValue.replace(dependentValue.split(":")[2], desgIFAMapping.getId());
					model.setIsDependencyOption(dependentValue);
				}

				modelList.add(model);

				finalMap.put(desg.getId(), modelList);
			}

		}

		return finalMap;
	}

	private List<?> getIFASupplyPointType(String dependentValue, String id) {

		List<IFASupplyPointMapping> ifaList = null;

		List<DesignationIFAMapping> desgIFAList = null;

		List<?> finalList = null;

		switch (dependentValue) {

		case "AWC":
			ifaList = ifaSupplyPointMappingRepository.findByTypeOrderBySlugIdAsc(IFASupplyPointType.AWC);
			break;
		case "VHNDImmunizationPoint":
			ifaList = ifaSupplyPointMappingRepository
					.findByTypeOrderBySlugIdAsc(IFASupplyPointType.VHNDImmunizationPoint);
			break;
		case "SubCenters":
			ifaList = ifaSupplyPointMappingRepository.findByTypeOrderBySlugIdAsc(IFASupplyPointType.SubCenters);
			break;
		case "PHC":
			ifaList = ifaSupplyPointMappingRepository.findByTypeOrderBySlugIdAsc(IFASupplyPointType.PHC);
			break;
		case "CHC":
			ifaList = ifaSupplyPointMappingRepository.findByTypeOrderBySlugIdAsc(IFASupplyPointType.CHC);
			break;
		case "SDH":
			ifaList = ifaSupplyPointMappingRepository.findByTypeOrderBySlugIdAsc(IFASupplyPointType.SDH);
			break;
		case "cf":
			desgIFAList = designationIFAMappingRepository.findByDesgId(id);
			break;
		case "s":
			desgIFAList = designationIFAMappingRepository.findByDesgId(id);
			break;

		}

		if (ifaList != null)
			finalList = ifaList;
		if (desgIFAList != null)
			finalList = desgIFAList;

		return finalList;
	}

	@Override
	public QuickStartModel getQuickStartValue() {

		/**
		 * get total t4session
		 */
		Integer T4SessionCount = getTotalsessionConducted(1);

		Integer mediaCount = getTotalsessionConducted(3);
		
		Integer hemocueCount = getTotalsessionConducted(4);
		
		/**
		 * get number of beneficiary reached
		 */
		Long t4BeneficiaryCount = getTotalBeneficiaryCount(1);
		Long mediaBeneficiaryCount = getTotalBeneficiaryCount(3);
		Long hemocueBeneficiaryCount = getTotalBeneficiaryCount(4);

		
		QuickStartModel quickStartModel = new QuickStartModel();
		
		//T4
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new LinkedHashMap<>();
		map.put(configurableEnvironment.getProperty("t4.session.indicator.name1"), T4SessionCount);
		map.put(configurableEnvironment.getProperty("t4.session.indicator.name2"), t4BeneficiaryCount);
		map.put("imageName", "t4data-quicks.jpg");
		map.put("name", configurableEnvironment.getProperty("t4.session.indicator.heading"));
		list.add(map);
		
		quickStartModel.setT4Data(list);

		//MEDIA
		list = new ArrayList<Map<String, Object>>();
		map = new LinkedHashMap<>();
		map.put(configurableEnvironment.getProperty("media.session.indicator.name1"), mediaCount);
		map.put(configurableEnvironment.getProperty("media.session.indicator.name2"), mediaBeneficiaryCount);
		map.put("imageName", "media-quicks.jpg");
		map.put("name", configurableEnvironment.getProperty("media.session.indicator.heading"));
		list.add(map);
		
		quickStartModel.setMediaData(list);
		
		//HEMOCUE
		list = new ArrayList<Map<String, Object>>();
		map = new LinkedHashMap<>();
		map.put(configurableEnvironment.getProperty("hemocue.session.indicator.name1"), hemocueCount);
		map.put(configurableEnvironment.getProperty("hemocue.session.indicator.name2"), hemocueBeneficiaryCount);
		map.put("imageName", "hemocue-quicks.jpg");
		map.put("name", configurableEnvironment.getProperty("hemocue.session.indicator.heading"));
		list.add(map);
		
		quickStartModel.setHmemocueData(list);
		
		return quickStartModel;
	}

	@SuppressWarnings("deprecation")
	private Long getTotalBeneficiaryCount(Integer formId) {

		DBCollection collection = mongoTemplate.getCollection("cFInputFormData");

		DBObject match = new BasicDBObject("$match", new BasicDBObject("formId", formId).append("isValid", true)
				.append("rejected", false).append("submissionCompleteStatus", "C"));
		
		DBObject sort = new BasicDBObject("$sort", new BasicDBObject("uniqueId", -1).append("syncDate", -1));
		DBObject group=null;
		switch(formId) {
			
		case 1:
			group = new BasicDBObject("$group",
					new BasicDBObject("_id", null).append("total", new BasicDBObject("$sum", "$data.FITN1")));
			break;
			
		case 3:
			group = new BasicDBObject("$group",
					new BasicDBObject("_id", null).append("total", new BasicDBObject("$sum", "$data.F3NET11")));
			break;
			
		case 4:
			group = new BasicDBObject("$group",
					new BasicDBObject("_id", null).append("total", new BasicDBObject("$sum", "$data.F4QT1")));
			break;
		}

		AggregationOptions aggregationOptions = AggregationOptions.builder()
				.outputMode(AggregationOptions.OutputMode.CURSOR).batchSize(25).allowDiskUse(true).build();

		List<DBObject> pipeline = new ArrayList<>();
		pipeline.add(match);
		pipeline.add(sort);
		pipeline.add(group);
		Cursor aggregate = collection.aggregate(pipeline, aggregationOptions);
		Long result = 0l;
		while (aggregate.hasNext()) {

			Map<String, Object> resultMap = (Map<String, Object>) aggregate.next();
			result = Long.parseLong(resultMap.get("total").toString());
		}

		return result;
	}

	private Integer getTotalsessionConducted(Integer formId) {

		SortOperation sortreviewData = Aggregation.sort(Sort.Direction.DESC, "uniqueId").and(Sort.Direction.DESC,
				"syncDate");

		MatchOperation match = Aggregation.match(Criteria.where("formId").is(formId).and("isValid").is(true)
				.and("submissionCompleteStatus").is("C").and("rejected").is(false));

		AggregationOperation countOperation = Aggregation.group().count().as("count");

		Object obj = mongoTemplate.aggregate(Aggregation.newAggregation(match, sortreviewData, countOperation),
				CFInputFormData.class, Object.class).getMappedResults();

		List<Map<String, Object>> result = (List<Map<String, Object>>) obj;

		if (!result.isEmpty())
			return (Integer) result.get(0).get("count");

		return 0;

	}

	@Override
	public ResponseEntity<String> bulkUserCreation(MultipartFile file,Principal p) {
		
		HSSFWorkbook workbook = null;
		HSSFSheet sheet = null;
		Row row;
		Cell cell;
		
		try{
			
			workbook = new HSSFWorkbook(file.getInputStream());
			sheet = workbook.getSheetAt(0);
			Map<String,Object> userMap = new HashMap<>();
			
			for (int rows = 1; rows <= sheet.getLastRowNum(); rows++) {
				
				userMap = new HashMap<>();
				
				row = sheet.getRow(rows);
				
				for (int cols = 0; cols < 6; cols++) {// column loop
					
					cell = row.getCell(cols);
					
					if (cell != null) {
						
						switch (cols) {
						
						case 0://name
							if(cell.getStringCellValue()==null){
								throw new RuntimeException("Name is blank in row number : "+rows);
							}else
								userMap.put("name", cell.getStringCellValue());
							
							break;
							
						case 1://username
							
							if(cell.getStringCellValue()==null){
								throw new RuntimeException("username is blank in row number : "+rows);
							}else
								userMap.put("userName", cell.getStringCellValue());
							
							break;
							
						case 2://password
							
							if(cell.getStringCellValue()==null){
								throw new RuntimeException("password is blank in row number : "+rows);
							}else
								userMap.put("password", cell.getStringCellValue());
							
							break;
							
						case 3://designation
							
								if(cell.getCellType()==Cell.CELL_TYPE_STRING){
									
									if(cell.getStringCellValue()==null){
										throw new RuntimeException("designation is blank in row number : "+rows);
									} 
									
									String[] desgns = cell.getStringCellValue().split(",");
									List<String> desgList = Arrays.stream(desgns).collect(Collectors.toList());
									List<Designation> designations = customDesignationRepository.findByCodeIn(desgList);
									List<String> desgs = designations.stream().map(k -> k.getId()).collect(Collectors.toList());
									userMap.put("designationIds", desgs);
								}else{
									
									if(cell.getCellType()==Cell.CELL_TYPE_BLANK){
										throw new RuntimeException("designation is blank in row number : "+rows);
									}
									String desg = String.valueOf((int)cell.getNumericCellValue());
									String desgn = "00".concat(desg);
									
									Designation designations = customDesignationRepository.findByCode(desgn);
									userMap.put("designationIds", Arrays.asList(designations.getId()));
								}
							
							break;
							
						case 4:
							//mobile
							if(cell.getCellType()==Cell.CELL_TYPE_BLANK){
								throw new RuntimeException("mobile is blank in row number : "+rows);
							}else{
								
								long mob = (long)cell.getNumericCellValue();
								//convert double to long than string
								userMap.put("mbl", String.valueOf(mob));
								
							}
							break;
							
						case 5:
							//area
							if(cell.getCellType()==Cell.CELL_TYPE_STRING){
								
								if(cell.getStringCellValue()==null){
									throw new RuntimeException("areaId is blank in row number : "+rows);
								}
								String[] desgns = cell.getStringCellValue().split(",");
								List<String> areaList=Arrays.stream(desgns).collect(Collectors.toList());
								
								List<Integer> areas = areaList.stream().map(Integer::valueOf).collect(Collectors.toList());
								
								userMap.put("areaId", areas);
							}else{
								
								if(cell.getCellType()==Cell.CELL_TYPE_BLANK){
									throw new RuntimeException("areaId is blank in row number : "+rows);
								}
								int id = (int)cell.getNumericCellValue();
								userMap.put("areaId", Arrays.asList(id));
							}
							
							break;
							
						}
					}
					
				}
				
				//save user in db
				mongoUserManagementServiceImpl.createUser(userMap, p);
				
			}
			workbook.close();
			return new ResponseEntity<String>("success",HttpStatus.OK);
			
		}catch(Exception e){
			log.error("Action : while uploading bulk user template :- ",e);
			throw new RuntimeException(e);
		}
		
	}

	@Override
	public String getTypeDetailsIdAndTypeDetailsName() {
		
		try{
			
			HSSFWorkbook workbook = new HSSFWorkbook();
			HSSFSheet sheet = workbook.createSheet("typeDetails");
			
			Row row;
			Cell cell;
			
			Integer columnIndex=0;
			
			List<TypeDetail> typeDetailsList = typeDetailRepository.findAll();
			
			
			for(int rows = 0; rows<typeDetailsList.size();rows++){
				
				columnIndex=0;
				TypeDetail typeDe = typeDetailsList.get(rows);
				
				row=sheet.createRow(rows);
				cell=row.createCell(columnIndex);
				cell.setCellValue(typeDe.getSlugId());
				
				columnIndex++;
				cell=row.createCell(columnIndex);
				cell.setCellValue(typeDe.getName());
				
				columnIndex++;
				cell=row.createCell(columnIndex);
				cell.setCellValue(typeDe.getType().getTypeName());
				
				columnIndex++;
				cell=row.createCell(columnIndex);
				cell.setCellValue(typeDe.getFormId());
				
				
			}
			
			String dir = configurableEnvironment.getProperty("report.path");

			File file = new File(dir);

			/*
			 * make directory if doesn't exist
			 */
			if (!file.exists())
				file.mkdirs();

			String name = "typeDetailstemplate" + "_"
					+ new SimpleDateFormat("ddMMyyyyHHmmss").format(new Date()) + ".xls";
			
			String path = dir + "" + name;

			FileOutputStream fos = new FileOutputStream(new File(path));
			workbook.write(fos);
			fos.close();
			workbook.close();
			return path; 
			
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResponseEntity<String> getQuickStartDates() {
		
		Calendar cal = Calendar.getInstance();
		
		cal.set(Calendar.MONTH, 7);
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		Date startDate = cal.getTime();
		String sDate = new SimpleDateFormat("dd MMM yyy").format(startDate);
		
		Date endDate = new Date();
		String eDate = new SimpleDateFormat("dd MMM yyy").format(endDate);
		
		return ResponseEntity.status(HttpStatus.OK).body(sDate +" - "+eDate);
	}

}
