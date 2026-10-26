package com.adtavera.rag.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the vector store used by the whole application.
 * <p>
 * A vector store keeps each chunk's text together with its embedding vector and can answer
 * "give me the N chunks most similar to this vector". It is where the RAG's knowledge lives:
 * written once during indexing ({@code add}) and queried on every question
 * ({@code similaritySearch}).
 * <p>
 * {@link SimpleVectorStore} keeps the vectors in memory: nothing to install, but the data is
 * lost on restart (the document is indexed again at startup). Because the rest of the code only
 * depends on the {@link VectorStore} interface, it can later be swapped for a persistent
 * implementation such as pgvector without touching the other classes.
 */
@Configuration
public class VectorStoreConfig {

	@Bean
	public VectorStore vectorStore(EmbeddingModel embeddingModel) {
		return SimpleVectorStore.builder(embeddingModel).build();
	}
}
