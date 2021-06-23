package org.sdrc.datum19.controller;

import java.util.Map;

import org.sdrc.datum19.document.UserIndicatorSearch;
import org.sdrc.datum19.model.SearchDataRequestModel;
import org.sdrc.datum19.repository.UserIndicatorSearchRepository;
import org.sdrc.datum19.service.SearchDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchDataController {

	@Autowired
	private SearchDataService searchDataService;
	
	@Autowired
	private UserIndicatorSearchRepository userIndicatorSearchRepo;
	
	@PostMapping("getSearchedIndicatorData")
	@ResponseBody
	public Map<String, Object>  getChartDataForSearchedIndicator(@RequestBody SearchDataRequestModel model){
		
//		save user search history for collaborative filtering model
		UserIndicatorSearch uis = userIndicatorSearchRepo.findByUsernameAndIndicatorId(model.getUsername(), Integer.parseInt(model.getIndicatorId()));
		if(uis!=null) {
			uis.setCount(uis.getCount()+1);
		} else {
			uis=new UserIndicatorSearch();
			uis.setUsername(model.getUsername());
			uis.setIndicatorId(Integer.parseInt(model.getIndicatorId()));
			uis.setCount(1);
		}
		userIndicatorSearchRepo.save(uis);
		
		return searchDataService.getChartDataForSearchedIndicator(model);
	}
}
