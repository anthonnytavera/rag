package com.adtavera.rag.rag.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta generada por el pipeline RAG")
public record AnswerResponseDto(
		@Schema(description = "Respuesta redactada por el LLM a partir del contexto recuperado") String answer,
		@Schema(description = "Chunks recuperados del documento y entregados al LLM como contexto")
		List<String> sources) {
}
