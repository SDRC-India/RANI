package org.sdrc.rani.models;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * @author subham
 *
 */
@Data
public class QuickStartModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4333905955984374297L;

	private List<Map<String, Object>> t4Data;

	private List<Map<String, Object>> mediaData;

	private List<Map<String, Object>> hmemocueData;

}
