package org.sdrc.rani.models;

import java.util.List;

import lombok.Data;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 * 
 *
 */
@Data
public class UserDetailsModel {

	private Integer siNo;

	private String name;

	private String userName;

	private List<String> roleNames;

	private List<String> areaName;

	private String userId;

	private List<Integer> areaId;

	private Boolean enable;

	private Long mobileNumber;

	private List<String> roleId;

	private Integer areaLevelId;
	
	private List<String> F1;
	
	private List<String> F2;
	
	private List<String> F3;
	
	private List<String> F4;
	
	private List<String> F5;
	
	private List<String> F6;
	
	private List<String> F7;
	
	private List<String> F8;

}
