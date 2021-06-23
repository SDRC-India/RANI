package org.sdrc.rani.document;

import org.springframework.data.annotation.Id;

import lombok.Data;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
@Data
public class FormMapping {

	@Id
	private String id;

	private Integer supervisorFormId;

	private Integer cfFormId;
}
