package org.example.homework.ai;

import org.example.homework.dto.response.ResourceRecommendation;
import org.example.homework.embedding.EmbeddingServiceClient;
import org.example.homework.entity.Employee;
import org.example.homework.repository.AllocationRepository;
import org.example.homework.repository.EmployeeRepository;
import com.pgvector.PGvector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceRecommendationServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AllocationRepository allocationRepository;

    @Mock
    private EmbeddingServiceClient embeddingServiceClient;

    @InjectMocks
    private ResourceRecommendationService service;

    @Test
    void recommend_WithAvailableResources_ShouldReturnSorted() {
        float[] dummyEmbedding = new float[384];
        dummyEmbedding[0] = 1.0f;
        when(embeddingServiceClient.generateEmbedding("Java")).thenReturn(dummyEmbedding);

        List<Employee> employees = List.of(
            Employee.builder().employeeId(1L).fullName("Dev A").role("Java Developer").build(),
            Employee.builder().employeeId(2L).fullName("Dev B").role("Senior Java Developer").build()
        );
        when(employeeRepository.findByRoleSimilarity(any(String.class), eq(20))).thenReturn(employees);
        when(allocationRepository.sumAllocationByEmployeeId(1L)).thenReturn(40);
        when(allocationRepository.sumAllocationByEmployeeId(2L)).thenReturn(60);

        List<ResourceRecommendation> result = service.recommend("Java", 30);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAvailable()).isEqualTo(60);
        assertThat(result.get(1).getAvailable()).isEqualTo(40);
    }

    @Test
    void recommend_WithNoMatch_ShouldReturnEmpty() {
        float[] dummyEmbedding = new float[384];
        when(embeddingServiceClient.generateEmbedding("Python")).thenReturn(dummyEmbedding);
        when(employeeRepository.findByRoleSimilarity(any(String.class), eq(20))).thenReturn(List.of());

        assertThat(service.recommend("Python", 50)).isEmpty();
    }
}
