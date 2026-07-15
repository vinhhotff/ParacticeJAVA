package org.example.homework.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.homework.dto.response.ResourceRecommendation;
import org.example.homework.embedding.EmbeddingServiceClient;
import org.example.homework.entity.Employee;
import org.example.homework.repository.AllocationRepository;
import org.example.homework.repository.EmployeeRepository;
import com.pgvector.PGvector;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceRecommendationService {

    private final EmployeeRepository employeeRepository;
    private final AllocationRepository allocationRepository;
    private final EmbeddingServiceClient embeddingServiceClient;

    /**
     * Recommend resources using semantic vector search
     */
    public List<ResourceRecommendation> recommend(String role, int minAvailable) {
        log.info("Recommend resources (semantic): role={}, minAvailable={}", role, minAvailable);

        // Generate embedding for the query role
        float[] queryEmbedding = embeddingServiceClient.generateEmbedding(role);
        String vectorStr = java.util.Arrays.toString(queryEmbedding);

        // Find employees with similar roles using vector similarity
        List<Employee> employees = employeeRepository.findByRoleSimilarity(vectorStr, 20);

        return employees.stream()
            .map(emp -> {
                int total = allocationRepository.sumAllocationByEmployeeId(emp.getEmployeeId());
                int available = 100 - total;
                return ResourceRecommendation.from(emp, available);
            })
            .filter(rec -> rec.getAvailable() >= minAvailable)
            .sorted(Comparator.comparingInt(ResourceRecommendation::getAvailable).reversed())
            .toList();
    }

    /**
     * Legacy method for exact role matching (fallback)
     */
    public List<ResourceRecommendation> recommendExact(String role, int minAvailable) {
        log.info("Recommend resources (exact): role={}, minAvailable={}", role, minAvailable);

        List<Employee> employees = employeeRepository.findByRoleContainingIgnoreCase(role);

        return employees.stream()
            .map(emp -> {
                int total = allocationRepository.sumAllocationByEmployeeId(emp.getEmployeeId());
                int available = 100 - total;
                return ResourceRecommendation.from(emp, available);
            })
            .filter(rec -> rec.getAvailable() >= minAvailable)
            .sorted(Comparator.comparingInt(ResourceRecommendation::getAvailable).reversed())
            .toList();
    }
}