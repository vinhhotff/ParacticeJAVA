package org.example.homework.validator;

import lombok.RequiredArgsConstructor;
import org.example.homework.dto.request.AllocationRequest;
import org.example.homework.entity.Project;
import org.example.homework.entity.enums.ProjectStatus;
import org.example.homework.exception.ProjectCompletedException;
import org.example.homework.exception.ProjectNotFoundException;
import org.example.homework.repository.ProjectRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectStatusValidator implements AllocationValidator {

    private final ProjectRepository projectRepository;

    @Override
    public void validate(AllocationRequest request, Long excludeAllocationId) {
        Project project = projectRepository.findById(request.getProjectId())
            .orElseThrow(() -> new ProjectNotFoundException(request.getProjectId()));

        if (ProjectStatus.COMPLETED == project.getStatus()) {
            throw new ProjectCompletedException(project.getProjectCode());
        }
    }
}
