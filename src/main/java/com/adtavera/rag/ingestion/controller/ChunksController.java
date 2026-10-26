package com.adtavera.rag.ingestion.controller;

import java.util.List;

import com.adtavera.rag.ingestion.dto.ChunksResponseDto;
import com.adtavera.rag.ingestion.service.DocumentIngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Step 6: lets you experiment with chunk sizes without touching the vector store.
 */
@RestController
@Tag(name = "3. Chunking", description = "Ver cómo se parte el documento antes de indexarlo")
public class ChunksController {

	private final DocumentIngestionService ingestionService;

	public ChunksController(DocumentIngestionService ingestionService) {
		this.ingestionService = ingestionService;
	}

	@GetMapping("/chunks")
	@Operation(summary = "Previsualizar el documento partido en chunks",
			description = "Parte el documento configurado con el tamaño indicado y devuelve los "
					+ "fragmentos. No guarda nada ni llama a ningún servicio externo: sirve para "
					+ "experimentar cómo cambian los cortes según el tamaño.")
	public ChunksResponseDto chunks(
			@Parameter(description = "Tamaño máximo de cada chunk, en tokens", example = "150")
			@RequestParam(defaultValue = "150") int size) {
		List<String> texts = ingestionService.split(size).stream().map(Document::getText).toList();
		return new ChunksResponseDto(size, texts.size(), texts);
	}
}
