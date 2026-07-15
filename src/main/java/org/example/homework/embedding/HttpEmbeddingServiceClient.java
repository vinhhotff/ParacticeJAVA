package org.example.homework.embedding;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.security.MessageDigest;
import java.util.*;

@Component
@Primary
@Slf4j
public class HttpEmbeddingServiceClient implements EmbeddingServiceClient {

    @Value("${embedding.service.url:http://localhost:5000}")
    private String serviceUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final int EMBEDDING_DIMENSION = 384;
    private final Random random = new Random(42);

    @Override
    public float[] generateEmbedding(String text) {
        try {
            String url = serviceUrl + "/embed/single";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("text", text);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);

            if (response != null && response.containsKey("embedding")) {
                List<Double> list = (List<Double>) response.get("embedding");
                float[] result = new float[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    result[i] = list.get(i).floatValue();
                }
                log.info("Successfully fetched single embedding from AI service");
                return result;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch embedding from Python service ({}), falling back to deterministic local embedding: {}", serviceUrl, e.getMessage());
        }

        return generateDeterministicEmbedding(text);
    }

    @Override
    public List<float[]> generateEmbeddings(List<String> texts) {
        try {
            String url = serviceUrl + "/embed";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, List<String>> requestBody = new HashMap<>();
            requestBody.put("texts", texts);

            HttpEntity<Map<String, List<String>>> entity = new HttpEntity<>(requestBody, headers);
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);

            if (response != null && response.containsKey("embeddings")) {
                List<List<Double>> lists = (List<List<Double>>) response.get("embeddings");
                List<float[]> result = new ArrayList<>();
                for (List<Double> list : lists) {
                    float[] arr = new float[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        arr[i] = list.get(i).floatValue();
                    }
                    result.add(arr);
                }
                log.info("Successfully fetched batch embeddings from AI service");
                return result;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch batch embeddings from Python service ({}), falling back to deterministic local embedding: {}", serviceUrl, e.getMessage());
        }

        return texts.stream()
            .map(this::generateDeterministicEmbedding)
            .toList();
    }

    private float[] generateDeterministicEmbedding(String text) {
        try {
            String normalized = text.toLowerCase().trim();
            float[] embedding = new float[EMBEDDING_DIMENSION];
            long seed = hashText(normalized);
            Random textRandom = new Random(seed);

            for (int i = 0; i < EMBEDDING_DIMENSION; i++) {
                double u1 = textRandom.nextDouble();
                double u2 = textRandom.nextDouble();
                double z0 = Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);
                embedding[i] = (float) (z0 * 0.1);
            }
            return normalize(embedding);
        } catch (Exception e) {
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
