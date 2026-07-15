package org.example.homework.service;

import org.example.homework.dto.request.EmployeeRequest;
import org.example.homework.dto.response.EmployeeResponse;
import org.example.homework.dto.response.WorkloadResponse;
import org.example.homework.entity.Employee;
import org.example.homework.exception.DuplicateException;
import org.example.homework.repository.AllocationRepository;
import org.example.homework.repository.EmployeeRepository;
import org.example.homework.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AllocationRepository allocationRepository;

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
}
