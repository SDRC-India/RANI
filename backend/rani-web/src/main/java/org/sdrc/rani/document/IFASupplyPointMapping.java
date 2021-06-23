package org.sdrc.rani.document;

import org.sdrc.rani.models.IFASupplyPointType;
import org.springframework.data.annotation.Id;

import lombok.Data;

/**
 * @author subham
 *
 */
@Data
public class IFASupplyPointMapping {

	@Id
	private String id;

	private String name;

	private IFASupplyPointType type;
	
	private String desgId;
	
	private Integer slugId;
	
	private Integer typeId;

}
