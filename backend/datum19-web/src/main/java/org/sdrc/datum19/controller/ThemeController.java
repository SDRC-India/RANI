package org.sdrc.datum19.controller;

import java.util.List;

import org.sdrc.datum19.document.Theme;
import org.sdrc.datum19.repository.ThemeRepository;
import org.sdrc.datum19.service.ThemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

//@CrossOrigin
@RestController
public class ThemeController {
	
	@Autowired
	private ThemeService themeService;
	
	@Autowired
	private ThemeRepository themeRepository;
	
	@PostMapping("/saveTheme")
	public String saveTheme(@RequestBody Theme theme) {
		return themeService.save(theme);
	}
	
	@PostMapping("/applyTheme")
	public Theme setTheme(@RequestParam("themeId") String themeId) {
		return themeService.setTheme(themeId);
	}
	
	@GetMapping("/getSelectedTheme")
	public Theme getTheme() {
		return themeRepository.findByIsActiveTrue();
	}
	
	@GetMapping("/getThemeList")
	public List<Theme> getThemes(){
		return themeRepository.findAll();
	}
}
