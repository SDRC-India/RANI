package org.sdrc.rani.models;

import java.util.List;

import lombok.Data;

/**
 * @author subham
 *
 */
@Data
public class QualitativeJSONTableModel {

	private List<String> tableColumn;

	private Object tableData;
}
