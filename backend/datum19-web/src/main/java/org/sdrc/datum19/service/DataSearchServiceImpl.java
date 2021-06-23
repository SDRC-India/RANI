package org.sdrc.datum19.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.sdrc.datum19.document.DataSearch;
import org.sdrc.datum19.document.DataValue;
import org.sdrc.datum19.document.Indicator;
import org.sdrc.datum19.document.IndicatorSuggestionModel;
import org.sdrc.datum19.document.KGrams;
import org.sdrc.datum19.document.TimePeriod;
import org.sdrc.datum19.model.ChartData;
import org.sdrc.datum19.model.ChartDataModel;
import org.sdrc.datum19.model.GroupChartDataModel;
import org.sdrc.datum19.repository.AreaRepository;
import org.sdrc.datum19.repository.DataDomainRepository;
import org.sdrc.datum19.repository.DataSearchRepository;
import org.sdrc.datum19.repository.IndicatorRepository;
import org.sdrc.datum19.repository.KGramRepository;
import org.sdrc.datum19.repository.SuggestionModelRepository;
import org.sdrc.datum19.repository.TimePeriodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

/*
 * author : Biswabhusan Pradhan
 * email : biswabhusan@sdrc.co.in
 */
@Service
public class DataSearchServiceImpl implements DataSearchService {

	@Autowired
	private SuggestionModelRepository suggestionModelRepository;
	
	@Autowired
	private DataSearchRepository dataSearchRepository;
	
	@Autowired
	private ConfigurableEnvironment configurableEnvironment;
	
	@Autowired
	private IndicatorRepository indicatorRepository;
	
	@Autowired
	private KGramRepository kGramRepository;
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private TimePeriodRepository timePeriodRepository;
	
	@Autowired
	private DataDomainRepository dataDomainRepository;
	
	@Autowired
	private AreaRepository arearepo;
	
//	public static List<String> substrings = new ArrayList<>();
	
	@Override
	public List<DataSearch> suggestIndicators(String username, Integer areaId, Integer tp) {
		List<IndicatorSuggestionModel> suggestedIndicators = suggestionModelRepository.findTop5ByUsernameOrderByPredictionAsc(username);
		List<Integer> indicators = new ArrayList<Integer>();
		suggestedIndicators.forEach(i -> {
			indicators.add(i.getIndicatorId());
		});
		
		List<DataSearch> suggestedSearchTexts = dataSearchRepository.findTop5ByInidInAndDatumIdAndTp(indicators, areaId, tp);
		return suggestedSearchTexts.stream().map(s -> new DataSearch(s.getIndicator(), s.getInid(), s.getTp(), s.getDatumId(), Double.parseDouble(String.format("%.2f", s.getDataValue()))))
				.collect(Collectors.toList());
	}

	@Override
	public List<DataSearch> getRelatedSearchResult(List<Integer> relatedIndiactors, Integer areaId, Integer tp) {
		List<DataSearch> relatedSearchResults = dataSearchRepository.findTop5ByInidInAndDatumIdAndTp(relatedIndiactors, areaId, tp);
		return relatedSearchResults.stream().map(s -> new DataSearch(s.getIndicator(), s.getInid(), s.getTp(), s.getDatumId(), Double.parseDouble(String.format("%.2f", s.getDataValue()))))
				.collect(Collectors.toList());
	}

	/*
	 * author : Biswabhusan Pradhan
	 * email : biswabhusan@sdrc.co.in
	 */

	/*
	 * author : Biswabhusan Pradhan
	 * email : biswabhusan@sdrc.co.in
	 * description : to generate the word dictionary and k-grams
	 */
	@Override
	public Set<String> generateKGrams(Integer k) {
//		Loading the corpus
		List<DataSearch> indicators = dataSearchRepository.findAll();
		List<KGrams> k_grams = new ArrayList<>();
		Set<String> dictionary = new HashSet<>();
		
//		Tokenizing and cleaning the words
		indicators.forEach(d -> {
			Arrays.asList(d.getIndicator().split(" ")).forEach(word -> {
				word.replace("\\", "");
				word.replace("\"", "");
				dictionary.add(word);
			});
		});
		
//		Defining the stopwords, those words will not get included in our NLP
		String[] stopwords = new String[] {"and", "or", "of", "the", "is", "are", "for", "with","where", "were", "which", "to", "their", "was", "there", "\\", "\"", ","};
		dictionary.removeAll(Arrays.asList(stopwords));
		
		//Generating K-grams from the remaining words in dictionary after removing stopwords
		dictionary.iterator().forEachRemaining(word -> {
			int start=0;
			int end = k;
			for (int i = 0; i < word.length()-(k-1); i++) {
				KGrams kgram = new KGrams();
				kgram.setWord(word);
				kgram.setGram(k);
				kgram.setKgram(word.substring(start, end).toLowerCase());
				kgram.setWeight(Double.parseDouble(k+"."+i));
				k_grams.add(kgram);
				start++;
				end++;
			}
		});
		
		kGramRepository.saveAll(k_grams);
		k_grams.forEach(d -> {
			System.out.println(d.getKgram()+" :: "+ d.getWord());
		});
		return dictionary;
	}

