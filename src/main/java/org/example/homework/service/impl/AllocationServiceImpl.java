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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AllocationServiceImpl implements AllocationService {

    private final AllocationRepository allocationRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final AllocationValidationOrchestrator validationOrchestrator;

    @Override
    @Transactional(readOnly = true)
    public List<AllocationResponse> getAll() {
        log.info("Get all allocations request");
        return allocationRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AllocationResponse getById(Long id) {
        log.info("Get allocation by id: {}", id);
        Allocation allocation = allocationRepository.findById(id)
            .orElseThrow(() -> new AllocationNotFoundException(id));
        return toResponse(allocation);
    }

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
            .status(org.example.homework.entity.enums.AllocationStatus.PENDING)
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
            .employeeCode(entity.getEmployee().getEmployeeCode())
            .employeeName(entity.getEmployee().getFullName())
            .projectId(entity.getProject().getProjectId())
            .projectCode(entity.getProject().getProjectCode())
            .projectName(entity.getProject().getProjectName())
            .allocationPercent(entity.getAllocationPercent())
            .roleInProject(entity.getRoleInProject())
            .startDate(entity.getStartDate())
            .endDate(entity.getEndDate())
            .status(entity.getStatus() != null ? entity.getStatus().name() : null)
            .build();
    }

    @Override
    @Transactional
    public AllocationResponse activateAllocation(Long id) {
        log.info("[ACTIVATE_ALLOCATION] | id={}", id);
        Allocation allocation = allocationRepository.findById(id)
            .orElseThrow(() -> new AllocationNotFoundException(id));

        if (allocation.getStatus() == org.example.homework.entity.enums.AllocationStatus.ACTIVE) {
            throw new org.example.homework.exception.InvalidWorkflowException("Allocation is already ACTIVE");
        }
        if (allocation.getStatus() == org.example.homework.entity.enums.AllocationStatus.ENDED) {
            throw new org.example.homework.exception.InvalidWorkflowException("Allocation is ENDED and cannot be activated again");
        }

        // Validate capacity using a temporary request structure
        AllocationRequest validationRequest = AllocationRequest.builder()
            .employeeId(allocation.getEmployee().getEmployeeId())
            .projectId(allocation.getProject().getProjectId())
            .allocationPercent(allocation.getAllocationPercent())
            .roleInProject(allocation.getRoleInProject())
            .startDate(allocation.getStartDate())
            .endDate(allocation.getEndDate())
            .build();

        validationOrchestrator.validate(validationRequest, id);

        allocation.setStatus(org.example.homework.entity.enums.AllocationStatus.ACTIVE);
        Allocation saved = allocationRepository.save(allocation);
        log.info("[ACTIVATE_ALLOCATION_SUCCESS] | id={}", id);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AllocationResponse endAllocation(Long id) {
        log.info("[END_ALLOCATION] | id={}", id);
        Allocation allocation = allocationRepository.findById(id)
            .orElseThrow(() -> new AllocationNotFoundException(id));

        allocation.setStatus(org.example.homework.entity.enums.AllocationStatus.ENDED);
        Allocation saved = allocationRepository.save(allocation);
        log.info("[END_ALLOCATION_SUCCESS] | id={}", id);
        return toResponse(saved);
    }
}
