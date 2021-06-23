package org.sdrc.rani.ugmthandler;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sdrc.rani.document.Area;
import org.sdrc.rani.document.DesignationIFAMapping;
import org.sdrc.rani.document.IFASupplyPointMapping;
import org.sdrc.rani.document.UserDetails;
import org.sdrc.rani.exception.DuplicateRecordException;
import org.sdrc.rani.repositories.AreaRepository;
import org.sdrc.rani.repositories.CustomAccountRepository;
import org.sdrc.rani.repositories.DesignationIFAMappingRepository;
import org.sdrc.rani.repositories.IFASupplyPointMappingRepository;
import org.sdrc.rani.service.DashboardService;
import org.sdrc.usermgmt.core.util.IUserManagementHandler;
import org.sdrc.usermgmt.mongodb.domain.Account;
import org.sdrc.usermgmt.mongodb.domain.AccountAudit;
import org.sdrc.usermgmt.mongodb.domain.AssignedDesignations;
import org.sdrc.usermgmt.mongodb.domain.Designation;
import org.sdrc.usermgmt.mongodb.repository.AccountRepository;
import org.sdrc.usermgmt.mongodb.repository.DesignationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
@Service
@Slf4j
public class SessionMapInitializerClass implements IUserManagementHandler {

	@Autowired
	private AreaRepository areaRepositoy;

	@Autowired
	@Qualifier("mongoDesignationRepository")
	private DesignationRepository designationRepository;

	@Autowired
	@Qualifier("mongoAccountRepository")
	private AccountRepository accountRepository;

	@Autowired
	private ConfigurableEnvironment configurableEnvironment;
	
	@Autowired
	private DesignationIFAMappingRepository designationIFAMappingRepository;
	
	@Autowired
	private IFASupplyPointMappingRepository ifaSupplyPointMappingRepository;
	
	@Autowired
	private CustomAccountRepository customAccountRepository;

	@Autowired
	private DashboardService dashboardService;
	
