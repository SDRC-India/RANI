package org.sdrc.datum19.controllerTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hamcrest.collection.IsMapContaining;
import org.junit.Before;
import org.junit.Test;
import org.sdrc.datum19.Datum19ApplicationTests;
import org.sdrc.datum19.document.Indicator;
import org.sdrc.datum19.document.Sector;
import org.sdrc.datum19.document.Subsector;
import org.sdrc.datum19.model.ChartDataModel;
import org.sdrc.datum19.model.ConditionModel;
import org.sdrc.datum19.model.DashboardIndicatorGroupModel;
import org.sdrc.datum19.model.GroupChartDataModel;
import org.sdrc.datum19.model.IndicatorConfigModel;
import org.sdrc.datum19.model.IndicatorGroupModel;
import org.sdrc.datum19.model.LegendModel;
import org.sdrc.datum19.model.QuestionModel;
import org.sdrc.datum19.model.SectorModel;
import org.sdrc.datum19.model.SubsectorModel;
import org.sdrc.datum19.model.TypeDetailsModel;
import org.sdrc.datum19.service.AggregationConfigService;
import org.sdrc.datum19.service.DashboardConfigService;
import org.sdrc.datum19.util.ValueObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AggregationConfigControllerTest extends Datum19ApplicationTests{

	@Autowired
	private WebApplicationContext webApplicationContext;
	private MockMvc mockMvc;
	
	@Autowired
	private AggregationConfigService aggregationConfigService;
	
	
	
	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}
	
//	@Test
//	public void testEmp()throws Exception{
//		mockMvc.perform(get("/getById?id=2"))
//		.andExpect(status().isOk())
//		.andExpect(jsonPath("$.name").value("bulu"));
//	}
	
	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void testGetAllForm() throws Exception{
		
		
		mockMvc.perform(get("/getAllForm"))
		.andExpect(status().isOk());
		
		List<ValueObject> data = aggregationConfigService.getAllForm();
//		assertEquals(true, data.size()>0?true:false);
//		assertEquals(false, aggregationConfigService.getAllForm().get(0).getValue().isEmpty());
		assertFalse("Form value is not empty",aggregationConfigService.getAllForm().get(0).getValue().isEmpty());
		
		assertThat(data).isNotNull()
		.isNotEmpty()
		.allMatch(p->(p.getValue() != null))
		.allMatch(p->(p.getKey() != null));
		
		
	}
	
	@Test	
	public void testGetIndicatorsForView() throws Exception{
		mockMvc.perform(get("/getIndicatorsForView"))
		.andExpect(status().isOk());
		
		List data = aggregationConfigService.getIndicatorsForView();
		assertTrue(aggregationConfigService.getIndicatorsForView().size()>0?true:false);
		
		
	}
	@Test
	public void testGetQuestions() throws Exception{
		mockMvc.perform(get("/getQuestions"))
		.andExpect(status().isOk());
		
		Map<Integer, List<QuestionModel>> data = aggregationConfigService.getQuestions();
		assertTrue(data.size()>0?true:false);
		
//		assertThat(data).isNotNull()
//		.isNotEmpty()
//		.entrySet()
//		.map(Map.Entry :: getKey)
//		.findFirst()
//		.allMatch(p->(p.getKey() != null));
		
//		assartThat(data,IsMapContaining.hasKey(""));
//		assertThat(map.size(), is(4));
//		assertThat(map, not(IsMapContaining.hasEntry("r", "ruby")));
	}
	
	@Test
	public void testGetAggregationTypes() throws Exception{
		mockMvc.perform(get("/getAggregationTypes"))
		.andExpect(status().isOk());
		List<ValueObject> data = aggregationConfigService.getAggregationTypes();
		assertTrue(aggregationConfigService.getAggregationTypes().size()>0?true:false);
		
		assertThat(data).isNotNull()
		.isNotEmpty()
		.allMatch(p->(p.getValue() != null))
		.allMatch(p->(p.getKey() != null))
		.allMatch(p->(p.getName() != null));
	}
	
	@Test
	public void testGetTypeDetails() throws Exception{
		mockMvc.perform(get("/getTypeDetails"))
		.andExpect(status().isOk());
		assertTrue(aggregationConfigService.getTypeDetails().size()>0?true:false);
	}
	
	@Test
	public void testGetAllIndicators() throws Exception{
		mockMvc.perform(get("/getAllIndicators"))
		.andExpect(status().isOk());
		List data = aggregationConfigService.getIndicatorsForView();
		assertTrue(aggregationConfigService.getIndicatorsForView().size()>0?true:false);
		
		assertThat(data)
		.isNotNull()
		.isNotEmpty();
//		.allMatch(p->(Indicator)p.getIndicatorDataMap != null)
		
	}
	
	@Test
	public void testGetAllSectors() throws Exception{
		mockMvc.perform(get("/getAllSectors"))
		.andExpect(status().isOk());
		assertTrue(aggregationConfigService.getAllSectors().size()>0?true:false);
	}
	@Test
	public void testGetAllSubSectors() throws Exception{
		mockMvc.perform(get("/getAllSubSectors"))
		.andExpect(status().isOk());
		assertTrue(aggregationConfigService.getAllSubSectors().size()>0?true:false);
	}
	
