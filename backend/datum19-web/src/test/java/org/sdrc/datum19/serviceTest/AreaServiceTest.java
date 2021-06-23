package org.sdrc.datum19.serviceTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sdrc.datum19.Datum19ApplicationTests;
import org.sdrc.datum19.service.AreaService;
import org.sdrc.datum19.service.DashboardConfigService;
import org.sdrc.datum19.util.AreaMapObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

public class AreaServiceTest extends Datum19ApplicationTests{

	@Autowired
	private WebApplicationContext webApplicationContext;
	private MockMvc mockMvc;
	
	@Autowired
	AreaService areaService;
	
	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();	
	}
	
	@Test
	public void testGetAreaForAggregation() throws Exception{
		AreaMapObject data = areaService.getAreaForAggregation(2);
		List<Integer> areaList = data.getAreaList();
		Map<Integer, Integer> areaMap = data.getAreaMap();
		
		
//		assertThat(data).isNotNull();
//		assertThat(areaList)
//		.isNotNull()
//		.isNotEmpty();
//		assertThat(areaMap)
//		.isNotNull()
//		.isNotEmpty();
		
		
	}
	
	@Test
	public void testGetAreaForAggregationFail() throws Exception{
		AreaMapObject data = areaService.getAreaForAggregation(0);
		List<Integer> areaList = data.getAreaList();
		Map<Integer, Integer> areaMap = data.getAreaMap();
		
		
		assertThat(data).isNotNull();
		assertThat(areaList)
		.isEmpty();
		assertThat(areaMap)
		.isEmpty();
		
		
	}
	
	
}
