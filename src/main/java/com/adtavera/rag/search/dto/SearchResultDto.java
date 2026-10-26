package com.adtavera.rag.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Chunk encontrado por la búsqueda de similitud")
public record SearchResultDto(
		@Schema(description = "Texto del chunk") String text,
		@Schema(description = "Similitud con la consulta (más alto es más parecido)", example = "0.78") Double score) {
}
