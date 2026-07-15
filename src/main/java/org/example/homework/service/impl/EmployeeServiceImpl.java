package org.example.homework.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.homework.dto.request.EmployeeRequest;
import org.example.homework.dto.response.EmployeeResponse;
import org.example.homework.dto.response.WorkloadResponse;
import org.example.homework.entity.Employee;
import org.example.homework.exception.DuplicateException;
import org.example.homework.exception.EmployeeNotFoundException;
import org.example.homework.repository.AllocationRepository;
import org.example.homework.repository.EmployeeRepository;
import org.example.homework.service.EmployeeService;
import org.example.homework.embedding.EmbeddingServiceClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AllocationRepository allocationRepository;
    private final EmbeddingServiceClient embeddingServiceClient;

    @Override
    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        log.info("[CREATE_EMPLOYEE] | code={} | email={}", request.getEmployeeCode(), request.getEmail());
        validateEmployeeUniqueness(request);

        Employee employee = toEntity(request);
        Employee saved = employeeRepository.save(employee);

        try {
            float[] embedding = embeddingServiceClient.generateEmbedding(saved.getRole());
            employeeRepository.updateRoleEmbedding(saved.getEmployeeId(), java.util.Arrays.toString(embedding));
        } catch (Exception e) {
            log.error("Failed to generate embedding for new employee: {}", e.getMessage());
        }

        log.info("[CREATE_EMPLOYEE_SUCCESS] | id={} | code={}", saved.getEmployeeId(), saved.getEmployeeCode());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse findById(Long id) {
        log.debug("Finding employee by id={}", id);
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Employee not found with id={}", id);
                return new EmployeeNotFoundException(id);
            });
        return toResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> findAll() {
        log.debug("Listing all employees");
        return employeeRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        log.info("[UPDATE_EMPLOYEE] | id={} | code={}", id, request.getEmployeeCode());
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));

        if (!employee.getEmployeeCode().equals(request.getEmployeeCode()) && employeeRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new DuplicateException("Employee code already exists");
        }
        if (!employee.getEmail().equals(request.getEmail()) && employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateException("Email already exists");
        }

        employee.setEmployeeCode(request.getEmployeeCode());
        employee.setFullName(request.getFullName());
        employee.setEmail(request.getEmail());
        employee.setRole(request.getRole());
        employee.setDepartment(request.getDepartment());

        Employee updated = employeeRepository.save(employee);

        try {
            float[] embedding = embeddingServiceClient.generateEmbedding(updated.getRole());
            employeeRepository.updateRoleEmbedding(updated.getEmployeeId(), java.util.Arrays.toString(embedding));
        } catch (Exception e) {
            log.error("Failed to update embedding for employee: {}", e.getMessage());
        }

        log.info("[UPDATE_EMPLOYEE_SUCCESS] | id={}", updated.getEmployeeId());
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("[DELETE_EMPLOYEE] | id={}", id);
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));
        employeeRepository.delete(employee);
        log.info("[DELETE_EMPLOYEE_SUCCESS] | id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkloadResponse getWorkload(Long id) {
        log.info("[GET_WORKLOAD] | employeeId={}", id);
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Employee not found for workload check: id={}", id);
                return new EmployeeNotFoundException(id);
            });

        int totalAllocation = allocationRepository.sumAllocationByEmployeeId(id);
        int available = 100 - totalAllocation;

        return WorkloadResponse.builder()
            .employeeId(employee.getEmployeeId())
            .employeeName(employee.getFullName())
            .totalAllocation(totalAllocation)
            .available(available)
            .build();
    }

    private void validateEmployeeUniqueness(EmployeeRequest request) {
        if (employeeRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new DuplicateException("Employee code already exists");
        }
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateException("Email already exists");
        }
    }

    private EmployeeResponse toResponse(Employee entity) {
        return EmployeeResponse.builder()
            .id(entity.getEmployeeId())
            .employeeCode(entity.getEmployeeCode())
            .fullName(entity.getFullName())
            .email(entity.getEmail())
            .role(entity.getRole())
            .department(entity.getDepartment())
            .build();
    }

    private Employee toEntity(EmployeeRequest request) {
        return Employee.builder()
            .employeeCode(request.getEmployeeCode())
            .fullName(request.getFullName())
            .email(request.getEmail())
            .role(request.getRole())
            .department(request.getDepartment())
            .build();
    }
}