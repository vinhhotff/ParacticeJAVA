package org.example.homework.embedding;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.util.List;
import java.util.Random;

@Component
@Slf4j
public class LocalEmbeddingServiceClient implements EmbeddingServiceClient {

    private static final int EMBEDDING_DIMENSION = 384;
    private final Random random = new Random(42);

    @Override
    public float[] generateEmbedding(String text) {
        // Deterministic hash-based embedding for local development
        // In production, replace with actual model inference (e.g., sentence-transformers, OpenAI, etc.)
        return generateDeterministicEmbedding(text);
    }

    @Override
    public List<float[]> generateEmbeddings(List<String> texts) {
        return texts.stream()
            .map(this::generateDeterministicEmbedding)
            .toList();
    }

    /**
     * Generate a deterministic embedding from text using hashing
     * This is a placeholder - in production use a real embedding model
     */
    private float[] generateDeterministicEmbedding(String text) {
        try {
            // Normalize text
            String normalized = text.toLowerCase().trim();

            // Create multiple hash seeds for distribution
            float[] embedding = new float[EMBEDDING_DIMENSION];

            // Use text hash as seed for deterministic random generation
            long seed = hashText(normalized);
            Random textRandom = new Random(seed);

            // Generate pseudo-random values
            for (int i = 0; i < EMBEDDING_DIMENSION; i++) {
                // Box-Muller transform for normal distribution
                double u1 = textRandom.nextDouble();
                double u2 = textRandom.nextDouble();
                double z0 = Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);
                embedding[i] = (float) (z0 * 0.1); // Scale to reasonable range
            }

            // Normalize to unit vector
            return normalize(embedding);

        } catch (Exception e) {
            log.error("Error generating embedding for text: {}", text, e);
            return randomEmbedding();
        }
    }

    private long hashText(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes());
            long result = 0;
            for (int i = 0; i < Math.min(8, hash.length); i++) {
                result = (result << 8) | (hash[i] & 0xFF);
            }
            return result;
        } catch (Exception e) {
            return text.hashCode();
        }
    }

    private float[] normalize(float[] vector) {
        double sum = 0;
        for (float v : vector) {
            sum += v * v;
        }
        double magnitude = Math.sqrt(sum);
        if (magnitude == 0) return vector;

        float[] normalized = new float[vector.length];
        for (int i = 0; i < vector.length; i++) {
            normalized[i] = vector[i] / (float) magnitude;
        }
        return normalized;
    }

    private float[] randomEmbedding() {
        float[] embedding = new float[EMBEDDING_DIMENSION];
        for (int i = 0; i < EMBEDDING_DIMENSION; i++) {
            embedding[i] = random.nextFloat() * 0.01f;
        }
        return normalize(embedding);
    }
}