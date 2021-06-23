package org.sdrc.rani.service;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.time.DateUtils;
import org.sdrc.rani.document.QualitativeReportFileData;
import org.sdrc.rani.document.QualitativeReportFormData;
import org.sdrc.rani.document.TimePeriod;
import org.sdrc.rani.document.UserDetails;
import org.sdrc.rani.exception.DeadLineDateCrossedException;
import org.sdrc.rani.exception.DuplicateRecordException;
import org.sdrc.rani.models.DateModel;
import org.sdrc.rani.models.QualitativeJSONTableModel;
import org.sdrc.rani.models.QualitativeReportTableModel;
import org.sdrc.rani.models.QualitativeTableModel;
import org.sdrc.rani.models.QualityReportModel;
import org.sdrc.rani.models.UserModel;
import org.sdrc.rani.repositories.QualitativeReportFileDataRepository;
import org.sdrc.rani.repositories.QualitativeReportFormDataRepository;
import org.sdrc.rani.repositories.TimePeriodRepository;
import org.sdrc.rani.util.TokenInfoExtracter;
import org.sdrc.usermgmt.mongodb.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Subham Ashish (subham@sdrc.co.in)
 *
 */
@Service
@Slf4j
public class QualitativeFormServiceImpl implements QualitativeFormService {

	@Autowired
	private QualitativeReportFormDataRepository qualitativeReportFormDataRepository;

	@Autowired
	private TimePeriodRepository timePeriodRepository;

	@Autowired
	private ConfigurableEnvironment configurableEnvironment;

	@Autowired
	private TokenInfoExtracter tokenInfoExtracter;

	@Autowired
	@Qualifier("mongoAccountRepository")
	private AccountRepository accountRepository;
	
	@Autowired
	private SubmissionServiceImpl submissionServiceImpl;
	
	@Autowired
	private QualitativeReportFileDataRepository qualitativeReportFileDataRepository;
	

	/**
	 * save qualityreport data in db
	 */
	@Override
	public ResponseEntity<String> saveQualitativeData(List<QualityReportModel> qualityReportModel, MultipartFile file,
			OAuth2Authentication oauth) {

		UserModel userModel = tokenInfoExtracter.getUserModelInfo(oauth);

		/**
		 * check whether for same month record is availble or not if availble
		 * than throws an exception("more than one record for same month is not
		 * allowed") Every fresh entry should be on or before 25th of month.
		 */
		TimePeriod timePeriod = timePeriodRepository.getCurrentTimePeriod(new Date(),
				configurableEnvironment.getProperty("timeperiod.periodicity.monthly"));

		QualitativeReportFormData data = qualitativeReportFormDataRepository.findByTimePeriodAndUserId(timePeriod,
				userModel.getUserId());

		if (data != null) {
			// for same user and timeperiod data is alreay submitted
			throw new DuplicateRecordException(
					configurableEnvironment.getProperty("qualitative.report.duplicate.message"));
		}

		Date currentDate = new Date();

		DateModel dateModel = getDatesForQualityReport(currentDate);

		if ((DateUtils.isSameDay(currentDate, dateModel.getStartDate()) || (currentDate.after(dateModel.getStartDate()))
				&& (DateUtils.isSameDay(currentDate, dateModel.getEndDate())
						|| currentDate.before(dateModel.getEndDate())))) {

			QualitativeReportFormData qualityReportFormData = new QualitativeReportFormData();
			qualityReportFormData.setCreatedDate(currentDate);
			qualityReportFormData.setTimePeriod(timePeriod);
			qualityReportFormData.setData(qualityReportModel);
			qualityReportFormData.setUserId(userModel.getUserId());
			qualityReportFormData.setUserName(SecurityContextHolder.getContext().getAuthentication().getName());

			String filePath=null;
			if(file==null) {
				qualityReportFormData.setData(qualityReportModel);
				// make pdf file and return path
				filePath = geneateQualityReport(qualityReportFormData);
			}else {
				
				filePath = submissionServiceImpl.getFilePath(file,
							configurableEnvironment.getProperty("qualitative.report.filename"),
							FilenameUtils.getExtension(file.getOriginalFilename()), file.getOriginalFilename(),
							configurableEnvironment.getProperty("qualitative.report.path"));
				qualityReportFormData.setExtension(FilenameUtils.getExtension(file.getOriginalFilename()));
			}
			
			/**
			 * for production environment split it with /opt
			 */
			String[] activeProfiles = configurableEnvironment.getActiveProfiles();
			if(configurableEnvironment.containsProperty("active.profile") && activeProfiles[0].equalsIgnoreCase(configurableEnvironment.getProperty("active.profile"))){
				qualityReportFormData.setFilePath(filePath.split("/opt")[1]);
			}else {
				qualityReportFormData.setFilePath(filePath.split(":")[1]);
			}
			
			qualitativeReportFormDataRepository.save(qualityReportFormData);
			log.info("saving new record of quality report successfull >> username {}",
					SecurityContextHolder.getContext().getAuthentication().getName());

			return new ResponseEntity<String>(
					new Gson().toJson(configurableEnvironment.getProperty("qualitative.report.success")),
					HttpStatus.OK);

		} else {
			// date crossed
			throw new DeadLineDateCrossedException(
					configurableEnvironment.getProperty("qualitative.report.cutoffdate.crossed.message"));
		}

	}

