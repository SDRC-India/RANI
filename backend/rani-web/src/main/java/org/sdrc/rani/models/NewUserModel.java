/**
 * 
 */
package org.sdrc.rani.models;

import lombok.Data;
import lombok.ToString;

/**
 * @author Subham Ashish(subham@sdrc.co.in) Created Date:05-Jul-2018 4:48:34 PM
 */
@Data
@ToString
public class NewUserModel {

	private String userName;

	private String password;

	private Integer roleId;

	private Integer areaId;
	
	private String emailId;
	
	private String fullName;

}
