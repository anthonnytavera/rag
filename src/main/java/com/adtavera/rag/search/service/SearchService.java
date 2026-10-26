package com.adtavera.rag.search.service;

import java.util.List;

import com.adtavera.rag.config.RagProperties;
import com.adtavera.rag.search.dto.SearchResultDto;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

/**
 * Step 5: the <b>Retrieval</b> part of RAG. Also reused by the full RAG flow.
 */
@Service
public class SearchService {

	private final VectorStore vectorStore;
	private final RagProperties properties;

	public SearchService(VectorStore vectorStore, RagProperties properties) {
		this.vectorStore = vectorStore;
		this.properties = properties;
	}

	/** Returns the top-K chunks whose embedding is most similar to the embedding of the query. */
	public List<SearchResultDto> search(String query) {
		SearchRequest request = SearchRequest.builder().query(query).topK(properties.topK()).build();
		return vectorStore.similaritySearch(request).stream()
				.map(doc -> new SearchResultDto(doc.getText(), doc.getScore()))
				.toList();
	}
}