//	@MockBean
//	AggregationConfigService AggregationConfigService2;
	@Test
	@Rollback(true)
	public void tsetSaveIndicator() throws Exception{
		
		
		IndicatorConfigModel model = new IndicatorConfigModel();
		ConditionModel conditon = new ConditionModel();
		conditon.setAssociation("test");
		conditon.setOperator("test");
		conditon.setQuestion(1);
		conditon.setValue(1.0);
				
		ArrayList<ConditionModel> c = new ArrayList<ConditionModel>();
		c.add(conditon);
		
		model.setAggregationRule("test");
		model.setAggregationType("test");
		model.setAreaColumn("test");
		model.setCollection("test");
		model.setConditions(c);
		model.setControllerType("test");
		model.setDenominator("test");
		model.setFormId("1");
		model.setHighIsGood("1");
		model.setIndicatorName("test");
		model.setIndicatorNid("1");
		model.setNumerator("1");
		model.setParentColumnName("test");
		model.setPeriodicity("1");
		model.setQuestionColumnName("test");
		model.setQuestionId("1");
		model.setQuestionName("test");
		model.setSector("test");
		model.setSubgroup("test");
		model.setSubsector("test");
		model.setTypeDetails(Arrays.asList(1,2,3));
		model.setUnit("1");
		
		ObjectMapper mapper = new ObjectMapper();
		mockMvc.perform(post("/saveIndicator").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(model)))
		.andExpect(status().isOk());
		
	}
	
	@Test
	public void testExcelDownload() throws Exception{
		mockMvc.perform(get("/excelDownloadIndicators"))
		.andExpect(status().isOk());
		
	}
	
	@Test
	public void testGetIndicatorsBySector() throws Exception{
		mockMvc.perform(get("/getIndicatorsBySector?formId=1&sector=T4&subsector=T4 CF"))
		.andExpect(status().isOk());
		
		List data = aggregationConfigService.getIndicatorsBySector("1","T4","T4 CF");
		
		assertThat(data)
		.isNotNull()
		.isNotEmpty();
	}
	
	@Test	
	public void testAddSector() throws Exception{
		
		SectorModel model = new SectorModel();
		
		SubsectorModel smodel = new SubsectorModel();
		
		IndicatorGroupModel imodel = new IndicatorGroupModel();	
		
		GroupChartDataModel chartDataModel = new GroupChartDataModel();
		
		ChartDataModel cmodel = new ChartDataModel();
		
		LegendModel legends = new LegendModel();
		
		cmodel.setAxis("x");
		cmodel.setCssClass("abc");
		cmodel.setDenominator("test");
		cmodel.setKey("a");
		cmodel.setLabel("lable");
		cmodel.setLegend("led");
		cmodel.setNumerator("2");
		cmodel.setUnit("cm");
		cmodel.setValue("5");
		List cmodlist = new ArrayList();
		cmodlist.add(cmodel);
		
		
		legends.setColor("red");
		legends.setCssClass("abc");
		legends.setEndRange(2.0);
		legends.setRange("1");
		legends.setStartRange(1.0);
		legends.setValue("1");
		List llist = new ArrayList();
		llist.add(legends);
		
//		chartDataModel.setHeaderIndicatorName("head test");
//		chartDataModel.setHeaderIndicatorValue(1);
//		chartDataModel.setLegends(llist);
//		chartDataModel.setChartDataValue(cmodlist);
		List clist = new ArrayList();
		clist.add(chartDataModel);
		
		imodel.setAlign("align");
		imodel.setCardType("ctype");
		imodel.setChartAlign("align");
		imodel.setChartData(clist);
		imodel.setChartGroup("cgro");
		List<String> cs = new ArrayList<String>();
		cs.add("1");
		imodel.setChartsAvailable(cs);
		imodel.setExtraInfo("extra");
		imodel.setGroupName("gname");
		imodel.setIndicatorGroupName("indicatorGroupName");
		imodel.setIndicatorId(1);
		imodel.setIndicatorName("testing");
		imodel.setIndicatorValue("5");
		imodel.setPeriodicity("2");
		imodel.setTimeperiod("1");
		imodel.setTimeperiodId(1);
		imodel.setUnit("cm");
		List imodelList = new ArrayList();
		imodelList.add(imodel);
		
		
		smodel.setFormId(1);
		smodel.setSectorId(1);
		smodel.setSubsectorName("subTest");
		smodel.setIndicators(imodelList);
		List smodelList = new ArrayList();
		smodelList.add(smodel);
		
		model.setFormId(1);
		model.setSectorName("test");
		model.setTimePeriod("1");
		model.setSubSectors(smodelList);
		
		ObjectMapper mapper = new ObjectMapper();
		mockMvc.perform(post("/addSector").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(model)))
		.andExpect(status().isOk());
		
