package com.adtavera.rag.embedding.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Similitud semántica entre dos textos")
public record SimilarityResponseDto(
		@Schema(description = "Primer texto") String textA,
		@Schema(description = "Segundo texto") String textB,
		@Schema(description = "Similitud coseno: cerca de 1 significa mismo significado, cerca de 0 significa sin relación",
				example = "0.82") double similarity) {
}
