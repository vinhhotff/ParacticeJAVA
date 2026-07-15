package org.example.homework.validator;

import org.example.homework.dto.request.AllocationRequest;
import org.example.homework.entity.Project;
import org.example.homework.entity.enums.ProjectStatus;
import org.example.homework.exception.ProjectCompletedException;
import org.example.homework.exception.ProjectNotFoundException;
import org.example.homework.repository.ProjectRepository;
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
class ProjectStatusValidatorTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectStatusValidator projectStatusValidator;

    @Test
    void should_Succeed_When_ProjectIsActive() {
        AllocationRequest request = AllocationRequest.builder()
            .projectId(1L)
            .build();
        Project project = Project.builder()
            .projectId(1L)
            .status(ProjectStatus.ACTIVE)
            .projectCode("NCG")
            .build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertDoesNotThrow(() -> projectStatusValidator.validate(request, null));
    }

    @Test
    void should_ThrowException_When_ProjectIsCompleted() {
        AllocationRequest request = AllocationRequest.builder()
            .projectId(1L)
            .build();
        Project project = Project.builder()
            .projectId(1L)
            .status(ProjectStatus.COMPLETED)
            .projectCode("NCG")
            .build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThrows(ProjectCompletedException.class, () -> projectStatusValidator.validate(request, null));
    }

    @Test
    void should_ThrowNotFoundException_When_ProjectDoesNotExist() {
        AllocationRequest request = AllocationRequest.builder()
            .projectId(99L)
            .build();

        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class, () -> projectStatusValidator.validate(request, null));
    }
}
