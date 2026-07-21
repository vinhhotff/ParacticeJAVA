package org.example.homework.service;

import org.example.homework.dto.request.AllocationRequest;
import org.example.homework.dto.response.AllocationResponse;
import org.example.homework.entity.Allocation;
import org.example.homework.entity.Employee;
import org.example.homework.entity.Project;
import org.example.homework.exception.AllocationNotFoundException;
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

    @Test
    void should_ActivateAllocation_When_Pending() {
        Employee employee = Employee.builder().employeeId(1L).employeeCode("E1").build();
        Project project = Project.builder().projectId(2L).projectCode("P2").build();
        Allocation allocation = Allocation.builder()
            .allocationId(10L)
            .employee(employee)
            .project(project)
            .allocationPercent(50)
            .status(org.example.homework.entity.enums.AllocationStatus.PENDING)
            .build();

        when(allocationRepository.findById(10L)).thenReturn(Optional.of(allocation));
        when(allocationRepository.save(any(Allocation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AllocationResponse response = allocationService.activateAllocation(10L);

        assertNotNull(response);
        assertEquals("ACTIVE", response.getStatus());
        verify(validationOrchestrator, times(1)).validate(any(AllocationRequest.class), eq(10L));
    }

    @Test
    void should_ThrowWorkflowException_When_AlreadyActiveOrEnded() {
        Allocation active = Allocation.builder()
            .allocationId(10L)
            .status(org.example.homework.entity.enums.AllocationStatus.ACTIVE)
            .build();
        Allocation ended = Allocation.builder()
            .allocationId(11L)
            .status(org.example.homework.entity.enums.AllocationStatus.ENDED)
            .build();

        when(allocationRepository.findById(10L)).thenReturn(Optional.of(active));
        when(allocationRepository.findById(11L)).thenReturn(Optional.of(ended));

        assertThrows(org.example.homework.exception.InvalidWorkflowException.class, () -> allocationService.activateAllocation(10L));
        assertThrows(org.example.homework.exception.InvalidWorkflowException.class, () -> allocationService.activateAllocation(11L));
    }

    @Test
    void should_EndAllocation_When_Called() {
        Allocation allocation = Allocation.builder()
            .allocationId(10L)
            .employee(Employee.builder().build())
            .project(Project.builder().build())
            .status(org.example.homework.entity.enums.AllocationStatus.ACTIVE)
            .build();

        when(allocationRepository.findById(10L)).thenReturn(Optional.of(allocation));
        when(allocationRepository.save(any(Allocation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AllocationResponse response = allocationService.endAllocation(10L);

        assertNotNull(response);
        assertEquals("ENDED", response.getStatus());
    }

    @Test
    void should_UpdateAllocation_When_Valid() {
        AllocationRequest request = AllocationRequest.builder()
            .employeeId(1L)
            .projectId(2L)
            .allocationPercent(80)
            .roleInProject("Lead Dev")
            .startDate(LocalDate.now())
            .build();

        Employee employee = Employee.builder().employeeId(1L).employeeCode("E1").fullName("Emp 1").build();
        Project project = Project.builder().projectId(2L).projectCode("P2").projectName("Proj 2").build();
        Allocation existing = Allocation.builder()
            .allocationId(10L)
            .employee(employee)
            .project(project)
            .allocationPercent(50)
            .roleInProject("Dev")
            .startDate(LocalDate.now().minusDays(10))
            .status(org.example.homework.entity.enums.AllocationStatus.ACTIVE)
            .build();

        when(allocationRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(employeeRepository.findByIdWithLock(1L)).thenReturn(Optional.of(employee));
        when(projectRepository.findById(2L)).thenReturn(Optional.of(project));
        when(allocationRepository.save(any(Allocation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AllocationResponse response = allocationService.updateAllocation(10L, request);

        assertNotNull(response);
        assertEquals(80, response.getAllocationPercent());
        assertEquals("Lead Dev", response.getRoleInProject());
        verify(validationOrchestrator, times(1)).validate(request, 10L);
    }

    @Test
    void should_RemoveAllocation_When_Valid() {
        Allocation allocation = Allocation.builder()
            .allocationId(10L)
            .employee(Employee.builder().build())
            .project(Project.builder().build())
            .build();

        when(allocationRepository.findById(10L)).thenReturn(Optional.of(allocation));

        allocationService.removeAllocation(10L);

        verify(allocationRepository, times(1)).delete(allocation);
    }

    @Test
    void should_ThrowAllocationNotFound_When_RemoveNonExistent() {
        when(allocationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AllocationNotFoundException.class, () -> allocationService.removeAllocation(99L));
    }
}
