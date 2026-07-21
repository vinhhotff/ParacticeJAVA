package org.example.homework.service;

import org.example.homework.dto.request.ProjectRequest;
import org.example.homework.dto.response.ProjectResponse;
import org.example.homework.entity.Project;
import org.example.homework.entity.enums.ProjectStatus;
import org.example.homework.exception.DuplicateException;
import org.example.homework.exception.InvalidDateRangeException;
import org.example.homework.exception.ProjectNotFoundException;
import org.example.homework.repository.ProjectRepository;
import org.example.homework.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private org.example.homework.embedding.EmbeddingServiceClient embeddingServiceClient;

    @InjectMocks
    private ProjectServiceImpl projectService;

    @Test
    void should_CreateProject_When_Valid() {
        ProjectRequest request = ProjectRequest.builder()
            .projectCode("PRJ001")
            .projectName("New Project")
            .customer("Customer A")
            .status(ProjectStatus.ACTIVE)
            .startDate(LocalDate.now())
            .build();

        Project project = Project.builder()
            .projectId(1L)
            .projectCode("PRJ001")
            .projectName("New Project")
            .customer("Customer A")
            .status(ProjectStatus.ACTIVE)
            .startDate(LocalDate.now())
            .build();

        when(projectRepository.existsByProjectCode("PRJ001")).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(embeddingServiceClient.generateEmbedding(anyString())).thenReturn(new float[384]);

        ProjectResponse response = projectService.create(request);

        assertNotNull(response);
        assertEquals("PRJ001", response.getProjectCode());
        assertEquals("New Project", response.getProjectName());
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void should_ThrowDuplicateException_When_CodeExists() {
        ProjectRequest request = ProjectRequest.builder()
            .projectCode("PRJ001")
            .build();

        when(projectRepository.existsByProjectCode("PRJ001")).thenReturn(true);

        assertThrows(DuplicateException.class, () -> projectService.create(request));
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void should_ThrowInvalidDateRange_When_EndDateBeforeStartDate() {
        ProjectRequest request = ProjectRequest.builder()
            .projectCode("PRJ001")
            .projectName("Test")
            .startDate(LocalDate.of(2026, 6, 1))
            .endDate(LocalDate.of(2026, 5, 1))
            .build();

        when(projectRepository.existsByProjectCode("PRJ001")).thenReturn(false);

        assertThrows(InvalidDateRangeException.class, () -> projectService.create(request));
    }

    @Test
    void should_ReturnProject_When_FindById() {
        Project project = Project.builder()
            .projectId(1L)
            .projectCode("PRJ001")
            .projectName("Test")
            .status(ProjectStatus.ACTIVE)
            .build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        ProjectResponse response = projectService.findById(1L);

        assertNotNull(response);
        assertEquals("PRJ001", response.getProjectCode());
    }

    @Test
    void should_ThrowNotFoundException_When_FindByIdNotFound() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class, () -> projectService.findById(99L));
    }

    @Test
    void should_ReturnAllProjects() {
        Project p1 = Project.builder().projectId(1L).projectCode("P1").projectName("A").status(ProjectStatus.ACTIVE).build();
        Project p2 = Project.builder().projectId(2L).projectCode("P2").projectName("B").status(ProjectStatus.PLANNING).build();

        when(projectRepository.findAll()).thenReturn(List.of(p1, p2));

        List<ProjectResponse> result = projectService.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void should_UpdateProject_When_Valid() {
        ProjectRequest request = ProjectRequest.builder()
            .projectCode("PRJ001")
            .projectName("Updated Name")
            .customer("Updated Customer")
            .status(ProjectStatus.ACTIVE)
            .build();

        Project existing = Project.builder()
            .projectId(1L)
            .projectCode("PRJ001")
            .projectName("Old Name")
            .status(ProjectStatus.PLANNING)
            .build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(projectRepository.save(any(Project.class))).thenReturn(existing);
        when(embeddingServiceClient.generateEmbedding(anyString())).thenReturn(new float[384]);

        ProjectResponse response = projectService.update(1L, request);

        assertNotNull(response);
        assertEquals("Updated Name", existing.getProjectName()); // modified in-place
        verify(projectRepository, times(1)).save(existing);
    }

    @Test
    void should_DeleteProject_When_Valid() {
        Project project = Project.builder().projectId(1L).projectCode("P1").projectName("Test").build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        projectService.delete(1L);

        verify(projectRepository, times(1)).delete(project);
    }

    @Test
    void should_ThrowNotFoundException_When_DeleteNotFound() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class, () -> projectService.delete(99L));
    }

    @Test
    void should_FindAllProjects_WithPagination() {
        Project proj = Project.builder()
            .projectId(1L)
            .projectCode("PRJ001")
            .projectName("Project A")
            .status(ProjectStatus.ACTIVE)
            .build();

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 5);
        org.springframework.data.domain.Page<Project> page = new org.springframework.data.domain.PageImpl<>(List.of(proj), pageable, 1);

        when(projectRepository.findAll(pageable)).thenReturn(page);

        org.example.homework.dto.response.PageResponse<ProjectResponse> response = projectService.findAll(pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("PRJ001", response.getContent().get(0).getProjectCode());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
    }
}
