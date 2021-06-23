package org.sdrc.datum19.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.sdrc.datum19.document.DataSearch;
import org.sdrc.datum19.document.Indicator;
import org.sdrc.datum19.document.KGrams;
import org.sdrc.datum19.document.UserIndicatorSearch;
import org.sdrc.datum19.model.BarChartData;
import org.sdrc.datum19.repository.AreaRepository;
import org.sdrc.datum19.repository.DataDomainRepository;
import org.sdrc.datum19.repository.DataSearchRepository;
import org.sdrc.datum19.repository.IndicatorRepository;
import org.sdrc.datum19.repository.UserIndicatorSearchRepository;
import org.sdrc.datum19.service.DataSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*
 * author : Biswabhusan Pradhan
 * email : biswabhusan@sdrc.co.in
 * Description : The APIs for pre-processing of data and data search and . 
 */

//@CrossOrigin
@RestController
public class DataSearchController {
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private DataSearchRepository dataSearchRepository;
	
	@Autowired
	private DataDomainRepository dataRepo;
	
	@Autowired
	private AreaRepository arearepo;
	
	@Autowired
	private UserIndicatorSearchRepository userIndicatorSearchRepo;
	
	@Autowired
	private DataSearchService dataSearchService;
	
	@Autowired
	private IndicatorRepository indicatorRepository;
	
	/*
	 * This controller get called when the user starts typing on search box.
	 * And it returns all possible words matches with the typed string.
	 * 
	 */
	@GetMapping("/getWords")
	List<KGrams> getWords(@RequestParam("charSet") String charSet) {
		/*
		 * Identifies the previously typed word and current word 
		 * from a sentence which is getting typed by the user.
		 */
		final String preTypedWords = charSet.contains(" ") ? charSet.substring(0, charSet.lastIndexOf(" ")) : "";
		charSet = charSet.contains(" ") ? charSet.split(" ")[charSet.split(" ").length-1] : charSet;
		List<KGrams> words = new ArrayList<>();
		/*
		 * Fetches all words matching with the currently typed word 
		 * and concats the previously typed string with the matching words
		 */
		dataSearchService.getWords(charSet)
		.forEach(c -> {
			c.setWord(preTypedWords + " " + c.getWord());
			words.add(c);
		});
		return words;
	}
	
	/*
	 * This method gets invoked when a space button is pressed and keypress event get called. 
	 * This method returns list of indicators matching with the entered string.
	 * NB :: Before invoking this method be sure that you have created a text corpus out of the aggregate or 
	 * secondary data. That means you should have the texts to describe your data. 
	 * The query is inside resources folder.
	 */
	@GetMapping("/getSearchedIndicators")
	List<DataSearch> getSearchedIndicators(@RequestParam("serachText") String serachText){
		List<DataSearch> indicators = new ArrayList<>();
		if(!serachText.isEmpty()) {
			
//		Querying the database using text index field
		TextCriteria textCriteria = TextCriteria.forDefaultLanguage().matchingAny(serachText);
		Query query = TextQuery.queryText(textCriteria)
				  .sortByScore().limit(5);
		indicators = mongoTemplate.find(query, DataSearch.class);
		}
		return indicators;
	}
	
	/*
	 * This method gets invoked when the user selects a particular suggested indicator in the UI.
	 * And this method return the search specific result.
	 */
	@GetMapping("getDataForSearchedIndicator")
	List<BarChartData> getDataForSearchedIndicator(@RequestParam("indicatorId")String indicatorId, @RequestParam("datumId")Integer datumId, 
			@RequestParam("username") String username){
		
//		save user search history for collaborative filtering model
		UserIndicatorSearch uis = userIndicatorSearchRepo.findByUsernameAndIndicatorId(username, Integer.parseInt(indicatorId));
		if(uis!=null) {
			uis.setCount(uis.getCount()+1);
		} else {
			uis=new UserIndicatorSearch();
			uis.setUsername(username);
			uis.setIndicatorId(Integer.parseInt(indicatorId));
			uis.setCount(1);
		}
		userIndicatorSearchRepo.save(uis);
		List<DataSearch> dataList = new ArrayList<>();
		dataList = dataSearchRepository.findByInidAndDatumId(Integer.parseInt(indicatorId), datumId);
		List<BarChartData> chartData = new ArrayList<>();
		dataList.forEach(_data -> {
			BarChartData barChart = new BarChartData();
			barChart.setLetter(_data.getIndicator().split(" ")[_data.getIndicator().split(" ").length-1]);
			barChart.setFrequency(_data.getDataValue());
			chartData.add(barChart);
		});
		return chartData;
	}
	
	/*
	 * This method gets invoked after getDataForSearchedIndicator. This method returns data for other related areas.
	 * */
	@GetMapping("suggestedAreas")
	List<BarChartData> getSuggestedAreas(@RequestParam("indicatorId")Integer indicatorId, @RequestParam("tp")Integer tp, 
			@RequestParam("areaId") Integer areaId){
		Integer parentAreaId = arearepo.findByAreaId(areaId).getParentAreaId();
		List<Integer> areaIds = arearepo.findByParentAreaIdOrderByAreaName(parentAreaId)
				.stream().map(a -> {
					return a.getAreaId();
				}).collect(Collectors.toList());
		List<DataSearch> dataList = new ArrayList<>();
		dataList = dataSearchRepository.findTop10ByInidAndTpAndDatumIdIn((indicatorId), tp, areaIds);
		List<BarChartData> chartData = new ArrayList<>();
		dataList.forEach(_data -> {
			BarChartData barChart = new BarChartData();
			barChart.setLetter(arearepo.findByAreaId(_data.getDatumId()).getAreaName().contains("(")
					?arearepo.findByAreaId(_data.getDatumId()).getAreaName().split("\\(")[0]
							:arearepo.findByAreaId(_data.getDatumId()).getAreaName());
			barChart.setFrequency(_data.getDataValue());
			chartData.add(barChart);
		});
		return chartData;
	}
	
	
	/*
	 * This method uses collaborative filtering algorithm to suggest users with indicators 
	 * based on their search history and other user's history.
	 */	
	@GetMapping("suggestedIndicators")
	List<DataSearch> suggestIndicators(@RequestParam("username")String username,@RequestParam("areaId")Integer areaId, 
			@RequestParam("tp")Integer tp){
		return dataSearchService.suggestIndicators(username, areaId, tp);
	}
	
	/*
	 * This method returns the related indicators on the basis of clustering algorithm.
	 * */
	@GetMapping("getRelatedIndicators")
	List<DataSearch> getRelatedIndicators(@RequestParam("indicatorId")Integer indicatorId,@RequestParam("areaId")Integer areaId, 
			@RequestParam("tp")Integer tp){
//		List<Integer> relatedIndiactors = indicatorClassificationService.getRelatedIndicators(indicatorId);
		List<Integer> relatedIndiactors = new ArrayList<>();
		Indicator ind=indicatorRepository.findByIndNid(String.valueOf(indicatorId));
		indicatorRepository.getGroupIndicators(String.valueOf(ind.getIndicatorDataMap().get("indicatorGid")))
		.forEach(i -> {
			relatedIndiactors.add(Integer.parseInt(String.valueOf(i.getIndicatorDataMap().get("indicatorNid"))));
		});
		List<DataSearch> relatedSearches = dataSearchService.getRelatedSearchResult(relatedIndiactors, areaId, tp);
		return relatedSearches;
	}
	
}
