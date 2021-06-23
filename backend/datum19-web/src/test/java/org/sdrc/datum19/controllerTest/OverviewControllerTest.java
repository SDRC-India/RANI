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

public class OverviewControllerTest extends Datum19ApplicationTests{

	@Autowired
	private WebApplicationContext webApplicationContext;
	private MockMvc mockMvc;
	
	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}
	
	@Test
	public void testAggregationHistory() throws Exception{
		mockMvc.perform(get("/aggregationHistory"))
		.andExpect(status().isOk());
			
				
	}
	
	@Test
	public void testApplicationDetails() throws Exception{
		mockMvc.perform(get("/applicationDetails"))
		.andExpect(status().isOk());
		
				
	}
	
}
