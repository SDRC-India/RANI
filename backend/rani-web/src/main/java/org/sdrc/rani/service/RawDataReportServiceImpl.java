package org.sdrc.rani.service;

import java.io.File;
import java.io.FileOutputStream;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.sdrc.rani.document.Area;
import org.sdrc.rani.document.CFInputFormData;
import org.sdrc.rani.document.DesignationFormMapping;
import org.sdrc.rani.document.IFASupplyPointMapping;
import org.sdrc.rani.models.SubmissionStatus;
import org.sdrc.rani.models.UserModel;
import org.sdrc.rani.repositories.AreaRepository;
import org.sdrc.rani.repositories.CFInputFormDataRepository;
import org.sdrc.rani.repositories.DesignationFormMappingRepository;
import org.sdrc.rani.repositories.IFASupplyPointMappingRepository;
import org.sdrc.rani.util.ExcelStyleSheet;
import org.sdrc.rani.util.TokenInfoExtracter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

import in.co.sdrc.sdrcdatacollector.document.EnginesForm;
import in.co.sdrc.sdrcdatacollector.document.Question;
import in.co.sdrc.sdrcdatacollector.document.TypeDetail;
import in.co.sdrc.sdrcdatacollector.models.AccessType;
import in.co.sdrc.sdrcdatacollector.models.FormAttachmentsModel;
import in.co.sdrc.sdrcdatacollector.models.MessageModel;
import in.co.sdrc.sdrcdatacollector.mongorepositories.EngineFormRepository;
import in.co.sdrc.sdrcdatacollector.mongorepositories.QuestionRepository;
import in.co.sdrc.sdrcdatacollector.mongorepositories.TypeDetailRepository;
import in.co.sdrc.sdrcdatacollector.util.Status;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 * 
 */
@Service
@Slf4j
public class RawDataReportServiceImpl implements RawDataReportService {

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private ConfigurableEnvironment configurableEnvironment;

	@Autowired
	private TypeDetailRepository typeDetailRepository;

	@Autowired
	private EngineFormRepository formRepository;

	@Autowired
	private AreaRepository areaRepository;

	@Autowired
	private DesignationFormMappingRepository designationFormMappingRepository;

	@Autowired
	private CFInputFormDataRepository cfinputFormDataRepository;

	@Autowired
	private TokenInfoExtracter tokenInfoExtracter;

	@Autowired
	private IFASupplyPointMappingRepository ifaSupplyPointMappingRepository;
	
	private SimpleDateFormat sdfDateTimeWithSeconds = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private SimpleDateFormat sDateFormat = new SimpleDateFormat("dd-MM-yyyy");

