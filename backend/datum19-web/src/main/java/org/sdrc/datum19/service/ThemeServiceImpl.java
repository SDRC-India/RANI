package org.sdrc.datum19.service;

import java.util.Optional;

import org.sdrc.datum19.document.Theme;
import org.sdrc.datum19.repository.ThemeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ThemeServiceImpl implements ThemeService {

	@Autowired
	private ThemeRepository themeRepository;
	@Override
	public String save(Theme theme) {
		// TODO Auto-generated method stub
		themeRepository.save(theme);
		return "success";
	}
	@Override
	public Theme setTheme(String themeId) {
		// TODO Auto-generated method stub
		Optional<Theme> themeObject = themeRepository.findById(themeId);
		Theme lastAppliedTheme = themeRepository.findByIsActiveTrue();
		
		Theme theme = new Theme();
		if(themeObject.isPresent()) {
			if(lastAppliedTheme!=null) {
				lastAppliedTheme.setIsActive(false);
				themeRepository.save(lastAppliedTheme);
			}
			
			theme = themeObject.get();
			theme.setIsActive(true);
			return themeRepository.save(theme);
		} else {
			return lastAppliedTheme;
		}
	}

}
