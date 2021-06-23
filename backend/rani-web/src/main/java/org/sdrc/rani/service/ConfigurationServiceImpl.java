package org.sdrc.rani.service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdrc.mongoclientdetails.MongoClientDetails;
import org.sdrc.mongoclientdetails.repository.MongoClientDetailsRepository;
import org.sdrc.rani.document.Area;
import org.sdrc.rani.document.AreaLevel;
import org.sdrc.rani.document.ClusterMapping;
import org.sdrc.rani.document.DesignationFormMapping;
import org.sdrc.rani.document.DesignationIFAMapping;
import org.sdrc.rani.document.FormMapping;
import org.sdrc.rani.document.IFAQuestionMapper;
import org.sdrc.rani.document.IFASupplyPointMapping;
import org.sdrc.rani.document.KeyGeneratorColumnsSetting;
import org.sdrc.rani.document.PlanningQuestions;
import org.sdrc.rani.document.TestingDateConfiguration;
import org.sdrc.rani.models.IFASupplyPointType;
import org.sdrc.rani.repositories.AreaLevelRepository;
import org.sdrc.rani.repositories.AreaRepository;
import org.sdrc.rani.repositories.ClusterMappingRepository;
import org.sdrc.rani.repositories.DesignationFormMappingRepository;
import org.sdrc.rani.repositories.DesignationIFAMappingRepository;
import org.sdrc.rani.repositories.FormMappingRepository;
import org.sdrc.rani.repositories.IFAQuestionMapperRepository;
import org.sdrc.rani.repositories.IFASupplyPointMappingRepository;
import org.sdrc.rani.repositories.KeyGeneratorColumnsSettingRepository;
import org.sdrc.rani.repositories.PlanningQuestionsRepository;
import org.sdrc.rani.repositories.TestingDateConfigurationRepo;
import org.sdrc.usermgmt.mongodb.domain.Authority;
import org.sdrc.usermgmt.mongodb.domain.Designation;
import org.sdrc.usermgmt.mongodb.domain.DesignationAuthorityMapping;
import org.sdrc.usermgmt.mongodb.repository.AuthorityRepository;
import org.sdrc.usermgmt.mongodb.repository.DesignationAuthorityMappingRepository;
import org.sdrc.usermgmt.mongodb.repository.DesignationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import in.co.sdrc.sdrcdatacollector.document.EnginesForm;
import in.co.sdrc.sdrcdatacollector.models.AccessType;
import in.co.sdrc.sdrcdatacollector.mongorepositories.EngineFormRepository;
import in.co.sdrc.sdrcdatacollector.util.Status;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 */
@Service
public class ConfigurationServiceImpl implements ConfigurationService {

	@Autowired
	private MongoClientDetailsRepository mongoClientDetailsRepository;

	@Autowired
	private AreaLevelRepository areaLevelRepository;

	@Autowired
	private AreaRepository areaRepository;

	@Autowired
	private AuthorityRepository authorityRepository;

	@Autowired
	private DesignationAuthorityMappingRepository designationAuthorityMappingRepository;

	@Qualifier("mongoDesignationRepository")
	@Autowired
	private DesignationRepository designationRepository;

	@Autowired
	private ConfigurableEnvironment configurableEnvironment;

	@Autowired
	private EngineFormRepository formRepository;

	@Autowired
	private KeyGeneratorColumnsSettingRepository keyGeneratorColumnsSettingRepository;

	@Autowired
	private ClusterMappingRepository clusterMappingRepository;

	@Autowired
	private DesignationFormMappingRepository designationFormMappingRepository;

	@Autowired
	private FormMappingRepository formMappingRepository;

	@Autowired
	private IFASupplyPointMappingRepository ifaSupplyPointMappingRepository;

	@Autowired
	private IFAQuestionMapperRepository ifaQuestionMapperRepository;
	
	@Autowired
	private DesignationIFAMappingRepository designationIFAMappingRepository;

	@Autowired
	private PlanningQuestionsRepository PlanningQuestionsRepository;
	
	@Autowired
	private TestingDateConfigurationRepo testingDateConfigurationRepo;
	
