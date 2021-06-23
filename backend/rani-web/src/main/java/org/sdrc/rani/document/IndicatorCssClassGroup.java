package org.sdrc.rani.document;

import java.io.Serializable;
import java.util.List;

import org.sdrc.rani.models.IndicatorModel;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document
@Data
public class IndicatorCssClassGroup implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	private String id;
	
	List<IndicatorModel> indicatorModels;
	
}

