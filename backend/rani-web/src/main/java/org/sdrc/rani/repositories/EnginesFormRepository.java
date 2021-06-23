package org.sdrc.rani.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import in.co.sdrc.sdrcdatacollector.document.EnginesForm;

public interface EnginesFormRepository  extends MongoRepository<EnginesForm, String> {

	List<EnginesForm> findByFormIdIn(List<Integer> formIds);

	EnginesForm findByFormId(Integer formId);

	EnginesForm findByName(String formId);

	EnginesForm findByDisplayName(String formId);
}