	/*
	 * This method accepts a string as a parameter and 
	 * returns a list of K-Gram that matches with the string.
	 */
	@Override
	public List<KGrams> getWords(String charSet) {
		// TODO Auto-generated method stub
		List<KGrams> kgramslist = new ArrayList<>();
		kgramslist = kGramRepository.findTop10ByKgramOrderByGramDescWeightAsc(charSet);
		int k = 4;	//k-gram=4
		List<String> substr = new ArrayList<>();
		String wordgrams = "";
		
		/*
		 * returns top 10 matches in wrt the weights of the word
		 */
		if(kgramslist.size()==0) {
			substr = findsubsequences(charSet,"",substr);
			kgramslist = kGramRepository.findTop10ByKgramInOrderByGramDescWeightAsc(substr);
		}
		
		return kgramslist;
	}
	
	private static List<String> findsubsequences(String s, String ans, List<String> substrings) { 
        if(s.length()==0) 
        { 
        	substrings.add(ans);  
            return substrings; 
        } 
        findsubsequences(s.substring(1),ans+s.charAt(0),substrings) ;
                // Not adding first character of the string 
                // because the concept of subsequence either  
                // character will present or not 
        findsubsequences(s.substring(1),ans,substrings);  
        
        return substrings;
    }

	@Override
	public GroupChartDataModel getDataSearchLegend(String indicatorId, Integer areaId) {
		GroupChartDataModel groupChartDataModel = new GroupChartDataModel();
		Indicator indicator = indicatorRepository.getIndicatorsByDatumId(indicatorId);
		
		groupChartDataModel.setHeaderIndicatorName(indicator.getIndicatorDataMap().get("indicatorName").toString());
		groupChartDataModel.setUnit(indicator.getIndicatorDataMap().get("unit").toString());
		groupChartDataModel.setChartsAvailable(Arrays.asList("trend"));
		
		ChartData chartData = new ChartData();
		chartData.setHeaderIndicatorName(indicator.getIndicatorDataMap().get("indicatorName").toString());
		
		List<List<ChartDataModel>> listOfListChartDataValues = new ArrayList<List<ChartDataModel>>();
		List<ChartDataModel> listOfChartDataValue = new ArrayList<>();
		ChartDataModel chartDataModel = null;
		List<TimePeriod> listOfTimePeriod = timePeriodRepository.findTop6ByPeriodicityOrderByCreatedDateDesc("12");
		List<Integer> timeperiodIds = new ArrayList<>();
		Map<Integer, String> timeperiodIdAndNameMap = new HashMap<>();
		for (TimePeriod timePeriod : listOfTimePeriod) {
			timeperiodIds.add(timePeriod.getTimePeriodId());
			timeperiodIdAndNameMap.put(timePeriod.getTimePeriodId(), timePeriod.getTimePeriodDuration());
			
		}
		List<DataValue> listOfDataValue = dataDomainRepository.findByDatumIdAndInidAndTpIn(areaId, Integer.parseInt(indicatorId), timeperiodIds);
		
		for (DataValue dataValue : listOfDataValue) {
			chartDataModel =  new ChartDataModel();
			chartDataModel.setAxis(timeperiodIdAndNameMap.get(dataValue.getTp()));
			chartDataModel.setValue(dataValue.getDataValue().toString());
			listOfChartDataValue.add(chartDataModel);
			
		}
		chartData.setChartDataValue(Arrays.asList(listOfChartDataValue));
		groupChartDataModel.setChartData(chartData);
	
		
		return groupChartDataModel;
	}

}
