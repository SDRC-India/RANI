package org.sdrc.rani.document;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.sdrc.usermgmt.mongodb.domain.Designation;
import org.springframework.data.annotation.Id;

import in.co.sdrc.sdrcdatacollector.document.EnginesForm;
import in.co.sdrc.sdrcdatacollector.models.AccessType;
import in.co.sdrc.sdrcdatacollector.util.Status;
import lombok.Data;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
@Data
public class DesignationFormMapping {

	@Id
	private String id;

	private Designation designation;

	private EnginesForm form;

	@CreationTimestamp
	private Date createdDate;

	private AccessType accessType;

	private Status status = Status.ACTIVE;

}
