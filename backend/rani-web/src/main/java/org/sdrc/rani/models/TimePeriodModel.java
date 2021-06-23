package org.sdrc.rani.models;

import java.io.Serializable;

import lombok.Data;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
@Data
public class TimePeriodModel implements Serializable {

	private static final long serialVersionUID = -7940299713476167797L;

	private String id;

	private Integer year;
	
	private String tpName;
	
	private Integer tpId;

}
