package org.sdrc.rani.job;

import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TimePeriodController {

	@Autowired
	private TimePeriodService timePeriodService;

	@GetMapping("/createMonthlyTimePeriod")
	public ResponseEntity<String> createMonthlyTimePeriod() throws ParseException {

		timePeriodService.createMonthlyTimePeriod();

		return new ResponseEntity<String>("success", HttpStatus.OK);
	}
	
	@GetMapping("/schedulePlanning")
	public ResponseEntity<String> schedulePlanning() throws ParseException {

		timePeriodService.schedulePlanning();

		return new ResponseEntity<String>("success", HttpStatus.OK);
	}
	
	
	@GetMapping("/updateAggregatedData")
	public ResponseEntity<String> updateAggregatedData() throws ParseException {

		timePeriodService.updateAggregatedData();

		return new ResponseEntity<String>("success", HttpStatus.OK);
	}
}
