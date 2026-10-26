package com.adtavera.rag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

/**
 * Typed settings for the RAG pipeline, bound from the {@code rag.*} keys in
 * {@code application.properties}.
 *
 * @param documentPath location of the document to index (for example {@code classpath:docs/company-handbook.txt})
 * @param promptPath   location of the prompt template sent to the LLM (placeholders: {@code {context}}, {@code {question}})
 * @param chunkSize    maximum size of each chunk, in tokens
 * @param topK         how many of the most similar chunks are retrieved per question
 */
@ConfigurationProperties(prefix = "rag")
public record RagProperties(Resource documentPath, Resource promptPath, int chunkSize, int topK) {
}
