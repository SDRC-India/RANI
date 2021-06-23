package org.sdrc.datum19.controller;

import java.io.IOException;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.sdrc.datum19.document.Area;
import org.sdrc.datum19.service.DevInfoAdapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ETLController {
	@Autowired
	private DevInfoAdapterService devInfoAdapterService;
	
	@GetMapping("/importDevInfoData")
	public String importDevInfoData(@RequestParam("sourceCode") Integer sourceCode, @RequestParam("sector") String sector,
			@RequestParam("subsector") String subsector, @RequestParam("sourcepath") String sourcepath) throws InvalidFormatException, IOException {
		return devInfoAdapterService.importDevinfoData(sourceCode,sector,subsector,sourcepath);
	}
	
	@GetMapping("/importArea")
	public String importDevinfoData(String filepath){
		
		return null;
	}
}
