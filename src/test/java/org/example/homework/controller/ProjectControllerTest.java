package org.example.homework.controller;

import org.example.homework.dto.request.ProjectRequest;
import org.example.homework.dto.response.ProjectResponse;
import org.example.homework.entity.enums.ProjectStatus;
import org.example.homework.service.ProjectService;
import org.example.homework.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @Test
    void createProject_ShouldReturn201() throws Exception {
        ProjectRequest request = ProjectRequest.builder()
            .projectCode("NCG")
            .projectName("NCG Training")
            .customer("Internal")
            .status(ProjectStatus.ACTIVE)
            .build();

        ProjectResponse response = ProjectResponse.builder()
            .id(1L)
            .projectCode("NCG")
            .projectName("NCG Training")
            .customer("Internal")
            .status(ProjectStatus.ACTIVE)
            .build();

        when(projectService.create(any(ProjectRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.projectCode").value("NCG"));
    }

    @Test
    void getProject_ShouldReturn200() throws Exception {
        ProjectResponse response = ProjectResponse.builder()
            .id(1L)
            .projectCode("NCG")
            .projectName("NCG Training")
            .status(ProjectStatus.ACTIVE)
            .build();

        when(projectService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/projects/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.projectCode").value("NCG"));
    }

    @Test
    void getAllProjects_ShouldReturn200() throws Exception {
        ProjectResponse p1 = ProjectResponse.builder().id(1L).projectCode("P1").projectName("A").status(ProjectStatus.ACTIVE).build();
        ProjectResponse p2 = ProjectResponse.builder().id(2L).projectCode("P2").projectName("B").status(ProjectStatus.PLANNING).build();

        when(projectService.findAll()).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/projects"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void updateProject_ShouldReturn200() throws Exception {
        ProjectRequest request = ProjectRequest.builder()
            .projectCode("NCG")
            .projectName("Updated")
            .build();

        ProjectResponse response = ProjectResponse.builder()
            .id(1L)
            .projectCode("NCG")
            .projectName("Updated")
            .build();

        when(projectService.update(eq(1L), any(ProjectRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/projects/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.projectName").value("Updated"));
    }

    @Test
    void deleteProject_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/projects/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    void createProject_WithInvalidBody_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"projectCode\":\"\"}"))
            .andExpect(status().isBadRequest());
    }
}
