package org.sdrc.datum19.repository;

import org.sdrc.datum19.document.ThematicFileData;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ThematicFileDataRepository extends MongoRepository<ThematicFileData, String> {

	ThematicFileData findBySlugId(Integer slugId);
}
