package org.sdrc.datum19.controllerTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.sdrc.datum19.Datum19ApplicationTests;
import org.sdrc.datum19.controller.DAUS19JobController;
import org.sdrc.datum19.document.TimePeriod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

public class DAUS19JobControllerTest extends Datum19ApplicationTests{

	@Autowired
	private WebApplicationContext webApplicationContext;
	private MockMvc mockMvc;
	
	@Autowired
	private DAUS19JobController dAUS19JobController;
	
	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}
	
	@Test
	public void testRunMonthlyJob() throws Exception{
//		mockMvc.perform(get("/runJob"))
//		.andExpect(status().isOk())
//		;
	}
	
	@Test
	public void testRunQuarterlyJob() throws Exception{
//		mockMvc.perform(get("/runQuarterlyJob"))
//		.andExpect(status().isOk())
//		;
	}
	
	@Test
	public void testRunYearlyJob() throws Exception{
//		mockMvc.perform(get("/runQuarterlyJob"))
//		.andExpect(status().isOk())
//		;
	}
	
	@Test
	public void testGetYearForAggregation() throws Exception{
//		
		
//		TimePeriod data = dAUS19JobController.getYearForAggregation();
//		
//		assertThat(data).isNotNull()
//		;
//		assertTrue(data.getTimePeriodDuration()!= null);
//		assertTrue(data.getPeriodicity() != null);
//		assertTrue(data.getFinancialYear() != null);
//		assertTrue(data.getYear() != null);
//		assertTrue(data.getTimePeriodId() != null);
	}
	
	@Test
	public void testGetQuarterForAggregation() throws Exception{
		
//		TimePeriod data = dAUS19JobController.getQuarterForAggregation();
//		
//		assertThat(data).isNotNull();
//		assertTrue(data.getTimePeriodDuration()!= null);
//		assertTrue(data.getPeriodicity() != null);
//		assertTrue(data.getFinancialYear() != null);
//		assertTrue(data.getYear() != null);
//		assertTrue(data.getTimePeriodId() != null);
	}
	
	@Test
	public void testGetTimePeriodForAggregation() throws Exception{
		
//		TimePeriod data = dAUS19JobController.getTimePeriodForAggregation();
//		
//		assertThat(data).isNotNull();
//		assertTrue(data.getTimePeriodDuration()!= null);
//		assertTrue(data.getPeriodicity() != null);
//		assertTrue(data.getFinancialYear() != null);
//		assertTrue(data.getYear() != null);
//		assertTrue(data.getTimePeriodId() != null);
	}
	
	@Test
	public void testToISO8601UTC() throws Exception{
		String data = dAUS19JobController.toISO8601UTC(new Date());
		
		assertThat(data).isNotNull();
	}
	
}
