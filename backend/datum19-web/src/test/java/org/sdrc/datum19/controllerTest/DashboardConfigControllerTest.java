package org.sdrc.datum19.controllerTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hamcrest.collection.IsMapContaining;
import org.junit.Before;
import org.junit.Test;
import org.sdrc.datum19.Datum19ApplicationTests;
import org.sdrc.datum19.document.DashboardIndicator;
import org.sdrc.datum19.document.DataSearch;
import org.sdrc.datum19.document.KGrams;
import org.sdrc.datum19.model.DashboardIndicatorGroupModel;
import org.sdrc.datum19.model.FormSectorModel;
import org.sdrc.datum19.model.SectorModel;
import org.sdrc.datum19.service.AggregationConfigService;
import org.sdrc.datum19.service.DashboardConfigService;
import org.sdrc.datum19.service.DashboardService;
import org.sdrc.datum19.service.DataSearchService;
import org.sdrc.datum19.util.ValueObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DashboardConfigControllerTest extends Datum19ApplicationTests{

	@Autowired	
	private WebApplicationContext webApplicationContext;
	private MockMvc mockMvc;
	
	
	
	@Autowired
	private DashboardConfigService dashboardConfigService;
	
	@Autowired
	public DashboardService dashboardService;
	
	@Autowired
	private DataSearchService dataSearchService;
	
	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}
	
	@Test
	public void testGetChartTypes() throws Exception{
		mockMvc.perform(get("/getChartTypes"))
		.andExpect(status().isOk());
		
		List<ValueObject> data = dashboardConfigService.getChartTypes();
		
		assertThat(data).isNotNull()
		.isNotEmpty()
		.allMatch(p->(p.getValue() != null))
		.allMatch(p->(p.getKey() != null))
		.allMatch(p->(p.getName() != null))
		.allMatch(p->(p.getValue().equals("BAR") 
				|| p.getValue().equals("all") 
				|| p.getValue().equals("card") 
				|| p.getValue().equals("column")
				|| p.getValue().equals("doughnut")
				|| p.getValue().equals("pie")
				|| p.getValue().equals("stack")
				|| p.getValue().equals("trend")
				));
		
		
	}
	
	@Test
	public void testGetUnits() throws Exception{
		mockMvc.perform(get("/getUnits"))
		.andExpect(status().isOk());
		
		List<ValueObject> data = dashboardConfigService.getUnits();
		
		assertThat(data).isNotNull()
		.isNotEmpty()
		.allMatch(p->(p.getValue() != null))
		.allMatch(p->(p.getKey() != null))
		.allMatch(p->(p.getName() != null))
		.allMatch(p->(p.getValue().equals("number") 
				|| p.getValue().equals("percent") 
				|| p.getValue().equals("avg") 
				|| p.getValue().equals("rate")
				));
	}
	
	@Test
	public void testGetAlignments() throws Exception{
		mockMvc.perform(get("/getAlignments"))
		.andExpect(status().isOk());
		
		List<ValueObject> data = dashboardConfigService.getAlignments();
		
		assertThat(data).isNotNull()
		.isNotEmpty()
		.allMatch(p->(p.getValue() != null))
		.allMatch(p->(p.getKey() != null))
		.allMatch(p->(p.getName() != null))
		.allMatch(p->(p.getName().equals("25 PERCENT") 
				|| p.getName().equals("33 PERCENT") 
				|| p.getName().equals("50 PERCENT") 
				|| p.getName().equals("100 PERCENT")))
		.allMatch(p->(p.getValue().equals("col-md-3") 
				|| p.getValue().equals("col-md-4") 
				|| p.getValue().equals("col-md-6") 
				|| p.getValue().equals("col-md-12")));
		
	}
	
	
	
	@Test
	@Rollback(true)
	public void tsetSaveDashboardIndicatorConfig() throws Exception{
		List<String> lst = Arrays.asList("1","2","3");
		List lst2 = new ArrayList();
		lst2.add(lst);
		DashboardIndicatorGroupModel model = new DashboardIndicatorGroupModel();
		model.setAlign("test");
		model.setChartGroup("test");
		model.setChartHeader("test");
		model.setChartIndicators(lst2);
		model.setChartLegends(lst);
		model.setChartType(lst);
		model.setColorLegends(lst);
		model.setExtraInfo("test");
		model.setFormId(1);
		model.setGroupName("test");
		model.setIndicatorGroup("tset");
		model.setKpiChartHeader("Test");
		model.setKpiIndicator(1);
		model.setSaveOrEdit("save");
		model.setSector("T4");
		model.setSubSector("T4 20CF");
		model.setSectorId(1);
		model.setSubSectorId(1);
		model.setUnit("number");
		model.setValueFrom("abcd");
		
		ObjectMapper mapper = new ObjectMapper();
		mockMvc.perform(post("/saveDashboardIndicatorConfig").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(model)))
		.andExpect(status().isOk());