//		String data = aggregationConfigService.addSector(model);
//		assertTrue(data.equals("Subsector added successfully. "));
		
		
	}
	
	
	@Test
	@Rollback(true)
	public void tsetSaveIndicatorFail1 () throws Exception{
		
		
		IndicatorConfigModel model = new IndicatorConfigModel();
		ConditionModel conditon = new ConditionModel();
		conditon.setAssociation("test");
		conditon.setOperator("test");
		conditon.setQuestion(1);
		conditon.setValue(1.0);
				
		ArrayList<ConditionModel> c = new ArrayList<ConditionModel>();
		c.add(conditon);
		
		model.setAggregationRule("test");
		model.setAggregationType("test");
		model.setAreaColumn("test");
		model.setCollection("test");
		model.setConditions(c);
		model.setControllerType("test");
		model.setDenominator("test");
		model.setFormId("o");
		model.setHighIsGood("1");
		model.setIndicatorName("test");
		model.setIndicatorNid("1");
		model.setNumerator("1");
		model.setParentColumnName("test");
		model.setPeriodicity("1");
		model.setQuestionColumnName("test");
		model.setQuestionId("1");
		model.setQuestionName("test");
		model.setSector("test");
		model.setSubgroup("test");
		model.setSubsector("test");
		model.setTypeDetails(Arrays.asList(1,2,3));
		model.setUnit("1");
		


			
//			String data = aggregationConfigService.saveIndiactor(model);
//			System.out.println(data);
//			
//			assertThat(data).isNotNull()
//			.contains("Some error occured, please try again...")
//			;
//		
//		.andExpect();
	}
	
	
	@Test	
	public void testAddSectorFail() throws Exception{
		
		SectorModel model = new SectorModel();
		
		SubsectorModel smodel = new SubsectorModel();
		
		IndicatorGroupModel imodel = new IndicatorGroupModel();	
		
		GroupChartDataModel chartDataModel = new GroupChartDataModel();
		
		ChartDataModel cmodel = new ChartDataModel();
		
		LegendModel legends = new LegendModel();
		
		cmodel.setAxis("x");
		cmodel.setCssClass("abc");
		cmodel.setDenominator("test");
		cmodel.setKey("a");
		cmodel.setLabel("lable");
		cmodel.setLegend("led");
		cmodel.setNumerator("2");
		cmodel.setUnit("cm");
		cmodel.setValue("5");
		List cmodlist = new ArrayList();
		cmodlist.add(cmodel);
		
		
		legends.setColor("red");
		legends.setCssClass("abc");
		legends.setEndRange(2.0);
		legends.setRange("1");
		legends.setStartRange(1.0);
		legends.setValue("1");
		List llist = new ArrayList();
		llist.add(legends);
		
		chartDataModel.setHeaderIndicatorName("head test");
		chartDataModel.setHeaderIndicatorValue(1);
		chartDataModel.setLegends(llist);
		chartDataModel.setChartDataValue(cmodlist);
		List clist = new ArrayList();
		clist.add(chartDataModel);
		
		imodel.setAlign("align");
		imodel.setCardType("ctype");
		imodel.setChartAlign("align");
		imodel.setChartData(clist);
		imodel.setChartGroup("cgro");
		List<String> cs = new ArrayList<String>();
		cs.add("1");
		imodel.setChartsAvailable(cs);
		imodel.setExtraInfo("extra");
		imodel.setGroupName("gname");
		imodel.setIndicatorGroupName("indicatorGroupName");
		imodel.setIndicatorId(1);
		imodel.setIndicatorName("testing");
		imodel.setIndicatorValue("5");
		imodel.setPeriodicity("2");
		imodel.setTimeperiod("1");
		imodel.setTimeperiodId(1);
		imodel.setUnit("cm");
		List imodelList = new ArrayList();
		imodelList.add(imodel);
		
		
		smodel.setFormId(10000);
		smodel.setSectorId(1);
		smodel.setSubsectorName("subTest");
		smodel.setIndicators(imodelList);
		List smodelList = new ArrayList();
		smodelList.add(smodel);
		
		model.setFormId(222);
		model.setSectorName("test");
		model.setTimePeriod("one");
		model.setSubSectors(smodelList);
		
//		ObjectMapper mapper = new ObjectMapper();
//		mockMvc.perform(post("/addSector").contentType(MediaType.APPLICATION_JSON)
//		.content(mapper.writeValueAsString(model)))
//		.andExpect(status().isOk());
		
		try {
//		String data = aggregationConfigService.addSector(model);
//		assertTrue(data.equals(""));
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	@Test
	public void testGetIndicatorsBySectorFail() throws Exception{
		
		List data = aggregationConfigService.getIndicatorsBySector("10000","T4","T4 CF");
		
		assertThat(data)
		.isEmpty();
	}
	
	
	@Test
	public void testGetAllForm2() throws Exception{
		
		
		mockMvc.perform(get("/getAllForm"))
		.andExpect(status().isOk());
		
		List<ValueObject> data = aggregationConfigService.getAllForm();
//		assertEquals(true, data.size()>0?true:false);
//		assertEquals(false, aggregationConfigService.getAllForm().get(0).getValue().isEmpty());
		assertFalse("Form value is not empty",aggregationConfigService.getAllForm().get(0).getValue().isEmpty());
		
		assertThat(data).isNotNull()
		.isNotEmpty()
		.allMatch(p->(p.getValue() != null))
		.allMatch(p->(p.getKey() != null))
		.allMatch(p->(p.getDesignationIds()==null))
		.allMatch(p->(p.getEnable()==null))
		.allMatch(p->(p.getName()==null))
		
		;
		
		
	}
	
	@Test	
	public void testGetIndicatorsForView2() throws Exception{
		mockMvc.perform(get("/getIndicatorsForView"))
		.andExpect(status().isOk());
		
		List data = aggregationConfigService.getIndicatorsForView();
		assertTrue(aggregationConfigService.getIndicatorsForView().size()>0?true:false);
		
		Indicator i = (Indicator) data.get(0);
		Map<String, Object> indicatorDataMap = i.getIndicatorDataMap();
		
		assertThat(data).isNotNull()
		.isNotEmpty()
		;
	}
	
	
	@Test
	public void testGetQuestions2() throws Exception{
		mockMvc.perform(get("/getQuestions"))
		.andExpect(status().isOk());
		
		Map<Integer, List<QuestionModel>> data = aggregationConfigService.getQuestions();
		assertTrue(data.size()>0?true:false);
		
		Set<Integer> k = data.keySet();
		int i = k.iterator().next();
		
		 List<QuestionModel> q = data.get(i);
		 
		 assertThat(q).isNotNull()
		 .isNotEmpty()
		 .allMatch(p->(p.getQuestionName() != null))
		 .allMatch(p->(p.getQuestionId() != null))
		 .allMatch(p->(p.getControllerType() != null))
		 .allMatch(p->(p.getFieldType() != null))
		 .allMatch(p->(p.getColumnName() != null))
		 .allMatch(p->(p.getFormId() != null))
		 .allMatch(p->(p.getSector() != null))
		 .allMatch(p->(p.getSubsector() != null))
		 ;
	}
	
	@Test
	public void testGetTypeDetails2() throws Exception{
		mockMvc.perform(get("/getTypeDetails"))
		.andExpect(status().isOk());
		assertTrue(aggregationConfigService.getTypeDetails().size()>0?true:false);
		Map<Integer, Map<Integer, List<TypeDetailsModel>>> data = aggregationConfigService.getTypeDetails();
		Set<Integer> k1 = data.keySet();
		int i1 = k1.iterator().next();
		Map<Integer, List<TypeDetailsModel>> d2 = data.get(i1);
		Set<Integer> k2 = d2.keySet();
		int i2 = k2.iterator().next();
		List<TypeDetailsModel> d3 = d2.get(i2);
		
		assertThat(d3).isNotNull()
		.isNotEmpty()
		.allMatch(p->(p.getTypeDetailId() != null))
		.allMatch(p->(p.getFormId() != null))
		.allMatch(p->(p.getTypeDetailName() != null))
		.allMatch(p->(p.getTypeId() != null))
		;
	}
	
	@Test
	public void testGetAllIndicators2() throws Exception{
		mockMvc.perform(get("/getAllIndicators"))
		.andExpect(status().isOk());
		List data = aggregationConfigService.getIndicatorsForView();
		assertTrue(aggregationConfigService.getIndicatorsForView().size()>0?true:false);
		
		Indicator i = (Indicator) data.get(0);
		Map<String, Object> indicatorDataMap = i.getIndicatorDataMap();
		
		assertThat(indicatorDataMap)
		.isNotNull()
		.isNotEmpty()
		;
		
	}
	
	
	@Test
	public void testGetAllSectors2() throws Exception{
		mockMvc.perform(get("/getAllSectors"))
		.andExpect(status().isOk());
		assertTrue(aggregationConfigService.getAllSectors().size()>0?true:false);
		
		List<Sector> data = (List<Sector>) aggregationConfigService.getAllSectors();
		
		assertThat(data).isNotNull()
		.isNotEmpty()
		.allMatch(p->(p.getFormId() != null))
		.allMatch(p->(p.getSectorId() != null))
		.allMatch(p->(p.getSectorName() != null))
		;
	}
	
	
	@Test
	public void testGetAllSubSectors2() throws Exception{
		mockMvc.perform(get("/getAllSubSectors"))
		.andExpect(status().isOk());
		assertTrue(aggregationConfigService.getAllSubSectors().size()>0?true:false);
		
		List<Subsector> data = (List<Subsector>) aggregationConfigService.getAllSubSectors();
		
		assertThat(data).isNotNull()
		.isNotEmpty()
		.allMatch(p->(p.getFormId() != null))
		.allMatch(p->(p.getSubSectorId() != null))
		
		;
	}
	
	@Test
	public void testUpdateOne() throws Exception{
		Indicator indicatorInfo = new Indicator();
		
		Map< String, Object> d = new HashMap<String, Object>();
		
		d.put("formId", "1000");
		d.put("indicatorNid", "8");
		d.put("indicatorName", "Persons with disability-Urban Female");
		d.put("sector", "Health");
		d.put("subsector", "Health");
		d.put("subgroup", "Urban Female");
		d.put("highIsGood", "false");
		d.put("unit", "number");
		d.put("typeDetailId", "");
		d.put("source", "secondary");
		
		
		indicatorInfo.setIndicatorDataMap(d);
		indicatorInfo.setId("1");
		
		ObjectMapper mapper = new ObjectMapper();
		mockMvc.perform(post("/updateOne").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(indicatorInfo)))
		.andExpect(status().isOk());
		
	}
	
	@Test
	public void testAddSubSector() throws Exception{
		SubsectorModel subsectorModel = new SubsectorModel();
		subsectorModel.setFormId(1);
		subsectorModel.setSectorId(1);
		subsectorModel.setSubsectorName("test");
		
		ObjectMapper mapper = new ObjectMapper();
		mockMvc.perform(post("/addSubSector").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(subsectorModel)))
		.andExpect(status().isOk());
	}
}
