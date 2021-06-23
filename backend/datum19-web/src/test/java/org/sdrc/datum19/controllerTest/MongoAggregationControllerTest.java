package org.sdrc.datum19.controllerTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.sdrc.datum19.Datum19ApplicationTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

public class MongoAggregationControllerTest extends Datum19ApplicationTests{

	@Autowired
	private WebApplicationContext webApplicationContext;
	private MockMvc mockMvc;
	
	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}
	
	@Test
	public void testGetMongoAggregatedData() throws Exception{
//		mockMvc.perform(get("/mongoAggregate?tp=1&periodicity=1"))
//		.andExpect(status().isOk())
//		;
	}
	
	@Test
	public void testAggregatepercent() throws Exception{
//		mockMvc.perform(get("/aggregatepercent?periodicity=1&type="))
//		.andExpect(status().isOk())
//		;
	}
	
	@Test
	public void testImportIndicators() throws Exception{
//		mockMvc.perform(get("/importIndicators"))
//		.andExpect(status().isOk())
//		;
	}
	
	@Test
	public void testAggregateCumulativeData() throws Exception{
//		mockMvc.perform(get("/aggregateCumulativeData"))
//		.andExpect(status().isOk())
//		;
	}
}
