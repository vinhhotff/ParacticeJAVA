package org.example.homework.service;

import org.example.homework.dto.request.AllocationRequest;
import org.example.homework.dto.response.AllocationResponse;
import org.example.homework.entity.Allocation;
import org.example.homework.entity.Employee;
import org.example.homework.entity.Project;
import org.example.homework.exception.EmployeeNotFoundException;
import org.example.homework.exception.ProjectNotFoundException;
import org.example.homework.repository.AllocationRepository;
import org.example.homework.repository.EmployeeRepository;
import org.example.homework.repository.ProjectRepository;
import org.example.homework.service.impl.AllocationServiceImpl;
import org.example.homework.validator.AllocationValidationOrchestrator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AllocationServiceTest {

    @Mock
    private AllocationRepository allocationRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private AllocationValidationOrchestrator validationOrchestrator;

    @InjectMocks
    private AllocationServiceImpl allocationService;

    @Test
    void should_CreateAllocation_When_Valid() {
        AllocationRequest request = AllocationRequest.builder()
            .employeeId(1L)
            .projectId(2L)
            .allocationPercent(50)
            .roleInProject("Dev")
            .startDate(LocalDate.now())
            .build();

        Employee employee = Employee.builder().employeeId(1L).employeeCode("E1").build();
        Project project = Project.builder().projectId(2L).projectCode("P2").build();
        Allocation savedAllocation = Allocation.builder()
            .allocationId(10L)
            .employee(employee)
            .project(project)
            .allocationPercent(50)
            .roleInProject("Dev")
            .startDate(LocalDate.now())
            .build();

        when(employeeRepository.findByIdWithLock(1L)).thenReturn(Optional.of(employee));
        when(projectRepository.findById(2L)).thenReturn(Optional.of(project));
        when(allocationRepository.save(any(Allocation.class))).thenReturn(savedAllocation);

        AllocationResponse response = allocationService.createAllocation(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        verify(validationOrchestrator, times(1)).validate(request, null);
        verify(allocationRepository, times(1)).save(any(Allocation.class));
    }

    @Test
    void should_ThrowEmployeeNotFound_When_EmployeeDoesNotExist() {
        AllocationRequest request = AllocationRequest.builder()
            .employeeId(99L)
            .build();

        when(employeeRepository.findByIdWithLock(99L)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> allocationService.createAllocation(request));
    }

    @Test
    void should_ThrowProjectNotFound_When_ProjectDoesNotExist() {
        AllocationRequest request = AllocationRequest.builder()
            .employeeId(1L)
            .projectId(99L)
            .build();

        when(employeeRepository.findByIdWithLock(1L)).thenReturn(Optional.of(new Employee()));
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class, () -> allocationService.createAllocation(request));
    }
}
