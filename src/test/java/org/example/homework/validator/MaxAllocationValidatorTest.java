package org.example.homework.validator;

import org.example.homework.config.AllocationProperties;
import org.example.homework.dto.request.AllocationRequest;
import org.example.homework.entity.Allocation;
import org.example.homework.exception.AllocationExceededException;
import org.example.homework.repository.AllocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaxAllocationValidatorTest {

    @Mock
    private AllocationRepository allocationRepository;

    @Mock
    private AllocationProperties allocationProperties;

    @InjectMocks
    private MaxAllocationValidator maxAllocationValidator;

    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();
        lenient().when(allocationProperties.getMaxPercent()).thenReturn(100);
    }

    @Test
    void should_Succeed_When_TotalAllocationUnderLimit() {
        AllocationRequest request = AllocationRequest.builder()
            .employeeId(1L)
            .projectId(1L)
            .allocationPercent(40)
            .roleInProject("Dev")
            .startDate(today)
            .build();

        Allocation existing = Allocation.builder()
            .allocationPercent(50)
            .startDate(today.minusDays(5))
            .endDate(today.plusDays(5))
            .build();

        when(allocationRepository.findOverlappingAllocations(1L, today, null, null))
            .thenReturn(List.of(existing));

        assertDoesNotThrow(() -> maxAllocationValidator.validate(request, null));
        verify(allocationRepository, times(1)).findOverlappingAllocations(1L, today, null, null);
    }

    @Test
    void should_ThrowException_When_TotalAllocationExceedsLimit() {
        AllocationRequest request = AllocationRequest.builder()
            .employeeId(1L)
            .projectId(1L)
            .allocationPercent(60)
            .roleInProject("Dev")
            .startDate(today)
            .build();

        Allocation existing = Allocation.builder()
            .allocationPercent(50)
            .startDate(today.minusDays(5))
            .endDate(today.plusDays(5))
            .build();

        when(allocationRepository.findOverlappingAllocations(1L, today, null, null))
            .thenReturn(List.of(existing));

        assertThrows(AllocationExceededException.class, () -> maxAllocationValidator.validate(request, null));
        verify(allocationRepository, times(1)).findOverlappingAllocations(1L, today, null, null);
    }

    @Test
    void should_Succeed_When_UpdatingAndExcludingCurrentAllocation() {
        AllocationRequest request = AllocationRequest.builder()
            .employeeId(1L)
            .projectId(1L)
            .allocationPercent(60)
            .roleInProject("Dev")
            .startDate(today)
            .build();

        Allocation existing = Allocation.builder()
            .allocationPercent(40)
            .startDate(today.minusDays(5))
            .endDate(today.plusDays(5))
            .build();

        when(allocationRepository.findOverlappingAllocations(1L, today, null, 10L))
            .thenReturn(List.of(existing));

        assertDoesNotThrow(() -> maxAllocationValidator.validate(request, 10L));
        verify(allocationRepository, times(1)).findOverlappingAllocations(1L, today, null, 10L);
    }
}
