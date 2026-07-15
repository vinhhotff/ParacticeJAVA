package org.example.homework.controller;

import org.example.homework.dto.request.EmployeeRequest;
import org.example.homework.dto.response.EmployeeResponse;
import org.example.homework.dto.response.WorkloadResponse;
import org.example.homework.service.EmployeeService;
import org.example.homework.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Test
    void createEmployee_ShouldReturn201() throws Exception {
        EmployeeRequest request = EmployeeRequest.builder()
            .employeeCode("EMP001")
            .fullName("Tuan Ho Anh")
            .email("tuanha@company.com")
            .role("Senior Developer")
            .department("FSOFT-Q1")
            .build();

        EmployeeResponse response = EmployeeResponse.builder()
            .id(1L)
            .employeeCode("EMP001")
            .fullName("Tuan Ho Anh")
            .email("tuanha@company.com")
            .role("Senior Developer")
            .department("FSOFT-Q1")
            .build();

        when(employeeService.create(any(EmployeeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.employeeCode").value("EMP001"));
    }

    @Test
    void getWorkload_ShouldReturn200() throws Exception {
        WorkloadResponse workload = WorkloadResponse.builder()
            .employeeId(1L)
            .employeeName("Tuan Ho Anh")
            .totalAllocation(80)
            .available(20)
            .build();

        when(employeeService.getWorkload(1L)).thenReturn(workload);

        mockMvc.perform(get("/api/employees/1/workload"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.employeeId").value(1))
            .andExpect(jsonPath("$.totalAllocation").value(80))
            .andExpect(jsonPath("$.available").value(20));
    }
}
