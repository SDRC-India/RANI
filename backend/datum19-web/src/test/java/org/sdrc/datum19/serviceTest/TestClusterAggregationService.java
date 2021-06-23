package org.sdrc.datum19.serviceTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.assertj.core.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.sdrc.datum19.Datum19ApplicationTests;
import org.sdrc.datum19.document.ClusterDataValue;
import org.sdrc.datum19.document.DataValue;
import org.sdrc.datum19.service.ClusterAggregationService;
import org.sdrc.datum19.util.AreaMapObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

public class TestClusterAggregationService extends Datum19ApplicationTests{

	@Autowired
	private WebApplicationContext webApplicationContext;
	private MockMvc mockMvc;
	
	@Autowired
	ClusterAggregationService clusterAggregationService;
	
	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();	
	}
	
	@Test
	public void testCreateClusterCollection() throws Exception{
		
		String data = clusterAggregationService.createClusterCollection();
		
		assertThat(data).isNotNull()
		.isEqualTo("success");
	}
	
	@Test
	public void testGetClusterList() throws Exception{
		
		/*List<Integer> d = new ArrayList<Integer>();
		//please add Appropriate data here
		d.add(0);
		AreaMapObject data = clusterAggregationService.getClusterList(d);
		
		List<Integer> areaList = data.getAreaList();
		Map<Integer, Integer> areaMap = data.getAreaMap();
		
		assertThat(data)
		.isNotNull();
		assertThat(areaList)
		.isNotNull()
		.isNotEmpty();
		assertThat(areaMap)
		.isNotNull().
		isNotEmpty();
		*/
	}
	
	@Test
	public void testaggregateFinalIndicators() throws Exception{
		
		List<ClusterDataValue> data = clusterAggregationService.aggregateFinalIndicators("nubmer","Health");
		
		assertThat(data).isNotNull();
	}
	
	@Test
	public void testgetAvgData() throws Exception{
		
		List<Integer> dependencies = new ArrayList();
		List<Integer> numlist = new ArrayList();
		List<Integer> denolist = new ArrayList();
		String aggrule = "abc";
		dependencies.add(1);
		numlist.add(1);
		denolist.add(1);
//		TypedAggregation<ClusterDataValue> data = clusterAggregationService.getAvgData(dependencies,numlist,denolist,aggrule);
	}
	
}

