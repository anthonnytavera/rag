package com.adtavera.rag.ingestion.runner;

import com.adtavera.rag.ingestion.service.DocumentIngestionService;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Indexes the document once, right after the application starts. The vector store is in
 * memory, so this has to run on every startup.
 */
@Component
public class IngestionRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(IngestionRunner.class);

	private final DocumentIngestionService ingestionService;

	public IngestionRunner(DocumentIngestionService ingestionService) {
		this.ingestionService = ingestionService;
	}

	@Override
	public void run(@NonNull ApplicationArguments args) {
		int chunks = ingestionService.ingest();
		log.info("Indexed {} chunks into the vector store", chunks);
	}
}
