package org.sdrc.datum19;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;

@SpringBootApplication
@EnableMongoRepositories(basePackages = { "org.sdrc.datum19.repository" })
@PropertySource("classpath:aggregation.properties")
@EnableAutoConfiguration
@EnableResourceServer
public class Datum19Application extends ResourceServerConfigurerAdapter{
	
	@Value(value="${server.authorization.server}")
	private String authServerURI;

	public static void main(String[] args) {
		SpringApplication.run(Datum19Application.class, args);
	}
	
	@Primary
	@Bean
	public RemoteTokenServices tokenService() {
		RemoteTokenServices tokenService = new RemoteTokenServices();
		tokenService.setCheckTokenEndpointUrl(authServerURI + "/oauth/check_token");
		tokenService.setClientId("etathya");
		tokenService.setClientSecret("test@123#");
		return tokenService;
	}
	
	@Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
         resources.resourceId("web-service");
    }
//	@Override
//	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
//		return application.sources(Datum19Application.class);
//	}

//	public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                registry.addMapping("/*").allowedOrigins("http://192.168.1.10:8080");
//            }
//        };
//    }
}
