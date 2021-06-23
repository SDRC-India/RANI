package org.sdrc.datum19.service;

import org.sdrc.datum19.document.Theme;

public interface ThemeService {

	String save(Theme theme);

	Theme setTheme(String themeId);

}
