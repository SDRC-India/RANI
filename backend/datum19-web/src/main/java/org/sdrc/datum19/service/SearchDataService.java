package org.sdrc.datum19.service;

import java.util.Map;

import org.sdrc.datum19.model.SearchDataRequestModel;

public interface SearchDataService {

	Map<String, Object> getChartDataForSearchedIndicator(SearchDataRequestModel model);

}