	@Override
	public ResponseEntity<MessageModel> exportRawaData(Integer formId, String sDate, String eDate, Principal principal,
			OAuth2Authentication auth) {

		try {

			sDate = sDate.concat(" 00:00:00");
			eDate = eDate.concat(" 23:59:59");

			Date startDate = sdfDateTimeWithSeconds.parse(sDate);

			Date endDate = sdfDateTimeWithSeconds.parse(eDate);

			List<Question> questionss = questionRepository.findAllByFormIdAndActiveTrueOrderByQuestionOrderAsc(formId);

			List<Question> questionList = questionss.stream().filter(v -> !"heading".equals(v.getControllerType()))
					.collect(Collectors.toList());

			String result = "";

//			isRejected is true and isValid is false
			List<CFInputFormData> datas = cfinputFormDataRepository
					.findAllByFormIdAndAndIsDeletedFalseAndSyncDateBetween(formId, startDate,endDate);
			
			/**
			 * if no data available return
			 */
			if (datas.isEmpty()) {

				MessageModel setMessageModel = setMessageModelDataNotFound();
				return new ResponseEntity<>(setMessageModel, HttpStatus.OK);
			}

			result = getExportDatasResult(datas, questionList, formId, startDate, endDate, principal);

			MessageModel model = new MessageModel();
			model.setMessage(result);
			model.setStatusCode(200);

			return new ResponseEntity<>(model, HttpStatus.OK);

		} catch (Exception e) {
			log.error("Error while generating Raw Data Report with payload {},{},{}", formId, sDate, eDate, e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	/**
	 * setting the message model here when list is empty
	 * 
	 * @return
	 */
	public MessageModel setMessageModelDataNotFound() {

		MessageModel model = new MessageModel();
		model.setMessage(configurableEnvironment.getProperty("no.data.found"));
		model.setStatusCode(204);
		return model;
	}

	private String exportData(List<CFInputFormData> list, List<Question> questionList, int formId, String sheetName, String fileName,
			Date startDate, Date endDate, String formName, Principal principal) {

	
		/*
		 * column name as a key
		 */
		Map<String, Question> questionMap = questionList.stream()
				.collect(Collectors.toMap(Question::getColumnName, question -> question));

		/*
		 * getting all the typeDetails by passing formId
		 */
		List<TypeDetail> typeDetailList = typeDetailRepository.findByFormId(formId);

		/*
		 * KEY-SlugId value-name
		 */
		Map<Integer, TypeDetail> typeDetailMap = typeDetailList.stream()
				.collect(Collectors.toMap(TypeDetail::getSlugId, typeDe -> typeDe));

		/**
		 * get all Area(only areaid and areaname)
		 */
		List<Area> areaList = areaRepository.findAreaIdAndAreaName();

		Map<Integer, String> areaMap = areaList.stream().collect(Collectors.toMap(Area::getAreaId, Area::getAreaName));
		
		List<IFASupplyPointMapping> ifaSupplyPointMapping = ifaSupplyPointMappingRepository.findAll();
		
		Map<Integer, String> ifaSupplyPointMap = ifaSupplyPointMapping.stream().collect(Collectors.toMap(IFASupplyPointMapping::getSlugId, IFASupplyPointMapping::getName));
		
		try {

			HSSFWorkbook workbook = null;
			HSSFSheet sheet = null;
			Row row;
			Cell cell;

			/*
			 * creating a questions heading
			 */
			workbook = setQuestions(workbook, sheet, questionList, sheetName, startDate, endDate, formName);

			/*
			 * get style for odd cell
			 */
			CellStyle colStyleOdd = ExcelStyleSheet.getStyleForOddCell(workbook,false);
			/*
			 * get style for even cell
			 */
			CellStyle colStyleEven = ExcelStyleSheet.getStyleForEvenCell(workbook,false);
			/*
			 * Iterating to set submission values
			 */
			Integer rowIndex = 4;
			Integer columnIndex = 0;
			Integer mapSize = null;

			/*
			 * begin-repeat variables
			 */
			Row beginRepeatRow = null;
			Cell beginRepeatcell = null;
			Integer beginRepeatRowCount = 1;
			Integer beginRepeatcellCount = 0;
			for (int i = 0; i < list.size(); i++) {

				sheet = workbook.getSheet(sheetName);
				row = sheet.createRow(rowIndex);
				/*
				 * setting serial number value
				 */
				cell = row.createCell(columnIndex);
				cell.setCellValue(i + 1);
				cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
				columnIndex++;

				CFInputFormData cfInputFormData = null;

				/*
				 * key is column name here,object is submitted value
				 */
				Map<String, Object> submissionDataMap = null;

				/*
				 * checking the type of List
				 */
				if (list.get(i) instanceof CFInputFormData) {
					cfInputFormData = (CFInputFormData) list.get(i);
					submissionDataMap = cfInputFormData.getData();
					/*
					 * setting user name
					 */
					cell = row.createCell(columnIndex);
					cell.setCellValue(cfInputFormData.getUserName());
					cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
					columnIndex++;
					/*
					 * setting submission date
					 */
					cell = row.createCell(columnIndex);
					cell.setCellValue(sDateFormat.format(cfInputFormData.getSyncDate()));
					cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
					columnIndex++;

					/**
					 * setting valid record status
					 */
					cell = row.createCell(columnIndex);
					cell.setCellValue(cfInputFormData.getIsValid() == true ? "Valid" : "Invalid");
					cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
					columnIndex++;

					/**
					 * setting submission status
					 */
					cell = row.createCell(columnIndex);
					cell.setCellValue(cfInputFormData.getSubmissionStatus().toString());
					cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
					columnIndex++;

					/**
					 * setting rejection status-
					 */
					cell = row.createCell(columnIndex);
					cell.setCellValue(cfInputFormData.isRejected() == false ? "Approved" : "Rejected");
					cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
					columnIndex++;

					/**
					 * setting aggregation status-
					 */
					cell = row.createCell(columnIndex);
					cell.setCellValue(cfInputFormData.getIsAggregated() == true ? "Aggregated" : "Not aggregated");
					cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
					columnIndex++;

					/**
					 * setting unique id
					 */
					cell = row.createCell(columnIndex);
					cell.setCellValue(cfInputFormData.getUniqueId());
					cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
					columnIndex++;
				}

				for (int j = 0; j < questionList.size(); j++) {

//					System.out.println("row count ===========" + j);
					String fieldValue = "";
					String colName = null;

					Question question = questionList.get(j);

					switch (question.getControllerType()) {

					case "dropdown":
					case "segment": {
						/*
						 * get sheet
						 */
						sheet = workbook.getSheet(sheetName);
						/*
						 * get column name
						 */
						colName = question.getColumnName();

						/*
						 * with column name against find value which is SlugId
						 * of TypeDetail documnet
						 */
						List<Integer> values = null;

						if (question.getFieldType().equals("checkbox")) {
							values = submissionDataMap.get(colName) != null
									? (List<Integer>) (submissionDataMap.get(colName)) : null;
						} else {
							Integer v = submissionDataMap.get(colName) != null ? (int) (submissionDataMap.get(colName))
									: null;
							if (v != null) {
								values = new ArrayList<>();
								values.add(v);
							}
						}

						if (values != null && !values.isEmpty()) {
							/*
							 * find typedetailname against this value
							 */
							String temp = null;
							for (Integer value : values) {

								if (question.getTableName() == null) {
									/*
									 * if there is no score present in
									 * typeDetails
									 */
									if (typeDetailMap.get(value).getScore() == null)
										temp = typeDetailMap.get(value).getName();
									else// if score is present
									{
										temp = typeDetailMap.get(value).getName().concat(" - score = ")
												.concat(typeDetailMap.get(value).getScore());
									}
								}

								else {
									/*
									 * find areaname against this value
									 */
									// area_group:F1Q2@ANDfetch_tables:area$$arealevel=district
									switch (question.getTableName().split("\\$\\$")[0].trim()) {

									case "area": {

										temp = areaMap.get(value);
									}
										break;
										
									case "ifasupply":{
										temp=ifaSupplyPointMap.get(value);
									}
									break;

									}
								}

								if (fieldValue.equals("")) {
									fieldValue = temp;
								} else
									fieldValue = fieldValue + "," + temp;
							}

						}

						// System.out.println(question.getQuestion()+":"+"COLUMN-NAME"+colName+":"+fieldValue
						// +"colcount ===="+columnIndex);

						cell = row.createCell(columnIndex);

//						System.out.println("fieldValue =====" + fieldValue + "column index === " + cell.getColumnIndex());

						cell.setCellValue(fieldValue != null ? fieldValue : "");
						cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
						columnIndex++;
						fieldValue = "";
					}
						break;

					case "beginrepeat": {
						String brSheeetName = null;
						brSheeetName = question.getQuestion();
						/*
						 * check the size of brSheeetName is size>=28 than
						 * reduce the sheetName
						 */
						if (brSheeetName.length() >= 28) {
							brSheeetName = brSheeetName.substring(0, 17);
						}
						/*
						 * it returns rowValue to be used
						 */
						sheet = workbook.getSheet(brSheeetName);
						/*
						 * get last row num creted in the sheet
						 */
						int rowValue = sheet.getLastRowNum();
						/*
						 * assign to beginRepeatRowCount to create row
						 */
						beginRepeatRowCount = rowValue + 1;
						/*
						 * get column name of question
						 */
						colName = question.getColumnName();
						/*
						 * beginRepeat type
						 */
						List<Map<String, Object>> beginRepeatMap = (List<Map<String, Object>>) submissionDataMap.get(colName);

						/*
						 * get size of map
						 */
						Map<String, Object> map2 = beginRepeatMap.get(0);
						mapSize = map2.size();
						int k = 1;
						/*
						 * iterating list of submission of beginrepeat, list
						 */
						for (Map<String, Object> map : beginRepeatMap) {

							/*
							 * creating row
							 */
							beginRepeatRow = sheet.createRow(beginRepeatRowCount);
							/*
							 * creating cell
							 */
							beginRepeatcell = beginRepeatRow.createCell(beginRepeatcellCount);
							/*
							 * setting reference-id column value
							 */
							beginRepeatcell.setCellValue(i + 1);
							/*
							 * setting style
							 */
							beginRepeatcell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
							beginRepeatcellCount++;
							
							/**
							 * unique id value setting
							 */
							beginRepeatcell = beginRepeatRow.createCell(beginRepeatcellCount);
							beginRepeatcell.setCellValue(cfInputFormData.getUniqueId());
							beginRepeatcell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
							beginRepeatcellCount++;
							/*
							 * iterating Map of beginrepeat
							 */
							for (Map.Entry<String, Object> entry : map.entrySet()) {

								if (k == 1)
									j++;// for question-loop it should increment
										// with size 1 only as value is multiple
										// but qstn is one

								switch (questionMap.get(entry.getKey()).getControllerType()) {

								case "dropdown":
								case "segment": {

									/*
									 * key == column name with column name
									 * against find value which is SlugId of
									 * TypeDetail documnet
									 */

									List<Integer> values = null;
									if (questionMap.get(entry.getKey()).getFieldType().equals("checkbox")) {
										values = entry.getValue() != null ? (List<Integer>) entry.getValue() : null;

									} else {
										Integer v=null;
										try {
											v = entry.getValue() != null ? Integer.valueOf(entry.getValue().toString()) : null;
										}catch(NumberFormatException e) {
											Double dd = entry.getValue() != null ? Double.valueOf(entry.getValue().toString()) : null;
											v=(int) Math.round(dd);
										}
										
										if (v != null) {
											values = new ArrayList<>();
											values.add(v);
										}

									}

									if (values != null && !values.isEmpty()) {
										String tempValue = null;
										/*
										 * find typedetailname against this
										 * value
										 */
										for (Integer value : values) {

											if (questionMap.get(entry.getKey()).getTableName() == null) {
												tempValue = typeDetailMap.get(value).getName();
											}

											else {
												/*
												 * find areaname against this
												 * value
												 */

												switch (questionMap.get(entry.getKey()).getTableName()
														.split("\\$\\$")[0].trim()) {

												case "area": {

													tempValue = areaMap.get(value);
												}
													break;

												}
											}

											if (fieldValue.equals("")) {
												fieldValue = tempValue;
											} else
												fieldValue = fieldValue + "," + tempValue;

										}

									}

									/*
									 * write in excel cell
									 */
									beginRepeatcell = beginRepeatRow.createCell(beginRepeatcellCount);
									beginRepeatcell.setCellValue(fieldValue != null ? fieldValue : "");
									beginRepeatcell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
									fieldValue = "";
									beginRepeatcellCount++;

								}
									break;

								default: {
//
//									System.out.println("fieldValue =====" + fieldValue + "column index === "
//											+ cell.getColumnIndex());
									try {
										fieldValue = entry.getValue() != null ? (String) entry.getValue() : null;
									} catch (ClassCastException e) {
										try {
											fieldValue = entry.getValue() != null
													? String.valueOf((Integer) entry.getValue()) : null;
										}catch(ClassCastException e1) {
											
											fieldValue = entry.getValue() != null
													? String.valueOf((Double) entry.getValue()) : null;
										}
										
									}

									/*
									 * write in excel cell
									 */
									beginRepeatcell = beginRepeatRow.createCell(beginRepeatcellCount);
									beginRepeatcell.setCellValue(fieldValue != null ? fieldValue : "");
									beginRepeatcell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
									fieldValue = "";
									beginRepeatcellCount++;
								}

								}// end of switch

							} // end of map - innerloop

							if (beginRepeatMap.size() > 1) {
								beginRepeatRowCount++;
								beginRepeatcellCount = 0;
							}
							k++;
						}

						beginRepeatcellCount = 0;
					}
						break;

					case "tableWithRowWiseArithmetic": {
						/*
						 * getting sheet
						 */
						sheet = workbook.getSheet(sheetName);
						/*
						 * column name of question
						 */
						colName = question.getColumnName();
						/*
						 * tableWithRowWiseArithmetic type
						 */
						List<Map<String, Object>> beginRepeatMap = (List<Map<String, Object>>) submissionDataMap.get(colName);

						for (Map<String, Object> map : beginRepeatMap) {

							for (Map.Entry<String, Object> entry : map.entrySet()) {

								j++;// for question-loop

								switch (questionMap.get(entry.getKey()).getControllerType()) {

								case "dropdown":
								case "segment": {

									// System.out.println("row num = "+j);
									/*
									 * key == column name with column name
									 * against find value which is SlugId of
									 * TypeDetail documnet
									 */

									List<Integer> values = null;
									if (questionMap.get(entry.getKey()).getFieldType().equals("checkbox")) {

										values = entry.getValue() != null ? (List<Integer>) entry.getValue() : null;

									} else {
										Integer v = entry.getValue() != null ? (int) entry.getValue() : null;

										if (v != null) {
											values = new ArrayList<>();
											values.add(v);
										}

									}

									if (values != null && !values.isEmpty()) {
										/*
										 * find typedetailname against this
										 * value
										 */
										for (Integer value : values) {

											String tempValue = null;

											if (questionMap.get(entry.getKey()).getTableName() == null)
												tempValue = typeDetailMap.get(value).getName();
											else {
												/*
												 * find areaname against this
												 * value
												 */
												switch (questionMap.get(entry.getKey()).getTableName()
														.split("\\$\\$")[0].trim()) {

												case "area": {

													tempValue = areaMap.get(value);
												}
													break;

												}
											}

											if (fieldValue.equals("")) {
												fieldValue = tempValue;
											} else
												fieldValue = fieldValue + "," + tempValue;
										}

									}

									// System.out.println(question.getQuestion()+":"+"COLUMN-NAME"+colName+":"+fieldValue
									// +"colcount ===="+columnIndex);
									/*
									 * write in excel cell
									 */
									cell = row.createCell(columnIndex);
									cell.setCellValue(fieldValue != null ? fieldValue : "");
									cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
									fieldValue = "";
									columnIndex++;

								}
									break;

								default: {

									fieldValue = entry.getValue() != null ? (String) entry.getValue() : null;
									/**
									 * write in excel cell
									 */
									// System.out.println(question.getQuestion()+":"+"COLUMN-NAME"+colName+":"+fieldValue
									// +"colcount ===="+columnIndex);
									cell = row.createCell(columnIndex);
									cell.setCellValue(fieldValue != null ? fieldValue : "");
									cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
									fieldValue = "";
									columnIndex++;
								}

								}// end of switch

							} // end of map - innerloop
						}
					}
						break;

					case "camera": {
						
						sheet = workbook.getSheet(sheetName);
						CreationHelper createHelper = workbook.getCreationHelper();

						String attachFileName = "";

						switch (formId) {

						case 1: 
						case 7:{
							Map<String, List<FormAttachmentsModel>> attachments = cfInputFormData.getAttachments();

							if (attachments != null) {
								for (FormAttachmentsModel model : attachments.get(question.getColumnName())) {
									if (attachFileName.equals(""))
										attachFileName = model.getFilePath();
									else
										attachFileName = attachFileName.concat(",").concat(model.getFilePath());
								}
							}

							List<String> commaSeperatedFilePath = Stream.of(attachFileName.split(",")).map(String::trim)
									.collect(Collectors.toList());

							for (int count = 1; count<=commaSeperatedFilePath.size(); count++) {

								cell = row.createCell(columnIndex);
								String linkAddress = configurableEnvironment.getProperty("image.download.path");
								cell.setCellValue("download image-" + count);
								linkAddress = linkAddress.concat("?path=" + Base64.getUrlEncoder().encodeToString(commaSeperatedFilePath.get(count-1).getBytes()));
								HSSFHyperlink link = (HSSFHyperlink) createHelper.createHyperlink(HyperlinkType.URL);
								link.setAddress(linkAddress);
								cell.setHyperlink(link);
								cell.setCellStyle(i % 2 == 0 ? ExcelStyleSheet.getStyleForEvenHyperLink(workbook) :  ExcelStyleSheet.getStyleForOddHyperLink(workbook));
								fieldValue = "";
								columnIndex++;

							}

							if (Integer.parseInt(question.getConstraints().split(":")[1]) != commaSeperatedFilePath.size()) {

								for (int v = 0; v < Integer.parseInt(question.getConstraints().split(":")[1])- commaSeperatedFilePath.size(); v++) {
									columnIndex++;
								}

							}

						}

							break;

						}
					}
						break;

					default: {

						sheet = workbook.getSheet(sheetName);

						colName = question.getColumnName();
						/*
						 * fieldValue- type int or string and writing in excel
						 */
						cell = row.createCell(columnIndex);

						if (question.getFieldType().equals("tel")) {

							if (submissionDataMap.get(colName) == null
									|| submissionDataMap.get(colName).toString().trim().equals("")) {
								cell.setCellType(CellType.STRING);
								cell.setCellValue("");
								cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);

							} else {

								fieldValue = submissionDataMap.get(colName).toString();
								/*
								 * check length of value if it is >9 make it
								 * string and write it in excel else make it
								 * Long type
								 */
								if (fieldValue.length() > 9) {
									cell.setCellType(CellType.STRING);
									cell.setCellValue(fieldValue);
									cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
									fieldValue = "";
								} else {
									cell.setCellValue((Long.valueOf(fieldValue)));
									cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
									fieldValue = "";
								}

							}

						} else if (question.getFieldType().equals("singledecimal")
								|| question.getFieldType().equals("doubledecimal")
								|| question.getFieldType().equals("threedecimal")) {

							if (submissionDataMap.get(colName) == null
									|| submissionDataMap.get(colName).toString().trim().equals("")) {
								cell.setCellType(CellType.STRING);
								cell.setCellValue("");
							} else {

								cell.setCellValue(Double.valueOf((String) submissionDataMap.get(colName)));
							}

							cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);

						} else {

							if (submissionDataMap.get(colName) == null
									|| submissionDataMap.get(colName).toString().trim().equals("")) {
								cell.setCellType(CellType.STRING);
								cell.setCellValue("");

							} else {
								fieldValue = (String) submissionDataMap.get(colName);
								if (formId == 5 && fieldValue.length() > 30) {

									sheet.setColumnWidth(cell.getColumnIndex(), 12000);
								}
								cell.setCellValue(fieldValue);
							}

							cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
							fieldValue = "";
						}
						columnIndex++;
					}
					}
				}
				rowIndex++;
				columnIndex = 0;
			}

			String dir = configurableEnvironment.getProperty("report.path");

			File file = new File(dir);

			/*
			 * make directory if doesn't exist
			 */
			if (!file.exists())
				file.mkdirs();

			String name = fileName + "_" + principal.getName() + "_"
					+ new SimpleDateFormat("ddMMyyyyHHmmss").format(new Date()) + ".xls";
			String path = dir + "" + name;

			FileOutputStream fos = new FileOutputStream(new File(path));
			workbook.write(fos);
			fos.close();
			workbook.close();
			return path;

		} catch (Exception e) {
			log.error("error while generating report with payload formId : {}, startdate {}, enddate{} : " + formId,
					startDate, endDate, e);
			throw new RuntimeException(e);
		}

	}

	/**
	 * setting all the questions in excel first row and making different sheets
	 * for begin repeat type
	 * 
	 * @param workbook
	 * @param sheet
	 * @param questionList
	 * @param sheetName
	 * @return
	 */
	private HSSFWorkbook setQuestions(HSSFWorkbook workbook, HSSFSheet sheet, List<Question> questionList,
			String sheetName, Date startDate, Date endDate, String formName) {

		/**
		 * make parent-id as a key, for begin-repeat
		 */
		List<Question> qList = new ArrayList<>();

		Map<String, List<Question>> questionMapBr = new LinkedHashMap<>();
		for (Question q : questionList) {

			if (questionMapBr.containsKey(q.getParentColumnName())) {
				questionMapBr.get(q.getParentColumnName()).add(q);
			} else {
				qList = new ArrayList<>();
				qList.add(q);
				questionMapBr.put(q.getParentColumnName(), qList);
			}

		}

		int mergeRange = 0;
		workbook = new HSSFWorkbook();
		sheet = workbook.createSheet(sheetName);
		Row row;
		Cell cell;
		/*
		 * style for headers
		 */
		// CellStyle headerStyle =
		// ExcelStyleSheet.getStyleForColorHeader(workbook);
		CellStyle styleForHeading = ExcelStyleSheet.getStyleForHeading(workbook);

		try {

			/*
			 * SINO
			 */
			row = sheet.createRow(3);
			row.setHeight((short) 1500);
			cell = row.createCell(0);
			sheet.setColumnWidth(cell.getColumnIndex(), 1700);
			sheet.setHorizontallyCenter(true);
			cell.setCellStyle(styleForHeading);
			cell.setCellValue("Sl. No.");
			mergeRange++;

			/*
			 * user name
			 */
			cell = row.createCell(1);
			sheet.setColumnWidth(cell.getColumnIndex(), 3500);
			sheet.setHorizontallyCenter(true);
			cell.setCellStyle(styleForHeading);
			cell.setCellValue("User Name");
			mergeRange++;
			/*
			 * submission date
			 */
			cell = row.createCell(2);
			sheet.setColumnWidth(cell.getColumnIndex(), 4000);
			sheet.setHorizontallyCenter(true);
			cell.setCellStyle(styleForHeading);
			cell.setCellValue("Submission Date");
			mergeRange++;
			
			/*
			 * submission date
			 */
			cell = row.createCell(3);
			sheet.setColumnWidth(cell.getColumnIndex(), 4000);
			sheet.setHorizontallyCenter(true);
			cell.setCellStyle(styleForHeading);
			cell.setCellValue("Valid/Invalid Record");
			mergeRange++;
			
			/**
			 * submission status---on/late submission
			 */
			cell = row.createCell(4);
			sheet.setColumnWidth(cell.getColumnIndex(), 4000);
			sheet.setHorizontallyCenter(true);
			cell.setCellStyle(styleForHeading);
			cell.setCellValue("Submission Status");
			mergeRange++;
			
			/**
			 * rejection status-true or false
			 */
			cell = row.createCell(5);
			sheet.setColumnWidth(cell.getColumnIndex(), 4000);
			sheet.setHorizontallyCenter(true);
			cell.setCellStyle(styleForHeading);
			cell.setCellValue("Rejected/Approved Status");
			mergeRange++;
			
			/**
			 * aggregation status -- true or false
			 */
			cell = row.createCell(6);
			sheet.setColumnWidth(cell.getColumnIndex(), 4000);
			sheet.setHorizontallyCenter(true);
			cell.setCellStyle(styleForHeading);
			cell.setCellValue("Aggregation Status");
			mergeRange++;
			
			/**
			 * unique-id value
			 */
			cell = row.createCell(7);
			sheet.setColumnWidth(cell.getColumnIndex(), 4000);
			sheet.setHorizontallyCenter(true);
			cell.setCellStyle(styleForHeading);
			cell.setCellValue("Unique-Id");
			mergeRange++;


			/*
			 * Setting all the questions
			 */
			int columnIndex = 8;
			int brColumnIndex = 1;
			String qstnValue = null;
			for (int i = 0; i < questionList.size(); i++) {

				Question question = questionList.get(i);

				switch (question.getControllerType()) {

				case "beginrepeat": {

					String brSheeetName = null;
					brSheeetName = question.getQuestion();
					/*
					 * check the size of brSheeetName is size>=28 than reduce
					 * the sheetName
					 */
					if (brSheeetName.length() >= 28) {
						brSheeetName = brSheeetName.substring(0, 17);
					}
					brColumnIndex = 2;
					
					sheet = workbook.createSheet(brSheeetName);
					Row row1 = sheet.createRow(0);
					
					Cell cell1 = row1.createCell(0);
					sheet.setColumnWidth(cell1.getColumnIndex(), 4000);
					sheet.setHorizontallyCenter(true);
					cell1.setCellStyle(styleForHeading);
					cell1.setCellValue("Reference_Id");

					cell1 = row1.createCell(1);
					sheet.setColumnWidth(cell1.getColumnIndex(), 4000);
					sheet.setHorizontallyCenter(true);
					cell1.setCellStyle(styleForHeading);
					cell1.setCellValue("Unique Id");
					
					List<Question> qHeaderlist = questionMapBr.get(question.getColumnName());

					for (Question que : qHeaderlist) {

						cell1 = row1.createCell(brColumnIndex);
						cell1.setCellStyle(styleForHeading);
						cell1.setCellValue(que.getQuestion());
						sheet.setColumnWidth(cell1.getColumnIndex(), 4000);

						brColumnIndex++;
						i++;
					}

				}
					break;

				case "tableWithRowWiseArithmetic": {

					sheet = workbook.getSheet(sheetName);
					qstnValue = question.getQuestion();

					List<Question> qHeaderlist = questionMapBr.get(question.getColumnName());

					for (Question que : qHeaderlist) {

						cell = row.createCell(columnIndex);
						cell.setCellStyle(styleForHeading);
						cell.setCellValue(qstnValue.concat("-") + que.getQuestion().replaceAll("@@split@@", "-"));
						sheet.setColumnWidth(cell.getColumnIndex(), 9500);
						columnIndex++;
						i++;
						mergeRange++;
					}

				}
					break;

				case "camera": {
					sheet = workbook.getSheet(sheetName);
					/**
					 * check image count, and make question accordingly
					 */

					String split = question.getConstraints().split(":")[1];

					for (Integer count = 1; count <= Integer.parseInt(split); count++) {

						cell = row.createCell(columnIndex);
						cell.setCellStyle(styleForHeading);
						cell.setCellValue(question.getQuestion().concat("-" + count));
						sheet.setColumnWidth(cell.getColumnIndex(), 4000);
						columnIndex++;
						mergeRange++;
					}

				}
					break;

				default: {

					sheet = workbook.getSheet(sheetName);
					cell = row.createCell(columnIndex);
					cell.setCellStyle(styleForHeading);
					cell.setCellValue(question.getQuestion());
					sheet.setColumnWidth(cell.getColumnIndex(), 4000);
					columnIndex++;
					mergeRange++;
				}

				}

			}
		} catch (Exception e) {

			log.error("error : ", e);
			throw new RuntimeException(e);
		}

		sheet = workbook.getSheet(sheetName);
		/*
		 * heading for raw data
		 */
		row = sheet.createRow(0);
		cell = row.createCell(0);
		cell.setCellStyle(styleForHeading);
		cell.setCellValue(
				configurableEnvironment.getProperty("project.name") + " : Submission Report " + "(" + formName + ")");
		sheet = ExcelStyleSheet.doMerge(0, 0, 0, mergeRange - 1, sheet);
		/*
		 * report generation date
		 */
		row = sheet.createRow(1);
		cell = row.createCell(0);
		cell.setCellStyle(styleForHeading);
		cell.setCellValue("Date of Report Generation : " + new SimpleDateFormat("dd-MM-yyy").format(new Date()));
		sheet = ExcelStyleSheet.doMerge(1, 1, 0, mergeRange - 1, sheet);

		/*
		 * from date and to date
		 */
		row = sheet.createRow(2);
		cell = row.createCell(0);
		cell.setCellStyle(styleForHeading);
		cell.setCellValue("From Date : " + new SimpleDateFormat("dd-MM-yyy").format(startDate) + "  To Date : "
				+ new SimpleDateFormat("dd-MM-yyy").format(endDate));
		sheet = ExcelStyleSheet.doMerge(2, 2, 0, mergeRange - 1, sheet);

		// workbook.getSheet(sheetName).createFreezePane(0, 3);

		return workbook;
	}

	@Override
	public List<EnginesForm> getRawDataReportAccessForms(OAuth2Authentication auth) {

		/*
		 * get loggedin user role id
		 */
		UserModel userModel = tokenInfoExtracter.getUserModelInfo(auth);

		Set<Integer> formIds = new HashSet<>();

		List<DesignationFormMapping> rfMappingList = designationFormMappingRepository.findByDesignationIdInAndAccessTypeAndStatusOrderByFormFormIdAsc(userModel.getRoleIds(), AccessType.DOWNLOAD_RAW_DATA, Status.ACTIVE);

		for (DesignationFormMapping rfm : rfMappingList) {

			formIds.add(rfm.getForm().getFormId());
		}
		 List<EnginesForm> forms =  formRepository.findByFormIdInOrderByFormId(formIds);
		return forms;

	}

	@Override
	public ResponseEntity<MessageModel> exportReviewDataReport(Integer formId, List<String> submissionIds,
			Principal principal) {

		try {

			List<Question> questionss = questionRepository.findAllByFormIdAndActiveTrueOrderByQuestionOrderAsc(formId);

			List<Question> questionList = questionss.stream().filter(v -> !"heading".equals(v.getControllerType()))
					.collect(Collectors.toList());

			String result = "";

			List<CFInputFormData> datas = cfinputFormDataRepository.findByIdInAndFormId(submissionIds, formId);
			/**
			 * if no data available return
			 */
			if (datas.isEmpty()) {

				MessageModel setMessageModel = setMessageModelDataNotFound();
				return new ResponseEntity<>(setMessageModel, HttpStatus.OK);
			}

			result = getExportDatasResult(datas, questionList, formId, new Date(), new Date(), principal);

			MessageModel model = new MessageModel();
			model.setMessage(result);
			model.setStatusCode(200);

			return new ResponseEntity<>(model, HttpStatus.OK);

		} catch (Exception e) {
			log.error("Action error while generating Review Report", e);
			throw new RuntimeException(e);
		}
	}

	private String getExportDatasResult(List<CFInputFormData> datas, List<Question> questionList, Integer formId,
			Date startDate, Date EndDate, Principal principal) {

		String formSheetName = configurableEnvironment.getProperty("form" + formId + ".sheet.name");
		String fileName = configurableEnvironment.getProperty("form" + formId + ".file.name");
		String name = configurableEnvironment.getProperty("form" + formId + ".name");

		return exportData(datas, questionList, formId, formSheetName, fileName, startDate, EndDate, name, principal);

	}

}