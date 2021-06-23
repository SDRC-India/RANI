package org.sdrc.rani.service;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang.time.DateUtils;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NotOLE2FileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.sdrc.rani.document.CFInputFormData;
import org.sdrc.rani.document.DesignationFormMapping;
import org.sdrc.rani.document.PlanningData;
import org.sdrc.rani.document.PlanningQuestions;
import org.sdrc.rani.document.TimePeriod;
import org.sdrc.rani.document.UUIdGenerator;
import org.sdrc.rani.document.UserDetails;
import org.sdrc.rani.exception.DateCrossedException;
import org.sdrc.rani.exception.InvalidFileException;
import org.sdrc.rani.models.AccountModel;
import org.sdrc.rani.models.DateModel;
import org.sdrc.rani.models.ManagePlanningTableModel;
import org.sdrc.rani.models.PlanVSAchievementTableModel;
import org.sdrc.rani.models.PlanningDataTargetModel;
import org.sdrc.rani.models.QualitativeJSONTableModel;
import org.sdrc.rani.models.TimePeriodModel;
import org.sdrc.rani.models.UserModel;
import org.sdrc.rani.repositories.CustomAccountRepository;
import org.sdrc.rani.repositories.CustomDesignationRepository;
import org.sdrc.rani.repositories.DesignationFormMappingRepository;
import org.sdrc.rani.repositories.PlanningDataRepository;
import org.sdrc.rani.repositories.PlanningQuestionsRepository;
import org.sdrc.rani.repositories.TimePeriodRepository;
import org.sdrc.rani.repositories.UUIDGeneratorRepository;
import org.sdrc.rani.util.ExcelStyleSheet;
import org.sdrc.rani.util.HeaderFooter;
import org.sdrc.rani.util.TokenInfoExtracter;
import org.sdrc.usermgmt.mongodb.domain.Account;
import org.sdrc.usermgmt.mongodb.domain.Designation;
import org.sdrc.usermgmt.mongodb.repository.DesignationRepository;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.html.WebColors;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.mongodb.AggregationOptions;
import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import in.co.sdrc.sdrcdatacollector.models.AccessType;
import in.co.sdrc.sdrcdatacollector.models.MessageModel;
import lombok.extern.slf4j.Slf4j;



/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
@Service
@Slf4j
public class PlanningModuleServiceImpl implements PlanningModuleService {

	@Autowired
	private TimePeriodRepository timePeriodRepository;

	@Autowired
	private ConfigurableEnvironment configurableEnvironment;

	@Autowired
	@Qualifier("mongoDesignationRepository")
	private DesignationRepository designationRepository;

	@Autowired
	private DesignationFormMappingRepository designationFormMappingRepository;

	@Qualifier("customeAccountRepository")
	@Autowired
	private CustomAccountRepository accountRepository;

	@Autowired
	private TokenInfoExtracter tokenInfoExtracter;

	@Autowired
	private UUIDGeneratorRepository uuidGeneratorRepository;

	@Autowired
	private PlanningDataRepository planningDataRepository;

	@Autowired
	@Qualifier("customMongoDesignationRepository")
	private CustomDesignationRepository customDesignationRepository;

	@Autowired
	private PlanningQuestionsRepository planningQuestionsRepository;

	@Autowired
	private MongoTemplate mongoTemplate;

//	@Autowired
//	private TestingDateConfigurationRepo testingDateConfigurationRepo;
	
