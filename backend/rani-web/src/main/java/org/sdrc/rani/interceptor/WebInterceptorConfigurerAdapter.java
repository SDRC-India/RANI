package org.sdrc.rani.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
@Component
public class WebInterceptorConfigurerAdapter extends WebMvcConfigurerAdapter {

	@Autowired
	private RequestInterceptor requestInterceptor;
	
	@Autowired
	private ConfigurableEnvironment configurableEnvironment;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		String pathByPath = configurableEnvironment.getProperty("security.allow.path");
		
		registry.addInterceptor(requestInterceptor).excludePathPatterns(pathByPath.split(","));
	}
}
