package org.sdrc.datum19.serviceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sdrc.datum19.Datum19ApplicationTests;
import org.sdrc.datum19.model.SectorModel;
import org.sdrc.datum19.service.AreaService;
import org.sdrc.datum19.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

public class TestDashboardService extends Datum19ApplicationTests{
	
	@Autowired
	private WebApplicationContext webApplicationContext;
	private MockMvc mockMvc;
	
	@Autowired
	public DashboardService dashboardService;
	
	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();	
	}
	
	@Test
	public void testGetDashboardData() throws Exception{
		
		
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
	public void testGetDashboardDataFail() throws Exception{
		
		
		List<SectorModel> data = null;//dashboardService.getDashboardData("T", null);
		
		assertThat(data).isNotNull()
		.isEmpty()
		;		
	}
	
	@Test
	public void testGetDashboardData2() throws Exception{
		
		
		List<SectorModel> data = null;//dashboardService.getDashboardData("T4 Approach", null);
		
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
}
