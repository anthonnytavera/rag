package com.adtavera.rag.ingestion.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "El documento partido en chunks")
public record ChunksResponseDto(
		@Schema(description = "Tamaño máximo de chunk usado, en tokens", example = "150") int chunkSize,
		@Schema(description = "Cantidad de chunks generados", example = "6") int totalChunks,
		@Schema(description = "Texto de cada chunk, en orden") List<String> chunks) {
}
