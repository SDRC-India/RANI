package org.sdrc.datum19.document;

import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document
@Data
public class ThematicFileData {

	private static final long serialVersionUID = 1519381375815795764L;
	
	@Id
	private String id;
	
	private Integer slugId;
	
	private Integer countryId;
	
	private Integer stateId;
	
	private Integer districtId;
	
	private Object shapeJSON;
	
	private String fileName;
	
}
