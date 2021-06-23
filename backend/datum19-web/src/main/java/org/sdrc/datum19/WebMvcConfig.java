//package org.sdrc.datum19;
//import java.io.IOException;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.core.io.Resource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.EnableWebMvc;
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
//import org.springframework.web.servlet.resource.PathResourceResolver;
//
//@Component
//@Configuration
//@EnableWebMvc
//public class WebMvcConfig extends WebMvcConfigurerAdapter {
//
//	   @Override
//	   public void addResourceHandlers(ResourceHandlerRegistry registry) {
//
//	     registry.addResourceHandler("/**/*")
//	       .addResourceLocations("classpath:/static/")
//	       .resourceChain(true)
//	       .addResolver(new PathResourceResolver() {
//	           @Override
//	           protected Resource getResource(String resourcePath,
//	               Resource location) throws IOException {
//	               Resource requestedResource = location.createRelative(resourcePath);
//	               return requestedResource.exists() && requestedResource.isReadable() ? requestedResource
//	               : new ClassPathResource("/static/index.html");
//	           }
//	       });
//	   }
//	   
//	   @Override
//	   public void addCorsMappings(CorsRegistry registry) {
//	        registry.addMapping("/**")
//	            .allowedOrigins("http://192.168.1.10:8080")
//	            .allowedOrigins("http://aggregation.sdrc.co.in:8080");
//	    }
//
//}