package org.sdrc.rani.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * @author subham
 *
 */
@Data
@Document
public class PlanningQuestions {

	@Id
	private String id;

	private Integer formId;

	private String planningQuestion;
}
