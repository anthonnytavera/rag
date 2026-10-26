package com.adtavera.rag.rag.service;

import java.util.List;

import com.adtavera.rag.config.RagProperties;
import com.adtavera.rag.rag.dto.AnswerResponseDto;
import com.adtavera.rag.search.dto.SearchResultDto;
import com.adtavera.rag.search.service.SearchService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Step 7: the complete RAG flow, written by hand so each stage is visible.
 * <ol>
 *   <li><b>Retrieval</b>: find the chunks most similar to the question.</li>
 *   <li><b>Augmentation</b>: paste those chunks into the prompt template as context.</li>
 *   <li><b>Generation</b>: the LLM answers using that context.</li>
 * </ol>
 * The prompt template lives in {@code resources/prompts/rag-prompt.txt}, so it can be edited
 * without touching Java. Spring AI also offers {@code QuestionAnswerAdvisor}, which does all of
 * this in one line.
 */
@Service
public class RagService {

	private static final String CHUNK_SEPARATOR = "\n---\n";

	private final SearchService searchService;
	private final ChatClient chatClient;
	private final RagProperties properties;

	public RagService(SearchService searchService, ChatClient.Builder chatClientBuilder, RagProperties properties) {
		this.searchService = searchService;
		this.chatClient = chatClientBuilder.build();
		this.properties = properties;
	}

	public AnswerResponseDto ask(String question) {
		// Retrieval
		List<String> sources = searchService.search(question).stream().map(SearchResultDto::text).toList();

		// Augmentation + Generation: the template placeholders are filled with the context and the question
		String answer = chatClient.prompt()
				.user(user -> user.text(properties.promptPath())
						.param("context", String.join(CHUNK_SEPARATOR, sources))
						.param("question", question))
				.call()
				.content();

		return new AnswerResponseDto(answer, sources);
	}
}
