package org.sdrc.datum19.controller;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sdrc.datum19.model.ParamModel;
import org.sdrc.datum19.model.SVGModel;
import org.sdrc.datum19.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExportController {
	
	@Autowired
	private ExportService exportService;
	
	@PostMapping(value = "/downloadChartDataPDF")
	public ResponseEntity<InputStreamResource> downloadChartDataPDF(@RequestBody List<SVGModel> listOfSvgs,
			@RequestParam(value = "sectorName", required = true) String sectorName,
			@RequestParam(value = "dashboardId", required = false) String dashboardId, HttpServletResponse response,
			HttpServletRequest request) {
		
		String filePath = "";
		try {
			filePath = exportService.downloadChartDataPDF(listOfSvgs, sectorName,dashboardId,request);
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

	@PostMapping(value = "/downloadChartDataExcel")
	public ResponseEntity<InputStreamResource> downloadChartDataExcel(@RequestBody ParamModel paramModel,
			HttpServletResponse response, HttpServletRequest request) {

		String filePath = "";
		try {
			filePath = exportService.downloadChartDataExcel(paramModel.getListOfSvgs(), paramModel, request);
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

}
