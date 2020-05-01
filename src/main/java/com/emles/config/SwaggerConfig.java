package com.emles.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Configuration file for Swagger Rest API documentation for this project.
 * 
 * @author darglk
 *
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

	/**
	 * Bean method with configuration of Swagger.
	 * 
	 * @return instance of Docket class.
	 */
	@Bean
	public Docket apiDocket() {
		Docket docket = new Docket(DocumentationType.SWAGGER_2)
				.select()
				.apis(RequestHandlerSelectors.basePackage("com.darglk"))
				.paths(PathSelectors.any())
				.build();
		return docket;
	}
}
