package org.sdrc.datum19.service;

import java.util.List;
import java.util.Set;

import org.sdrc.datum19.document.DataSearch;
import org.sdrc.datum19.document.KGrams;
import org.sdrc.datum19.model.GroupChartDataModel;
import org.sdrc.datum19.model.Top5Bot5;

/*
 * author : Biswabhusan Pradhan
 * email : biswabhusan@sdrc.co.in
 */
public interface DataSearchService {
	List<DataSearch> suggestIndicators(String username, Integer areaId, Integer tp);

	List<DataSearch> getRelatedSearchResult(List<Integer> relatedIndiactors, Integer areaId, Integer tp);
	
	public Set<String> generateKGrams(Integer k);
	
	public List<KGrams> getWords(String charSet);
	
	public GroupChartDataModel getDataSearchLegend(String indicatorId,Integer areaId);
	
}
