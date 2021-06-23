package org.sdrc.rani.repositories;


import java.util.List;

import org.sdrc.rani.document.IFASupplyPointMapping;
import org.sdrc.rani.models.IFASupplyPointType;
import org.springframework.data.mongodb.repository.MongoRepository;


/**
 * @author subham
 *
 */
public interface IFASupplyPointMappingRepository extends MongoRepository<IFASupplyPointMapping, String>{

	List<IFASupplyPointMapping> findByType(IFASupplyPointType awc);

	List<IFASupplyPointMapping> findByDesgId(String id);

	List<IFASupplyPointMapping> findByIdIn(List<String> list);

	List<IFASupplyPointMapping> findByTypeOrderBySlugIdAsc(IFASupplyPointType awc);

	List<IFASupplyPointMapping> findByIdInOrderBySlugIdAsc(List<String> awcs);

}
