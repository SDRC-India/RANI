package org.sdrc.rani.implhandlers;

import java.util.List;
import java.util.Map;

import org.sdrc.rani.repositories.AreaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import in.co.sdrc.sdrcdatacollector.document.Question;
import in.co.sdrc.sdrcdatacollector.document.TypeDetail;
import in.co.sdrc.sdrcdatacollector.handlers.IDbReviewQueryHandler;
import in.co.sdrc.sdrcdatacollector.models.DataModel;
import in.co.sdrc.sdrcdatacollector.models.DataObject;
import in.co.sdrc.sdrcdatacollector.models.ReviewHeader;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
@Component
public class IDbReviewQueryHandlerImpl implements IDbReviewQueryHandler {

	@Autowired
	private AreaRepository areaRepository;

	@Autowired
	private ConfigurableEnvironment ConfigurableEnvironment;

	@Override
	@SuppressWarnings("unchecked")
	public DataObject setReviewHeaders(DataObject dataObject, Question question,
			Map<Integer, TypeDetail> typeDetailsMap, DataModel submissionData, String type) {

		if (question.getReviewHeader() != null && question.getReviewHeader().trim().length() > 0) {

			ReviewHeader header = new ReviewHeader();

			switch (question.getReviewHeader().split("_")[0]) {

			case "L1":
			case "L3":
			case "L4":
			case "L5":
				switch (question.getControllerType()) {
				case "dropdown":
					if (question.getControllerType().equals("dropdown")
							&& (question.getTableName() == null || question.getTableName().equals(""))) {

						if (question.getFieldType().equals("option")) {
							header = new ReviewHeader();
							header.setName(question.getReviewHeader());
							header.setValue(typeDetailsMap.get(submissionData.getData().get(question.getColumnName())).getName()
									.toString());

							dataObject.getFormDataHead().put(header.getName(), header.getValue());
						} else {

							// checkbox- case ie multiple option selection
							List<Integer> ids = (List<Integer>) submissionData.getData().get(question.getColumnName());
							String values = null;
							for (Integer id : ids) {

								String val = typeDetailsMap.get(id).getName().toString();
								if (values == null) {
									values = val;
								} else {
									values = values.concat(",") + val;
								}

							}
							
							header = new ReviewHeader();
							header.setName(question.getReviewHeader());
							header.setValue(values);

							dataObject.getFormDataHead().put(header.getName(), header.getValue());

						}

					} else if (question.getTableName() != null && question.getTableName().trim().length() > 0) {

						switch (question.getTableName().split("\\$")[0]) {

						case "area":
							header = new ReviewHeader();
							header.setName(question.getReviewHeader());
							header.setValue(areaRepository
									.findByAreaId(Integer.parseInt(
											submissionData.getData().get(question.getColumnName()).toString()))
									.getAreaName());

							dataObject.getFormDataHead().put(header.getName(), header.getValue());
						}
					}
					break;
				case "textbox":
				case "Date Widget":
					header = new ReviewHeader();
					header.setName(question.getReviewHeader());
					header.setValue(submissionData.getData().get(question.getColumnName()) != null
							? submissionData.getData().get(question.getColumnName()).toString() : null);

					dataObject.getFormDataHead().put(header.getName(), header.getValue());

				}
				break;

			}
		}

		// add data submission user name
		ReviewHeader header = new ReviewHeader();
		header.setName("L2_Username");
		header.setValue(submissionData.getUserName());
		
		dataObject.getFormDataHead().put(header.getName(), header.getValue());
		dataObject.getFormDataHead().put(header.getName(), header.getValue());
		
		//add sync date
		if(submissionData.getExtraKeys().containsKey("rejectedOn")){
			header.setName("L0_Date of rejection");
			header.setValue(submissionData.getExtraKeys().get("rejectionDate").toString());
		}else{
			header.setName("L0_Date of submisson");
			header.setValue(submissionData.getExtraKeys().get("submittedOn").toString());
		}
		
		dataObject.getFormDataHead().put(header.getName(), header.getValue());
		dataObject.getFormDataHead().put(header.getName(), header.getValue());

		// save date of visit
		String dateOfVisit = (String) submissionData.getData().get(ConfigurableEnvironment.getProperty("form"+submissionData.getFormId()+".datevisit.key"));
		
		Map<String, Object> extravalues = submissionData.getExtraKeys();
		
		extravalues.put("dateOfVisit", dateOfVisit);
		dataObject.setExtraKeys(extravalues);
		
		return dataObject;
	}
}
