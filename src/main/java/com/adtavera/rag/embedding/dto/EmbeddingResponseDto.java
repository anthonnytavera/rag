package com.adtavera.rag.embedding.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumen del embedding generado para un texto")
public record EmbeddingResponseDto(
		@Schema(description = "Número total de dimensiones del vector", example = "3072") int dimensions,
		@Schema(description = "Primeros 10 valores del vector (el vector completo es demasiado largo para mostrarlo)")
		float[] firstValues) {
}