	@Override
	public ResponseEntity<String> importAreas() {

		AreaLevel areaLevel = new AreaLevel();
		areaLevel.setAreaLevelId(1);
		areaLevel.setAreaLevelName("NATIONAL");

		areaLevelRepository.save(areaLevel);

		areaLevel = new AreaLevel();
		areaLevel.setAreaLevelId(2);
		areaLevel.setAreaLevelName("STATE");

		areaLevelRepository.save(areaLevel);

		areaLevel = new AreaLevel();
		areaLevel.setAreaLevelId(3);
		areaLevel.setAreaLevelName("DISTRICT");

		areaLevelRepository.save(areaLevel);

		areaLevel = new AreaLevel();
		areaLevel.setAreaLevelId(4);
		areaLevel.setAreaLevelName("BLOCK");

		areaLevelRepository.save(areaLevel);

		areaLevel = new AreaLevel();
		areaLevel.setAreaLevelId(5);
		areaLevel.setAreaLevelName("VILLAGE");

		areaLevelRepository.save(areaLevel);

		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource("area/");
		String path = url.getPath().replaceAll("%20", " ");
		File files[] = new File(path).listFiles();

		if (files == null) {
			throw new RuntimeException("No file found in path " + path);
		}

		for (int f = 0; f < files.length; f++) {

			XSSFWorkbook workbook = null;

			try {
				workbook = new XSSFWorkbook(files[f]);
			} catch (InvalidFormatException | IOException e) {

				e.printStackTrace();
			}
			XSSFSheet areaSheet = workbook.getSheetAt(0);

			Integer id = null;
			String areaCode = null;
			String areaName = null;
			String parentAreaId = null;
			Integer areaLevelId = null;
			Area area = null;

			for (int row = 1; row <= areaSheet.getLastRowNum(); row++) {// row
																		// loop
				// System.out.println("Rows::" + row);

				area = new Area();

				XSSFRow xssfRow = areaSheet.getRow(row);

				for (int cols = 0; cols < 5; cols++) {// column loop

					Cell cell = xssfRow.getCell(cols);

					switch (cols) {

					case 0:
						id = (int) cell.getNumericCellValue();
						break;

					case 1:
						areaCode = cell.getStringCellValue();
						break;

					case 2:
						areaName = cell.getStringCellValue();
						break;

					case 3:
						parentAreaId = cell.getStringCellValue();
						break;

					case 4:
						areaLevelId = (int) cell.getNumericCellValue();

						Area parentArea = null;
						if (areaLevelId >= 3) {
							parentArea = areaRepository.findByAreaCode(parentAreaId);
						}

						switch (areaLevelId) {

						case 3:
							// district
							area.setStateId(parentArea.getAreaId());
							break;
						case 4:
							// block
							area.setDistrictId(parentArea.getAreaId());
							area.setStateId(areaRepository.findByAreaId(parentArea.getAreaId()).getParentAreaId());
							break;

						case 5:
							// village
							area.setBlockId(parentArea.getAreaId());
							area.setDistrictId(areaRepository.findByAreaId(area.getBlockId()).getParentAreaId());
							area.setStateId(areaRepository.findByAreaId(area.getDistrictId()).getParentAreaId());

							break;
						}
						break;
					}

				}
				area.setAreaId(id);
				area.setAreaCode(areaCode);
				area.setAreaName(areaName);
				area.setAreaLevel(areaLevelRepository.findByAreaLevelId(areaLevelId));
				area.setParentAreaId(parentAreaId != "" ? areaRepository.findByAreaCode(parentAreaId).getAreaId() : -1);
				area.setLive(true);
				areaRepository.save(area);
			}
		}

		return new ResponseEntity<>("succsess", HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> formsValue() {

		KeyGeneratorColumnsSetting value = new KeyGeneratorColumnsSetting();
		value.setColKey(configurableEnvironment.getProperty("form1.datevisit.key"));
		value.setColValue(configurableEnvironment.getProperty("form1.datevisit.value"));
		value.setForm(formRepository.findByName(configurableEnvironment.getProperty("form1.name")));
		keyGeneratorColumnsSettingRepository.save(value);

		value = new KeyGeneratorColumnsSetting();
		value.setColKey(configurableEnvironment.getProperty("form2.datevisit.key"));
		value.setColValue(configurableEnvironment.getProperty("form2.datevisit.value"));
		value.setForm(formRepository.findByName(configurableEnvironment.getProperty("form2.name")));
		keyGeneratorColumnsSettingRepository.save(value);

		value = new KeyGeneratorColumnsSetting();
		value.setColKey(configurableEnvironment.getProperty("form3.datevisit.key"));
		value.setColValue(configurableEnvironment.getProperty("form3.datevisit.value"));
		value.setForm(formRepository.findByName(configurableEnvironment.getProperty("form3.name")));
		keyGeneratorColumnsSettingRepository.save(value);

		value = new KeyGeneratorColumnsSetting();
		value.setColKey(configurableEnvironment.getProperty("form4.datevisit.key"));
		value.setColValue(configurableEnvironment.getProperty("form4.datevisit.value"));
		value.setForm(formRepository.findByName(configurableEnvironment.getProperty("form4.name")));
		keyGeneratorColumnsSettingRepository.save(value);

		value = new KeyGeneratorColumnsSetting();
		value.setColKey(configurableEnvironment.getProperty("form5.datevisit.key"));
		value.setColValue(configurableEnvironment.getProperty("form5.datevisit.value"));
		value.setForm(formRepository.findByName(configurableEnvironment.getProperty("form5.name")));
		keyGeneratorColumnsSettingRepository.save(value);

		value = new KeyGeneratorColumnsSetting();
		value.setColKey(configurableEnvironment.getProperty("form6.datevisit.key"));
		value.setColValue(configurableEnvironment.getProperty("form6.datevisit.value"));
		value.setForm(formRepository.findByName(configurableEnvironment.getProperty("form6.name")));
		keyGeneratorColumnsSettingRepository.save(value);

		value = new KeyGeneratorColumnsSetting();
		value.setColKey(configurableEnvironment.getProperty("form7.datevisit.key"));
		value.setColValue(configurableEnvironment.getProperty("form7.datevisit.value"));
		value.setForm(formRepository.findByName(configurableEnvironment.getProperty("form7.name")));
		keyGeneratorColumnsSettingRepository.save(value);

		value = new KeyGeneratorColumnsSetting();
		value.setColKey(configurableEnvironment.getProperty("form8.datevisit.key"));
		value.setColValue(configurableEnvironment.getProperty("form8.datevisit.value"));
		value.setForm(formRepository.findByName(configurableEnvironment.getProperty("form8.name")));
		keyGeneratorColumnsSettingRepository.save(value);

		value = new KeyGeneratorColumnsSetting();
		value.setColKey(configurableEnvironment.getProperty("form9.datevisit.key"));
		value.setColValue(configurableEnvironment.getProperty("form9.datevisit.value"));
		value.setForm(formRepository.findByName(configurableEnvironment.getProperty("form9.name")));
		keyGeneratorColumnsSettingRepository.save(value);

		value = new KeyGeneratorColumnsSetting();
		value.setColKey(configurableEnvironment.getProperty("form10.datevisit.key"));
		value.setColValue(configurableEnvironment.getProperty("form10.datevisit.value"));
		value.setForm(formRepository.findByName(configurableEnvironment.getProperty("form10.name")));
		keyGeneratorColumnsSettingRepository.save(value);

		value = new KeyGeneratorColumnsSetting();
		value.setColKey(configurableEnvironment.getProperty("form11.datevisit.key"));
		value.setColValue(configurableEnvironment.getProperty("form11.datevisit.value"));
		value.setForm(formRepository.findByName(configurableEnvironment.getProperty("form11.name")));
		keyGeneratorColumnsSettingRepository.save(value);
		
		value = new KeyGeneratorColumnsSetting();
		value.setColKey(configurableEnvironment.getProperty("form12.datevisit.key"));
		value.setColValue(configurableEnvironment.getProperty("form12.datevisit.value"));
		value.setForm(formRepository.findByName(configurableEnvironment.getProperty("form12.name")));
		keyGeneratorColumnsSettingRepository.save(value);

		return new ResponseEntity<String>("success", HttpStatus.OK);
	}

	@Override
	public String createMongoOauth2Client() {

		try {

			MongoClientDetails mongoClientDetails = new MongoClientDetails();

			HashSet<String> scopeSet = new HashSet<>();
			scopeSet.add("read");
			scopeSet.add("write");

			List<GrantedAuthority> authorities = new ArrayList<>();
			authorities.add(new SimpleGrantedAuthority("dashboard"));

			Set<String> authorizedGrantTypes = new HashSet<>();
			authorizedGrantTypes.add("refresh_token");
			authorizedGrantTypes.add("client_credentials");
			authorizedGrantTypes.add("password");

			Set<String> resourceIds = new HashSet<>();
			resourceIds.add("web-service");

			mongoClientDetails.setClientId("rani");
			mongoClientDetails.setClientSecret("rani@123#!");
			mongoClientDetails.setScope(scopeSet);
			mongoClientDetails.setAccessTokenValiditySeconds(30000);
			mongoClientDetails.setRefreshTokenValiditySeconds(40000);
			mongoClientDetails.setAuthorities(authorities);
			mongoClientDetails.setAuthorizedGrantTypes(authorizedGrantTypes);
			mongoClientDetails.setResourceIds(resourceIds);

			mongoClientDetailsRepository.save(mongoClientDetails);
			return "success";

		} catch (Exception e) {

			throw new RuntimeException(e);
		}

	}

	@Override
	public ResponseEntity<String> config() {
		// create designation
		List<Designation> designationList = new ArrayList<>();

		Designation desg = new Designation();
		desg.setCode("001");
		desg.setName("ADMIN");
		desg.setSlugId(1);
		designationList.add(desg);

		desg = new Designation();
		desg.setCode("002");
		desg.setName("SUPERVISOR");
		desg.setSlugId(2);
		designationList.add(desg);

		desg = new Designation();
		desg.setCode("003");
		desg.setName("COMMUNITY FACILITATOR");
		desg.setSlugId(3);
		designationList.add(desg);

		desg = new Designation();
		desg.setCode("004");
		desg.setName("DISTRICT DATA MANAGER");
		desg.setSlugId(4);
		designationList.add(desg);

		desg = new Designation();
		desg.setCode("005");
		desg.setName("DISTRICT PROGRAMME IMPLEMENTATION MANAGER");
		desg.setSlugId(5);
		designationList.add(desg);

		desg = new Designation();
		desg.setCode("006");
		desg.setName("STATE");
		desg.setSlugId(6);
		designationList.add(desg);

		desg = new Designation();
		desg.setCode("007");
		desg.setName("NATIONAL");
		desg.setSlugId(7);
		designationList.add(desg);

		desg = new Designation();
		desg.setCode("008");
		desg.setName("GWU-M&E");
		desg.setSlugId(8);
		designationList.add(desg);

		desg = new Designation();
		desg.setCode("009");
		desg.setName("GWU-RESEARCH");
		desg.setSlugId(9);
		designationList.add(desg);

//		designationRepository.save(designationList);

		// create Authority

		List<Authority> authorityList = new ArrayList<>();

		Authority authority = new Authority();
		authority.setAuthority("USER_MGMT_ALL_API");
		authority.setDescription("Allow user to manage usermanagement module");
		authorityList.add(authority);

		authority = new Authority();
		authority.setAuthority("CREATE_USER");
		authority.setDescription("Allow user to access createuser API");
		authorityList.add(authority);

		authority = new Authority();
		authority.setAuthority("CHANGE_PASSWORD");
		authority.setDescription("Allow user to access changepassword API");
		authorityList.add(authority);

		authority = new Authority();
		authority.setAuthority("UPDATE_USER");
		authority.setDescription("Allow user to access updateuser API");
		authorityList.add(authority);

		authority = new Authority();
		authority.setAuthority("ENABLE_DISABLE_USER");
		authority.setDescription("Allow user to access enable/disable user API");
		authorityList.add(authority);

		authority = new Authority();
		authority.setAuthority("RESET_PASSWORD");
		authority.setDescription("Allow user to access resetpassword API");
		authorityList.add(authority);

		authority = new Authority();
		authority.setAuthority("DATA_ENTRY");
		authority.setDescription("Allow user to  dataentry module");
		authorityList.add(authority);

		authority = new Authority();
		authority.setAuthority("DOWNLOAD_RAWDATA_REPORT");
		authority.setDescription("Allow user to  download raw data report");
		authorityList.add(authority);

		authority = new Authority();
		authority.setAuthority("DASHBOARD");
		authority.setDescription("Allow user to  access program dashboard");
		authorityList.add(authority);

		authority = new Authority();
		authority.setAuthority("REVIEW-REJECT");
		authority.setDescription("Allow user to access submission management and reject it");
		authorityList.add(authority);

		authority = new Authority();
		authority.setAuthority("SUBMISSION_MANAGEMENT");
		authority.setDescription("Allow user to access submission management");
		authorityList.add(authority);

		authority = new Authority();
		authority.setAuthority("SEND_SMS");
		authority.setDescription("Allow user to  send sms");
		authorityList.add(authority);

		authority = new Authority();
		authority.setAuthority("PLANNING_MODULE");
		authority.setDescription("Allow user to  access planning module(download/upload plan,edit plan,planning report)");
		authorityList.add(authority);

		authority = new Authority();
		authority.setAuthority("QUALITATIVE_REPORT_DATA_ENTRY");
		authority.setDescription("Allow user to  access QUALITATIVE report data entry module");
		authorityList.add(authority);

		authority = new Authority();
		authority.setAuthority("QUALITATIVE_REPORT_UPLOAD");
		authority.setDescription("Allow user to  access QUALITATIVE report data DOWNLOAD and upload summary report");
		authorityList.add(authority);
		
		authority = new Authority();
		authority.setAuthority("PLANNING_REPORT");
		authority.setDescription("Allow user to  access planning Report");
		authorityList.add(authority);
		
		//submission report
		authority = new Authority();
		authority.setAuthority("SUBMISSION_REPORT");
		authority.setDescription("Allow user to  access submission Report");
		authorityList.add(authority);
		
		//REJECTION REPORT
		authority = new Authority();
		authority.setAuthority("REJECTION_REPORT");
		authority.setDescription("Allow user to  access rejection Report");
		authorityList.add(authority);
		
		//PERFORMANCE DASHBOARD
		authority = new Authority();
		authority.setAuthority("PERFORMANCE_DASHBOARD");
		authority.setDescription("Allow user to  access performance dashboard");
		authorityList.add(authority);
		
		
        //QUALITATIVE_REPORT
		authority = new Authority();
		authority.setAuthority("QUALITATIVE_REPORT");
		authority.setDescription("Allow user to  access qualitative report");
		authorityList.add(authority);
		
		authorityRepository.save(authorityList);

		// Designation-Authority Mapping

		List<DesignationAuthorityMapping> damList = new ArrayList<>();

		DesignationAuthorityMapping dam = new DesignationAuthorityMapping();

		dam.setAuthority(authorityRepository.findByAuthority("USER_MGMT_ALL_API"));
		dam.setDesignation(designationRepository.findByCode("001"));
		damList.add(dam);

		/**
		 * SUPERVISOR -
		 * DATA_ENTRY,RAWDATA,SUBMISSION_MANAGEMENT,QUALITATIVE_REPORT_DATA_ENTRY,REJECTION_REPORT,SUBMISSION_REPORT
		 */
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("DATA_ENTRY"));
		dam.setDesignation(designationRepository.findByCode("002"));
		damList.add(dam);

		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("CHANGE_PASSWORD"));
		dam.setDesignation(designationRepository.findByCode("002"));
		damList.add(dam);

		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("DOWNLOAD_RAWDATA_REPORT"));
		dam.setDesignation(designationRepository.findByCode("002"));
		damList.add(dam);

		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("SUBMISSION_MANAGEMENT"));
		dam.setDesignation(designationRepository.findByCode("002"));
		damList.add(dam);

		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("QUALITATIVE_REPORT_DATA_ENTRY"));
		dam.setDesignation(designationRepository.findByCode("002"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("PLANNING_REPORT"));
		dam.setDesignation(designationRepository.findByCode("002"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("SUBMISSION_REPORT"));
		dam.setDesignation(designationRepository.findByCode("002"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("REJECTION_REPORT"));
		dam.setDesignation(designationRepository.findByCode("002"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("PERFORMANCE_DASHBOARD"));
		dam.setDesignation(designationRepository.findByCode("002"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("DASHBOARD"));
		dam.setDesignation(designationRepository.findByCode("002"));
		damList.add(dam);

		/**
		 * COOMUNITY FACILITATOR-- DATA_ENTRY
		 */
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("DATA_ENTRY"));
		dam.setDesignation(designationRepository.findByCode("003"));
		damList.add(dam);

		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("CHANGE_PASSWORD"));
		dam.setDesignation(designationRepository.findByCode("003"));
		damList.add(dam);

		/**
		 * DDM-REVIEW,DASHOBARD,SEND_SMS,PLANNING_MODULE,REVIEW-REJECT,QUALITATIVE_REPORT_UPLOAD
		 */
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("CHANGE_PASSWORD"));
		dam.setDesignation(designationRepository.findByCode("004"));
		damList.add(dam);

		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("REVIEW-REJECT"));
		dam.setDesignation(designationRepository.findByCode("004"));
		damList.add(dam);

		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("DOWNLOAD_RAWDATA_REPORT"));
		dam.setDesignation(designationRepository.findByCode("004"));
		damList.add(dam);

		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("DASHBOARD"));
		dam.setDesignation(designationRepository.findByCode("004"));
		damList.add(dam);

		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("SEND_SMS"));
		dam.setDesignation(designationRepository.findByCode("004"));
		damList.add(dam);

		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("PLANNING_MODULE"));
		dam.setDesignation(designationRepository.findByCode("004"));
		damList.add(dam);

		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("QUALITATIVE_REPORT_UPLOAD"));
		dam.setDesignation(designationRepository.findByCode("004"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("SUBMISSION_REPORT"));
		dam.setDesignation(designationRepository.findByCode("004"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("REJECTION_REPORT"));
		dam.setDesignation(designationRepository.findByCode("004"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("PERFORMANCE_DASHBOARD"));
		dam.setDesignation(designationRepository.findByCode("004"));
		damList.add(dam);

		/**
		 * DISTRICT PROGRAMME IMPLEMENTATION MANAGER --
		 * RAWDATA,DASHBOARD,SUBMISSION_MANAGEMENT
		 */
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("DOWNLOAD_RAWDATA_REPORT"));
		dam.setDesignation(designationRepository.findByCode("005"));
		damList.add(dam);

		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("DASHBOARD"));
		dam.setDesignation(designationRepository.findByCode("005"));
		damList.add(dam);

		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("SUBMISSION_MANAGEMENT"));
		dam.setDesignation(designationRepository.findByCode("005"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("PLANNING_REPORT"));
		dam.setDesignation(designationRepository.findByCode("005"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("SUBMISSION_REPORT"));
		dam.setDesignation(designationRepository.findByCode("005"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("REJECTION_REPORT"));
		dam.setDesignation(designationRepository.findByCode("005"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("PERFORMANCE_DASHBOARD"));
		dam.setDesignation(designationRepository.findByCode("005"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("QUALITATIVE_REPORT"));
		dam.setDesignation(designationRepository.findByCode("005"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("CHANGE_PASSWORD"));
		dam.setDesignation(designationRepository.findByCode("005"));
		damList.add(dam);

		/**
		 * STATE--RAWDATA,DASHBOARD,SUBMISSION_MANAGEMENT
		 */
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("DOWNLOAD_RAWDATA_REPORT"));
		dam.setDesignation(designationRepository.findByCode("006"));
		damList.add(dam);

		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("DASHBOARD"));
		dam.setDesignation(designationRepository.findByCode("006"));
		damList.add(dam);

		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("SUBMISSION_MANAGEMENT"));
		dam.setDesignation(designationRepository.findByCode("006"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("PLANNING_REPORT"));
		dam.setDesignation(designationRepository.findByCode("006"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("SUBMISSION_REPORT"));
		dam.setDesignation(designationRepository.findByCode("006"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("REJECTION_REPORT"));
		dam.setDesignation(designationRepository.findByCode("006"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("PERFORMANCE_DASHBOARD"));
		dam.setDesignation(designationRepository.findByCode("006"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("QUALITATIVE_REPORT"));
		dam.setDesignation(designationRepository.findByCode("006"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("CHANGE_PASSWORD"));
		dam.setDesignation(designationRepository.findByCode("006"));
		damList.add(dam);

		/**
		 * NATIONAL--RAWDATA,DASHBOARD,SUBMISSION_MANAGEMENT
		 */
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("DOWNLOAD_RAWDATA_REPORT"));
		dam.setDesignation(designationRepository.findByCode("007"));
		damList.add(dam);

		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("DASHBOARD"));
		dam.setDesignation(designationRepository.findByCode("007"));
		damList.add(dam);

		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("SUBMISSION_MANAGEMENT"));
		dam.setDesignation(designationRepository.findByCode("007"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("PLANNING_REPORT"));
		dam.setDesignation(designationRepository.findByCode("007"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("SUBMISSION_REPORT"));
		dam.setDesignation(designationRepository.findByCode("007"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("REJECTION_REPORT"));
		dam.setDesignation(designationRepository.findByCode("007"));
		damList.add(dam);

		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("PERFORMANCE_DASHBOARD"));
		dam.setDesignation(designationRepository.findByCode("007"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("QUALITATIVE_REPORT"));
		dam.setDesignation(designationRepository.findByCode("007"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("CHANGE_PASSWORD"));
		dam.setDesignation(designationRepository.findByCode("007"));
		damList.add(dam);
		
		/**
		 * GWU-M&E--RAWDATA,DASHBOARD,SUBMISSION_MANAGEMENT
		 */
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("DOWNLOAD_RAWDATA_REPORT"));
		dam.setDesignation(designationRepository.findByCode("008"));
		damList.add(dam);

		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("DASHBOARD"));
		dam.setDesignation(designationRepository.findByCode("008"));
		damList.add(dam);

		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("SUBMISSION_MANAGEMENT"));
		dam.setDesignation(designationRepository.findByCode("008"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("PLANNING_REPORT"));
		dam.setDesignation(designationRepository.findByCode("008"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("SUBMISSION_REPORT"));
		dam.setDesignation(designationRepository.findByCode("008"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("REJECTION_REPORT"));
		dam.setDesignation(designationRepository.findByCode("008"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("PERFORMANCE_DASHBOARD"));
		dam.setDesignation(designationRepository.findByCode("008"));
		damList.add(dam);

		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("QUALITATIVE_REPORT"));
		dam.setDesignation(designationRepository.findByCode("008"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("CHANGE_PASSWORD"));
		dam.setDesignation(designationRepository.findByCode("008"));
		damList.add(dam);
		
		/**
		 * GWU-RESEARCH----RAWDATA,DASHBOARD
		 */
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("DOWNLOAD_RAWDATA_REPORT"));
		dam.setDesignation(designationRepository.findByCode("009"));
		damList.add(dam);

		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("DASHBOARD"));
		dam.setDesignation(designationRepository.findByCode("009"));
		damList.add(dam);

		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("SUBMISSION_MANAGEMENT"));
		dam.setDesignation(designationRepository.findByCode("009"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("CHANGE_PASSWORD"));
		dam.setDesignation(designationRepository.findByCode("009"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("QUALITATIVE_REPORT"));
		dam.setDesignation(designationRepository.findByCode("009"));
		damList.add(dam);

		designationAuthorityMappingRepository.save(damList);

		return new ResponseEntity<String>("success", HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> configureClustersMapping() {

		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource("area/");
		String path = url.getPath().replaceAll("%20", " ");
		File files[] = new File(path).listFiles();

		if (files == null) {
			throw new RuntimeException("No file found in path " + path);
		}

		for (int f = 0; f < files.length; f++) {

			XSSFWorkbook workbook = null;

			try {
				workbook = new XSSFWorkbook(files[f]);
			} catch (InvalidFormatException | IOException e) {

				e.printStackTrace();
			}
			XSSFSheet areaSheet = workbook.getSheetAt(1);

			Integer clusterNumber = null;
			String blockName = null;
			String villageName = null;
//			String clusterCode=null;
			
			ClusterMapping clusterMapping = null;

			for (int row = 1; row <= areaSheet.getLastRowNum(); row++) {// row
																		// loop
				// System.out.println("Rows::" + row);
				clusterMapping = new ClusterMapping();

				XSSFRow xssfRow = areaSheet.getRow(row);

				for (int cols = 0; cols < 3; cols++) {// column loop

					Cell cell = xssfRow.getCell(cols);

					switch (cols) {

					case 0:
						clusterNumber = (int) cell.getNumericCellValue();
						break;

					case 1:
						blockName = cell.getStringCellValue();
						break;

					case 2:
						villageName = cell.getStringCellValue();
						break;
					
//					case 3:
//						clusterCode = cell.getStringCellValue();
//						break;

					}

				}

				// block
				// areaLevelRepository.findByAreaLevelName("BLOCK");
				clusterMapping.setBlock(areaRepository.findByAreaNameAndAreaLevel(blockName,
						areaLevelRepository.findByAreaLevelName("BLOCK")));
				clusterMapping.setVillage(areaRepository.findByAreaNameAndAreaLevel(villageName,
						areaLevelRepository.findByAreaLevelName("VILLAGE")));
				clusterMapping.setDistrict(areaRepository.findByAreaNameAndAreaLevel("Anugul",
						areaLevelRepository.findByAreaLevelName("DISTRICT")));
				clusterMapping.setClusterNumber(clusterNumber);
				
				clusterMappingRepository.save(clusterMapping);

			}
		}

		return new ResponseEntity<>("succsess", HttpStatus.OK);
	}

	/**
	 * configure role based form access review,dataentry,rawdata
	 */
	@Override
	public ResponseEntity<String> configureRoleFormMapping() {

		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource("templates/");
		String path = url.getPath().replaceAll("%20", " ");
		File files[] = new File(path).listFiles();

		if (files == null) {
			throw new RuntimeException("No file found in path " + path);
		}

		for (int f = 0; f < files.length; f++) {

			XSSFWorkbook workbook = null;

			try {
				workbook = new XSSFWorkbook(files[f]);
				XSSFSheet sheet = workbook.getSheetAt(1);

				for (int row = 1; row <= sheet.getLastRowNum(); row++) {// row
																		// loop
					if (sheet.getRow(row) == null)
						break;
					XSSFRow xssfRow = sheet.getRow(row);

					String formName = null;
					String roleCode = null;
					Integer formId = null;
					String active = null;
					String displayName=null;
					Integer formVersion = null;

					System.out.println(row);
					for (int cols = 0; cols < 4; cols++) {// column loop

						Cell cell = xssfRow.getCell(cols);

						switch (cols) {

						case 0:// form

							if (cell != null && CellType.STRING == cell.getCellTypeEnum()) {

								// "1:Community Facilitator Input Form:Active"

								formId = Integer
										.valueOf(StringUtils.trimWhitespace(cell.getStringCellValue().split(":")[0]));
								formName = StringUtils.trimWhitespace(cell.getStringCellValue().split(":")[1].split("_")[0]);
								displayName = StringUtils.trimWhitespace(cell.getStringCellValue().split(":")[1].split("_")[1]);
								active = StringUtils.trimWhitespace(cell.getStringCellValue().split(":")[2]);
								formVersion = Integer
										.parseInt(StringUtils.trimWhitespace(cell.getStringCellValue().split(":")[3]));
								/**
								 * Check the status while uploading form: if
								 * found active- check whether the same form is
								 * present, if not fount insert. if found
								 * inactive - check whether the form exist, if
								 * exist set its status to inactive and update
								 * in db
								 */
								if (active != null) {

									if (active.equals("active")) {

										EnginesForm engineForm = formRepository.findByNameAndFormIdAndStatus(formName,
												formId, Status.ACTIVE);

										if (engineForm == null) {

											EnginesForm form = new EnginesForm();
											form.setFormId(formId);
											form.setName(formName);
											form.setVersion(formVersion);
											form.setDisplayName(displayName);
											form.setCreatedDate(new Date());
											form.setUpdatedDate(new Date());
											formRepository.save(form);

										}

									} else if (active.equals("inactive")) {

										EnginesForm engineForm = formRepository.findByNameAndFormId(formName, formId);

										if (engineForm != null) {
											engineForm.setStatus(Status.INACTIVE);
											formRepository.save(engineForm);
										}

									} else {
										throw new RuntimeException("Invalid active value while uploading FORM-NAME");
									}
								} else {
									throw new RuntimeException(
											"active or inactive status is not found while uploading sheet");
								}

							}
							break;

						case 1:// role-DATAENTRY
							if (cell != null && CellType.STRING == cell.getCellTypeEnum()) {

								// "'rolecode:active/inactive','rolecode,active/inactive'"

								List<String> roleDataEntry = Arrays.asList((cell.getStringCellValue().split(",")));
								for (String rdt : roleDataEntry) {

									// insert in role document
									roleCode = StringUtils.trimWhitespace(rdt.split(":")[0]);
									active = StringUtils.trimWhitespace(rdt.split(":")[1]);

									/**
									 * Check the status while uploading form: if
									 * found active- check whether the same form
									 * is present, if not fount insert. if found
									 * inactive - check whether the form exist,
									 * if exist set its status to inactive and
									 * update in db
									 */
									if (active != null) {
										if (active.equals("active")) {

											DesignationFormMapping dfm = designationFormMappingRepository
													.findByDesignationCodeAndFormFormIdAndAccessTypeAndStatus(roleCode,
															formId, AccessType.DATA_ENTRY, Status.ACTIVE);

											if (dfm == null) {
												DesignationFormMapping dsgFormMapping = new DesignationFormMapping();
												dsgFormMapping.setForm(formRepository.findByName(formName));
												dsgFormMapping
														.setDesignation(designationRepository.findByCode(roleCode));
												dsgFormMapping.setAccessType(AccessType.DATA_ENTRY);
												designationFormMappingRepository.save(dsgFormMapping);
											}

										} else if (active.equals("inactive")) {
											DesignationFormMapping dfm = designationFormMappingRepository
													.findByDesignationCodeAndFormFormIdAndAccessType(roleCode, formId,
															AccessType.DATA_ENTRY);
											if (dfm != null) {
												dfm.setStatus(Status.INACTIVE);
												designationFormMappingRepository.save(dfm);
											}
										} else {
											throw new RuntimeException(
													"Invalid active value while uploading ENGINEROLE(ROLECODE:status)-DATAENTRY");
										}
									} else {

										throw new RuntimeException(
												"active or inactive status is not found while uploading sheet");
									}

								}

							}
							break;

						case 2:// role-REVIEW
							if (cell != null && CellType.STRING == cell.getCellTypeEnum()) {

								// "'rolecode:active/inactive','rolecode,active/inactive'"

								List<String> roleDataEntry = Arrays.asList((cell.getStringCellValue().split(",")));
								for (String rdt : roleDataEntry) {

									// insert in role document
									roleCode = StringUtils.trimWhitespace(rdt.split(":")[0]);
									active = StringUtils.trimWhitespace(rdt.split(":")[1]);

									/**
									 * Check the status while uploading form: if
									 * found active- check whether the same form
									 * is present, if not fount insert. if found
									 * inactive - check whether the form exist,
									 * if exist set its status to inactive and
									 * update in db
									 */
									if (active != null) {
										if (active.equals("active")) {

											DesignationFormMapping dfm = designationFormMappingRepository
													.findByDesignationCodeAndFormFormIdAndAccessTypeAndStatus(roleCode,
															formId, AccessType.REVIEW, Status.ACTIVE);

											if (dfm == null) {
												DesignationFormMapping dsgFormMapping = new DesignationFormMapping();
												dsgFormMapping.setForm(formRepository.findByName(formName));
												dsgFormMapping
														.setDesignation(designationRepository.findByCode(roleCode));
												dsgFormMapping.setAccessType(AccessType.REVIEW);
												designationFormMappingRepository.save(dsgFormMapping);
											}

										} else if (active.equals("inactive")) {
											DesignationFormMapping dfm = designationFormMappingRepository
													.findByDesignationCodeAndFormFormIdAndAccessType(roleCode, formId,
															AccessType.REVIEW);
											if (dfm != null) {
												dfm.setStatus(Status.INACTIVE);
												designationFormMappingRepository.save(dfm);
											}
										} else {
											throw new RuntimeException(
													"Invalid active value while uploading ENGINEROLE(ROLECODE:status)-REVIEW");
										}
									} else {

										throw new RuntimeException(
												"active or inactive status is not found while uploading sheet");
									}

								}

							}
							break;

						case 3:// role-DOWNLOAD_RAW_DATA

							if (cell != null && CellType.STRING == cell.getCellTypeEnum()) {

								// "'rolecode:active/inactive','rolecode,active/inactive'"

								List<String> roleDataEntry = Arrays.asList((cell.getStringCellValue().split(",")));
								for (String rdt : roleDataEntry) {

									// insert in role document
									roleCode = StringUtils.trimWhitespace(rdt.split(":")[0]);
									active = StringUtils.trimWhitespace(rdt.split(":")[1]);

									/**
									 * Check the status while uploading form: if
									 * found active- check whether the same form
									 * is present, if not fount insert. if found
									 * inactive - check whether the form exist,
									 * if exist set its status to inactive and
									 * update in db
									 */
									if (active != null) {
										if (active.equals("active")) {

											DesignationFormMapping dfm = designationFormMappingRepository
													.findByDesignationCodeAndFormFormIdAndAccessTypeAndStatus(roleCode,
															formId, AccessType.DOWNLOAD_RAW_DATA, Status.ACTIVE);

											if (dfm == null) {
												DesignationFormMapping dsgFormMapping = new DesignationFormMapping();
												dsgFormMapping.setForm(formRepository.findByName(formName));
												dsgFormMapping
														.setDesignation(designationRepository.findByCode(roleCode));
												dsgFormMapping.setAccessType(AccessType.DOWNLOAD_RAW_DATA);
												designationFormMappingRepository.save(dsgFormMapping);
											}

										} else if (active.equals("inactive")) {
											DesignationFormMapping dfm = designationFormMappingRepository
													.findByDesignationCodeAndFormFormIdAndAccessType(roleCode, formId,
															AccessType.DOWNLOAD_RAW_DATA);
											if (dfm != null) {
												dfm.setStatus(Status.INACTIVE);
												designationFormMappingRepository.save(dfm);
											}
										} else {
											throw new RuntimeException(
													"Invalid active value while uploading ENGINEROLE(ROLECODE:status)-RAWDATA REPORT");
										}
									} else {

										throw new RuntimeException(
												"active or inactive status is not found while uploading sheet");
									}

								}

							}
							break;

						}
					}
				}
				workbook.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}
		return new ResponseEntity<String>("success", HttpStatus.OK);

	}

	@Override
	public ResponseEntity<String> configureFormMapping() {

		FormMapping formMapping = new FormMapping();

		formMapping.setCfFormId(1);
		formMapping.setSupervisorFormId(7);
		formMappingRepository.save(formMapping);

		formMapping = new FormMapping();
		formMapping.setCfFormId(2);
		formMapping.setSupervisorFormId(8);
		formMappingRepository.save(formMapping);

		formMapping = new FormMapping();
		formMapping.setCfFormId(3);
		formMapping.setSupervisorFormId(9);
		formMappingRepository.save(formMapping);

		formMapping = new FormMapping();

		formMapping.setCfFormId(4);
		formMapping.setSupervisorFormId(10);
		formMappingRepository.save(formMapping);

		formMapping = new FormMapping();

		formMapping.setCfFormId(5);
		formMapping.setSupervisorFormId(11);
		formMappingRepository.save(formMapping);

		formMapping = new FormMapping();

		formMapping.setCfFormId(6);
		formMapping.setSupervisorFormId(12);
		formMappingRepository.save(formMapping);

		return new ResponseEntity<String>("success", HttpStatus.OK);

	}

	@Override
	public ResponseEntity<String> configIFAsupplyPoint() {

		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource("IFA/");
		String path = url.getPath().replaceAll("%20", " ");
		File files[] = new File(path).listFiles();

		if (files == null) {
			throw new RuntimeException("No file found in path " + path);
		}

		for (int f = 0; f < files.length; f++) {

			XSSFWorkbook workbook = null;

			try {
				workbook = new XSSFWorkbook(files[f]);
				XSSFSheet sheet = workbook.getSheetAt(0);

				for (int row = 1; row <= sheet.getLastRowNum(); row++) {// row
																		// loop
					if (sheet.getRow(row) == null)
						break;
					XSSFRow xssfRow = sheet.getRow(row);

					for (int cols = 0; cols < 7; cols++) {// column loop

						Cell cell = xssfRow.getCell(cols);

						switch (cols) {

						case 0:// AWC---applicable for community-facilitator

							if (cell != null && CellType.STRING == cell.getCellTypeEnum()) {
								IFASupplyPointMapping ifaMapping = new IFASupplyPointMapping();
								ifaMapping.setName(cell.getStringCellValue().split(",")[0]);
								ifaMapping.setType(IFASupplyPointType.AWC);
								ifaMapping.setDesgId(designationRepository.findByCode("003").getId());
								ifaMapping.setSlugId((int)ifaSupplyPointMappingRepository.count()+1);
								ifaMapping.setTypeId(Integer.valueOf(cell.getStringCellValue().split(",")[1]));
								ifaSupplyPointMappingRepository.save(ifaMapping);
							}
							break;

						case 1:// VHND/Immunization Point--applicable for
								// community-facilitator
							if (cell != null && CellType.STRING == cell.getCellTypeEnum()) {

								IFASupplyPointMapping ifaMapping = new IFASupplyPointMapping();
								ifaMapping.setName(cell.getStringCellValue().split(",")[0]);
								ifaMapping.setType(IFASupplyPointType.VHNDImmunizationPoint);
								ifaMapping.setDesgId(designationRepository.findByCode("003").getId());
								ifaMapping.setSlugId((int)ifaSupplyPointMappingRepository.count()+1);
								ifaMapping.setTypeId(Integer.valueOf(cell.getStringCellValue().split(",")[1]));
								ifaSupplyPointMappingRepository.save(ifaMapping);
							}
							break;

						case 2:// Sub centers---applicable for
								// community-facilitator
							if (cell != null && CellType.STRING == cell.getCellTypeEnum()) {

								IFASupplyPointMapping ifaMapping = new IFASupplyPointMapping();
								ifaMapping.setName(cell.getStringCellValue().split(",")[0]);
								ifaMapping.setType(IFASupplyPointType.SubCenters);
								ifaMapping.setDesgId(designationRepository.findByCode("003").getId());
								ifaMapping.setSlugId((int)ifaSupplyPointMappingRepository.count()+1);
								ifaMapping.setTypeId(Integer.valueOf(cell.getStringCellValue().split(",")[1]));
								ifaSupplyPointMappingRepository.save(ifaMapping);
							}
							break;

						case 3:// PHC --applicable for supervisor

							if (cell != null && CellType.STRING == cell.getCellTypeEnum()) {

								IFASupplyPointMapping ifaMapping = new IFASupplyPointMapping();
								ifaMapping.setName(cell.getStringCellValue().split(",")[0]);
								ifaMapping.setType(IFASupplyPointType.PHC);
								ifaMapping.setDesgId(designationRepository.findByCode("002").getId());
								ifaMapping.setSlugId((int)ifaSupplyPointMappingRepository.count()+1);
								ifaMapping.setTypeId(Integer.valueOf(cell.getStringCellValue().split(",")[1]));
								ifaSupplyPointMappingRepository.save(ifaMapping);
							}
							break;

						case 4:// CHC --applicable for supervisor

							if (cell != null && CellType.STRING == cell.getCellTypeEnum()) {

								IFASupplyPointMapping ifaMapping = new IFASupplyPointMapping();
								ifaMapping.setName(cell.getStringCellValue().split(",")[0]);
								ifaMapping.setType(IFASupplyPointType.CHC);
								ifaMapping.setDesgId(designationRepository.findByCode("002").getId());
								ifaMapping.setSlugId((int)ifaSupplyPointMappingRepository.count()+1);
								ifaMapping.setTypeId(Integer.valueOf(cell.getStringCellValue().split(",")[1]));
								ifaSupplyPointMappingRepository.save(ifaMapping);
							}
							break;

						case 5:// SDH --applicable for supervisor

							if (cell != null && CellType.STRING == cell.getCellTypeEnum()) {
								IFASupplyPointMapping ifaMapping = new IFASupplyPointMapping();
								ifaMapping.setName(cell.getStringCellValue().split(",")[0]);
								ifaMapping.setType(IFASupplyPointType.SDH);
								ifaMapping.setDesgId(designationRepository.findByCode("002").getId());
								ifaMapping.setSlugId((int)ifaSupplyPointMappingRepository.count()+1);
								ifaMapping.setTypeId(Integer.valueOf(cell.getStringCellValue().split(",")[1]));
								ifaSupplyPointMappingRepository.save(ifaMapping);
							}
							break;

						}
					}
				}
				workbook.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}
		return new ResponseEntity<String>("success", HttpStatus.OK);

	}

	@Override
	public ResponseEntity<String> importIFAQuestions() {

		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource("IFA/");
		String path = url.getPath().replaceAll("%20", " ");
		File files[] = new File(path).listFiles();

		if (files == null) {
			throw new RuntimeException("No file found in path " + path);
		}

		List<IFAQuestionMapper> list = new ArrayList<>();

		for (int f = 0; f < files.length; f++) {

			XSSFWorkbook workbook = null;

			try {
				workbook = new XSSFWorkbook(files[f]);
				XSSFSheet sheet = workbook.getSheetAt(1);

				for (int row = 1; row <= sheet.getLastRowNum(); row++) {// row
																		// loop
					if (sheet.getRow(row) == null)
						break;
					XSSFRow xssfRow = sheet.getRow(row);
					IFAQuestionMapper mapper = new IFAQuestionMapper();

					for (int cols = 0; cols < 10; cols++) {// column loop

						Cell cell = xssfRow.getCell(cols);

						switch (cols) {

						case 0:

							if (cell != null && CellType.NUMERIC == cell.getCellTypeEnum()) {
								mapper.setSlugId((int) Math.round(cell.getNumericCellValue()));

							}
							break;

						case 1:
							if (cell != null && CellType.STRING == cell.getCellTypeEnum()) {
								mapper.setLabel(cell.getStringCellValue());
							}
							break;

						case 2:
							if (cell != null && CellType.STRING == cell.getCellTypeEnum()) {
								mapper.setControlType(cell.getStringCellValue());
							}
							break;

						case 3:

							if (cell != null && CellType.STRING == cell.getCellTypeEnum()) {
								mapper.setFieldType(cell.getStringCellValue());
							}
							break;

						case 4:

							if (cell != null && CellType.STRING == cell.getCellTypeEnum()) {
								mapper.setColName(cell.getStringCellValue());
							}
							break;

						case 5:

							if (cell != null && CellType.STRING == cell.getCellTypeEnum()) {
								mapper.setDependentCondition(cell.getStringCellValue());
							}
							break;

						case 6:

							if (cell != null && CellType.STRING == cell.getCellTypeEnum()) {
								mapper.setDropDownValue(cell.getStringCellValue());
							}
							break;

						case 7:

							if (cell != null && CellType.STRING == cell.getCellTypeEnum()) {
								mapper.setDesgName(cell.getStringCellValue());
							}
							break;
							
						case 8:

							if (cell != null && CellType.BOOLEAN == cell.getCellTypeEnum()) {
								mapper.setIsDependency(cell.getBooleanCellValue());
							}
							break;

					case 9:

						if (cell != null && CellType.STRING == cell.getCellTypeEnum()) {
							mapper.setName(cell.getStringCellValue());
						}
						break;

					}
					}

					// add in question list
					list.add(mapper);
				}
				// save in db
				ifaQuestionMapperRepository.save(list);
				workbook.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}
		return new ResponseEntity<String>("success", HttpStatus.OK);

	}

	@Override
	public ResponseEntity<String> createDesgIFAMapping() {

		List<DesignationIFAMapping> list = new ArrayList<>();

		DesignationIFAMapping designationIFAMapping = new DesignationIFAMapping();
		designationIFAMapping.setDesgId(designationRepository.findByCode("003").getId());
		designationIFAMapping.setIfaSuppyName(IFASupplyPointType.AWC.toString());
		list.add(designationIFAMapping);

		designationIFAMapping = new DesignationIFAMapping();
		designationIFAMapping.setDesgId(designationRepository.findByCode("003").getId());
		designationIFAMapping.setIfaSuppyName(IFASupplyPointType.VHNDImmunizationPoint.toString());
		list.add(designationIFAMapping);

		designationIFAMapping = new DesignationIFAMapping();
		designationIFAMapping.setDesgId(designationRepository.findByCode("003").getId());
		designationIFAMapping.setIfaSuppyName(IFASupplyPointType.SubCenters.toString());
		list.add(designationIFAMapping);

		designationIFAMapping = new DesignationIFAMapping();
		designationIFAMapping.setDesgId(designationRepository.findByCode("002").getId());
		designationIFAMapping.setIfaSuppyName(IFASupplyPointType.PHC.toString());
		list.add(designationIFAMapping);

		designationIFAMapping = new DesignationIFAMapping();
		designationIFAMapping.setDesgId(designationRepository.findByCode("002").getId());
		designationIFAMapping.setIfaSuppyName(IFASupplyPointType.CHC.toString());
		list.add(designationIFAMapping);

		designationIFAMapping = new DesignationIFAMapping();
		designationIFAMapping.setDesgId(designationRepository.findByCode("002").getId());
		designationIFAMapping.setIfaSuppyName(IFASupplyPointType.SDH.toString());
		list.add(designationIFAMapping);

		designationIFAMappingRepository.save(list);
		return new ResponseEntity<String>("success", HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> createPlanningQuestions() {

		List<PlanningQuestions> list = new ArrayList<>();

		for (int i = 1; i <= 12; i++) {

			if (i != 5 && i != 11) {
				PlanningQuestions planningQuestions = new PlanningQuestions();
				planningQuestions.setFormId(i);
				planningQuestions
						.setPlanningQuestion(configurableEnvironment.getProperty("plan.form" + i + ".question"));
				list.add(planningQuestions);
			}

		}

		PlanningQuestionsRepository.save(list);

		return new ResponseEntity<String>("success", HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> configTestingDate() {
		
		TestingDateConfiguration dateConfig = new TestingDateConfiguration();
		dateConfig.setSlugId(1);
		testingDateConfigurationRepo.save(dateConfig);
		
		return new ResponseEntity<String>("success", HttpStatus.OK);
	}

}
