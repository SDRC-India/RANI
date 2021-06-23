package org.sdrc.datum19.controller;

import java.util.ArrayList;
import java.util.List;

import org.sdrc.datum19.model.ApplicationDetails;
import org.sdrc.datum19.model.BarChartData;
import org.sdrc.datum19.repository.AggregationDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

//@CrossOrigin(origins = "http://aggregation.sdrc.co.in:8080")
//@CrossOrigin
@RestController
public class OverviewController {
	
	@Autowired
	private ConfigurableEnvironment configurableEnvironment;
	
	@Autowired
	private AggregationDetailsRepository aggregationDetailsRepository;
	@GetMapping("applicationDetails")
	public ApplicationDetails getAplplicationdetails() {
		ApplicationDetails appDetails = new ApplicationDetails();
		appDetails.setApplicationName(configurableEnvironment.getProperty("datum.project.name"));
		appDetails.setDbHost(configurableEnvironment.getProperty("spring.data.mongodb.host"));
		appDetails.setDbName(configurableEnvironment.getProperty("spring.data.mongodb.database"));
		return appDetails;
	}
	
	@GetMapping("aggregationHistory")
	public List<BarChartData> getAggregationHistory(){
		List<BarChartData> chartData=new ArrayList<BarChartData>();
		aggregationDetailsRepository.findAll().stream().forEach(o -> {
			BarChartData data=new BarChartData();
			data.setLetter(String.valueOf(o.getTimePeriod().getTimePeriodDuration())+"-"+o.getTimePeriod().getYear());
			data.setFrequency(o.getTimeTaken());
			chartData.add(data);
		});
		return chartData;
	}
}
