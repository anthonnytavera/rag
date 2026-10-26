package com.adtavera.rag.ingestion.service;

import java.util.List;

import com.adtavera.rag.config.RagProperties;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

/**
 * Step 6: the <b>indexing</b> half of RAG.
 * <ol>
 *   <li>Read the document from disk.</li>
 *   <li>Split it into chunks.</li>
 *   <li>Embed every chunk and store it in the vector store.</li>
 * </ol>
 */
@Service
public class DocumentIngestionService {

	private static final int MIN_CHUNK_SIZE_CHARS = 100;

	private final RagProperties properties;
	private final VectorStore vectorStore;

	public DocumentIngestionService(RagProperties properties, VectorStore vectorStore) {
		this.properties = properties;
		this.vectorStore = vectorStore;
	}

	/**
	 * Reads the configured document and splits it into chunks of at most {@code chunkSize}
	 * tokens. Does not call any external service, so it is free and fast.
	 */
	public List<Document> split(int chunkSize) {
		List<Document> documents = new TextReader(properties.documentPath()).get();
		TokenTextSplitter splitter = TokenTextSplitter.builder()
				.withChunkSize(chunkSize)
				.withMinChunkSizeChars(MIN_CHUNK_SIZE_CHARS)
				.build();
		return splitter.split(documents);
	}

	/**
	 * Full indexing: split with the configured chunk size, then store the chunks. Adding to the
	 * vector store calls the embedding model once per batch of chunks.
	 *
	 * @return number of chunks indexed
	 */
	public int ingest() {
		List<Document> chunks = split(properties.chunkSize());
		vectorStore.add(chunks);
		return chunks.size();
	}
}
