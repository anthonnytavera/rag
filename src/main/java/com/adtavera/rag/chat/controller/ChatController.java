package com.adtavera.rag.chat.controller;

import com.adtavera.rag.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Step 3: talks to the LLM directly, <b>without</b> any document context.
 * <p>
 * Useful as a baseline: ask something about the company handbook here and the model will
 * not know it (or will make it up). Compare with {@code /ask}, which uses RAG.
 */
@RestController
@Tag(name = "1. Chat", description = "Llamada directa al LLM, sin RAG")
public class ChatController {

	private final ChatService chatService;

	public ChatController(ChatService chatService) {
		this.chatService = chatService;
	}

	@GetMapping("/chat")
	@Operation(summary = "Preguntar directamente al LLM",
			description = "Envía la pregunta a Gemini tal cual. El modelo solo conoce lo que aprendió "
					+ "durante su entrenamiento, por eso no puede responder sobre documentos privados.")
	public String chat(
			@Parameter(description = "Cualquier pregunta", example = "¿Qué es un RAG?")
			@RequestParam String question) {
		return chatService.ask(question);
	}
}
