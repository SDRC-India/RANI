package org.sdrc.rani.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import in.co.sdrc.sdrcdatacollector.document.EnginesForm;
import lombok.Data;


/**
 * @author Subham Ashish(subham@sdrc.co.in)
 */

@Document
@Data
public class KeyGeneratorColumnsSetting {

	@Id
	private String id;

	private String colKey;
	
	private EnginesForm form;

	private String colValue;
}
