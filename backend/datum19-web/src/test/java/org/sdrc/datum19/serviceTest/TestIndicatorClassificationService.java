/*
 * package org.sdrc.datum19.serviceTest;
 * 
 * import static org.assertj.core.api.Assertions.assertThat;
 * 
 * import java.util.List;
 * 
 * import org.junit.Before; import org.junit.Test; import
 * org.sdrc.datum19.Datum19ApplicationTests; import
 * org.sdrc.datum19.service.DashboardConfigService; import
 * org.sdrc.datum19.service.IndicatorClassificationService; import
 * org.springframework.beans.factory.annotation.Autowired; import
 * org.springframework.test.web.servlet.MockMvc; import
 * org.springframework.test.web.servlet.setup.MockMvcBuilders; import
 * org.springframework.web.context.WebApplicationContext;
 * 
 * public class TestIndicatorClassificationService extends
 * Datum19ApplicationTests{
 * 
 * @Autowired private WebApplicationContext webApplicationContext; private
 * MockMvc mockMvc;
 * 
 * @Autowired IndicatorClassificationService indicatorClassificationService;
 * 
 * @Before public void setup() { mockMvc =
 * MockMvcBuilders.webAppContextSetup(webApplicationContext).build(); }
 * 
 * @Test public void testGetRelatedIndicators() throws Exception{
 * 
 * List<Integer> data = indicatorClassificationService.getRelatedIndicators(8);
 * 
 * 
 * assertThat(data).isNotNull() .isNotEmpty() ; }
 * 
 * 
 * @Test public void testGetRelatedIndicatorsFail() throws Exception{
 * 
 * List<Integer> data =
 * indicatorClassificationService.getRelatedIndicators(8000);
 * 
 * 
 * assertThat(data) .isNotEmpty() ; } }
 */