//		.andExpect();
	}
	
		@Test
		public void testGetAllChecklistSectors() throws Exception{
			mockMvc.perform(get("/getAllChecklistSectors"))
			.andExpect(status().isOk());
			
			Map< String, List<FormSectorModel>> data = dashboardConfigService.getAllChecklistSectors();
			assertTrue(data.size()>0?true:false);
			
			assertThat(data,IsMapContaining.hasKey("T4"));
			assertThat(data,IsMapContaining.hasKey("CEM-CF"));
			assertThat(data,IsMapContaining.hasKey("Hemocue Testing"));
			assertThat(data,IsMapContaining.hasKey("T4 New"));
			assertThat(data,IsMapContaining.hasKey("MEDIA"));
			
	//		assertThat(data,IsMapContaining.hasValue(List));
			
		}
	
	@Test
	public void testGetDashboardData() throws Exception{
		mockMvc.perform(get("/getDashboardData?sectorName=T4"))
		.andExpect(status().isOk());
		
		List<SectorModel> data = null;//dashboardService.getDashboardData("T4", null);
		assertThat(data).isNotNull()
		.isNotEmpty()
		.allMatch(p->(p.getSectorName() != null))
		.allMatch(p->(p.getSubSectors() != null))
		.allMatch(p->(p.getSubSectors().get(0).getSubsectorName() != null))
		.allMatch(p->(p.getSubSectors().get(0).getIndicators().get(0).getIndicatorValue() != null))
		.allMatch(p->(p.getSubSectors().get(0).getIndicators().get(0).getIndicatorGroupName() != null))
		.allMatch(p->(p.getSubSectors().get(0).getIndicators().get(0).getChartData().get(0).getHeaderIndicatorName() != null))
		.allMatch(p->(p.getSubSectors().get(0).getIndicators().get(0).getChartData().get(0).getHeaderIndicatorValue() != null))
		.allMatch(p->(p.getSubSectors().get(0).getIndicators().get(0).getChartData().get(0).getChartDataValue().get(0).get(0).getAxis() != null))
		.allMatch(p->(p.getSubSectors().get(0).getIndicators().get(0).getChartData().get(0).getChartDataValue().get(0).get(0).getValue() != null))
		.allMatch(p->(p.getSubSectors().get(0).getIndicators().get(0).getChartData().get(0).getChartDataValue().get(0).get(0).getUnit() != null));
		
	}
	
	
	@Test
	public void testSuggestedIndicator() throws Exception{
		mockMvc.perform(get("/suggestedIndicators?username=subrata&areaId=6&tp=1"))
		.andExpect(status().isOk());
		
		List<DataSearch> data = dataSearchService.suggestIndicators("subrata", 6, 1);
		
		assertThat(data).isNotNull()
		.isNotEmpty()
		.allMatch(p->(p.getIndicator() != null))
		.allMatch(p->(p.getDatumId() != null))
		.allMatch(p->(p.getDataValue() != null))
		.allMatch(p->(p.getTp() != null))
		.allMatch(p->(p.getInid() != null))
		;
				
	}
	
	
	@Test
	public void testGetWords() throws Exception{
		mockMvc.perform(get("/getWords?charSet=lat"))
		.andExpect(status().isOk());
		
		List<KGrams> data = dataSearchService.getWords("lat");
		
		assertThat(data).isNotNull()
		.isNotEmpty()
		.allMatch(p->(p.getWord() != null))
		.allMatch(p->(p.getGram() != null))
		.allMatch(p->(p.getKgram() != null))
		.allMatch(p->(p.getWeight() != null))
		;
				
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
	
	@Test
	public void testGetDashboardIndicatorConfigGr() throws Exception{
		mockMvc.perform(get("/getDashboardIndicatorConfigGr"))
		.andExpect(status().isOk());
		
		List<DashboardIndicator> data = null;//dashboardConfigService.getDashboardIndicatorConfigGr();
		
		assertThat(data).isNotNull()
		.isNotEmpty()
		.allMatch(p->(p.getIndicatorGroup() != null))
		.allMatch(p->(p.getChartType() != null))
		.allMatch(p->(p.getSector() != null))
		.allMatch(p->(p.getSectorId() != null))
		.allMatch(p->(p.getSubSector() != null))
		.allMatch(p->(p.getSubSectorId() != null))
		.allMatch(p->(p.getUnit() != null))
		.allMatch(p->(p.getChartGroup() != null))
		.allMatch(p->(p.getFormId() != null))
		;
		
	}
	
	
	
	@Test
	@Rollback(true)
	public void tsetSaveDashboardIndicatorConfigFail() throws Exception{
		List<String> lst = Arrays.asList("1","2","3");
		List lst2 = new ArrayList();
		lst2.add(lst);
		DashboardIndicatorGroupModel model = new DashboardIndicatorGroupModel();
		model.setAlign("test");
		model.setChartGroup("test111");
		model.setChartHeader("test");
		model.setChartIndicators(lst2);
		model.setChartLegends(lst);
		model.setChartType(lst);
		model.setColorLegends(lst);
		model.setExtraInfo("test");
		model.setFormId(1);
		model.setGroupName("test");
		model.setIndicatorGroup("tset");
		model.setKpiChartHeader("Test");
		model.setKpiIndicator(1);
		model.setSaveOrEdit("save");
		model.setSector("T4");
		model.setSubSector("T4 20CF");
		model.setSectorId(1);
		model.setSubSectorId(1);
		model.setUnit("number");
		model.setValueFrom("abcd11");
		
//		ResponseEntity<String> data = dashboardConfigService.saveDashboardIndicatorConfig(model);
//		String status = data.getStatusCode().toString();HttpStatus d = data.getStatusCode();
//		System.out.println(status);
//		assertThat(status).isNotNull()
//		.isEqualTo("200 OK")
//		.isEqualTo("500 Internal Server Error.")
		;
//		.andExpect();
	}
	
	@Test
	public void testGetDashboardDataFail() throws Exception{
		
		
		/*
		 * List<SectorModel> data = dashboardService.getDashboardData("T", null);
		 * 
		 * assertThat(data).isNotNull() .isEmpty() ;
		 */
	}
	
	@Test
	public void testSuggestedIndicatorFail() throws Exception{
		
		
		List<DataSearch> data = dataSearchService.suggestIndicators("rajani", 6, 1);
		
		assertThat(data).isNotNull()
		.isEmpty()
		
		;
				
	}
	
	@Test
	public void testGetUnits2() throws Exception{
		mockMvc.perform(get("/getUnits"))
		.andExpect(status().isOk());
		
		List<ValueObject> data = dashboardConfigService.getUnits();
		
		assertThat(data).isNotNull()
		.isNotEmpty()
		.allMatch(p->(p.getValue() != null))
		.allMatch(p->(p.getKey() != null))
		.allMatch(p->(p.getName() != null))
		
		.allMatch(p->(p.getValue().equals("number") 
				|| p.getValue().equals("percent") 
				|| p.getValue().equals("avg") 
				|| p.getValue().equals("rate")
				));
	}
	
	@Test
	public void testGetAllChecklistSectors2() throws Exception{
		mockMvc.perform(get("/getAllChecklistSectors"))
		.andExpect(status().isOk());
		
		Map< String, List<FormSectorModel>> data = dashboardConfigService.getAllChecklistSectors();
		assertTrue(data.size()>0?true:false);
		
		assertThat(data,IsMapContaining.hasKey("T4"));
		assertThat(data,IsMapContaining.hasKey("CEM-CF"));
		assertThat(data,IsMapContaining.hasKey("Hemocue Testing"));
		assertThat(data,IsMapContaining.hasKey("T4 New"));
		assertThat(data,IsMapContaining.hasKey("MEDIA"));
		
		Set<String> k = data.keySet();
		String i = k.iterator().next();
		
		List<FormSectorModel> d = data.get(i);
		
		assertThat(d).isNotNull()
		.isNotEmpty()
		.allMatch(p->(p.getFormId() != null))
		.allMatch(p->(p.getSectorId() != null))
		.allMatch(p->(p.getSectorName() != null))
		.allMatch(p->(p.getFormName() != null))
		;
		
//		assertThat(data,IsMapContaining.hasValue(List));
		
	}
	
	@Test
	public void testDeleteChart() throws Exception{
		mockMvc.perform(get("/deleteChart?id=5e8dd39bc7e9964768ee3e45"))
		.andExpect(status().isOk());
	}
	
	@Test
	public void testEditIndiactor() throws Exception{
		mockMvc.perform(get("/getIndicatorConfigEdit?chartGroupName=Gr100"))
		.andExpect(status().isOk());
	}
	
	@Test
	public void testGetTypes() throws Exception{
		mockMvc.perform(get("/getTypes"))
		.andExpect(status().isOk());
	}
	
	@Test
	public void testGetDashboardGroupData() throws Exception{
		mockMvc.perform(get("/getDashboardGroupData?groupName=Gr59"))
		.andExpect(status().isOk());
	}
	
}
