package org.sdrc.rani.implhandlers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.sdrc.rani.document.DesignationFormMapping;
import org.sdrc.rani.models.UserModel;
import org.sdrc.rani.repositories.DesignationFormMappingRepository;
import org.sdrc.rani.util.TokenInfoExtracter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

import in.co.sdrc.sdrcdatacollector.document.EnginesForm;
import in.co.sdrc.sdrcdatacollector.models.AccessType;
import in.co.sdrc.sdrcdatacollector.mongorepositories.EngineFormRepository;
import in.co.sdrc.sdrcdatacollector.util.IProgatiInterface;
import in.co.sdrc.sdrcdatacollector.util.Status;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
@Service
public class IProgatiHandler implements IProgatiInterface {

	@Autowired
	private DesignationFormMappingRepository designationFormMappingRepository;

	@Autowired
	private TokenInfoExtracter tokenInfoExtracter;
	
	@Autowired
	private EngineFormRepository engineFormRepository;

	@Override
	public List<EnginesForm> getAssignesFormsForDataEntry(AccessType dataEntry) {

		OAuth2Authentication auth = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
		UserModel userModel = tokenInfoExtracter.getUserModelInfo(auth);

		List<EnginesForm> formList = new ArrayList<>();

		Set<String> roleIds = userModel.getRoleIds();
		
		roleIds.forEach(v->{
			System.out.println(v);
			
		});
		
		List<DesignationFormMapping> designationFormMappingList = designationFormMappingRepository
				.findByDesignationIdInAndAccessTypeAndStatus(userModel.getRoleIds(), dataEntry, Status.ACTIVE);

		designationFormMappingList.forEach(desgFormMapping -> formList.add(desgFormMapping.getForm()));

		return formList;
	}

	@Override
	public List<EnginesForm> getAssignesFormsForReview(AccessType review) {

		OAuth2Authentication auth = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
		UserModel userModel = tokenInfoExtracter.getUserModelInfo(auth);

		List<EnginesForm> formList = new ArrayList<>();

		List<DesignationFormMapping> designationFormMappingList = designationFormMappingRepository
				.findByDesignationIdInAndAccessTypeAndStatus(userModel.getRoleIds(), review, Status.ACTIVE);

		designationFormMappingList.forEach(desgFormMapping -> formList.add(desgFormMapping.getForm()));

		return formList;
	}

	@Override
	public List<EnginesForm> getAssignesFormsForDataEntryByCreatedDate(AccessType dataEntry, Date createdDate) {

		OAuth2Authentication auth = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
		UserModel userModel = tokenInfoExtracter.getUserModelInfo(auth);

		List<DesignationFormMapping> designationPartnerFormMapping = designationFormMappingRepository
				.findByDesignationIdInAndAccessTypeAndStatus(userModel.getRoleIds(), dataEntry, Status.ACTIVE);

		List<Integer> formIds = new ArrayList<>();
		for (DesignationFormMapping dpfm : designationPartnerFormMapping) {

			formIds.add(dpfm.getForm().getFormId());
		}
		
		List<EnginesForm> forms = engineFormRepository.findByFormIdInAndUpdatedDate(formIds,createdDate);
		
		return forms;
	
	}

}
