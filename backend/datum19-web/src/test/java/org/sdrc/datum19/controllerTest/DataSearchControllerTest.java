/*
 * package org.sdrc.datum19.controllerTest;
 * 
 * import static org.assertj.core.api.Assertions.assertThat; import static
 * org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
 * import static
 * org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
 * import static
 * org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
 * import static
 * org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 * 
 * import java.util.List;
 * 
 * import org.junit.Before; import org.junit.Test; import
 * org.sdrc.datum19.Datum19ApplicationTests; import
 * org.sdrc.datum19.document.DataSearch; import
 * org.sdrc.datum19.document.KGrams; import org.sdrc.datum19.model.BarChartData;
 * import org.sdrc.datum19.service.DataSearchService; import
 * org.sdrc.datum19.service.IndicatorClassificationService; import
 * org.springframework.beans.factory.annotation.Autowired; import
 * org.springframework.test.web.servlet.MockMvc; import
 * org.springframework.test.web.servlet.result.MockMvcResultMatchers; import
 * org.springframework.test.web.servlet.setup.MockMvcBuilders; import
 * org.springframework.web.context.WebApplicationContext; import
 * org.sdrc.datum19.controller.DataSearchController;
 * 
 * public class DataSearchControllerTest extends Datum19ApplicationTests{
 * 
 * @Autowired private WebApplicationContext webApplicationContext; private
 * MockMvc mockMvc;
 * 
 * @Autowired private DataSearchService dataSearchService;
 * 
 * @Autowired DataSearchController dataSearchController;
 * 
 * @Autowired private IndicatorClassificationService
 * indicatorClassificationService;
 * 
 * 
 * 
 * @Before public void setup() { mockMvc =
 * MockMvcBuilders.webAppContextSetup(webApplicationContext).build(); }
 * 
 * @Test public void testSuggestedIndicator() throws Exception{
 * mockMvc.perform(get("/suggestedIndicators?username=subrata&areaId=6&tp=1"))
 * .andExpect(status().isOk());
 * 
 * List<DataSearch> data = dataSearchService.suggestIndicators("subrata", 6, 1);
 * 
 * assertThat(data).isNotNull() .isNotEmpty() .allMatch(p->(p.getIndicator() !=
 * null)) .allMatch(p->(p.getDatumId() != null)) .allMatch(p->(p.getDataValue()
 * != null)) .allMatch(p->(p.getTp() != null)) .allMatch(p->(p.getInid() !=
 * null)) ;
 * 
 * }
 * 
 * 
 * @Test public void testGetWords() throws Exception{
 * mockMvc.perform(get("/getWords?charSet=lat")) .andExpect(status().isOk());
 * 
 * List<KGrams> data = dataSearchService.getWords("lat");
 * 
 * assertThat(data).isNotNull() .isNotEmpty() .allMatch(p->(p.getWord() !=
 * null)) .allMatch(p->(p.getGram() != null)) .allMatch(p->(p.getKgram() !=
 * null)) .allMatch(p->(p.getWeight() != null)) ;
 * 
 * }
 * 
 * @Test public void testGetDataForSearchedIndicator() throws Exception{
 * mockMvc.perform(get(
 * "/getDataForSearchedIndicator?indicatorId=462&datumId=5007&username=subrata")
 * ) .andDo(print()) // .andExpect(jsonPath("$.letter").exists()) //
 * .andExpect(jsonPath("$.frequency").exists()) ;
 * 
 * // List<BarChartData> data =
 * dataSearchController.getDataForSearchedIndicator("462",5007,"subrat"); }
 * 
 * @Test public void testGetSuggestedAreas() throws Exception{
 * mockMvc.perform(get("/suggestedAreas?indicatorId=462&tp=1&areaId=6"))
 * .andExpect(status().isOk()) .andDo(print()) //
 * .andExpect(jsonPath("$.letter").exists()) //
 * .andExpect(jsonPath("$.frequency").exists()) ; }
 * 
 * @Test public void testGetRelatedIndicators() throws Exception{
 * mockMvc.perform(get("/getRelatedIndicators?indicatorId=462&areaId=6&tp=1"))
 * .andExpect(status().isOk()) ;
 * 
 * List<Integer> relatedIndiactors =
 * indicatorClassificationService.getRelatedIndicators(462);
 * 
 * List<DataSearch> data =
 * dataSearchService.getRelatedSearchResult(relatedIndiactors, 6, 1);
 * 
 * assertThat(data).isNotNull() .isNotEmpty() .allMatch(p->(p.getIndicator() !=
 * null)) .allMatch(p->(p.getInid() != null)) .allMatch(p->(p.getTp() != null))
 * .allMatch(p->(p.getDataValue() != null)) ; }
 * 
 * @Test public void testClassifyIndicators() throws Exception{ //
 * mockMvc.perform(get("/classifyIndicators")) // .andExpect(status().isOk()) //
 * ; }
 * 
 * @Test public void testGenerateNgrams() throws Exception{ //
 * mockMvc.perform(get("/generateNgrams?n=1")) // .andExpect(status().isOk()) //
 * ; }
 * 
 * // @Test // public void testGenerateKGrams() throws Exception{ //
 * mockMvc.perform(get("/generateNgrams?k=1")) // .andExpect(status().isOk()) //
 * ; // } //
 * 
 * @Test public void testGetSearchedIndicators() throws Exception{
 * mockMvc.perform(
 * get("/getSearchedIndicators?serachText=Persons with disability-Total"))
 * .andExpect(status().isOk()) ; }
 * 
 * 
 * @Test public void testSuggestedIndicatorFail() throws Exception{
 * 
 * List<DataSearch> data = dataSearchService.suggestIndicators("rajani", 6, 1);
 * 
 * assertThat(data) .isNullOrEmpty(); ;
 * 
 * }
 * 
 * @Test public void testGetWordsFail() throws Exception{
 * 
 * List<KGrams> data = dataSearchService.getWords("");
 * 
 * assertThat(data) .isEmpty()
 * 
 * ;
 * 
 * }
 * 
 * @Test public void testGetRelatedIndicators2() throws Exception{
 * 
 * 
 * List<Integer> relatedIndiactors =
 * indicatorClassificationService.getRelatedIndicators(462);
 * 
 * assertThat(relatedIndiactors).isNotNull() .isNotEmpty() ;
 * 
 * List<DataSearch> data =
 * dataSearchService.getRelatedSearchResult(relatedIndiactors, 6, 1);
 * 
 * assertThat(data).isNotNull() .isNotEmpty() .allMatch(p->(p.getIndicator() !=
 * null)) .allMatch(p->(p.getInid() != null)) .allMatch(p->(p.getTp() != null))
 * .allMatch(p->(p.getDataValue() != null)) ; }
 * 
 * 
 * @Test public void testGetRelatedIndicatorsFail() throws Exception{
 * 
 * 
 * List<Integer> relatedIndiactors =
 * indicatorClassificationService.getRelatedIndicators(462);
 * 
 * assertThat(relatedIndiactors).isNotNull() .isNotEmpty() ;
 * 
 * List<DataSearch> data =
 * dataSearchService.getRelatedSearchResult(relatedIndiactors, 60, 1);
 * 
 * assertThat(data) .isEmpty() ; }
 * 
 * }
 */