	@Override
	public String downloadTemplate(String date, String roleId) {

		/**
		 * get assigned form and users present corresponding to roleId
		 */
		List<DesignationFormMapping> desgFormMappings = designationFormMappingRepository
				.findByDesignationIdAndAccessTypeOrderByFormFormId(roleId, AccessType.DATA_ENTRY);

		Designation desg = designationRepository.findById(roleId);

		HSSFWorkbook workbook = null;
		HSSFSheet sheet = null;
		Row row;
		Cell cell;
		OAuth2Authentication oauth = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
		UserModel userModel = tokenInfoExtracter.getUserModelInfo(oauth);

		try {

			Date selectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(date);
			Integer rownum = 0;
			Integer colNum = 0;
			Date currentDate = new Date();
//			String[] activeProfiles = configurableEnvironment.getActiveProfiles();
			
//			if(configurableEnvironment.containsProperty("active.profile") && activeProfiles[0].equals(configurableEnvironment.getProperty("active.profile"))){
//				
//				TestingDateConfiguration testDate = testingDateConfigurationRepo.findAllBySlugId(1);
//				currentDate = testDate.getDate();
//				
//			}
			
			Integer currentMonth = currentDate.getMonth() + 1;
			Integer currentYear = currentDate.getYear() + 1900;

			if (currentMonth == 3 || currentMonth == 6 || currentMonth == 9 || currentMonth == 12) {

				DateModel datesForPlanningModuleTemplateUpload = getDatesForPlanningModuleTemplateUpload(currentDate);

				if ((DateUtils.isSameDay(currentDate, datesForPlanningModuleTemplateUpload.getStartDate())
						|| (currentDate.after(datesForPlanningModuleTemplateUpload.getStartDate()))
								&& (DateUtils.isSameDay(currentDate, datesForPlanningModuleTemplateUpload.getEndDate())
										|| currentDate.before(datesForPlanningModuleTemplateUpload.getEndDate())))) {

					workbook = new HSSFWorkbook();
					sheet = workbook.createSheet("planning");
					/*
					 * get style for odd cell
					 */
					CellStyle colStyleOdd = ExcelStyleSheet.getStyleForOddCell(workbook, false);
					/*
					 * get style for even cell
					 */
					CellStyle colStyleEven = ExcelStyleSheet.getStyleForEvenCell(workbook, false);

					/*
					 * get style for heading
					 */
					CellStyle styleForHeading = ExcelStyleSheet.getStyleForPlanningModuleHeading(workbook);
					CellStyle styleForNoteHeading = ExcelStyleSheet.getStyleForPlanningModuleNoteHeading(workbook);
					
					row = sheet.createRow(rownum);
					cell = row.createCell(colNum);
					sheet.setColumnWidth(cell.getColumnIndex(), 6000);
					cell.setCellStyle(styleForHeading);
					cell.setCellValue("RANI: Planning Sheet for the Month of "
							+ new SimpleDateFormat("MMM").format(selectedDate));
					sheet = ExcelStyleSheet.doMerge(0, 0, 0, desgFormMappings.size(), sheet);
					rownum++;

					row = sheet.createRow(rownum);
					cell = row.createCell(colNum);
					sheet.setColumnWidth(cell.getColumnIndex(), 6000);
					cell.setCellStyle(styleForHeading);
					cell.setCellValue("Role : " + desg.getName());
					sheet = ExcelStyleSheet.doMerge(1, 1, 0, desgFormMappings.size(), sheet);
					rownum++;

					row = sheet.createRow(rownum);
					cell = row.createCell(colNum);
					sheet.setColumnWidth(cell.getColumnIndex(), 6000);
					cell.setCellStyle(styleForHeading);
					sheet = ExcelStyleSheet.doMerge(2, 2, 0, desgFormMappings.size(), sheet);
					cell.setCellValue("Date of planning : " + new SimpleDateFormat("dd-MM-yyyy").format(currentDate));
					rownum++;

					// currentMonth - 1, currentYear
					row = sheet.createRow(rownum);
					row.setHeight((short) 700);
					cell = row.createCell(colNum);
					sheet.setColumnWidth(cell.getColumnIndex(), 6000);
					cell.setCellStyle(styleForNoteHeading);
					sheet = ExcelStyleSheet.doMerge(3, 3, 0, desgFormMappings.size(), sheet);
					cell.setCellValue("   NOTE : 1.Template generated based on "
							+ Month.of(currentMonth - 1).getDisplayName(TextStyle.SHORT, Locale.UK) + "'" + currentYear
							+ " planning data.                                                                             2.Maximum target value can not be more than 50000 for each form.");
					
					rownum++;

					row = sheet.createRow(rownum);
					cell = row.createCell(colNum);
					sheet.setColumnWidth(cell.getColumnIndex(), 6000);
					cell.setCellStyle(styleForHeading);
					cell.setCellValue("Username");
					sheet = ExcelStyleSheet.doMerge(4, 5, 0, 0, sheet);
					
					colNum++;
					
					cell = row.createCell(colNum);
					sheet.setColumnWidth(cell.getColumnIndex(), 6000);
					cell.setCellStyle(styleForHeading);
					cell.setCellValue("Name of the Forms");
					sheet = ExcelStyleSheet.doMerge(4, 4, 1, desgFormMappings.size(), sheet);
					rownum++;

					row = sheet.createRow(rownum);
					colNum = 1;
					for (DesignationFormMapping form : desgFormMappings) {

						cell = row.createCell(colNum);
						sheet.setColumnWidth(cell.getColumnIndex(), 8000);
						cell.setCellStyle(styleForHeading);
						cell.setCellValue(form.getForm().getName().split("-")[1]);
						colNum++;
					}

					rownum++;

					/**
					 * restrict sheet cell value to accept value between 0 to 50000
					 * 
					 */

					CellRangeAddressList addressList = new CellRangeAddressList(5, 1000, 1, 10);
					DVConstraint dvConstraint = DVConstraint.createNumericConstraint(
							DVConstraint.ValidationType.INTEGER, DVConstraint.OperatorType.BETWEEN, "0", "50000");
					DataValidation dataValidation = new HSSFDataValidation(addressList, dvConstraint);
					sheet.addValidationData(dataValidation);

					/**
					 * get all the user associated with the role
					 */
					List<Account> accounts = accountRepository
							.findByAssignedDesignationsDesignationIdsInAndEnabledTrue(Arrays.asList(roleId));

					for (int i = 0; i < accounts.size(); i++) {

						row = sheet.createRow(rownum);
						colNum = 0;
						cell = row.createCell(colNum);
						sheet.setColumnWidth(cell.getColumnIndex(), 8000);
						// cell.setCellStyle(i % 2 == 0 ? colStyleEven :
						// colStyleOdd);
						cell.setCellValue(accounts.get(i).getUserName());

						CellStyle styleForEvenCellLock = ExcelStyleSheet.getStyleForEvenCell(workbook, true);
						CellStyle styleForOddCellLock = ExcelStyleSheet.getStyleForOddCell(workbook, true);
						cell.setCellStyle(i % 2 == 0 ? styleForEvenCellLock : styleForOddCellLock);

						rownum++;

						for (int j = 0; j < desgFormMappings.size(); j++) {

							colNum++;
							cell = row.createCell(colNum);
							sheet.setColumnWidth(cell.getColumnIndex(), 4000);
							sheet.setHorizontallyCenter(true);
							// cell.setCellType(CellType.NUMERIC);
							cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);

							/**
							 * fetch LAST MONTH OF UPLOADING MONTH planning data
							 * and set it in cell value
							 * 
							 * UPLOADING MONTH -- FETCH PREVIOUS MONTH PLANNING
							 * DATA FOR april,may,june -- get feb month planning
							 * data july,aug,sep--- get may month data
							 * oct,nov,dec --- get aug month data jan,feb,march
							 * -- get nov month data.
							 */

							PlanningData planningData;
							switch (j) {

							case 0:

								planningData = planningDataRepository.findByFormIdAndMonthAndYearAndDesgIdAndUserName(
										desgFormMappings.get(j).getForm().getFormId(), currentMonth - 1, currentYear,
										desgFormMappings.get(j).getDesignation().getId(),
										accounts.get(i).getUserName());

								if (planningData == null)
									cell.setCellValue(0);
								else
									cell.setCellValue(planningData.getTarget());

								break;

							case 1:

								planningData = planningDataRepository.findByFormIdAndMonthAndYearAndDesgIdAndUserName(
										desgFormMappings.get(j).getForm().getFormId(), currentMonth - 1, currentYear,
										desgFormMappings.get(j).getDesignation().getId(),
										accounts.get(i).getUserName());

								if (planningData == null)
									cell.setCellValue(0);
								else
									cell.setCellValue(planningData.getTarget());
								break;

							case 2:
								planningData = planningDataRepository.findByFormIdAndMonthAndYearAndDesgIdAndUserName(
										desgFormMappings.get(j).getForm().getFormId(), currentMonth - 1, currentYear,
										desgFormMappings.get(j).getDesignation().getId(),
										accounts.get(i).getUserName());

								if (planningData == null)
									cell.setCellValue(0);
								else
									cell.setCellValue(planningData.getTarget());
								break;

							case 3:
								planningData = planningDataRepository.findByFormIdAndMonthAndYearAndDesgIdAndUserName(
										desgFormMappings.get(j).getForm().getFormId(), currentMonth - 1, currentYear,
										desgFormMappings.get(j).getDesignation().getId(),
										accounts.get(i).getUserName());

								if (planningData == null)
									cell.setCellValue(0);
								else
									cell.setCellValue(planningData.getTarget());
								break;

							case 4:
								UserDetails user = (UserDetails) accounts.get(i).getUserDetails();

								if (user.getIsIFAuser() != null && user.getIsIFAuser() == true) {
									cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);

									planningData = planningDataRepository
											.findByFormIdAndMonthAndYearAndDesgIdAndUserName(
													desgFormMappings.get(j).getForm().getFormId(), currentMonth - 1,
													currentYear, desgFormMappings.get(j).getDesignation().getId(),
													accounts.get(i).getUserName());

									if (planningData == null)
										cell.setCellValue(0);
									else
										cell.setCellValue(planningData.getTarget());
								} else {
									// user is not IFA assigned
									cell.setCellStyle(i % 2 == 0 ? styleForEvenCellLock : styleForOddCellLock);
								}
								break;

							}

						}

					}
					sheet = workbook.getSheet("planning");
					sheet.protectSheet(configurableEnvironment.getProperty("uuid.sheet.password"));

					String uuidValue = null;
					UUIdGenerator uuidGenerator = new UUIdGenerator();
					/**
					 * check for same month year and userid whether uuidvalue
					 * exist or not
					 */
					UUIdGenerator uuidGen = uuidGeneratorRepository.findByMonthAndYearAndAccountIdAndDesgId(
							selectedDate.getMonth() + 1, selectedDate.getYear() + 1900, userModel.getUserId(), roleId);

					if (uuidGen == null) {
						UUID uuid = UUID.randomUUID();
						uuidValue = uuid.toString();
						uuidGenerator = new UUIdGenerator();
						uuidGenerator.setAccountId(userModel.getUserId());
						uuidGenerator.setCreatedDate(currentDate);
						uuidGenerator.setMonth(selectedDate.getMonth() + 1);
						uuidGenerator.setYear(selectedDate.getYear() + 1900);
						uuidGenerator.setDesgId(roleId);
						uuidGenerator.setUuid(uuidValue);
					} else {
						uuidValue = uuidGen.getUuid();
					}

					/**
					 * create one hidden and protected sheet and write uuid
					 * value
					 */

					sheet = workbook.createSheet("UUID_Verify");
					row = sheet.createRow(0);
					cell = row.createCell(0);
					cell.setCellValue("uuid");
					cell = row.createCell(1);
					cell.setCellValue(uuidValue);

					sheet = workbook.getSheet("UUID_Verify");
					sheet.protectSheet(configurableEnvironment.getProperty("uuid.sheet.password"));
					workbook.setSheetHidden(workbook.getSheetIndex("UUID_Verify"),
							HSSFWorkbook.SHEET_STATE_VERY_HIDDEN);

					String dir = configurableEnvironment.getProperty("report.path");

					File file = new File(dir);

					/*
					 * make directory if doesn't exist
					 */
					if (!file.exists())
						file.mkdirs();

					String name = configurableEnvironment.getProperty("planning.file.name") + "_"
							+ desg.getName().toLowerCase() + "_"
							+ new SimpleDateFormat("ddMMyyyyHHmmsssss").format(currentDate) + ".xls";

					String path = dir + "" + name;

					FileOutputStream fos = new FileOutputStream(new File(path));
					workbook.write(fos);
					fos.close();
					workbook.close();

					// save uuid in db
					if (uuidGenerator.getUuid() != null)
						uuidGeneratorRepository.save(uuidGenerator);

					return path;

				}
				throw new DateCrossedException(
						configurableEnvironment.getProperty("data.cross.download.exception.message"));
			}
			throw new DateCrossedException(
					configurableEnvironment.getProperty("data.cross.download.exception.message"));
		} catch (DateCrossedException e) {
			log.error("Action : while downloading planning template ", e);
			throw new DateCrossedException(e.getMessage());
		} catch (Exception e) {
			log.error("Action : while downloading planning template ", e);
			throw new RuntimeException(e);
		}

	}

	@Override
	public ResponseEntity<String> uploadPlanningTemplate(MultipartFile file, String date, String roleId,
			OAuth2Authentication oauth) {

		HSSFWorkbook workbook = null;
		HSSFSheet sheet = null;
		Row row;
		Cell cell;

		Date currentDate = new Date();

//		String[] activeProfiles = configurableEnvironment.getActiveProfiles();
//		if(configurableEnvironment.containsProperty("active.profile") && activeProfiles[0].equals(configurableEnvironment.getProperty("active.profile"))){
//			
//			TestingDateConfiguration testDate = testingDateConfigurationRepo.findAllBySlugId(1);
//			currentDate = testDate.getDate();
//			
//		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(currentDate);
		Integer currentMonth = cal.get(Calendar.MONTH) + 1;

		UserModel userModel = tokenInfoExtracter.getUserModelInfo(oauth);

		List<DesignationFormMapping> desgFormMappings = designationFormMappingRepository
				.findByDesignationIdAndAccessTypeOrderByFormFormId(roleId, AccessType.DATA_ENTRY);

		try {

			Date selectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(date);

			workbook = new HSSFWorkbook(file.getInputStream());
			Designation desg = designationRepository.findById(roleId);

			// checking uuid sheet exist or not
			sheet = workbook.getSheet("UUID_Verify");

			if (sheet == null) {
				workbook.close();
				log.error(
						"Action : while uploading planning module template <> UUID sheet doesnt exist! username {} Role {}",
						userModel.getName(), desg.getName());
				throw new InvalidFileException(configurableEnvironment.getProperty("planning.invalid.file.error"));
			}

			row = sheet.getRow(0);
			cell = row.getCell(1);
			String uuidValue = cell.getStringCellValue();

			Integer month = selectedDate.getMonth() + 1;
			Integer year = selectedDate.getYear() + 1900;
			// checking uuid value for reporting month and year if found allowed
			// to upload
			UUIdGenerator uuidGenerator = uuidGeneratorRepository.findByUuidAndMonthAndYearAndDesgId(uuidValue, month,year, roleId);

			if (uuidGenerator == null) {
				workbook.close();
				log.error(
						"Action : while uploading planning module template <> Invalid UUID value! username {} Role {}",
						userModel.getName(), desg.getName());
				throw new InvalidFileException(configurableEnvironment.getProperty("planning.invalid.file.error"));
			}

			/**
			 * plan can be uploaded in the month of march,june,september,
			 * december and dates between 20th to last day of the month.
			 * 
			 * everytime update the existing plan if it exists in db As between
			 * the dates multiple times a user can update the plan
			 */

			if (currentMonth == 3 || currentMonth == 6 || currentMonth == 9 || currentMonth == 12) {

				DateModel datesForPlanningModuleTemplateUpload = getDatesForPlanningModuleTemplateUpload(currentDate);

				if ((DateUtils.isSameDay(currentDate, datesForPlanningModuleTemplateUpload.getStartDate())
						|| (currentDate.after(datesForPlanningModuleTemplateUpload.getStartDate()))
								&& (DateUtils.isSameDay(currentDate, datesForPlanningModuleTemplateUpload.getEndDate())
										|| currentDate.before(datesForPlanningModuleTemplateUpload.getEndDate())))) {

					String name = null;
					Integer form1Value = 0;
					Integer form2Value = 0;
					Integer form3Value = 0;
					Integer form4Value = 0;
					Integer form5Value = 0;

					List<PlanningData> planningDataList = new ArrayList<>();
					PlanningData data = null;
					PlanningData prePlannedData = null;

					sheet = workbook.getSheet("planning");

					for (int rows = 6; rows <= sheet.getLastRowNum(); rows++) {

						row = sheet.getRow(rows);

						for (int cols = 0; cols < 6; cols++) {// column loop

							cell = row.getCell(cols);

							if (cell != null) {

								switch (cols) {

								case 0:
									name = cell.getStringCellValue() != null ? cell.getStringCellValue() : null;
									break;

								case 1:
									form1Value = (int) cell.getNumericCellValue();

									/**
									 * check whether pre-planned value exist if
									 * exist than just update the target
									 */
									prePlannedData = planningDataRepository
											.findByFormIdAndMonthAndYearAndDesgIdAndUserName(
													desgFormMappings.get(cols - 1).getForm().getFormId(), month, year,
													roleId, name);
									if (prePlannedData == null) {
										data = setTargetData(desgFormMappings.get(cols - 1), form1Value, name, month,
												year, roleId);
										planningDataList.add(data);
									} else {
										prePlannedData.setTarget(form1Value);
										prePlannedData.setUpdatedDate(currentDate);
										planningDataRepository.save(prePlannedData);
									}

									break;

								case 2:
									form2Value = (int) cell.getNumericCellValue();

									/**
									 * check whether pre-planned value exist if
									 * exist than just update the target
									 */
									prePlannedData = planningDataRepository
											.findByFormIdAndMonthAndYearAndDesgIdAndUserName(
													desgFormMappings.get(cols - 1).getForm().getFormId(), month, year,
													roleId, name);
									if (prePlannedData == null) {
										data = setTargetData(desgFormMappings.get(cols - 1), form2Value, name, month,
												year, roleId);
										planningDataList.add(data);
									} else {
										prePlannedData.setTarget(form2Value);
										prePlannedData.setUpdatedDate(currentDate);
										planningDataRepository.save(prePlannedData);
									}

									break;

								case 3:
									form3Value = (int) cell.getNumericCellValue();
									/**
									 * check whether pre-planned value exist if
									 * exist than just update the target
									 */
									prePlannedData = planningDataRepository
											.findByFormIdAndMonthAndYearAndDesgIdAndUserName(
													desgFormMappings.get(cols - 1).getForm().getFormId(), month, year,
													roleId, name);

									if (prePlannedData == null) {
										data = setTargetData(desgFormMappings.get(cols - 1), form3Value, name, month,
												year, roleId);
										planningDataList.add(data);
									} else {
										prePlannedData.setTarget(form3Value);
										prePlannedData.setUpdatedDate(currentDate);
										planningDataRepository.save(prePlannedData);
									}

									break;

								case 4:
									form4Value = (int) cell.getNumericCellValue();
									/**
									 * check whether pre-planned value exist if
									 * exist than just update the target
									 */
									prePlannedData = planningDataRepository
											.findByFormIdAndMonthAndYearAndDesgIdAndUserName(
													desgFormMappings.get(cols - 1).getForm().getFormId(), month, year,
													roleId, name);

									if (prePlannedData == null) {
										data = setTargetData(desgFormMappings.get(cols - 1), form4Value, name, month,
												year, roleId);
										planningDataList.add(data);
									} else {
										prePlannedData.setTarget(form4Value);
										prePlannedData.setUpdatedDate(currentDate);
										planningDataRepository.save(prePlannedData);
									}

									break;

								case 5: {

									Account acc = accountRepository.findByUserName(name);
									UserDetails userDetail = (UserDetails) acc.getUserDetails();

									if (userDetail.getIsIFAuser()) {

										form5Value = (int) cell.getNumericCellValue();

										/**
										 * check whether pre-planned value exist
										 * if exist than just update the target
										 */
										prePlannedData = planningDataRepository
												.findByFormIdAndMonthAndYearAndDesgIdAndUserName(
														desgFormMappings.get(cols - 1).getForm().getFormId(), month,
														year, roleId, name);

										if (prePlannedData == null) {
											data = setTargetData(desgFormMappings.get(cols - 1), form5Value, name,
													month, year, roleId);
											planningDataList.add(data);
										} else {
											prePlannedData.setTarget(form5Value);
											prePlannedData.setUpdatedDate(currentDate);
											planningDataRepository.save(prePlannedData);
										}

									}
								}
									break;

								}
							}
						} // column loop ends

					}

					// save planning data in db
					planningDataRepository.save(planningDataList);

					workbook.close();
					return new ResponseEntity<>(
							new Gson().toJson(configurableEnvironment.getProperty("planning.upload.template.success")),
							HttpStatus.OK);

				}
				workbook.close();
				throw new DateCrossedException(configurableEnvironment.getProperty("data.cross.exception.message"));
			}

			workbook.close();
			throw new DateCrossedException(configurableEnvironment.getProperty("data.cross.exception.message"));

		} catch (DateCrossedException e) {
			log.error("Action : while uploading planning template ", e);
			throw new DateCrossedException(e.getMessage());
		} catch (InvalidFileException e) {
			log.error("Action : while uploading planning template ", e);
			throw new InvalidFileException(e.getMessage());
		} catch (NotOLE2FileException e) {
			log.error("Action : while uploading planning template ", e);
			throw new InvalidFileException(configurableEnvironment.getProperty("planning.invalid.file.error"));
		} catch (Exception e) {
			log.error("Action : while uploading planning template ", e);
			throw new RuntimeException(e);
		}

	}

	private PlanningData setTargetData(DesignationFormMapping designationFormMapping, Integer value, String name,
			Integer month, Integer year, String roleId) {

		PlanningData data = new PlanningData();
		data.setFormId(designationFormMapping.getForm().getFormId());
		data.setAccId(accountRepository.findByUserName(name).getId());
		data.setMonth(month);
		data.setYear(year);
		data.setDesgId(roleId);
		data.setTarget(value);
		data.setUserName(name);

		return data;
	}

	@Override
	public Map<String, List<TimePeriodModel>> getTimePeriod() {

		List<TimePeriod> timePeriods = timePeriodRepository.findAllByPeriodicityOrderByCreatedDateAsc(
				configurableEnvironment.getProperty("timeperiod.periodicity.monthly"));

		Map<String, List<TimePeriodModel>> finalMap = new LinkedHashMap<>();

		List<TimePeriodModel> tpModeList = new ArrayList<>();

		for (TimePeriod tp : timePeriods) {

			if (finalMap.containsKey(tp.getTimePeriodDuration())) {

				TimePeriodModel model = new TimePeriodModel();
				model.setId(tp.getId());
				model.setYear(tp.getYear());
				finalMap.get(tp.getTimePeriodDuration()).add(model);

			} else {
				tpModeList = new ArrayList<>();

				TimePeriodModel model = new TimePeriodModel();
				model.setId(tp.getId());
				model.setYear(tp.getYear());
				tpModeList.add(model);
				finalMap.put(tp.getTimePeriodDuration(), tpModeList);

			}

		}

		return finalMap;
	}

	@Override
	public Map<Boolean, List<Map<String, String>>> getTimePeriodForTemplateUpload() {

		/**
		 * if it is month of march,june,sep,dec than DDM will plan for the
		 * subsequent 3 months
		 * 
		 * if march than planning would be for april may june, within 20th to
		 * last day of month.
		 * 
		 */

		Date currentDate = new Date();

//		String[] activeProfiles = configurableEnvironment.getActiveProfiles();
//		if(configurableEnvironment.containsProperty("active.profile") && activeProfiles[0].equals(configurableEnvironment.getProperty("active.profile"))){
//			
//			TestingDateConfiguration testDate = testingDateConfigurationRepo.findAllBySlugId(1);
//			currentDate = testDate.getDate();
//			
//		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(currentDate);
		Integer month = cal.get(Calendar.MONTH) + 1;

		DateModel datesForPlanningModuleTemplateUpload = getDatesForPlanningModuleTemplateUpload(currentDate);
		List<Map<String, String>> tpList = new ArrayList<>();

		Map<Boolean, List<Map<String, String>>> finalMap = new LinkedHashMap<>();

		if (month == 3 || month == 6 || month == 9 || month == 12) {

			if ((DateUtils.isSameDay(currentDate, datesForPlanningModuleTemplateUpload.getStartDate())
					|| (currentDate.after(datesForPlanningModuleTemplateUpload.getStartDate()))
							&& (DateUtils.isSameDay(currentDate, datesForPlanningModuleTemplateUpload.getEndDate())
									|| currentDate.before(datesForPlanningModuleTemplateUpload.getEndDate())))) {
				tpList = getSubsequentThreeMonths(currentDate, tpList);
				finalMap.put(true, tpList);
				return finalMap;
			}
		}
		finalMap.put(false, tpList);
		return finalMap;
	}

	private List<Map<String, String>> getSubsequentThreeMonths(Date currentDate, List<Map<String, String>> tpList) {

		if (tpList.isEmpty())
			tpList = new ArrayList<>();

		Integer month = currentDate.getMonth() + 1;

		Map<String, String> tpMap = new LinkedHashMap<>();

		SimpleDateFormat sdf = new SimpleDateFormat("MMM");
		Date date;

		/**
		 * for the month of march , june, sep and oct the subsequent 3 quarter
		 * to be shown
		 */
		if (month == 3 || month == 6 || month == 9 || month == 12) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(currentDate);
			cal.add(Calendar.MONTH, 1);
			date = cal.getTime();
			tpMap.put("month", sdf.format(date) + " " + new SimpleDateFormat("yyyy").format(date));
			tpMap.put("date", new SimpleDateFormat("dd-MM-yyyy").format(date));
			tpList.add(tpMap);

			tpMap = new LinkedHashMap<>();
			cal.setTime(currentDate);
			cal.add(Calendar.MONTH, 2);
			date = cal.getTime();
			tpMap.put("month", sdf.format(date) + " " + new SimpleDateFormat("yyyy").format(date));
			tpMap.put("date", new SimpleDateFormat("dd-MM-yyyy").format(date));
			tpList.add(tpMap);

			tpMap = new LinkedHashMap<>();
			cal.setTime(currentDate);
			cal.add(Calendar.MONTH, 3);
			date = cal.getTime();
			tpMap.put("month", sdf.format(date) + " " + new SimpleDateFormat("yyyy").format(date));
			tpMap.put("date", new SimpleDateFormat("dd-MM-yyyy").format(date));
			tpList.add(tpMap);

		}

		/**
		 * if the month is april, july october and january than subsequent 3
		 * months to be shown
		 */
		if (month == 4 || month == 7 || month == 10 || month == 1) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(currentDate);
			cal.add(Calendar.MONTH, 1);
			date = cal.getTime();
			tpMap.put("month", sdf.format(date) + " " + new SimpleDateFormat("yyyy").format(date));
			tpMap.put("date", new SimpleDateFormat("dd-MM-yyyy").format(date));
			tpList.add(tpMap);

			tpMap = new LinkedHashMap<>();
			cal.setTime(currentDate);
			cal.add(Calendar.MONTH, 2);
			date = cal.getTime();
			tpMap.put("month", sdf.format(date) + " " + new SimpleDateFormat("yyyy").format(date));
			tpMap.put("date", new SimpleDateFormat("dd-MM-yyyy").format(date));
			tpList.add(tpMap);

		}

		/**
		 * if month is may, aug, nov and feb than subsequent 1 month to be shown
		 */
		if (month == 5 || month == 8 || month == 11 || month == 2) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(currentDate);
			cal.add(Calendar.MONTH, 1);
			date = cal.getTime();
			tpMap.put("month", sdf.format(date) + " " + new SimpleDateFormat("yyyy").format(date));
			tpMap.put("date", new SimpleDateFormat("dd-MM-yyyy").format(date));
			tpList.add(tpMap);
		}

		return tpList;
	}

	private DateModel getDatesForPlanningModuleTemplateUpload(Date currentDate) {

		DateModel model = new DateModel();

		Calendar cal = Calendar.getInstance();
		cal.setTime(currentDate);
		cal.set(Calendar.DATE, 20);

		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MILLISECOND, 0);

		model.setStartDate(cal.getTime());

		cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE));

		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 59);

		model.setEndDate(cal.getTime());

		return model;

	}

	@Override
	public List<Map<String, String>> getTimePeriodForManagePlanning() {

		/**
		 * if current date is between 1 to 10 than current month would be
		 * available and there after subsequent 3 months would be available for
		 * manage planning.
		 */
		SimpleDateFormat sdf = new SimpleDateFormat("MMM");
		Date currentDate = new Date();

//		String[] activeProfiles = configurableEnvironment.getActiveProfiles();
//		if(configurableEnvironment.containsProperty("active.profile") && activeProfiles[0].equals(configurableEnvironment.getProperty("active.profile"))){
//			
//			TestingDateConfiguration testDate = testingDateConfigurationRepo.findAllBySlugId(1);
//			currentDate = testDate.getDate();
//			
//		}
		
		DateModel datesForPlanningModuleTemplateUpload = getDatesForManagePlanningModule(currentDate);

		List<Map<String, String>> tpList = new ArrayList<>();
		Map<String, String> tpMap = new LinkedHashMap<>();

		if ((DateUtils.isSameDay(currentDate, datesForPlanningModuleTemplateUpload.getStartDate())
				|| (currentDate.after(datesForPlanningModuleTemplateUpload.getStartDate()))
						&& (DateUtils.isSameDay(currentDate, datesForPlanningModuleTemplateUpload.getEndDate())
								|| currentDate.before(datesForPlanningModuleTemplateUpload.getEndDate())))) {

			tpMap.put("month", sdf.format(currentDate) + " " + new SimpleDateFormat("yyyy").format(currentDate));
			tpMap.put("date", new SimpleDateFormat("dd-MM-yyyy").format(currentDate));
			tpList.add(tpMap);

		}

		tpList = getSubsequentThreeMonths(currentDate, tpList);

		return tpList;
	}

	private DateModel getDatesForManagePlanningModule(Date currentDate) {

		DateModel model = new DateModel();

//		String[] activeProfiles = configurableEnvironment.getActiveProfiles();
//		if(configurableEnvironment.containsProperty("active.profile") && activeProfiles[0].equals(configurableEnvironment.getProperty("active.profile"))){
//			
//			TestingDateConfiguration testDate = testingDateConfigurationRepo.findAllBySlugId(1);
//			currentDate = testDate.getDate();
//			
//		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(currentDate);
		cal.set(Calendar.DATE, cal.getActualMinimum(Calendar.DATE));

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
	public Map<String, QualitativeJSONTableModel> getManagePlanValue(String roleId, String date, String accId) {

		List<PlanningQuestions> planningQuestions = planningQuestionsRepository.findAll();

		Map<Integer, String> planningQuestionsMap = planningQuestions.stream()
				.collect(Collectors.toMap(PlanningQuestions::getFormId, PlanningQuestions::getPlanningQuestion));

		List<String> tableColumn = new ArrayList<>();
		tableColumn.add("formName");
		tableColumn.add("target");

		Map<String, String> action = new LinkedHashMap<>();

		Date currentDate = new Date();
		
//		String[] activeProfiles = configurableEnvironment.getActiveProfiles();
//		if(configurableEnvironment.containsProperty("active.profile") && activeProfiles[0].equals(configurableEnvironment.getProperty("active.profile"))){
//			
//			TestingDateConfiguration testDate = testingDateConfigurationRepo.findAllBySlugId(1);
//			currentDate = testDate.getDate();
//			
//		}
		
		try {

			Date selectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(date);
			Integer month = selectedDate.getMonth() + 1;
			Integer year = selectedDate.getYear() + 1900;

			List<PlanningData> planningDatas = planningDataRepository.findByMonthAndYearAndDesgIdAndAccId(month, year,
					roleId, accId);

			List<ManagePlanningTableModel> managePlanningTableModelList = new ArrayList<>();

			for (PlanningData data : planningDatas) {

				ManagePlanningTableModel tableModel = new ManagePlanningTableModel();
				tableModel.setFormName(planningQuestionsMap.get(data.getFormId()));
				tableModel.setTarget(data.getTarget());

				action = new LinkedHashMap<>();
				action.put("id", data.getId());
				tableModel.setAction(action);
				managePlanningTableModelList.add(tableModel);

			}
			QualitativeJSONTableModel model = new QualitativeJSONTableModel();
			model.setTableColumn(tableColumn);
			model.setTableData(managePlanningTableModelList);
			Map<String, QualitativeJSONTableModel> finalMap = new HashMap<>();

			finalMap.put(new SimpleDateFormat("MMM-yyyy").format(currentDate), model);

			return finalMap;

		} catch (Exception e) {
			log.error("Action : while fetching manage planning daya with roleId {} date {} and accountId {} ", roleId,
					date, accId, e);
			throw new RuntimeException(e);
		}

	}

	@Override
	public ResponseEntity<String> updatePlan(PlanningDataTargetModel model) {

		PlanningData data = planningDataRepository.findById(model.getPlanId());

		data.setTarget(model.getTargetValue());
		planningDataRepository.save(data);

		return new ResponseEntity<>(new Gson().toJson(configurableEnvironment.getProperty("plan.update.success")),
				HttpStatus.OK);
	}

	@Override
	public Map<String, List<AccountModel>> getUserAccounts(OAuth2Authentication oauth) {

		List<Designation> designations = customDesignationRepository.findByCodeIn(Arrays.asList("002", "003"));

		List<String> desgIds = designations.stream().map(Designation::getId).collect(Collectors.toList());

		List<Account> accounts = accountRepository.findByAssignedDesignationsDesignationIdsInAndEnabledTrue(desgIds);

		List<AccountModel> accModelList = new ArrayList<>();

		Map<String, List<AccountModel>> finalMap = new LinkedHashMap<>();

		for (Account acc : accounts) {
			if (finalMap.containsKey(acc.getAssignedDesignations().get(0).getDesignationIds())) {

				AccountModel model = new AccountModel();
				model.setAccId(acc.getId());

				UserDetails userDetails = (UserDetails) acc.getUserDetails();

				model.setName(userDetails.getFullName());
				model.setUserName(acc.getUserName());

				finalMap.get(acc.getAssignedDesignations().get(0).getDesignationIds()).add(model);
			} else {

				accModelList = new ArrayList<>();

				AccountModel model = new AccountModel();
				model.setAccId(acc.getId());
				UserDetails userDetails = (UserDetails) acc.getUserDetails();
				model.setName(userDetails.getFullName());
				model.setUserName(acc.getUserName());

				accModelList.add(model);
				finalMap.put(acc.getAssignedDesignations().get(0).getDesignationIds(), accModelList);

			}

		}
		return finalMap;
	}

	@Override
	public List<Designation> getPlanningDesignations(OAuth2Authentication oauth) {

		return customDesignationRepository.findByCodeIn(Arrays.asList("002", "003"));
	}

	@Override
	public ResponseEntity<MessageModel> getPlanningReport(String roleId, String timePeriodId) {

		TimePeriod timePeriod = timePeriodRepository.findOne(timePeriodId);

		Date date = timePeriod.getStartDate();

		Integer month = date.getMonth() + 1;
		Integer year = date.getYear() + 1900;

		Date currentDate = new Date();
		
//		String[] activeProfiles = configurableEnvironment.getActiveProfiles();
//		if(configurableEnvironment.containsProperty("active.profile") && activeProfiles[0].equals(configurableEnvironment.getProperty("active.profile"))){
//			
//			TestingDateConfiguration testDate = testingDateConfigurationRepo.findAllBySlugId(1);
//			currentDate = testDate.getDate();
//			
//		}

		List<PlanningData> datas = planningDataRepository.findByMonthAndYearAndDesgId(month, year, roleId);

		if(datas.isEmpty()){
			
			MessageModel model = new MessageModel();
			model.setMessage(configurableEnvironment.getProperty("no.data.found"));
			model.setStatusCode(204);

			return new ResponseEntity<>(model, HttpStatus.OK);
		}
		
		Map<String, Integer> planningTargetDataMap = datas.stream()
				.collect(Collectors.toMap(plan -> plan.getAccId() + "-" + plan.getFormId(), PlanningData::getTarget));

		/**
		 * get assigned form and users present corresponding to roleId
		 */
		List<DesignationFormMapping> desgFormMappings = designationFormMappingRepository
				.findByDesignationIdAndAccessTypeOrderByFormFormId(roleId, AccessType.DATA_ENTRY);

		Designation desg = designationRepository.findById(roleId);

		HSSFWorkbook workbook = null;
		HSSFSheet sheet = null;
		Row row;
		Cell cell;
		Integer rowNum = 0, colNum = 0;

		try {
			workbook = new HSSFWorkbook();
			sheet = workbook.createSheet("planning-report");
			/*
			 * get style for odd cell
			 */
			CellStyle colStyleOdd = ExcelStyleSheet.getStyleForOddCell(workbook, false);
			/*
			 * get style for even cell
			 */
			CellStyle colStyleEven = ExcelStyleSheet.getStyleForEvenCell(workbook, false);

			/*
			 * get style for heading
			 */
			CellStyle styleForHeading = ExcelStyleSheet.getStyleForPlanningModuleHeading(workbook);

			row = sheet.createRow(rowNum);
			cell = row.createCell(colNum);
			sheet.setColumnWidth(cell.getColumnIndex(), 6000);
			cell.setCellStyle(styleForHeading);
			cell.setCellValue("RANI: Planning Report for the Month of " + new SimpleDateFormat("MMM").format(date));
			sheet = ExcelStyleSheet.doMerge(0, 0, 0, desgFormMappings.size() * 3, sheet);
			rowNum++;

			row = sheet.createRow(rowNum);
			cell = row.createCell(colNum);
			sheet.setColumnWidth(cell.getColumnIndex(), 6000);
			cell.setCellStyle(styleForHeading);
			cell.setCellValue("Role : " + desg.getName());
			sheet = ExcelStyleSheet.doMerge(1, 1, 0, desgFormMappings.size() * 3, sheet);
			rowNum++;

			
			row = sheet.createRow(rowNum);
			cell = row.createCell(colNum);
			sheet.setColumnWidth(cell.getColumnIndex(), 6000);
			cell.setCellStyle(styleForHeading);
			sheet = ExcelStyleSheet.doMerge(2, 2, 0, desgFormMappings.size() * 3, sheet);
			cell.setCellValue("Date of Report Generation : " + new SimpleDateFormat("dd-MM-yyyy").format(currentDate));
			rowNum++;

			row = sheet.createRow(rowNum);
			cell = row.createCell(colNum);
			sheet.setColumnWidth(cell.getColumnIndex(), 6000);
			cell.setCellStyle(styleForHeading);
			cell.setCellValue("Username");
			sheet = ExcelStyleSheet.doMerge(3, 5, 0, 0, sheet);
			colNum++;

			cell = row.createCell(colNum);
			sheet.setColumnWidth(cell.getColumnIndex(), 6000);
			cell.setCellStyle(styleForHeading);
			cell.setCellValue("Name of the Forms");
			sheet = ExcelStyleSheet.doMerge(3, 3, 1, desgFormMappings.size() * 3, sheet);
			rowNum++;

			row = sheet.createRow(rowNum);
			row.setHeight((short) 600);
			colNum = 1;
			for (DesignationFormMapping form : desgFormMappings) {

				cell = row.createCell(colNum);
				sheet.setColumnWidth(cell.getColumnIndex(), 6000);
				cell.setCellStyle(styleForHeading);
				cell.setCellValue(form.getForm().getName().split("-")[1]);
				sheet = ExcelStyleSheet.doMerge(rowNum, rowNum, colNum, colNum + 2, sheet);
				colNum = colNum + 3;

			}

			rowNum++;
			row = sheet.createRow(rowNum);
			colNum = 1;
			for (DesignationFormMapping form : desgFormMappings) {

				cell = row.createCell(colNum);
				sheet.setColumnWidth(cell.getColumnIndex(), 4000);
				cell.setCellStyle(styleForHeading);
				cell.setCellValue("Planned");
				colNum++;

				cell = row.createCell(colNum);
				sheet.setColumnWidth(cell.getColumnIndex(), 4000);
				cell.setCellStyle(styleForHeading);
				cell.setCellValue("Achieved");
				colNum++;
				
				cell = row.createCell(colNum);
				sheet.setColumnWidth(cell.getColumnIndex(), 4000);
				cell.setCellStyle(styleForHeading);
				cell.setCellValue("Percentage");
				colNum++;

			}

			/**
			 * get all the user associated with the role
			 */
			List<Account> accounts = accountRepository
					.findByAssignedDesignationsDesignationIdsInAndEnabledTrue(Arrays.asList(roleId));

			rowNum++;
			for (int i = 0; i < accounts.size(); i++) {

				row = sheet.createRow(rowNum);
				colNum = 0;
				cell = row.createCell(colNum);
				sheet.setColumnWidth(cell.getColumnIndex(), 8000);
				cell.setCellValue(accounts.get(i).getUserName());
				CellStyle styleForEvenCellLock = ExcelStyleSheet.getStyleForEvenCell(workbook, true);
				CellStyle styleForOddCellLock = ExcelStyleSheet.getStyleForOddCell(workbook, true);
				cell.setCellStyle(i % 2 == 0 ? styleForEvenCellLock : styleForOddCellLock);

				rowNum++;

				BigDecimal achValue = null;
				BigDecimal tarValue = null;
				
				Integer targetValue = null;
				Integer achievementValue = null;
				for (int j = 0; j < desgFormMappings.size(); j++) {

					colNum++;
					cell = row.createCell(colNum);
					sheet.setColumnWidth(cell.getColumnIndex(), 4000);
					sheet.setHorizontallyCenter(true);
					cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);

					switch (j) {

					case 0:
					case 1:
					case 2:
					case 3:
						targetValue = planningTargetDataMap
								.get(accounts.get(i).getId() + "-" + desgFormMappings.get(j).getForm().getFormId());
						cell.setCellValue(targetValue != null ? targetValue : 0);

						/**
						 * achievement
						 */
						colNum++;
						if (desgFormMappings.get(j).getForm().getFormId() != 4) {
							// count number of submissions for given timeperiod
							// and role
							achievementValue = getTotalSubmissionCount(desgFormMappings.get(j).getForm().getFormId(),timePeriod,accounts.get(i).getId());
							cell = row.createCell(colNum);
							cell.setCellValue(achievementValue != null ? achievementValue : 0);
							cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
							
							 /**
				             * percentage-- aciev/target *100
				             * if target is 0 than percentage is undefined
				             */
							colNum++;
				            if(targetValue==null || targetValue==0 ){
				            	cell = row.createCell(colNum);
								cell.setCellValue("N/A");
								cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
				            }else{
				            	
				            	achValue=new BigDecimal(achievementValue);
				            	tarValue = new BigDecimal(targetValue);
				            	
				            	BigDecimal perc = achValue.divide(tarValue,3,RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
				            	String value = String.valueOf(new DecimalFormat("##.#").format(Double.parseDouble(String.valueOf(perc))));
				            	
				            	cell = row.createCell(colNum);
								cell.setCellValue(value.toString().concat("%"));
								cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
				            	
				            }
						} else {

							/**
							 * hemocue form of CF -- calculate number of
							 * beginrepeat submission, suppose total 10
							 * submission is present for given timeperiod and
							 * role than count the number of beginrepeat for those 10
							 * submissions
							 */
							Long achvValue = getTotalBeginRepeatCountOfHemocueForm(desgFormMappings.get(j).getForm().getFormId(),timePeriod,accounts.get(i).getId());
							cell = row.createCell(colNum);
							cell.setCellValue(achvValue != null ? achvValue : 0);
							cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
							
			
							 /**
				             * percentage-- aciev/target *100
				             * if target is 0 than percentage is undefined
				             */
							colNum++;
				            if(targetValue==null || targetValue==0 ){
				            	cell = row.createCell(colNum);
								cell.setCellValue("N/A");
								cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
				            }else{
				            	
				            	achValue=new BigDecimal(achvValue);
				            	tarValue = new BigDecimal(targetValue);
				            	
				            	BigDecimal perc = achValue.divide(tarValue,3,RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
				            	String value = String.valueOf(new DecimalFormat("##.#").format(Double.parseDouble(String.valueOf(perc))));
				            	cell = row.createCell(colNum);
								cell.setCellValue(value.toString().concat("%"));
								cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
				            	
				            }
						}

						break;

					case 4:
						UserDetails user = (UserDetails) accounts.get(i).getUserDetails();

						if (user.getIsIFAuser() != null && user.getIsIFAuser() == true) {
							cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
							targetValue = planningTargetDataMap
									.get(accounts.get(i).getId() + "-" + desgFormMappings.get(j).getForm().getFormId());
							cell.setCellValue(targetValue != null ? targetValue : 0);

							colNum++;
							cell = row.createCell(colNum);
							achievementValue = getTotalSubmissionCount(desgFormMappings.get(j).getForm().getFormId(),timePeriod,accounts.get(i).getId());
							cell.setCellValue(achievementValue != null ? achievementValue : 0);
							cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
							
							/**
				             * percentage-- aciev/target *100
				             * if target is 0 than percentage is undefined
				             */
							colNum++;
				            if(targetValue==null || targetValue==0 ){
				            	cell = row.createCell(colNum);
								cell.setCellValue("N/A");
								cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
				            }else{
				            	
				            	achValue=new BigDecimal(achievementValue);
				            	tarValue = new BigDecimal(targetValue);
				            	
				            	BigDecimal perc = achValue.divide(tarValue,3,RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
				            	String value = String.valueOf(new DecimalFormat("##.#").format(Double.parseDouble(String.valueOf(perc))));
				            	cell = row.createCell(colNum);
								cell.setCellValue(value.toString().concat("%"));
								cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
				            	
				            }

						} else {
							// user is not IFA assigned
							cell.setCellStyle(i % 2 == 0 ? styleForEvenCellLock : styleForOddCellLock);
							cell.setCellValue("N/A");
							/**
							 * achievement
							 */
							colNum++;
							cell = row.createCell(colNum);
							cell.setCellStyle(i % 2 == 0 ? styleForEvenCellLock : styleForOddCellLock);
							cell.setCellValue("N/A");
							
							/**
							 * percentage
							 */
							colNum++;
							cell = row.createCell(colNum);
							cell.setCellStyle(i % 2 == 0 ? styleForEvenCellLock : styleForOddCellLock);
							cell.setCellValue("N/A");
						}
						break;

					}

				}

			}

			
			String dir = configurableEnvironment.getProperty("report.path");

			File file = new File(dir);

			/*
			 * make directory if doesn't exist
			 */
			if (!file.exists())
				file.mkdirs();

			String name = configurableEnvironment.getProperty("planning.report.file.name") + "_"
					+ desg.getName().toLowerCase() + "_" + new SimpleDateFormat("ddMMyyyyHHmmsssss").format(currentDate)
					+ ".xls";

			String path = dir + "" + name;

			FileOutputStream fos = new FileOutputStream(new File(path));
			workbook.write(fos);
			fos.close();
			workbook.close();

			MessageModel model = new MessageModel();
			model.setMessage(path);
			model.setStatusCode(200);

			return new ResponseEntity<>(model, HttpStatus.OK);

		} catch (Exception e) {
			log.error("Action : while generating planning report ", e);
			throw new RuntimeException(e);
		}

	}

	private Long getTotalBeginRepeatCountOfHemocueForm(Integer formId, TimePeriod timePeriod,String userId) {

		// @formatter:off

		DBCollection collection = mongoTemplate.getCollection("cFInputFormData");

		DBObject match = new BasicDBObject("$match", new BasicDBObject("formId", formId)
				.append("isValid", true)
				.append("rejected", false)
				.append("submissionCompleteStatus", "C")
				.append("userId", userId)
				.append("timePeriod.timePeriodId", timePeriod.getTimePeriodId()));
		
		DBObject sort = new BasicDBObject("$sort", 
				new BasicDBObject("uniqueId", -1)
				.append("syncDate", -1));
		
		DBObject group = new BasicDBObject("$group",
				new BasicDBObject("_id", null)
				.append("total", 
						new BasicDBObject("$sum", new BasicDBObject("$size","$data.F4BEGINREPEAT001"))));

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
		// @formatter:on
	}

	private Integer getTotalSubmissionCount(Integer formId, TimePeriod timePeriod,String userId) {

		// @formatter:off

		SortOperation sortreviewData = Aggregation.sort(
				 Sort.Direction.DESC, "uniqueId")
				.and(Sort.Direction.DESC,"syncDate");

		MatchOperation match = Aggregation.match(
				Criteria.where("formId").is(formId)
				.and("isValid").is(true)
				.and("submissionCompleteStatus").is("C")
				.and("rejected").is(false)
				.and("timePeriod.timePeriodId").is(timePeriod.getTimePeriodId())
				.and("userId").is(userId));

		AggregationOperation countOperation = Aggregation.group().count().as("count");

		Object obj = mongoTemplate.aggregate(Aggregation.newAggregation(match, sortreviewData, countOperation),
				CFInputFormData.class, Object.class).getMappedResults();

		List<Map<String, Object>> result = (List<Map<String, Object>>) obj;

		if (!result.isEmpty())
			return (Integer) result.get(0).get("count");

		return 0;
		// @formatter:on
	}

	@Override
	public Map<String, QualitativeJSONTableModel> getPlanningAndAchievemntForMobileData(String accId) {

		Account account = accountRepository.findById(accId);
		
		String roleId = account.getAssignedDesignations().get(0).getDesignationIds();
		
		List<PlanningQuestions> planningQuestions = planningQuestionsRepository.findAll();

		Map<Integer, String> planningQuestionsMap = planningQuestions.stream().collect(Collectors.toMap(PlanningQuestions::getFormId, PlanningQuestions::getPlanningQuestion));

		List<String> tableColumn = new ArrayList<>();
		tableColumn.add("formName");
		tableColumn.add("target");
		tableColumn.add("achievement");

		Date currentDate = new Date();
		
//		String[] activeProfiles = configurableEnvironment.getActiveProfiles();
//		if(configurableEnvironment.containsProperty("active.profile") && activeProfiles[0].equals(configurableEnvironment.getProperty("active.profile"))){
//			
//			TestingDateConfiguration testDate = testingDateConfigurationRepo.findAllBySlugId(1);
//			currentDate = testDate.getDate();
//			
//		}
		
		//fetch current-timeperiod
		TimePeriod timePeriod = timePeriodRepository.getCurrentTimePeriod(currentDate,configurableEnvironment.getProperty("timeperiod.periodicity.monthly"));
		
		try {
			
			Integer month = currentDate.getMonth() + 1;
			Integer year = currentDate.getYear() + 1900;

			List<PlanningData> planningDatas = planningDataRepository.findByMonthAndYearAndDesgIdAndAccId(month, year,roleId, accId);

			List<PlanVSAchievementTableModel> managePlanningTableModelList = new ArrayList<>();

			for (PlanningData data : planningDatas) {

				PlanVSAchievementTableModel tableModel = new PlanVSAchievementTableModel();
				tableModel.setFormName(planningQuestionsMap.get(data.getFormId()));
				tableModel.setTarget(data.getTarget());
				
				if(data.getFormId()!=4){
					Integer achievement = getTotalSubmissionCount(data.getFormId(),timePeriod,data.getAccId());
					tableModel.setAchievement(Long.valueOf(achievement));
				}else{
					
					Long achievement = getTotalBeginRepeatCountOfHemocueForm(data.getFormId(),timePeriod,data.getAccId());
					tableModel.setAchievement(achievement);
				}
				
				managePlanningTableModelList.add(tableModel);

			}
			QualitativeJSONTableModel model = new QualitativeJSONTableModel();
			model.setTableColumn(tableColumn);
			model.setTableData(managePlanningTableModelList);
			Map<String, QualitativeJSONTableModel> finalMap = new HashMap<>();

			finalMap.put(new SimpleDateFormat("MMM-yyyy").format(currentDate), model);

			return finalMap;

		} catch (Exception e) {
			log.error("Action : while fetching plan vs achievement data with roleId {} date {} and accountId {} ", roleId,
					currentDate, accId, e);
			throw new RuntimeException(e);
		}

	}

	@Override
	public ResponseEntity<MessageModel> getPlanningReportPDF(String roleId, String timePeriodId) {
		
		String fileName = configurableEnvironment.getProperty("qualitative.report.filename");

		try {
			
			TimePeriod timePeriod = timePeriodRepository.findOne(timePeriodId);

			Date date = timePeriod.getStartDate();

			Integer month = date.getMonth() + 1;
			Integer year = date.getYear() + 1900;

			Date currentDate = new Date();
			
			List<PlanningData> datas = planningDataRepository.findByMonthAndYearAndDesgId(month, year, roleId);

			if(datas.isEmpty()){
				
				MessageModel model = new MessageModel();
				model.setMessage(configurableEnvironment.getProperty("no.data.found"));
				model.setStatusCode(204);

				return new ResponseEntity<>(model, HttpStatus.OK);
			}
			
			String dir = configurableEnvironment.getProperty("qualitative.report.path");

			Map<String, Integer> planningTargetDataMap = datas.stream()
					.collect(Collectors.toMap(plan -> plan.getAccId() + "-" + plan.getFormId(), PlanningData::getTarget));

			/**
			 * get assigned form and users present corresponding to roleId
			 */
			List<DesignationFormMapping> desgFormMappings = designationFormMappingRepository
					.findByDesignationIdAndAccessTypeOrderByFormFormId(roleId, AccessType.DATA_ENTRY);

			Designation desg = designationRepository.findById(roleId);
			
			
			File file = new File(dir);
			/*
			 * make directory if doesn't exist
			 */
			if (!file.exists())
				file.mkdirs();

			
			String name = fileName + "_" + new SimpleDateFormat("ddMMyyyyHHmmss").format(new Date()) + ".pdf";
			String path = dir + "" + name;

			FileOutputStream fos = new FileOutputStream(new File(path));

			Document document = new Document(PageSize.A3.rotate());
			PdfWriter writer = PdfWriter.getInstance(document, fos);
			HeaderFooter headerFooter = new HeaderFooter("Rani", "planning");
			writer.setPageEvent(headerFooter);
			document.open();
			
			Font fontHeader = new Font(FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
			BaseColor headerBgColor = WebColors.getRGBColor("#C5D9F1");
			BaseColor evenColColor = WebColors.getRGBColor("#C0C0C0");
			
            PdfPTable tablea = new PdfPTable(16);
            tablea.setWidthPercentage(100f); // Width 100%
            tablea.setSpacingBefore(5f);
//            float[] widths = new float[] { 2f, 2f, 2f, 2f, 2f,2f, 2f, 2f, 2f, 2f,2f,2f,2f, 2f, 2f,2f };
//            tablea.setWidths(widths);
            tablea.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            
            
            PdfPCell header1 = new PdfPCell(new Phrase("RANI: Planning Report for the Month of " + new SimpleDateFormat("MMM").format(date), fontHeader));
            header1.setColspan(16);
            header1.setVerticalAlignment(Element.ALIGN_MIDDLE);
            header1.setHorizontalAlignment(Element.ALIGN_CENTER);
            header1.setBackgroundColor(headerBgColor);
            header1.setFixedHeight(20f);
            tablea.addCell(header1);
            
            
            PdfPCell header2 = new PdfPCell(new Phrase("Role : " + desg.getName(), fontHeader));
            header2.setColspan(16);
            header2.setVerticalAlignment(Element.ALIGN_MIDDLE);
            header2.setHorizontalAlignment(Element.ALIGN_CENTER);
            header2.setBackgroundColor(headerBgColor);
            header2.setFixedHeight(20f);
            tablea.addCell(header2);
            
            PdfPCell header3 = new PdfPCell(new Phrase("Date of Report Generation : " + new SimpleDateFormat("dd-MM-yyyy").format(currentDate), fontHeader));
            header3.setColspan(16);
            header3.setVerticalAlignment(Element.ALIGN_MIDDLE);
            header3.setHorizontalAlignment(Element.ALIGN_CENTER);
            header3.setBackgroundColor(headerBgColor);
            header3.setFixedHeight(20f);
            tablea.addCell(header3);
            
            PdfPCell userName = new PdfPCell(new Phrase("Username", fontHeader));
            userName.setRowspan(3);	
            userName.setVerticalAlignment(Element.ALIGN_MIDDLE);
            userName.setHorizontalAlignment(Element.ALIGN_CENTER);
            userName.setBackgroundColor(headerBgColor);
            userName.setMinimumHeight(25f);
            tablea.addCell(userName);
            
            userName = new PdfPCell(new Phrase("Name of the Forms", fontHeader));
            userName.setColspan(15);
            userName.setVerticalAlignment(Element.ALIGN_MIDDLE);
            userName.setHorizontalAlignment(Element.ALIGN_CENTER);
            userName.setBackgroundColor(headerBgColor);
            userName.setMinimumHeight(25f);
            tablea.addCell(userName);
            
            
            for( DesignationFormMapping desig:desgFormMappings)
            {
            	
            	 userName = new PdfPCell(new Phrase(desig.getForm().getName(), fontHeader));
                 userName.setColspan(3);
                 userName.setVerticalAlignment(Element.ALIGN_MIDDLE);
                 userName.setHorizontalAlignment(Element.ALIGN_CENTER);
                 userName.setBackgroundColor(headerBgColor);
                 userName.setMinimumHeight(35f);
                 tablea.addCell(userName);
            }
            
            for( int i =0;i<desgFormMappings.size();i++)
            {
            	 userName = new PdfPCell(new Phrase("Planned ", fontHeader));
                 userName.setVerticalAlignment(Element.ALIGN_MIDDLE);
                 userName.setHorizontalAlignment(Element.ALIGN_CENTER);
                 userName.setBackgroundColor(headerBgColor);
                 userName.setMinimumHeight(25f);
                 tablea.addCell(userName);
                 
            	 userName = new PdfPCell(new Phrase("Achieved ", fontHeader));
                 userName.setVerticalAlignment(Element.ALIGN_MIDDLE);
                 userName.setHorizontalAlignment(Element.ALIGN_CENTER);
                 userName.setBackgroundColor(headerBgColor);
                 userName.setMinimumHeight(25f);
                 tablea.addCell(userName);
                 
            	 userName = new PdfPCell(new Phrase("Percentage ", fontHeader));
                 userName.setVerticalAlignment(Element.ALIGN_MIDDLE);
                 userName.setHorizontalAlignment(Element.ALIGN_CENTER);
                 userName.setBackgroundColor(headerBgColor);
                 userName.setMinimumHeight(25f);
                 tablea.addCell(userName);
            }
            
            /**
			 * get all the user associated with the role
			 */
			List<Account> accounts = accountRepository
					.findByAssignedDesignationsDesignationIdsInAndEnabledTrue(Arrays.asList(roleId));

			for (int i = 0; i < accounts.size(); i++) {

				PdfPCell pcell = new PdfPCell(new Phrase(accounts.get(i).getUserName(), fontHeader));
				pcell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
				if(i%2==0)
					pcell.setBackgroundColor(evenColColor);
				pcell.setMinimumHeight(25f);
	            tablea.addCell(pcell);
	            
	            BigDecimal achValue = null;
				BigDecimal tarValue = null;
				
				Integer targetValue = null;
				Integer achievementValue = null;
				
				for (int j = 0; j < desgFormMappings.size(); j++) {

					switch (j) {

					case 0:
					case 1:
					case 2:
					case 3:
						targetValue = planningTargetDataMap
								.get(accounts.get(i).getId() + "-" + desgFormMappings.get(j).getForm().getFormId());

						pcell = new PdfPCell(new Phrase(targetValue != null ? targetValue.toString() : "0", fontHeader));
						pcell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
						if(i%2==0)
							pcell.setBackgroundColor(evenColColor);
						
						pcell.setMinimumHeight(25f);
			            tablea.addCell(pcell);
						
						/**
						 * achievement
						 */
						if (desgFormMappings.get(j).getForm().getFormId() != 4) {
							// count number of submissions for given timeperiod
							// and role
							achievementValue = getTotalSubmissionCount(desgFormMappings.get(j).getForm().getFormId(),timePeriod,accounts.get(i).getId());
							
							pcell = new PdfPCell(new Phrase(achievementValue != null ? achievementValue.toString() : "0", fontHeader));
							pcell.setVerticalAlignment(Element.ALIGN_MIDDLE);
							pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
							if(i%2==0)
								pcell.setBackgroundColor(evenColColor);
							
							pcell.setMinimumHeight(25f);
				            tablea.addCell(pcell);
				            
				            /**
				             * percentage-- aciev/target *100
				             * if target is 0 than percentage is undefined
				             */
				            if(targetValue==null || targetValue==0 ){
				            	pcell = new PdfPCell(new Phrase("N/A", fontHeader));
								pcell.setVerticalAlignment(Element.ALIGN_MIDDLE);
								pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
								if(i%2==0)
									pcell.setBackgroundColor(evenColColor);
								pcell.setMinimumHeight(25f);
					            tablea.addCell(pcell);
				            }else{
				            	
				            	achValue=new BigDecimal(achievementValue);
				            	tarValue = new BigDecimal(targetValue);
				            	
				            	BigDecimal perc = achValue.divide(tarValue,3,RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
				            	String value = String.valueOf(new DecimalFormat("##.#").format(Double.parseDouble(String.valueOf(perc))));
				            	
				            	pcell = new PdfPCell(new Phrase(value.toString().concat("%"), fontHeader));
								pcell.setVerticalAlignment(Element.ALIGN_MIDDLE);
								pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
								if(i%2==0)
									pcell.setBackgroundColor(evenColColor);
								pcell.setMinimumHeight(25f);
					            tablea.addCell(pcell);
				            }
							
						} else {

							/**
							 * hemocue form of CF -- calculate number of
							 * beginrepeat submission, suppose total 10
							 * submission is present for given timeperiod and
							 * role than count the number of beginrepeat for those 10
							 * submissions
							 */
							Long achvValue = getTotalBeginRepeatCountOfHemocueForm(desgFormMappings.get(j).getForm().getFormId(),timePeriod,accounts.get(i).getId());
							
							pcell = new PdfPCell(new Phrase(achvValue != null ? achvValue.toString() : "0", fontHeader));
							pcell.setVerticalAlignment(Element.ALIGN_MIDDLE);
							pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
							if(i%2==0)
								pcell.setBackgroundColor(evenColColor);
							pcell.setMinimumHeight(25f);
				            tablea.addCell(pcell);
							
				            /**
				             * percentage-- aciev/target *100
				             * if(target is 0 than percentage is undefined
				             */
				            
				            if(targetValue==null || targetValue==0){
				            	pcell = new PdfPCell(new Phrase("N/A", fontHeader));
								pcell.setVerticalAlignment(Element.ALIGN_MIDDLE);
								pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
								if(i%2==0)
									pcell.setBackgroundColor(evenColColor);
								pcell.setMinimumHeight(25f);
					            tablea.addCell(pcell);
				            }
				            else{
				            	
				            	achValue=new BigDecimal(achievementValue);
				            	tarValue = new BigDecimal(targetValue);
				            	
				            	BigDecimal perc = achValue.divide(tarValue,3,RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
				            	String value = String.valueOf(new DecimalFormat("##.#").format(Double.parseDouble(String.valueOf(perc))));
				            	
				            	pcell = new PdfPCell(new Phrase(value.toString().concat("%"), fontHeader));
								pcell.setVerticalAlignment(Element.ALIGN_MIDDLE);
								pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
								if(i%2==0)
									pcell.setBackgroundColor(evenColColor);
								pcell.setMinimumHeight(25f);
					            tablea.addCell(pcell);
				            }
				            
				            
						}

						break;

					case 4:
						UserDetails user = (UserDetails) accounts.get(i).getUserDetails();

						if (user.getIsIFAuser() != null && user.getIsIFAuser() == true) {
							targetValue = planningTargetDataMap
									.get(accounts.get(i).getId() + "-" + desgFormMappings.get(j).getForm().getFormId());

							pcell = new PdfPCell(new Phrase(targetValue != null ? targetValue.toString() : "0", fontHeader));
							pcell.setVerticalAlignment(Element.ALIGN_MIDDLE);
							pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
							if(i%2==0)
								pcell.setBackgroundColor(evenColColor);
							pcell.setMinimumHeight(25f);
				            tablea.addCell(pcell);
							
							achievementValue = getTotalSubmissionCount(desgFormMappings.get(j).getForm().getFormId(),timePeriod,accounts.get(i).getId());
							
							pcell = new PdfPCell(new Phrase(achievementValue != null ? achievementValue.toString() : "0", fontHeader));
							pcell.setVerticalAlignment(Element.ALIGN_MIDDLE);
							pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
							if(i%2==0)
								pcell.setBackgroundColor(evenColColor);
							pcell.setMinimumHeight(25f);
				            tablea.addCell(pcell);
				            
				            /**
				             * percentage-- aciev/target *100
				             * if(target is 0 than percentage is undefined
				             */
				            
				            if(targetValue==null || targetValue==0){
				            	pcell = new PdfPCell(new Phrase("N/A", fontHeader));
								pcell.setVerticalAlignment(Element.ALIGN_MIDDLE);
								pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
								if(i%2==0)
									pcell.setBackgroundColor(evenColColor);
								pcell.setMinimumHeight(25f);
					            tablea.addCell(pcell);
				            } 
				            else{
				            	
				            	achValue=new BigDecimal(achievementValue);
				            	tarValue = new BigDecimal(targetValue);
				            	
				            	BigDecimal perc = achValue.divide(tarValue,3,RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
				            	String value = String.valueOf(new DecimalFormat("##.#").format(Double.parseDouble(String.valueOf(perc))));
				            	
				            	pcell = new PdfPCell(new Phrase(value.toString().concat("%"), fontHeader));
								pcell.setVerticalAlignment(Element.ALIGN_MIDDLE);
								pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
								if(i%2==0)
									pcell.setBackgroundColor(evenColColor);
								pcell.setMinimumHeight(25f);
					            tablea.addCell(pcell);
				            }
				            
				            
				            
						} else {
							// user is not IFA assigned
							
							pcell = new PdfPCell(new Phrase("N/A", fontHeader));
							pcell.setVerticalAlignment(Element.ALIGN_MIDDLE);
							pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
							if(i%2==0)
								pcell.setBackgroundColor(evenColColor);
							pcell.setMinimumHeight(25f);
				            tablea.addCell(pcell);
				            
							/**
							 * achievement
							 */
				            pcell = new PdfPCell(new Phrase("N/A", fontHeader));
							pcell.setVerticalAlignment(Element.ALIGN_MIDDLE);
							pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
							if(i%2==0)
								pcell.setBackgroundColor(evenColColor);
							pcell.setMinimumHeight(25f);
				            tablea.addCell(pcell);
				            
				            /**
							 * Percentage
							 */
				            pcell = new PdfPCell(new Phrase("N/A", fontHeader));
							pcell.setVerticalAlignment(Element.ALIGN_MIDDLE);
							pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
							if(i%2==0)
								pcell.setBackgroundColor(evenColColor);
							pcell.setMinimumHeight(25f);
				            tablea.addCell(pcell);
						}
						break;

					}

				}

			}
            
//			Paragraph p = new Paragraph();
//			p.add("          ");
//			p.setSpacingBefore(1);
//			p.setSpacingAfter(30);
//			document.add(p);
			
            document.add(tablea);
			MessageModel model = new MessageModel();
			model.setStatusCode(200);
			model.setMessage(path);
			
			document.close();
			return new ResponseEntity<>(model,HttpStatus.OK);
			
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		
	}
}
