package org.sdrc.datum19.repository;

import java.util.Optional;

import org.sdrc.datum19.document.Theme;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ThemeRepository extends MongoRepository<Theme, String> {

	Theme findByIsActiveTrue();

	Theme findByThemeName(String themeName);

}
