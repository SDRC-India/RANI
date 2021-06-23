package org.sdrc.rani.implhandlers;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.sdrc.rani.document.Area;
import org.sdrc.rani.document.IFASupplyPointMapping;
import org.sdrc.rani.document.UserDetails;
import org.sdrc.rani.models.IFASupplyPointType;
import org.sdrc.rani.models.UserModel;
import org.sdrc.rani.repositories.AreaRepository;
import org.sdrc.rani.repositories.IFASupplyPointMappingRepository;
import org.sdrc.usermgmt.mongodb.domain.Account;
import org.sdrc.usermgmt.mongodb.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import in.co.sdrc.sdrcdatacollector.document.Question;
import in.co.sdrc.sdrcdatacollector.document.TypeDetail;
import in.co.sdrc.sdrcdatacollector.handlers.IDbQueryHandler;
import in.co.sdrc.sdrcdatacollector.models.OptionModel;
import in.co.sdrc.sdrcdatacollector.models.QuestionModel;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
@Component
public class IDbQueryHandlerImpl implements IDbQueryHandler {

	@Autowired
	private AreaRepository areaRepository;

	@Autowired
	private IFASupplyPointMappingRepository ifaSupplyPointMappingRepository;
	
	@Autowired
	@Qualifier("mongoAccountRepository")
	private AccountRepository accountRepository;

