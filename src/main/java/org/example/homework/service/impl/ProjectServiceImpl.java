package org.example.homework.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.homework.dto.request.ProjectRequest;
import org.example.homework.dto.response.ProjectResponse;
import org.example.homework.entity.Project;
import org.example.homework.entity.Allocation;
import org.example.homework.entity.enums.ProjectStatus;
import org.example.homework.exception.DuplicateException;
import org.example.homework.exception.InvalidDateRangeException;
import org.example.homework.exception.ProjectNotFoundException;
import org.example.homework.repository.ProjectRepository;
import org.example.homework.service.ProjectService;
import org.example.homework.embedding.EmbeddingServiceClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final EmbeddingServiceClient embeddingServiceClient;

    @Override
    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        log.info("[CREATE_PROJECT] | code={} | name={}", request.getProjectCode(), request.getProjectName());

        if (projectRepository.existsByProjectCode(request.getProjectCode())) {
            throw new DuplicateException("Project code already exists");
        }

        if (request.getEndDate() != null && request.getStartDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new InvalidDateRangeException("Project end date cannot be before start date");
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

        try {
            String desc = saved.getProjectName() + ". " + (saved.getCustomer() != null ? "Customer: " + saved.getCustomer() + ". " : "") + "Status: " + saved.getStatus() + ". ";
            float[] embedding = embeddingServiceClient.generateEmbedding(desc);
            projectRepository.updateDescriptionEmbedding(saved.getProjectId(), java.util.Arrays.toString(embedding));
        } catch (Exception e) {
            log.error("Failed to generate embedding for new project: {}", e.getMessage());
        }

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

    @Override
    @Transactional(readOnly = true)
    public org.example.homework.dto.response.PageResponse<ProjectResponse> findAll(org.springframework.data.domain.Pageable pageable) {
        log.debug("Listing projects with pagination: {}", pageable);
        org.springframework.data.domain.Page<Project> page = projectRepository.findAll(pageable);
        List<ProjectResponse> content = page.getContent().stream()
            .map(this::toResponse)
            .toList();
        return org.example.homework.dto.response.PageResponse.from(page, content);
    }

    @Override
    @Transactional
    public ProjectResponse update(Long id, ProjectRequest request) {
        log.info("[UPDATE_PROJECT] | id={}", id);
        Project project = projectRepository.findById(id)
            .orElseThrow(() -> new ProjectNotFoundException(id));

        if (!project.getProjectCode().equals(request.getProjectCode()) && projectRepository.existsByProjectCode(request.getProjectCode())) {
            throw new DuplicateException("Project code already exists");
        }

        if (request.getEndDate() != null && request.getStartDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new InvalidDateRangeException("Project end date cannot be before start date");
        }

        project.setProjectCode(request.getProjectCode());
        project.setProjectName(request.getProjectName());
        project.setCustomer(request.getCustomer());
        project.setStatus(request.getStatus() != null ? request.getStatus() : project.getStatus());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());

        Project updated = projectRepository.save(project);

        try {
            String desc = updated.getProjectName() + ". " + (updated.getCustomer() != null ? "Customer: " + updated.getCustomer() + ". " : "") + "Status: " + updated.getStatus() + ". ";
            float[] embedding = embeddingServiceClient.generateEmbedding(desc);
            projectRepository.updateDescriptionEmbedding(updated.getProjectId(), java.util.Arrays.toString(embedding));
        } catch (Exception e) {
            log.error("Failed to update embedding for project: {}", e.getMessage());
        }

        log.info("[UPDATE_PROJECT_SUCCESS] | id={}", updated.getProjectId());
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("[DELETE_PROJECT] | id={}", id);
        Project project = projectRepository.findById(id)
            .orElseThrow(() -> new ProjectNotFoundException(id));
        if (project.getAllocations() != null) {
            for (Allocation allocation : project.getAllocations()) {
                allocation.setStatus(org.example.homework.entity.enums.AllocationStatus.ENDED);
            }
        }
        projectRepository.delete(project);
        log.info("[DELETE_PROJECT_SUCCESS] | id={}", id);
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