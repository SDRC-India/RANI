package org.sdrc.rani.repositories;

import java.util.List;

import org.sdrc.rani.document.KeyGeneratorColumnsSetting;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author subham
 *
 */
public interface KeyGeneratorColumnsSettingRepository extends MongoRepository<KeyGeneratorColumnsSetting, String> {
	
	List<KeyGeneratorColumnsSetting> findByFormFormId(int formId);
}
