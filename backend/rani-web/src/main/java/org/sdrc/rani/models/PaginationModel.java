package org.sdrc.rani.models;

import java.util.List;
import java.util.Map;

import org.sdrc.rani.document.CFInputFormData;

import lombok.Data;

/**
 * @author Subham Ashish(subahm@sdrc.co.in)
 *
 */
@Data
public class PaginationModel {

	
	List<CFInputFormData> results;
	
	List<Map<String,Integer>> count;
}
