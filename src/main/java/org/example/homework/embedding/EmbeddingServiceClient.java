package org.example.homework.embedding;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
public interface EmbeddingServiceClient {

    /**
     * Generate embedding vector for given text
     * @param text input text to embed
     * @return float array of 384 dimensions (all-MiniLM-L6-v2)
     */
    float[] generateEmbedding(String text);

    /**
     * Generate embeddings for multiple texts (batch)
     * @param texts list of texts to embed
     * @return list of float arrays
     */
    List<float[]> generateEmbeddings(List<String> texts);
}