	@Override
	public List<OptionModel> getOptions(QuestionModel questionModel, Map<Integer, TypeDetail> typeDetailsMap,
			Question question, String checkedValue, Object u,Map<String,Object> paramKeyValueMap) {

		List<OptionModel> listOfOptions = new ArrayList<>();
		String tableName = questionModel.getTableName().split("\\$\\$")[0].trim();
		List<Area> areas = null;

		UserModel user = (UserModel) u;

		List<Area> areaList = areaRepository.findByAreaIdIn(user.getAreaIds());

		switch (tableName) {

		case "area": {

			String areaLevel = questionModel.getTableName().split("\\$\\$")[1].trim().split("=")[1];
			switch (areaLevel) {

			case "district":
			// @formatter:off
			{
				List<Area> area = areaList;
				// not to store duplicate district as combination of multiple
				// blocks,village might belong to same district
				Set<Area> district = new LinkedHashSet<>();

				for (Area a : area) {

					switch (a.getAreaLevel().getAreaLevelId()) {

					case 4:// get all district associated to block
					case 5: // get district associated to village
						areas = areaRepository.findByAreaLevelAreaLevelIdAndAreaId(3,a.getDistrictId());
						break;
						
					case 2:

						break;
					case 3:// district
						areas = areaRepository.findByAreaIdOrderByAreaName(a.getAreaId());
						break;

					}
					district.addAll(areas);
				}

				// convert set to list
				areas = new ArrayList<>(district);
			}
				// @formatter:on

				break;

			case "block":
			// @formatter:off
			{

				List<Area> area = areaList;
				Set<Area> block = new LinkedHashSet<>();

				for (Area a : area) {

					switch (a.getAreaLevel().getAreaLevelId()) {

					case 4:
						areas = areaRepository.findByAreaIdOrderByAreaName(a.getAreaId());
						break;
					case 2:

						break;
					case 3:
						areas = areaRepository.findByAreaLevelAreaLevelIdAndDistrictId(4,a.getAreaId());
						break;

					case 5:// get block associated with the village
						areas = areaRepository.findByAreaLevelAreaLevelIdAndAreaId(4,a.getBlockId());
						break;
					}
					block.addAll(areas);
				}
				// convert set to list
				areas = new ArrayList<>(block);
			}
				// @formatter:on

				break;

			case "village":
			// @formatter:off
			{
				List<Area> area = areaList;
				Set<Area> village = new LinkedHashSet<>();
				
				for (Area a : area) {
					
					switch (a.getAreaLevel().getAreaLevelId()) {
					case 4://block level user
						areas = areaRepository.findByAreaLevelAreaLevelIdAndBlockId(5, a.getAreaId());
						break;
					case 2:

						break;
					case 3:
						areas = areaRepository.findByAreaLevelAreaLevelIdAndDistrictId(5,a.getAreaId());
						break;
					case 5:
						areas = areaRepository.findByAreaIdOrderByAreaName(a.getAreaId());
						break;
					}
					village.addAll(areas);
				}
				// convert set to list
				areas = new ArrayList<>(village);
				
			}
				// @formatter:on
				break;
			}

			if (areas != null) {

				int order = 0;
				for (Area area : areas) {
					OptionModel optionModel = new OptionModel();
					optionModel.setKey(area.getAreaId());
					optionModel.setValue(area.getAreaName());
					optionModel.setOrder(order++);
					optionModel.setParentId(area.getParentAreaId());
					optionModel.setLevel(area.getAreaLevel().getAreaLevelId());
					optionModel.setVisible(true);
					listOfOptions.add(optionModel);
				}
				questionModel.setOptions(listOfOptions);
			}
		}

			break;

		case "ifasupply": {
			String ifaSupplPoint = questionModel.getTableName().split("\\$\\$")[1].trim().split("=")[1];

				Account account = accountRepository.findById(user.getUserId());
				UserDetails userDetails = (UserDetails) account.getUserDetails();
			// @formatter:off
				List<IFASupplyPointMapping> ifaSupplyPoints=new ArrayList<>();
				
				switch(ifaSupplPoint){

					case "AWC":{
						
						if(userDetails.getAwcs()!=null)
							ifaSupplyPoints=ifaSupplyPointMappingRepository.findByIdInOrderBySlugIdAsc(userDetails.getAwcs());
						
						 if(userDetails.getAwcs()==null && paramKeyValueMap.containsKey("review"))
							ifaSupplyPoints=ifaSupplyPointMappingRepository.findByTypeOrderBySlugIdAsc(IFASupplyPointType.AWC);
						
					}
					break;
					
					case "VHND":{
						if(userDetails.getVhdnImmunizationPoints()!=null)
							ifaSupplyPoints=ifaSupplyPointMappingRepository.findByIdInOrderBySlugIdAsc(userDetails.getVhdnImmunizationPoints());
						
						 if(userDetails.getVhdnImmunizationPoints()==null && paramKeyValueMap.containsKey("review"))
								ifaSupplyPoints=ifaSupplyPointMappingRepository.findByTypeOrderBySlugIdAsc(IFASupplyPointType.VHNDImmunizationPoint);
					}
					break;
					
					case "SUBCENTERS":{
						if(userDetails.getSubCenters()!=null)
							ifaSupplyPoints=ifaSupplyPointMappingRepository.findByIdInOrderBySlugIdAsc(userDetails.getSubCenters());
						
						 if(userDetails.getSubCenters()==null && paramKeyValueMap.containsKey("review"))
								ifaSupplyPoints=ifaSupplyPointMappingRepository.findByTypeOrderBySlugIdAsc(IFASupplyPointType.SubCenters);
					}
					break;
					
					case "PHC":{
						if(userDetails.getPhcs()!=null)
							ifaSupplyPoints=ifaSupplyPointMappingRepository.findByIdInOrderBySlugIdAsc(userDetails.getPhcs());
						
						 if(userDetails.getPhcs()==null && paramKeyValueMap.containsKey("review"))
								ifaSupplyPoints=ifaSupplyPointMappingRepository.findByTypeOrderBySlugIdAsc(IFASupplyPointType.PHC);
					}
					break;
					
					case "CHC":{
						if(userDetails.getChcs()!=null)
							ifaSupplyPoints=ifaSupplyPointMappingRepository.findByIdInOrderBySlugIdAsc(userDetails.getChcs());
						
						 if(userDetails.getChcs()==null && paramKeyValueMap.containsKey("review"))
								ifaSupplyPoints=ifaSupplyPointMappingRepository.findByTypeOrderBySlugIdAsc(IFASupplyPointType.CHC);
					}
					break;
					
					case "SDH":{
						if(userDetails.getSdhs()!=null)
							ifaSupplyPoints=ifaSupplyPointMappingRepository.findByIdInOrderBySlugIdAsc(userDetails.getSdhs());
						
						 if(userDetails.getSdhs()==null && paramKeyValueMap.containsKey("review"))
								ifaSupplyPoints=ifaSupplyPointMappingRepository.findByTypeOrderBySlugIdAsc(IFASupplyPointType.SDH);
					}
					break;
			
				}
 
				if (!ifaSupplyPoints.isEmpty()) {
					int order = 0;
					for (IFASupplyPointMapping ifa : ifaSupplyPoints) {
						OptionModel optionModel = new OptionModel();
						optionModel.setKey(ifa.getSlugId());
						optionModel.setValue(ifa.getName());
						optionModel.setOrder(order++);
						optionModel.setVisible(true);
						listOfOptions.add(optionModel);
					}
					questionModel.setOptions(listOfOptions);
				}
				
				// @formatter:on

		}
			break;
		}

		return listOfOptions;
	}

	@Override
	public String getDropDownValueForRawData(String tableName, Integer value) {

		Map<Integer, String> areaMap;

		/**
		 * get all Area(only areaid and areaname)
		 */
		List<Area> areaList = areaRepository == null ? null : areaRepository.findAreaIdAndAreaName();

		areaMap = areaList != null ? areaList.stream().collect(Collectors.toMap(Area::getAreaId, Area::getAreaName))
				: null;

		/*
		 * find areaname against this value
		 */
		switch (tableName.split(":")[0].trim()) {

		case "area": {
			return areaMap.get(value);
		}

		}

		return "success";
	}

	@Override
	public QuestionModel setValueForTextBoxFromExternal(QuestionModel qModel, Question question,
			Map<String, Object> paramKeyValMap, HttpSession session, Object user) {
		// TODO Auto-generated method stub
		return null;
	}

}