	private DateModel getDatesForQualityReport(Date date) {

		DateModel model = new DateModel();

		Calendar cal = Calendar.getInstance();

		cal.setTime(date);

		cal.set(Calendar.DATE, cal.getActualMinimum(Calendar.DATE));

		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MILLISECOND, 0);

		model.setStartDate(cal.getTime());

		cal.set(Calendar.DATE, 25);

		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 59);

		model.setEndDate(cal.getTime());

		return model;
	}

	/**
	 * @author RajaniKanta Sahoo (rajanikanta@sdrc.co.in) generate pdf and
	 *         return path.
	 */
	private String geneateQualityReport(QualitativeReportFormData qualitativeReportFormData) {

		String fileName = configurableEnvironment.getProperty("qualitative.report.filename");

		try {

			String dir = configurableEnvironment.getProperty("qualitative.report.path");

			File file = new File(dir);

			/*
			 * make directory if doesn't exist
			 */
			if (!file.exists())
				file.mkdirs();

			String name = fileName + "_" + new SimpleDateFormat("ddMMyyyyHHmmss").format(new Date()) + ".pdf";
			String path = dir + "" + name;

			FileOutputStream fos = new FileOutputStream(new File(path));

			Document document = new Document();
			PdfWriter.getInstance(document, fos);
			document.open();

			Font blue = new Font(FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLUE);
			// set fonts
			Font qustionLevelFont = new Font(FontFamily.HELVETICA, 8, Font.BOLD, BaseColor.BLACK);
			Font qustionFont = new Font(FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.BLACK);
			Font answerFont = new Font(FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.BLACK);

			Chunk headLineText = new Chunk("Qualitative Data Reporting Format for Supervisors", blue);
			Paragraph para1 = new Paragraph(headLineText);
			para1.setAlignment(Element.ALIGN_CENTER);
			document.add(para1);

			Chunk rname = new Chunk("Supervisor Name : " + qualitativeReportFormData.getUserName(), qustionLevelFont);
			Paragraph namePara = new Paragraph(rname);
			document.add(namePara);

			Date cdate = qualitativeReportFormData.getCreatedDate();
			Calendar cal = Calendar.getInstance();
			cal.setTime(cdate);
			Month month = Month.of(cal.get(Calendar.MONTH) + 1);
			Chunk monthChunk = new Chunk("Report Month : " + month, qustionLevelFont);
			Paragraph monthPpara = new Paragraph(monthChunk);
			document.add(monthPpara);

			int i = 1;
			List<QualityReportModel> data = qualitativeReportFormData.getData();

			for (QualityReportModel repData : data) {

				document.add(Chunk.NEWLINE);
				Chunk qustionChunk = new Chunk("Q : " + repData.getLabel(), qustionFont);
				Chunk qustionLevelChunk = new Chunk(repData.getHeader(), qustionLevelFont);
				Chunk answerChunk = new Chunk("A : " + repData.getValue(), answerFont);

				qustionLevelChunk.setFont(qustionLevelFont);
				qustionChunk.setFont(qustionFont);
				answerChunk.setFont(answerFont);
				// Add ordered list for all qustions
				Paragraph para2 = new Paragraph(i + ".  " + qustionLevelChunk);
				document.add(para2);
				Paragraph para3 = new Paragraph(qustionChunk);
				document.add(para3);
				document.add(Chunk.NEWLINE);
				Paragraph para4 = new Paragraph(answerChunk);
				document.add(para4);
				document.add(Chunk.NEWLINE);
				i++;

			}

			document.close();

			return path;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public Map<String, QualitativeJSONTableModel> getQualitativeData(OAuth2Authentication oauth) {

		List<String> tableColumn = new ArrayList<>();
		tableColumn.add("reportingMonth");
		tableColumn.add("dateOfCreation");

		List<QualitativeTableModel> qualitativeTableModelList = new ArrayList<>();

		Map<String, String> action = new LinkedHashMap<>();
		/**
		 * get all data in Descending order.
		 * 
		 */
		List<QualitativeReportFormData> datas = qualitativeReportFormDataRepository
				.findAllByUserNameOrderByCreatedDateDesc(
						SecurityContextHolder.getContext().getAuthentication().getName());

		Boolean flag = false;

		// find current time period
		Date currentDate = new Date();
		TimePeriod currentTimePeriod = timePeriodRepository.getCurrentTimePeriod(currentDate, "1");

		for (QualitativeReportFormData qrData : datas) {

			QualitativeTableModel tablemodel = new QualitativeTableModel();
			tablemodel.setDateOfCreation(new SimpleDateFormat("dd-MM-yyyy").format(qrData.getCreatedDate()));
			TimePeriod timePeriod = qrData.getTimePeriod();
			action = new LinkedHashMap<>();

			String tPeriod = timePeriod.getTimePeriodDuration() + "-" + timePeriod.getYear();
			tablemodel.setReportingMonth(tPeriod);
			action.put("id", qrData.getId());
			tablemodel.setAction(action);
			qualitativeTableModelList.add(tablemodel);
		}

		/**
		 * if no data present in qualitative report than find whether the add
		 * new form button should be enable or disabled.
		 */
		if (datas.isEmpty()) {
			DateModel dateModel = getDatesForQualityReport(currentDate);

			if ((DateUtils.isSameDay(currentDate, dateModel.getStartDate())
					|| (currentDate.after(dateModel.getStartDate()))
							&& (DateUtils.isSameDay(currentDate, dateModel.getEndDate())
									|| currentDate.before(dateModel.getEndDate())))) {

				flag = true;
			}
			
		} else {

			/**
			 * Check whether current timeperiod data is present if present than
			 * set flag to false to disable add new form button if current time
			 * period data is not present than check date if it is between
			 * 1-25th of month than enable new form button.
			 */
			log.info("datas : {}", datas);
			if (datas.get(0).getTimePeriod().getId().equals(currentTimePeriod.getId())) {
				flag = false;
			} else {

				DateModel dateModel = getDatesForQualityReport(currentDate);

				if ((DateUtils.isSameDay(currentDate, dateModel.getStartDate())
						|| (currentDate.after(dateModel.getStartDate()))
								&& (DateUtils.isSameDay(currentDate, dateModel.getEndDate())
										|| currentDate.before(dateModel.getEndDate())))) {

					flag = true;
				} 
			}

		}

		QualitativeJSONTableModel model = new QualitativeJSONTableModel();
		model.setTableColumn(tableColumn);
		model.setTableData(qualitativeTableModelList);

		Map<String, QualitativeJSONTableModel> finalMap = new HashMap<>();

		String date = new SimpleDateFormat("MMM-yyyy").format(currentDate);
		finalMap.put(date.concat("@AND@" + flag), model);

		return finalMap;
	}

	@Override
	public Map<String, QualitativeJSONTableModel> getQualitativeDDMview(OAuth2Authentication oauth) {

		List<String> tableColumn = new ArrayList<>();
		tableColumn.add("reportingMonth");
		tableColumn.add("dateOfCreation");
		tableColumn.add("supervisorName");

		List<QualitativeReportTableModel> qualitativeTableModelList = new ArrayList<>();

		Map<String, String> action = new LinkedHashMap<>();

		/**
		 * get all data in Descending order.
		 * 
		 */
		List<QualitativeReportFormData> datas = qualitativeReportFormDataRepository.findAllByOrderByCreatedDateDesc();

		// Boolean flag = null;

		for (QualitativeReportFormData qrData : datas) {

			QualitativeReportTableModel tablemodel = new QualitativeReportTableModel();
			tablemodel.setDateOfCreation(new SimpleDateFormat("dd-MM-yyyy").format(qrData.getCreatedDate()));
			TimePeriod timePeriod = qrData.getTimePeriod();
			action = new LinkedHashMap<>();

			String tPeriod = timePeriod.getTimePeriodDuration() + "-" + timePeriod.getYear();
			tablemodel.setReportingMonth(tPeriod);

			UserDetails acc = (UserDetails) accountRepository.findById(qrData.getUserId()).getUserDetails();
			tablemodel.setSupervisorName(acc.getFullName());

			action.put("id", qrData.getId());
			tablemodel.setAction(action);
			qualitativeTableModelList.add(tablemodel);
		}

		QualitativeJSONTableModel model = new QualitativeJSONTableModel();
		model.setTableColumn(tableColumn);
		model.setTableData(qualitativeTableModelList);

		Map<String, QualitativeJSONTableModel> finalMap = new HashMap<>();

		String date = new SimpleDateFormat("MMM-yyyy").format(new Date());
		finalMap.put(date, model);

		return finalMap;
	}

	@Override
	public ResponseEntity<String> uploadQualitativeReport(OAuth2Authentication oauth, MultipartFile file) {

		// find current time period
		Date currentDate = new Date();
		TimePeriod currentTimePeriod = timePeriodRepository.getCurrentTimePeriod(currentDate, "1");
				
		QualitativeReportFileData datas = qualitativeReportFileDataRepository.findByTimePeriod(currentTimePeriod);

		if (datas != null) {
			// for same user and timeperiod data is alreay submitted
			throw new DuplicateRecordException(configurableEnvironment.getProperty("qualitative.report.duplicate.message"));
		}

		String filePath = submissionServiceImpl.getFilePath(file,
					configurableEnvironment.getProperty("qualitative.report.file.name"),
					FilenameUtils.getExtension(file.getOriginalFilename()), file.getOriginalFilename(),
					configurableEnvironment.getProperty("qualitative.report.path"));

			QualitativeReportFileData data = new QualitativeReportFileData();
			data.setCreatedDate(new Date());
			data.setExtension(FilenameUtils.getExtension(file.getOriginalFilename()));
			data.setName(file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf('.')));
			data.setSize(file.getSize());
			
			/**
			 * for production environment split it with /opt
			 */
			String[] activeProfiles = configurableEnvironment.getActiveProfiles();
			if(configurableEnvironment.containsProperty("active.profile") && activeProfiles[0].equalsIgnoreCase(configurableEnvironment.getProperty("active.profile"))){
				data.setFilePath(filePath.split("/opt")[1]);
			}else {
				data.setFilePath(filePath.split(":")[1]);
			}
			data.setTimePeriod(currentTimePeriod);

			qualitativeReportFileDataRepository.save(data);

			return new ResponseEntity<String>(new Gson().toJson(configurableEnvironment.getProperty("ddm.qreport.upload.success")), HttpStatus.OK);
			
	}

	@Override
	public Map<String, QualitativeJSONTableModel> getDDMQualitativeData(OAuth2Authentication oauth) {

		List<String> tableColumn = new ArrayList<>();
		tableColumn.add("reportingMonth");
		tableColumn.add("dateOfCreation");

		List<QualitativeTableModel> qualitativeTableModelList = new ArrayList<>();

		Map<String, String> action = new LinkedHashMap<>();

		/**
		 * get all data in Descending order.
		 * 
		 */
		List<QualitativeReportFileData> datas = qualitativeReportFileDataRepository.findAllByOrderByCreatedDateDesc();

		// Boolean flag = null;

		for (QualitativeReportFileData qrData : datas) {

			QualitativeTableModel tablemodel = new QualitativeTableModel();
			tablemodel.setDateOfCreation(new SimpleDateFormat("dd-MM-yyyy").format(qrData.getCreatedDate()));
			TimePeriod timePeriod = qrData.getTimePeriod();
			action = new LinkedHashMap<>();

			String tPeriod = timePeriod.getTimePeriodDuration() + "-" + timePeriod.getYear();
			tablemodel.setReportingMonth(tPeriod);

			action.put("id", qrData.getId());
			tablemodel.setAction(action);
			qualitativeTableModelList.add(tablemodel);
		}

		QualitativeJSONTableModel model = new QualitativeJSONTableModel();
		model.setTableColumn(tableColumn);
		model.setTableData(qualitativeTableModelList);

		Map<String, QualitativeJSONTableModel> finalMap = new HashMap<>();

		String date = new SimpleDateFormat("MMM-yyyy").format(new Date());
		finalMap.put(date, model);

		return finalMap;
	}

}
