package org.example.homework.controller;

import org.example.homework.dto.request.AllocationRequest;
import org.example.homework.dto.response.AllocationResponse;
import org.example.homework.service.AllocationService;
import org.example.homework.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AllocationController.class)
class AllocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AllocationService allocationService;

    @Test
    void createAllocation_ShouldReturn201() throws Exception {
        AllocationRequest request = AllocationRequest.builder()
            .employeeId(1L)
            .projectId(2L)
            .allocationPercent(60)
            .roleInProject("Backend Developer")
            .startDate(LocalDate.of(2026, 1, 1))
            .build();

        AllocationResponse response = AllocationResponse.builder()
            .id(1L)
            .employeeId(1L)
            .projectId(2L)
            .allocationPercent(60)
            .roleInProject("Backend Developer")
            .startDate(LocalDate.of(2026, 1, 1))
            .build();

        when(allocationService.createAllocation(any(AllocationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/allocations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.allocationPercent").value(60));
    }

    @Test
    void createAllocation_WithInvalidPercent_ShouldReturn400() throws Exception {
        AllocationRequest request = AllocationRequest.builder()
            .employeeId(1L)
            .projectId(2L)
            .allocationPercent(150) // invalid (> 100)
            .roleInProject("Backend Developer")
            .startDate(LocalDate.of(2026, 1, 1))
            .build();

        mockMvc.perform(post("/api/allocations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }
}
