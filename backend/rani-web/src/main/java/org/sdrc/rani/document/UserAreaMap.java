package org.sdrc.rani.document;

import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Document
@Setter @Getter
public class UserAreaMap {
	public String _id;
//	public Map userData;
	public String username;
	public Integer areaId;
	public String status;
	public Integer tp;
	public Date startDate;
	public Date endDate;
}
