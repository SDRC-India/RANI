package org.sdrc.rani.repositories;

import org.sdrc.rani.document.PlanningQuestions;
import org.springframework.data.mongodb.repository.MongoRepository;


/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
public interface PlanningQuestionsRepository extends MongoRepository<PlanningQuestions, String>{

}
