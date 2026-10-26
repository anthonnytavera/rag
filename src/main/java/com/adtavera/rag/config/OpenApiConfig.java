package com.adtavera.rag.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadata shown at the top of Swagger UI ({@code /swagger-ui.html}) and in the OpenAPI
 * document ({@code /v3/api-docs}). The texts are in Spanish on purpose: they are user-facing
 * documentation.
 */
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI ragOpenApi() {
		return new OpenAPI().info(new Info()
				.title("API de aprendizaje de RAG")
				.version("0.0.1")
				.description("Endpoints que exponen cada etapa de un RAG: chat, embeddings, "
						+ "chunking, búsqueda por similitud y preguntas sobre un documento."));
	}
}
