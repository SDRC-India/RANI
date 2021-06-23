package org.sdrc.datum19.controllerTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.sdrc.datum19.Datum19ApplicationTests;
import org.sdrc.datum19.service.ClusterAggregationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

public class ClusterAggregationControllerTest extends Datum19ApplicationTests{

	@Autowired
	private WebApplicationContext webApplicationContext;
	private MockMvc mockMvc;
	
	@Autowired
	private ClusterAggregationService clusterAggregationService;
	
	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}
	
	@Test
	public void testCreateClusterCollection() throws Exception{
		
//		mockMvc.perform(get("/createCluster"))
//		.andExpect(status().isOk());
//		
//		String data = clusterAggregationService.createClusterCollection();
//		
//		assertThat(data).isNotNull()
//		.isBlank()
//		.isEqualTo("success");
	}
	
	@Test
	public void testExportTypeDetails() throws Exception{
//		mockMvc.perform(get("/exportTypeDetails"))
//		.andExpect(status().isOk());
	}
	
	@Test
	public void testUserAggregation() throws Exception{
//		mockMvc.perform(get("/testUserAggregation"))
//		.andExpect(status().isOk());
	}
	
}
