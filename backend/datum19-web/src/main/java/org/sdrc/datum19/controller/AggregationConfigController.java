package org.sdrc.datum19.controller;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

import org.sdrc.datum19.document.Indicator;
import org.sdrc.datum19.model.IndicatorConfigModel;
import org.sdrc.datum19.model.QuestionModel;
import org.sdrc.datum19.model.SectorModel;
import org.sdrc.datum19.model.SubsectorModel;
import org.sdrc.datum19.model.TypeDetailsModel;
import org.sdrc.datum19.repository.TypeDetailsRepository;
import org.sdrc.datum19.service.AggregationConfigService;
import org.sdrc.datum19.util.ValueObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/*
 * author : Biswabhusan Pradhan
 * email : biswabhusan@sdrc.co.in
 * Description : The root controller to configure the indicators and render master data for indicator configuration.
 * 
 */

//@CrossOrigin(origins = {"http://aggregation.sdrc.co.in:8080"})
//@CrossOrigin
@RestController
public class AggregationConfigController {

	@Autowired
	public AggregationConfigService aggregationConfigService;

	@Autowired
	public TypeDetailsRepository typeDetailsRepository;

	//Returns list of forms from the collection enginesForm
	@GetMapping("/getAllForm")
	public List<ValueObject> getAllForm() {
		return aggregationConfigService.getAllForm();
	}

	//Returns list of questions from the collection question
	@GetMapping("/getQuestions")
	@ResponseBody
	public Map<Integer, List<QuestionModel>> getQuestions() {
		return aggregationConfigService.getQuestions();
	}

	//Returns list of options from the collection typeDetail
	@GetMapping("/getTypeDetails")
	@ResponseBody
	public Map<Integer, Map<Integer, List<TypeDetailsModel>>> getTypeDetails() {
		return aggregationConfigService.getTypeDetails();
	}

	//Returns list of aggregationTypes defined in AggregationType enum class
	@GetMapping("/getAggregationTypes")
	@ResponseBody
	public List<ValueObject> getAggregationTypes() {
		return aggregationConfigService.getAggregationTypes();
	}

	//Returns list of indicators with limited metadata for dropdowns and table. 
	@GetMapping("/getAllIndicators")
	@ResponseBody
	public List<?> getAllIndicators() {
		return aggregationConfigService.getAllIndicators();
	}

	//Save the configured indicator to indicator collection
	@PostMapping("/saveIndicator")
	@ResponseBody
	public String saveIndiactor(@RequestBody IndicatorConfigModel indicatorConfigModel) {
		return aggregationConfigService.saveIndiactor(indicatorConfigModel);
	}

	//Save the configured sector to sector collection
	@PostMapping("/addSector")
	@ResponseBody
	public String addSector(@RequestBody SectorModel sectorModel) {
		return aggregationConfigService.addSector(sectorModel);
	}

	//Save the configured sub-sector to subsector collection
	@PostMapping("/addSubSector")
	@ResponseBody
	public String addSubSector(@RequestBody SubsectorModel subsectorModel) {
		return aggregationConfigService.addSubSector(subsectorModel);
	}

	//Returns list of indicators to display in Indicator view table
	@GetMapping("/getIndicatorsForView")
	@ResponseBody
	public List<?> getIndicatorsForView() {
		return aggregationConfigService.getIndicatorsForView();
	}

	//Returns list of sectors to display in Indicator configuration.
	@GetMapping("/getAllSectors")
	@ResponseBody
	public List<?> getAllSectors() {
		return aggregationConfigService.getAllSectors();
	}

	//Returns list of sub-sectors to display in Indicator configuration.
	@GetMapping("/getAllSubSectors")
	@ResponseBody
	public List<?> getAllSubSectors() {
		return aggregationConfigService.getAllSubSectors();
	}

	//Updates an pre-configured indicator. This gets called from indicator edit module.
	@PostMapping("/updateOne")
	@ResponseBody
	public ResponseEntity<String> updateOne(@RequestBody Indicator indicatorInfo) {
		return new ResponseEntity<String>(aggregationConfigService.updateOne(indicatorInfo), HttpStatus.OK);
	}
	
	// Downloads all configured indicators along with their metadata in excel format.
	@GetMapping(value = "excelDownloadIndicators")
	public ResponseEntity<InputStreamResource> excelDownloadIndicators() {

		String filePath = "";
		try {
			filePath = aggregationConfigService.excelDownloadIndicators();
			File file = new File(filePath);

			HttpHeaders respHeaders = new HttpHeaders();
			respHeaders.add("Content-Disposition", "attachment; filename=" + file.getName());
			InputStreamResource isr = new InputStreamResource(new FileInputStream(file));
			
			file.delete();
			return new ResponseEntity<InputStreamResource>(isr, respHeaders, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
	
	//This returns filtered indicator list on the basis of selected form, sector and sub-sector. This API gets called from Dashboard Configuration module.
	@GetMapping("/getIndicatorsBySector")
	@ResponseBody
	public List<?> getIndicatorsBySector(@RequestParam("formId") String formId, @RequestParam("sector") String sector, @RequestParam("subsector") String subsector) {
		System.out.println(formId +":"+sector+ ":"+subsector);
		return aggregationConfigService.getIndicatorsBySector(formId, sector, subsector);
	}

}
