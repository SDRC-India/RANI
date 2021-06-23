package org.sdrc.datum19.repository;

import java.util.List;

import org.sdrc.datum19.document.Heading;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface HeadingRepository extends MongoRepository<Heading, String> {

	Heading findByTitle(String title);
	
	@Query(value = "{ 'title':{$regex:?0,$options:'i'}}")
	List<Heading> findByTitleLikeOrderByTitle(String upperCase);

}
