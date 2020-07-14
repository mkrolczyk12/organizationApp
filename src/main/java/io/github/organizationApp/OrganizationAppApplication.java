package io.github.organizationApp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@EnableAsync
@SpringBootApplication
public class OrganizationAppApplication {
	private static final Logger logger = LoggerFactory.getLogger(OrganizationAppApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(OrganizationAppApplication.class, args);
		logger.info("server started");
		System.out.println("server started");
	}

	@Bean
	Validator validator() {
		return new LocalValidatorFactoryBean();
	}
}
