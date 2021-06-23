package org.sdrc.rani.document;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * This Entity class will keep all the area details
 * 
 * @since version 1.0.0.0
 *
 */
@Document
@Data
public class Area implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1558538811474305739L;

	@Id
	private String id;

	private Integer areaId;

	private String areaName;

	private String areaCode;

	private Integer parentAreaId;

	private Boolean live;

	private AreaLevel areaLevel;

	private Integer blockId;

	private Integer districtId;

	private Integer stateId;

	private Integer panchayatId;

	private Integer villageId;

	private String createdBy;
	
	public Area() {
		super();
	}

	public Area(int areaId) {
		this.areaId = areaId;
	}

}
