package org.example.homework.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.example.homework.service.AllocationService;
import org.example.homework.validator.AllocationValidationOrchestrator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AllocationServiceImpl implements AllocationService {

    private final AllocationRepository allocationRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final AllocationValidationOrchestrator validationOrchestrator;

    @Override
    @Transactional
    public AllocationResponse createAllocation(AllocationRequest request) {
        log.info("[CREATE_ALLOCATION] | employeeId={} | projectId={} | percent={}",
            request.getEmployeeId(), request.getProjectId(), request.getAllocationPercent());

        Employee employee = employeeRepository.findByIdWithLock(request.getEmployeeId())
            .orElseThrow(() -> new EmployeeNotFoundException(request.getEmployeeId()));

        Project project = projectRepository.findById(request.getProjectId())
            .orElseThrow(() -> new ProjectNotFoundException(request.getProjectId()));

        // Business rules validation (e.g. COMPLETED check, SUM check)
        validationOrchestrator.validate(request, null);

        Allocation allocation = Allocation.builder()
            .employee(employee)
            .project(project)
            .allocationPercent(request.getAllocationPercent())
            .roleInProject(request.getRoleInProject())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .build();

        Allocation saved = allocationRepository.save(allocation);
        log.info("[CREATE_ALLOCATION_SUCCESS] | allocId={} | employeeId={} | projectId={}",
            saved.getAllocationId(), employee.getEmployeeId(), project.getProjectId());

        return toResponse(saved);
    }

    @Override
    @Transactional
    public AllocationResponse updateAllocation(Long id, AllocationRequest request) {
        log.info("[UPDATE_ALLOCATION] | allocId={} | employeeId={} | projectId={} | percent={}",
            id, request.getEmployeeId(), request.getProjectId(), request.getAllocationPercent());

        Allocation allocation = allocationRepository.findById(id)
            .orElseThrow(() -> new AllocationNotFoundException(id));

        Employee employee = employeeRepository.findByIdWithLock(request.getEmployeeId())
            .orElseThrow(() -> new EmployeeNotFoundException(request.getEmployeeId()));

        Project project = projectRepository.findById(request.getProjectId())
            .orElseThrow(() -> new ProjectNotFoundException(request.getProjectId()));

        // Business rules validation with exclude ID
        validationOrchestrator.validate(request, id);

        allocation.setEmployee(employee);
        allocation.setProject(project);
        allocation.setAllocationPercent(request.getAllocationPercent());
        allocation.setRoleInProject(request.getRoleInProject());
        allocation.setStartDate(request.getStartDate());
        allocation.setEndDate(request.getEndDate());

        Allocation updated = allocationRepository.save(allocation);
        log.info("[UPDATE_ALLOCATION_SUCCESS] | allocId={}", updated.getAllocationId());

        return toResponse(updated);
    }

    @Override
    @Transactional
    public void removeAllocation(Long id) {
        log.info("[REMOVE_ALLOCATION] | allocId={}", id);
        Allocation allocation = allocationRepository.findById(id)
            .orElseThrow(() -> new AllocationNotFoundException(id));
        allocationRepository.delete(allocation);
        log.info("[REMOVE_ALLOCATION_SUCCESS] | allocId={}", id);
    }

    private AllocationResponse toResponse(Allocation entity) {
        return AllocationResponse.builder()
            .id(entity.getAllocationId())
            .employeeId(entity.getEmployee().getEmployeeId())
            .projectId(entity.getProject().getProjectId())
            .allocationPercent(entity.getAllocationPercent())
            .roleInProject(entity.getRoleInProject())
            .startDate(entity.getStartDate())
            .endDate(entity.getEndDate())
            .build();
    }
}
