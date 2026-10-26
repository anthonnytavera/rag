package com.adtavera.rag.chat.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Step 3: sends a question straight to the LLM, with no document context.
 */
@Service
public class ChatService {

	private final ChatClient chatClient;

	public ChatService(ChatClient.Builder builder) {
		this.chatClient = builder.build();
	}

	public String ask(String question) {
		return chatClient.prompt()
				.user(question)
				.call()
				.content();
	}
}
