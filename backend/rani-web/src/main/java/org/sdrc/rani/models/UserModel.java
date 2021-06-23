package org.sdrc.rani.models;

import java.util.List;
import java.util.Set;

import org.sdrc.rani.document.Area;

import lombok.Data;

/**
 * @author subham
 *
 */
@Data
public class UserModel {

	private String userId;

	private Set<String> roleIds;

	private Set<String> roles;

	private String name;

	private String emailId;

	private List<Area> areas;

	private List<Integer> desgSlugIds;
	
	private List<Integer> areaIds;
}
