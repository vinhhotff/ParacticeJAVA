package org.example.homework.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.homework.dto.request.ProjectRequest;
import org.example.homework.dto.response.ProjectResponse;
import org.example.homework.entity.Project;
import org.example.homework.entity.enums.ProjectStatus;
import org.example.homework.exception.DuplicateException;
import org.example.homework.exception.ProjectNotFoundException;
import org.example.homework.repository.ProjectRepository;
import org.example.homework.service.ProjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        log.info("[CREATE_PROJECT] | code={} | name={}", request.getProjectCode(), request.getProjectName());
        
        if (projectRepository.existsByProjectCode(request.getProjectCode())) {
            throw new DuplicateException("Project code already exists");
        }

        if (request.getEndDate() != null && request.getStartDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new org.example.homework.exception.InvalidDateRangeException("Project end date cannot be before start date");
        }

        ProjectStatus status = request.getStatus() != null ? request.getStatus() : ProjectStatus.PLANNING;
        Project project = Project.builder()
            .projectCode(request.getProjectCode())
            .projectName(request.getProjectName())
            .customer(request.getCustomer())
            .status(status)
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .build();

        Project saved = projectRepository.save(project);
        log.info("[CREATE_PROJECT_SUCCESS] | id={} | code={}", saved.getProjectId(), saved.getProjectCode());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse findById(Long id) {
        log.debug("Finding project by id={}", id);
        Project project = projectRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Project not found with id={}", id);
                return new ProjectNotFoundException(id);
            });
        return toResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> findAll() {
        log.debug("Listing all projects");
        return projectRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    private ProjectResponse toResponse(Project entity) {
        return ProjectResponse.builder()
            .id(entity.getProjectId())
            .projectCode(entity.getProjectCode())
            .projectName(entity.getProjectName())
            .customer(entity.getCustomer())
            .status(entity.getStatus())
            .startDate(entity.getStartDate())
            .endDate(entity.getEndDate())
            .build();
    }
}
