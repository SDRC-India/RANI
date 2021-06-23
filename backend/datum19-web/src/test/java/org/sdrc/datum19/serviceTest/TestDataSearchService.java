package org.sdrc.datum19.serviceTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.assertj.core.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.sdrc.datum19.Datum19ApplicationTests;
import org.sdrc.datum19.document.DataSearch;
import org.sdrc.datum19.document.KGrams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.sdrc.datum19.service.DataSearchService;

public class TestDataSearchService extends Datum19ApplicationTests{

	@Autowired
	private WebApplicationContext webApplicationContext;
	private MockMvc mockMvc;
	
	@Autowired
	DataSearchService dataSearchService;
	
	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();	
	}
	
	@Test
	public void testSuggestIndicators() throws Exception{
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
	public void testGetRelatedSearchResult() throws Exception{
		List<Integer> d = new ArrayList<Integer>();
		d.add(462);
		List<DataSearch> data = dataSearchService.getRelatedSearchResult(d,6,1);
		
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
	public void testGenerateKGramss() throws Exception{
		
//		Set<String> data = dataSearchService.generateKGrams(1);
//		
//		assertThat(data).isNotNull()
//		.isNotEmpty()
//		;
	}
	
	@Test 
	public void testgenerateNgrams() throws Exception{
		
//		dataSearchService.generateNgrams(1);
	}
	
	
	@Test
	public void testSuggestIndicatorsFail() throws Exception{
		List<DataSearch> data = dataSearchService.suggestIndicators("rajani", 6, 1);
		
		assertThat(data).isNotNull()
		.isEmpty()
		
		;
	}
	
	@Test
	public void testGetRelatedSearchResultFail() throws Exception{
		List<Integer> d = new ArrayList<Integer>();
		d.add(555);
		List<DataSearch> data = dataSearchService.getRelatedSearchResult(d,6,1);
		
		assertThat(data).isNotNull()
		.isEmpty()
		
		;
	}
	
	
	@Test
	public void testSuggestIndicators2() throws Exception{
		List<DataSearch> data = dataSearchService.suggestIndicators("subrata", 6, 1);
		
		assertThat(data).isNotNull()
		.isNotEmpty()
		.allMatch(p->(p.getIndicator() != null))
		.allMatch(p->(p.getDatumId() != null))
		.allMatch(p->(p.getDataValue() != null))
		.allMatch(p->(p.getTp() != null))
		.allMatch(p->(p.getInid() != null))
		.allSatisfy(p->{
			
			assertThat(p.getIndicator()).isNotNull();
			assertThat(p.getDatumId()).isNotNull();
			assertThat(p.getDataValue()).isNotNull();
			assertThat(p.getTp()).isNotNull();
			assertThat(p.getInid()).isNotNull();
			})
		;
	}
	
	@Test
	public void testGetRelatedSearchResult2() throws Exception{
		List<Integer> d = new ArrayList<Integer>();
		d.add(462);
		List<DataSearch> data = dataSearchService.getRelatedSearchResult(d,6,1);
		
		assertThat(data).isNotNull()
		.isNotEmpty()
		.allMatch(p->(p.getIndicator() != null))
		.allMatch(p->(p.getDatumId() != null))
		.allMatch(p->(p.getDataValue() != null))
		.allMatch(p->(p.getTp() != null))
		.allMatch(p->(p.getInid() != null))
//		.allSatisfy(p->{
//			assertThat(p.getTags()).isEmpty();
//			assertThat(p.getIndicator()).isNotNull();
//			assertThat(p.getDatumId()).isNotNull();
//			assertThat(p.getDataValue()).isNotNull();
//			assertThat(p.getTp()).isNotNull();
//			assertThat(p.getInid()).isNotNull();
//			})
		;
	}
	
	@Test
	public void testGetWords2() throws Exception{
		
		List<KGrams> data = dataSearchService.getWords("lat");
		
		assertThat(data).isNotNull()
		.isNotEmpty()
		.allMatch(p->(p.getWord() != null))
		.allMatch(p->(p.getGram() != null))
		.allMatch(p->(p.getKgram() != null))
		.allMatch(p->(p.getWeight() != null))
		.allSatisfy(p -> {
			assertThat(p.getWord()).isNotNull();
			assertThat(p.getGram()).isNotNull();
			assertThat(p.getKgram()).isNotNull();
			assertThat(p.getWeight()).isNotNull();
		})
		;
	}
}
