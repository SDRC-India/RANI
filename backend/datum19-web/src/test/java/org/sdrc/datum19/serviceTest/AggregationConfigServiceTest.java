package org.sdrc.datum19.serviceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.assertj.core.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.sdrc.datum19.Datum19ApplicationTests;
import org.sdrc.datum19.document.Indicator;
import org.sdrc.datum19.document.TimePeriod;
import org.sdrc.datum19.model.ChartDataModel;
import org.sdrc.datum19.model.ConditionModel;
import org.sdrc.datum19.model.GroupChartDataModel;
import org.sdrc.datum19.model.IndicatorConfigModel;
import org.sdrc.datum19.model.IndicatorGroupModel;
import org.sdrc.datum19.model.LegendModel;
import org.sdrc.datum19.model.QuestionModel;
import org.sdrc.datum19.model.SectorModel;
import org.sdrc.datum19.model.SubsectorModel;
import org.sdrc.datum19.model.TypeDetailsModel;
import org.sdrc.datum19.service.AggregationConfigService;
import org.sdrc.datum19.util.ValueObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AggregationConfigServiceTest extends Datum19ApplicationTests {

	@Autowired
	private WebApplicationContext webApplicationContext;
	private MockMvc mockMvc;
	
	@Autowired
	private AggregationConfigService aggregationConfigService;
	
	
	
	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}
	
	@Test
	public void testAddSector() throws Exception{
		SectorModel sectorModel = new SectorModel();
		sectorModel.setFormId(1);
		sectorModel.setSectorName("test");
		sectorModel.setTimePeriod("1");
		
		String data = aggregationConfigService.addSector(sectorModel);
		
		assertTrue(data.equals("Sector added successfully. ")?true:false);
	}
	
	@Test
	public void testAddSubSector() throws Exception{
		
		SubsectorModel subsectorModel = new SubsectorModel();
		subsectorModel.setFormId(1);
		subsectorModel.setSectorId(1);
		subsectorModel.setSubsectorName("test");
		
		String data = aggregationConfigService.addSubSector(subsectorModel);
		
		assertTrue(data.equals("Subsector added successfully. ")?true:false);
	}
	
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
	public void testGetQuestions() throws Exception{

		
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
		
		List<ValueObject> data = aggregationConfigService.getAggregationTypes();
		assertTrue(aggregationConfigService.getAggregationTypes().size()>0?true:false);
		
		assertThat(data).isNotNull()
		.isNotEmpty()
		.allMatch(p->(p.getValue() != null))
		.allMatch(p->(p.getKey() != null))
		.allMatch(p->(p.getName() != null));
	}
	
	@Test
	public void testGetAllIndicators() throws Exception{
		
		List data = aggregationConfigService.getIndicatorsForView();
		assertTrue(aggregationConfigService.getIndicatorsForView().size()>0?true:false);
		
		assertThat(data)
		.isNotNull()
		.isNotEmpty();
//		.allMatch(p->(Indicator)p.getIndicatorDataMap != null)
		
	}
	
	@Test
	public void testGetTypeDetails() throws Exception{
		
		assertTrue(aggregationConfigService.getTypeDetails().size()>0?true:false);
	}
	
	@Test
	public void testGetIndicatorsBySector() throws Exception{
		
		
		List data = aggregationConfigService.getIndicatorsBySector("1","T4","T4 CF");
		
		assertThat(data)
		.isNotNull()
		.isNotEmpty();
	}
	
	@Test
	public void testExcelDownloadIndicators() throws Exception{
		String data = aggregationConfigService.excelDownloadIndicators();
		
		assertThat(data).isNotBlank();
	}
	
	@Test
	public void testGetAllSectors() throws Exception{
		List data = aggregationConfigService.getAllSectors();
		
			assertThat(data).isNotEmpty()
			.isNotNull();
	}
	
	@Test
	public void testGetAllSubSectors() throws Exception{
		
		List data = aggregationConfigService.getAllSubSectors();
		
		assertThat(data).isNotNull()
		.isNotEmpty();
	}
	
	@Test
	public void testSaveIndiactor() throws Exception{
		
		IndicatorConfigModel dataModel = new IndicatorConfigModel();
		
		dataModel.setAggregationRule("test");
		dataModel.setAggregationType("ag-test");
		dataModel.setAreaColumn("area");
		dataModel.setCollection("collection");
		
		ConditionModel cmodel = new ConditionModel();
		cmodel.setAssociation("aso");
		cmodel.setOperator("operator");
		cmodel.setQuestion(1);
		cmodel.setValue(1.0);
		List<ConditionModel> cmodelList = new ArrayList<ConditionModel>();
		cmodelList.add(cmodel);
		
		dataModel.setConditions(cmodelList);
		dataModel.setControllerType("ctype");
		dataModel.setDenominator("1");
		dataModel.setFormId("1");
		dataModel.setHighIsGood("high");
		dataModel.setIndicatorName("iname");
		dataModel.setIndicatorNid("1");
		dataModel.setNumerator("1");
		dataModel.setParentColumnName("test");
		dataModel.setPeriodicity("1");
		dataModel.setQuestionColumnName("qname");
		dataModel.setQuestionId("1");
		dataModel.setQuestionName("qname");
		dataModel.setSector("test");
		dataModel.setSubgroup("subgroup");
		dataModel.setSubsector("subsector");
		
		List<Integer>  d = new ArrayList<Integer>();
		d.add(1);
		d.add(2);
		d.add(3);
		dataModel.setTypeDetails(d);
		
		dataModel.setUnit("unit");
		
		String data = aggregationConfigService.saveIndiactor(dataModel);
		
		assertThat(data).isNotBlank()
		.isEqualTo("Indicator added successfully. ");
		
	}
	
	@Test
	public void testUpdateOne() throws Exception{
		
		Indicator indicatorInfo = new Indicator();
		
		Map< String, Object> d = new HashMap<String, Object>();
		
		d.put("formId", "14");
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
	}
	
	
	@Test
	public void testGetIndicatorsBySectorFail() throws Exception{
		
		
		List data = aggregationConfigService.getIndicatorsBySector("1000","T","T4 CF");
		
		assertThat(data)
		.isEmpty();
	}
	
	@Test
	public void testSaveIndiactorFail() throws Exception{
		
		IndicatorConfigModel dataModel = new IndicatorConfigModel();
		
		dataModel.setAggregationRule("test");
		dataModel.setAggregationType("ag-test");
		dataModel.setAreaColumn("area");
		dataModel.setCollection("collection");
		
		ConditionModel cmodel = new ConditionModel();
		cmodel.setAssociation("aso");
		cmodel.setOperator("operator");
		cmodel.setQuestion(1);
		cmodel.setValue(1.0);
		List<ConditionModel> cmodelList = new ArrayList<ConditionModel>();
		cmodelList.add(cmodel);
		
		dataModel.setConditions(cmodelList);
		dataModel.setControllerType("ctype");
		dataModel.setDenominator("1");
		dataModel.setFormId("ok");
		dataModel.setHighIsGood("high");
		dataModel.setIndicatorName("iname");
		dataModel.setIndicatorNid("1");
		dataModel.setNumerator("1");
		dataModel.setParentColumnName("test");
		dataModel.setPeriodicity("1");
		dataModel.setQuestionColumnName("qname");
		dataModel.setQuestionId("1");
		dataModel.setQuestionName("qname");
		dataModel.setSector("test");
		dataModel.setSubgroup("subgroup");
		dataModel.setSubsector("subsector");
		
		List<Integer>  d = new ArrayList<Integer>();
		d.add(1);
		d.add(2);
		d.add(3);
		dataModel.setTypeDetails(d);
		
		dataModel.setUnit("unit");
		
		String data = aggregationConfigService.saveIndiactor(dataModel);
		
		assertThat(data).isNotBlank()
		.isEqualTo("Some error occured, please try again...");
		
	}
	
	@Test
	public void testUpdateOneFail() throws Exception{
		
		Indicator indicatorInfo = new Indicator();
		
		Map< String, Object> d = new HashMap<String, Object>();
		
		d.put("formId", "1400");
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
		
		String  data = aggregationConfigService.updateOne(indicatorInfo);
		
		assertThat(data).isNotBlank()
		.isNotNull()
		.isEqualTo("Unable to update indicator, please try again.");
	}
	@Test
	public void testGetQuestions2() throws Exception{

		
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
	public void testGetAllIndicators2() throws Exception{
		
		List data = aggregationConfigService.getIndicatorsForView();
		assertTrue(aggregationConfigService.getIndicatorsForView().size()>0?true:false);
		
		assertThat(data)
		.isNotNull()
		.isNotEmpty();
//		.allMatch(p->(Indicator)p.getIndicatorDataMap != null)
		

		Indicator i = (Indicator) data.get(0);
		Map<String, Object> indicatorDataMap = i.getIndicatorDataMap();
		
		assertThat(indicatorDataMap)
		.isNotNull()
		.isNotEmpty()
		;
		
	}
	
	@Test
	public void testGetTypeDetails2() throws Exception{
		
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
}


