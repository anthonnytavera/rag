package com.adtavera.rag.rag.controller;

import com.adtavera.rag.rag.dto.AnswerResponseDto;
import com.adtavera.rag.rag.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Step 7: HTTP entry point of the complete RAG pipeline.
 */
@RestController
@Tag(name = "5. RAG", description = "Preguntas respondidas a partir del documento indexado")
public class AskController {

	private final RagService ragService;

	public AskController(RagService ragService) {
		this.ragService = ragService;
	}

	@GetMapping("/ask")
	@Operation(summary = "Hacer una pregunta sobre el documento indexado",
			description = "Recupera los chunks más relevantes, se los entrega a Gemini como contexto y "
					+ "devuelve su respuesta junto con los chunks usados (fuentes). Si el documento no "
					+ "contiene la respuesta, el modelo tiene la instrucción de decirlo.")
	public AnswerResponseDto ask(
			@Parameter(description = "Pregunta sobre el manual de la empresa",
					example = "¿Cuántos días de vacaciones tengo?")
			@RequestParam String question) {
		return ragService.ask(question);
	}
}
