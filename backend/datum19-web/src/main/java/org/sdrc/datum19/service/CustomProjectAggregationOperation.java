package org.sdrc.datum19.service;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;

/*
 * author : Biswabhusan Pradhan
 * email : biswabhusan@sdrc.co.in
 */
public class CustomProjectAggregationOperation implements AggregationOperation {

	private String jsonOperation;

    public CustomProjectAggregationOperation(String jsonOperation) {
        this.jsonOperation = jsonOperation;
    }

	@Override
	public Document toDocument(AggregationOperationContext context) {
		// TODO Auto-generated method stub
		return context.getMappedObject(Document.parse(jsonOperation));
	}

}
