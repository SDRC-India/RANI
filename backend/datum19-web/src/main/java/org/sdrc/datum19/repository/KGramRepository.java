package org.sdrc.datum19.repository;

import java.util.List;

import org.sdrc.datum19.document.KGrams;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface KGramRepository extends MongoRepository<KGrams, String> {

	List<KGrams> findTop10ByKgramOrderByGramDescWeightAsc(String charSet);

	List<KGrams> findTop10ByKgramInOrderByGramDescWeightAsc(List<String> substr);

}
