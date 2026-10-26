package com.adtavera.rag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point of the RAG learning project.
 * <p>
 * {@code @ConfigurationPropertiesScan} registers the {@code rag.*} settings class
 * ({@link com.adtavera.rag.config.RagProperties}) as a bean.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class RagApplication {

	public static void main(String[] args) {
		SpringApplication.run(RagApplication.class, args);
	}

}
