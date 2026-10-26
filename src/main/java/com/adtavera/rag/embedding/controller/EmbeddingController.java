package com.adtavera.rag.embedding.controller;

import java.util.Arrays;

import com.adtavera.rag.embedding.dto.EmbeddingResponseDto;
import com.adtavera.rag.embedding.dto.SimilarityResponseDto;
import com.adtavera.rag.embedding.service.EmbeddingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Step 4: exposes embeddings so you can see how "meaning" becomes numbers.
 */
@RestController
@Tag(name = "2. Embeddings", description = "Convertir texto en vectores y comparar su significado")
public class EmbeddingController {

	private static final int PREVIEW_SIZE = 10;

	private final EmbeddingService embeddingService;

	public EmbeddingController(EmbeddingService embeddingService) {
		this.embeddingService = embeddingService;
	}

	@GetMapping("/embed")
	@Operation(summary = "Generar el embedding de un texto",
			description = "Devuelve el número de dimensiones y los primeros valores del vector.")
	public EmbeddingResponseDto embed(
			@Parameter(description = "Texto que se convertirá en vector", example = "hola mundo")
			@RequestParam String text) {
		float[] vector = embeddingService.embed(text);
		return new EmbeddingResponseDto(vector.length, Arrays.copyOf(vector, PREVIEW_SIZE));
	}

	@GetMapping("/similarity")
	@Operation(summary = "Comparar el significado de dos textos",
			description = "Genera el embedding de ambos textos y devuelve su similitud coseno. Textos con "
					+ "significado parecido puntúan más alto aunque no compartan palabras.")
	public SimilarityResponseDto similarity(
			@Parameter(description = "Primer texto", example = "perro") @RequestParam String textA,
			@Parameter(description = "Segundo texto", example = "canino") @RequestParam String textB) {
		return new SimilarityResponseDto(textA, textB, embeddingService.similarity(textA, textB));
	}
}
