package com.adtavera.rag.embedding.service;

import java.util.List;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

/**
 * Step 4: turns text into vectors (embeddings) and compares them.
 */
@Service
public class EmbeddingService {

	private final EmbeddingModel embeddingModel;

	public EmbeddingService(EmbeddingModel embeddingModel) {
		this.embeddingModel = embeddingModel;
	}

	/** Converts a text into its embedding vector. */
	public float[] embed(String text) {
		return embeddingModel.embed(text);
	}

	/** Cosine similarity between the embeddings of two texts (1 = same meaning, 0 = unrelated). */
	public double similarity(String textA, String textB) {
		List<float[]> vectors = embeddingModel.embed(List.of(textA, textB));
		return cosine(vectors.get(0), vectors.get(1));
	}

	/**
	 * Cosine similarity: dot(x, y) / (|x| * |y|). It measures the angle between two vectors,
	 * ignoring their length.
	 */
	static double cosine(float[] x, float[] y) {
		double dot = 0;
		double normX = 0;
		double normY = 0;
		for (int i = 0; i < x.length; i++) {
			dot += x[i] * y[i];
			normX += x[i] * x[i];
			normY += y[i] * y[i];
		}
		return dot / (Math.sqrt(normX) * Math.sqrt(normY));
	}
}
