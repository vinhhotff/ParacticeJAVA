package org.example.homework.service;

import org.example.homework.dto.request.EmployeeRequest;
import org.example.homework.dto.response.EmployeeResponse;
import org.example.homework.dto.response.WorkloadResponse;
import org.example.homework.entity.Employee;
import org.example.homework.exception.DuplicateException;
import org.example.homework.exception.EmployeeNotFoundException;
import org.example.homework.repository.AllocationRepository;
import org.example.homework.repository.EmployeeRepository;
import org.example.homework.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AllocationRepository allocationRepository;

    @Mock
    private org.example.homework.embedding.EmbeddingServiceClient embeddingServiceClient;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @Test
    void should_CreateEmployee_When_Valid() {
        EmployeeRequest request = EmployeeRequest.builder()
            .employeeCode("EMP001")
            .fullName("John Doe")
            .email("john@doe.com")
            .role("Dev")
            .department("FSOFT")
            .build();

        Employee employee = Employee.builder()
            .employeeId(1L)
            .employeeCode("EMP001")
            .fullName("John Doe")
            .email("john@doe.com")
            .role("Dev")
            .department("FSOFT")
            .build();

        when(employeeRepository.existsByEmployeeCode("EMP001")).thenReturn(false);
        when(employeeRepository.existsByEmail("john@doe.com")).thenReturn(false);
        when(embeddingServiceClient.generateEmbedding("Dev")).thenReturn(new float[384]);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        EmployeeResponse response = employeeService.create(request);

        assertNotNull(response);
        assertEquals("EMP001", response.getEmployeeCode());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void should_ThrowDuplicateException_When_CodeExists() {
        EmployeeRequest request = EmployeeRequest.builder()
            .employeeCode("EMP001")
            .email("john@doe.com")
            .build();

        when(employeeRepository.existsByEmployeeCode("EMP001")).thenReturn(true);

        assertThrows(DuplicateException.class, () -> employeeService.create(request));
    }

    @Test
    void should_ReturnWorkloadCorrectly() {
        Employee employee = Employee.builder()
            .employeeId(1L)
            .fullName("John Doe")
            .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(allocationRepository.sumAllocationByEmployeeId(1L)).thenReturn(75);

        WorkloadResponse workload = employeeService.getWorkload(1L);

        assertEquals(75, workload.getTotalAllocation());
        assertEquals(25, workload.getAvailable());
    }

    @Test
    void should_ThrowNotFound_When_FindByIdNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.findById(99L));
    }

    @Test
    void should_UpdateEmployee_When_Valid() {
        EmployeeRequest request = EmployeeRequest.builder()
            .employeeCode("EMP001")
            .fullName("Updated Name")
            .email("updated@email.com")
            .role("Senior Lead")
            .department("FSOFT-Q2")
            .build();

        Employee existing = Employee.builder()
            .employeeId(1L)
            .employeeCode("EMP001")
            .fullName("Old Name")
            .email("old@email.com")
            .role("Dev")
            .department("FSOFT-Q1")
            .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.save(any(Employee.class))).thenReturn(existing);
        when(embeddingServiceClient.generateEmbedding("Senior Lead")).thenReturn(new float[384]);

        EmployeeResponse response = employeeService.update(1L, request);

        assertNotNull(response);
        assertEquals("Updated Name", existing.getFullName());
        verify(employeeRepository, times(1)).save(existing);
    }

    @Test
    void should_ThrowDuplicateException_When_UpdateWithExistingCode() {
        EmployeeRequest request = EmployeeRequest.builder()
            .employeeCode("EMP002")
            .email("old@email.com")
            .build();

        Employee existing = Employee.builder()
            .employeeId(1L)
            .employeeCode("EMP001")
            .email("old@email.com")
            .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.existsByEmployeeCode("EMP002")).thenReturn(true);

        assertThrows(DuplicateException.class, () -> employeeService.update(1L, request));
    }

    @Test
    void should_DeleteEmployee_When_Valid() {
        Employee employee = Employee.builder()
            .employeeId(1L)
            .employeeCode("EMP001")
            .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        employeeService.delete(1L);

        verify(employeeRepository, times(1)).delete(employee);
    }

    @Test
    void should_ThrowNotFound_When_DeleteNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.delete(99L));
    }

    @Test
    void should_ThrowNotFound_When_WorkloadNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getWorkload(99L));
    }

    @Test
    void should_FindAllEmployees_WithPagination() {
        Employee emp = Employee.builder()
            .employeeId(1L)
            .employeeCode("EMP001")
            .fullName("John Doe")
            .email("john@doe.com")
            .role("Dev")
            .department("ENG")
            .build();

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 5);
        org.springframework.data.domain.Page<Employee> page = new org.springframework.data.domain.PageImpl<>(List.of(emp), pageable, 1);

        when(employeeRepository.findAll(pageable)).thenReturn(page);

        org.example.homework.dto.response.PageResponse<EmployeeResponse> response = employeeService.findAll(pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("EMP001", response.getContent().get(0).getEmployeeCode());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
    }
}
