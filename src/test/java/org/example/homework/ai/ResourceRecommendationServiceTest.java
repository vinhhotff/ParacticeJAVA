package org.example.homework.ai;

import org.example.homework.dto.response.ResourceRecommendation;
import org.example.homework.entity.Employee;
import org.example.homework.repository.AllocationRepository;
import org.example.homework.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceRecommendationServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AllocationRepository allocationRepository;

    @InjectMocks
    private ResourceRecommendationService service;

    @Test
    void recommend_WithAvailableResources_ShouldReturnSorted() {
        List<Employee> employees = List.of(
            Employee.builder().employeeId(1L).fullName("Dev A").role("Java Developer").build(),
            Employee.builder().employeeId(2L).fullName("Dev B").role("Senior Java Developer").build()
        );
        when(employeeRepository.findByRoleContainingIgnoreCase("Java")).thenReturn(employees);
        when(allocationRepository.sumAllocationByEmployeeId(1L)).thenReturn(40);
        when(allocationRepository.sumAllocationByEmployeeId(2L)).thenReturn(60);

        List<ResourceRecommendation> result = service.recommend("Java", 30);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAvailable()).isGreaterThanOrEqualTo(result.get(1).getAvailable());
    }

    @Test
    void recommend_WithNoMatch_ShouldReturnEmpty() {
        when(employeeRepository.findByRoleContainingIgnoreCase("Python")).thenReturn(List.of());
        assertThat(service.recommend("Python", 50)).isEmpty();
    }
}
