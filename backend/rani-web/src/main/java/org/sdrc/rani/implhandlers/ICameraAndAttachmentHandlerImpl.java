package org.sdrc.rani.implhandlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import in.co.sdrc.sdrcdatacollector.handlers.ICameraAndAttachmentsDataHandler;
import in.co.sdrc.sdrcdatacollector.models.DataModel;
import in.co.sdrc.sdrcdatacollector.models.FormAttachmentsModel;
import in.co.sdrc.sdrcdatacollector.models.QuestionModel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
@Service
@Slf4j
public class ICameraAndAttachmentHandlerImpl implements ICameraAndAttachmentsDataHandler {

	@Override
	public QuestionModel readExternal(QuestionModel model, DataModel dataModel,Map<String,Object> paramKeyValMap) {

		try {
			Map<String, List<FormAttachmentsModel>> attachmentsMap = dataModel.getAttachments();

			List<String> base64Values = new ArrayList<>();

			// List<String> files = new ArrayList<>();
			List<String> localDevicePaths = new ArrayList<>();

			if (attachmentsMap != null) {

				List<FormAttachmentsModel> attachments = attachmentsMap.get(model.getColumnName());

				if (!attachments.isEmpty()) {

					for (FormAttachmentsModel faModel : attachments) {

						String filePath = faModel.getFilePath();
						localDevicePaths.add(faModel.getLocalDevicePath());
						
						if(!paramKeyValMap.containsKey("review")){
							String encodstring = encodeFileToBase64Binary(filePath, faModel.getFileExtension());
							base64Values.add(encodstring);
						}else{
							base64Values.add(filePath);
						}

					}

				}

			}

			model.setValue(localDevicePaths);
			model.setAttachmentsInBase64(base64Values);

			return model;

		} catch (Exception e) {
			log.error("Action while generating base 64 value for camera {}",e);
			throw new RuntimeException(e);
		}

	}

	/**
	 * 
	 * @param file-
	 *            convert file to base64 string
	 * @return - base64 converted value
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private String encodeFileToBase64Binary(String filePath, String extension) throws IOException {

		String encodedString = "data:image/".concat(extension).concat(";base64,");
		byte[] fileContent = FileUtils.readFileToByteArray(new File(filePath));
		encodedString = encodedString.concat(java.util.Base64.getEncoder().encodeToString(fileContent));

		return encodedString;
	}

}