	@Override
	public Map<String, Object> sessionMap(Object account) {

		Account acc = (Account) account;
		
		/*
		 * setting extra parameters to be sent while user logged in.
		 */
		Map<String, Object> sessionMap = new HashMap<>();

		sessionMap.put("areaIds", acc.getMappedAreaIds());

		return sessionMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean saveAccountDetails(Map<String, Object> map, Object account) {

		Account acc = (Account) account;
//		if (map.get("mbl") == null || map.get("mbl").toString().isEmpty())
//			throw new RuntimeException("key : mbl not found in map");

		if (map.get("areaId") == null || map.get("areaId").toString().isEmpty())
			throw new RuntimeException("key : areaId not found in map");

		if (map.get("name") == null || map.get("name").toString().isEmpty())
			throw new RuntimeException("key : name not found in map");

		UserDetails userDetails = new UserDetails();
		
		if (map.get("mbl") != null){
			userDetails.setMobileNumber((Long.parseLong(map.get("mbl").toString())));
		}
		
		userDetails.setFullName(map.get("name").toString());

		//set ifa details
		userDetails=setUserDetails(map, userDetails,designationRepository.findOne(acc.getAssignedDesignations().get(0).getDesignationIds()).getCode());
		
		
		/**
		 * set designation slug id in userdetails
		 */
		Designation dg = designationRepository.findById(acc.getAssignedDesignations().get(0).getDesignationIds());
		userDetails.setDesgSlugId(dg.getSlugId());
		// set userDetails to account
		acc.setUserDetails(userDetails);
		
		List<Integer> areaIds = (List<Integer>) map.get("areaId");
		
		/**
		 * check duplicate village assigned to community facilitator irrespective of IFA
		 */
		if(designationRepository.findById(acc.getAssignedDesignations().get(0).getDesignationIds()).getCode().equals("003") || designationRepository.findById(acc.getAssignedDesignations().get(0).getDesignationIds()).getCode().equals("002")){
			
			List<Account> dupAccs = customAccountRepository.findByAreaIdAndIsIFAuserFalseAndDesignation((List<Integer>) map.get("areaId"),false,acc.getAssignedDesignations());
			if(!dupAccs.isEmpty()){
				throw new DuplicateRecordException(configurableEnvironment.getProperty("area.duplicate.error"));
			}
		}
		
		// verify whether areaId provided is exist or not
		List<Area> arIds = areaRepositoy.findByAreaIdIn(areaIds);
		if (!arIds.isEmpty() && arIds.size() == areaIds.size()) {
			acc.setMappedAreaIds(areaIds);
			accountRepository.save(acc);
			return true;
		} else {
			throw new RuntimeException("Key : areaId is invalid");
		}

	}

	@Override
	public boolean updateAccountDetails(Map<String, Object> map, Object account, Principal p) {

		try {

			Account acc = (Account) account;

			UserDetails userDetails = (UserDetails) acc.getUserDetails();

			if (map.get("mbl") != null && !map.get("mbl").toString().isEmpty()) {
				userDetails.setMobileNumber(Long.parseLong(map.get("mbl").toString()));
			}else {
				userDetails.setMobileNumber(null);
			}

			if (map.get("name") != null && !map.get("name").toString().isEmpty()) {
				userDetails.setFullName(map.get("name").toString());
			}
			
			if (map.get("designationIds") != null && !map.get("designationIds").toString().isEmpty()) {

				// set assigned designation to null and updated new one
				acc.setAssignedDesignations(null);

				List<String> designationIds = (List<String>) map.get("designationIds");

				List<Designation> designations = designationRepository.findByIdIn(designationIds);

				// check whether the user wanted to create admin user, if yes
				// than does
				// user set the property 'allow.admin.creation' = true
				if ((!configurableEnvironment.containsProperty("allow.admin.creation"))
						|| configurableEnvironment.getProperty("allow.admin.creation").equals("false")) {
					designations.forEach(desgs -> {
						if (desgs.getName().equals("ADMIN")) {
							throw new RuntimeException("you do not have permission to create admin user!");
						}
					});
				}

				// setting multiple AssignedDesignations in account
				List<AssignedDesignations> assDesgList = new ArrayList<>();
				designations.forEach(d -> {

					AssignedDesignations assignedDesignations = new AssignedDesignations();
					assignedDesignations.setDesignationIds(d.getId());
					assDesgList.add(assignedDesignations);
				});
				acc.setAssignedDesignations(assDesgList);
			}
			
			/**
			 * check wether user is changing designation or not if designation is changed from cf to supervisor or vice versa 
			 * than remove its IFA dependent value
			 */
			
			if(!(acc.getAssignedDesignations().get(0).getDesignationIds().equals(map.get("designationIds").toString()))) {

				Designation designation = designationRepository.findById(acc.getAssignedDesignations().get(0).getDesignationIds());	
				
				//supervisor
				if(designation.getCode().equals("002")) {
					userDetails.setChcs(null);
					userDetails.setPhcs(null);
					userDetails.setSdhs(null);
				}
				
				//CF
				if(designation.getCode().equals("003")) {
					userDetails.setAwcs(null);
					userDetails.setVhdnImmunizationPoints(null);
					userDetails.setSubCenters(null);
				}
				
			}
			List<String> dd = (List<String>)map.get("designationIds");
			Designation dg = designationRepository.findOne(dd.get(0));
			
			userDetails=setUserDetails(map,userDetails,dg.getCode());
			
			/**
			 * set designation slugId in userDetails
			 */
			userDetails.setDesgSlugId(dg.getSlugId());
			// set userDetails to account
			acc.setUserDetails(userDetails);

			if (map.get("areaId") != null && !map.get("areaId").toString().isEmpty()) {

				List<Integer> areaIds = (List<Integer>) map.get("areaId");

				// verify whether areaId provided is exist or not
				List<Area> arIds = areaRepositoy.findByAreaIdIn(areaIds);
				if (!arIds.isEmpty() && arIds.size() == areaIds.size()) {

					/**
					 * check duplicate village assigned to community facilitator
					 * irrespective of IFA
					 */
					if (designationRepository.findById(acc.getAssignedDesignations().get(0).getDesignationIds())
							.getCode().equals("003")
							|| designationRepository.findById(acc.getAssignedDesignations().get(0).getDesignationIds())
									.getCode().equals("002")) {

						/**
						 * check whether user has changed any area or not if changed than only check for duplicate else just let it update
						 */
						if(!acc.getMappedAreaIds().equals((List<Integer>) map.get("areaId"))){
							
							List<Integer> areas = (List<Integer>) map.get("areaId");
							
							for(Integer a : areas){
								List<Account> dupAccs = customAccountRepository.findByAreaIdAndIsIFAuserFalseAndDesignation(
										Arrays.asList(a), false, acc.getAssignedDesignations());
								
								if((!dupAccs.isEmpty())&&(!dupAccs.get(0).getId().equals(acc.getId()))){
									if (!dupAccs.isEmpty()) {
										throw new DuplicateRecordException(
												configurableEnvironment.getProperty("area.duplicate.error"));
									}
								}
							}
							
						}
					}

					acc.setMappedAreaIds(null);
					acc.setMappedAreaIds(areaIds);
				} else {
					throw new RuntimeException("Key : areaId is invalid");
				}
			}

			/*
			 * Audit
			 */
			List<AccountAudit> audits = acc.getChangeHistory();
			List<AccountAudit> accAuditList = new ArrayList<>();

			AccountAudit audit = new AccountAudit();
			ObjectMapper mapper = new ObjectMapper();
			acc.setChangeHistory(accAuditList);
			audit.setAccount(mapper.writeValueAsString(acc));
			audit.setAuditBy(p.getName());
			audit.setAuditDate(new Date());

			if (audits != null) {
				audits.add(audit);
			} else {
				audits = new ArrayList<>();
				audits.add(audit);
			}

			acc.setChangeHistory(audits);

			Account accSave = accountRepository.save(acc);

			if (accSave!=null) {
				dashboardService.updateUserMap(acc.getUserName(), acc.getMappedAreaIds());
			}
			
			return true;
		}
		catch (DuplicateRecordException e) {
			log.error("Action : While updating user with payload {}", map, e);
			throw new DuplicateRecordException(e.getMessage());
		}
		catch (Exception e) {
			log.error("Action : While updating user with payload {}", map, e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<?> getAllAuthorities() {
		
		return null;
	}
	
	/**
	 * This method sets IFA details.
	 * 
	 * @param map
	 * @param userDetails
	 * @return
	 */
	private UserDetails setUserDetails(Map<String,Object> map,UserDetails userDetails,String designationCode){
		
		/**
		 * get all the IFA mapping values
		 * 
		 * F1-->supplypoint
		 */
		List<String> ifaSupplyPoints = null;
		if(designationCode.equals("003")) {
			if (map.get("F1") != null && (!map.get("F1").equals(""))) {

				ifaSupplyPoints = new ArrayList<>();
				
				List<DesignationIFAMapping> desgIfaMappingsz = designationIFAMappingRepository.findByIdIn((List<String>) map.get("F1"));

				for (DesignationIFAMapping ifas : desgIfaMappingsz) {
					ifaSupplyPoints.add(ifas.getId());
				}
				if(!ifaSupplyPoints.isEmpty()){
					userDetails.setIfaSupplyPoints(null);
					userDetails.setIfaSupplyPoints(ifaSupplyPoints);
					userDetails.setIsIFAuser(true);
				}
			}
		}

		/**
		 *  F5-->supplypoint
		 *
		 */
		if(designationCode.equals("002")) {
			if (map.get("F5") != null && (!map.get("F5").equals(""))) {
	
				ifaSupplyPoints = new ArrayList<>();
				
				List<DesignationIFAMapping> desgIfaMappingsz = designationIFAMappingRepository.findByIdIn((List<String>) map.get("F5"));
	
				for (DesignationIFAMapping ifas : desgIfaMappingsz) {
					ifaSupplyPoints.add(ifas.getId());
				}
				if(!ifaSupplyPoints.isEmpty()){
					userDetails.setIfaSupplyPoints(null);
					userDetails.setIfaSupplyPoints(ifaSupplyPoints);
					userDetails.setIsIFAuser(true);
				}
			}
		}
		// F2--> AWC
		if (map.get("F2") != null && (!map.get("F2").equals(""))) {
			ifaSupplyPoints = new ArrayList<>();
			List<IFASupplyPointMapping> desgIfaMappingsz = ifaSupplyPointMappingRepository
					.findByIdIn((List<String>) map.get("F2"));

			for (IFASupplyPointMapping ifas : desgIfaMappingsz) {
				ifaSupplyPoints.add(ifas.getId());
			}
			userDetails.setAwcs(null);
			userDetails.setAwcs(ifaSupplyPoints);
			userDetails.setIsIFAuser(true);
		}

		// F3--> VHND
		if (map.get("F3") != null && (!map.get("F3").equals(""))) {
			ifaSupplyPoints = new ArrayList<>();
			List<IFASupplyPointMapping> desgIfaMappingsz = ifaSupplyPointMappingRepository
					.findByIdIn((List<String>) map.get("F3"));

			for (IFASupplyPointMapping ifas : desgIfaMappingsz) {
				ifaSupplyPoints.add(ifas.getId());
			}
			userDetails.setVhdnImmunizationPoints(null);
			userDetails.setVhdnImmunizationPoints(ifaSupplyPoints);
			userDetails.setIsIFAuser(true);
		}

		// F4--> subcenters
		if (map.get("F4") != null && (!map.get("F4").equals(""))) {
			ifaSupplyPoints = new ArrayList<>();
			List<IFASupplyPointMapping> desgIfaMappingsz = ifaSupplyPointMappingRepository
					.findByIdIn((List<String>) map.get("F4"));

			for (IFASupplyPointMapping ifas : desgIfaMappingsz) {
				ifaSupplyPoints.add(ifas.getId());
			}
			userDetails.setSubCenters(null);
			userDetails.setSubCenters(ifaSupplyPoints);
			userDetails.setIsIFAuser(true);
		}

		// F6--> PHC
		if (map.get("F6") != null && (!map.get("F6").equals(""))) {
			ifaSupplyPoints = new ArrayList<>();
			List<IFASupplyPointMapping> desgIfaMappingsz = ifaSupplyPointMappingRepository
					.findByIdIn((List<String>) map.get("F6"));

			for (IFASupplyPointMapping ifas : desgIfaMappingsz) {
				ifaSupplyPoints.add(ifas.getId());
			}
			userDetails.setPhcs(null);
			userDetails.setPhcs(ifaSupplyPoints);
			userDetails.setIsIFAuser(true);
		}

		// F7--> CHC
		if (map.get("F7") != null && (!map.get("F7").equals(""))) {
			ifaSupplyPoints = new ArrayList<>();
			List<IFASupplyPointMapping> desgIfaMappingsz = ifaSupplyPointMappingRepository
					.findByIdIn((List<String>) map.get("F7"));

			for (IFASupplyPointMapping ifas : desgIfaMappingsz) {
				ifaSupplyPoints.add(ifas.getId());
			}
			userDetails.setChcs(null);
			userDetails.setChcs(ifaSupplyPoints);
			userDetails.setIsIFAuser(true);
		}

		// F8 --> SDH
		if (map.get("F8") != null && (!map.get("F8").equals(""))) {
			ifaSupplyPoints = new ArrayList<>();
			List<IFASupplyPointMapping> desgIfaMappingsz = ifaSupplyPointMappingRepository
					.findByIdIn((List<String>) map.get("F8"));

			for (IFASupplyPointMapping ifas : desgIfaMappingsz) {
				ifaSupplyPoints.add(ifas.getId());
			}
			userDetails.setSdhs(null);
			userDetails.setSdhs(ifaSupplyPoints);
			userDetails.setIsIFAuser(true);
		}
		return userDetails;
	}

}
