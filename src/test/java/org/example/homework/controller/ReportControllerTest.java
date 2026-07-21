package org.example.homework.controller;

import org.example.homework.dto.response.AvailableResourceItem;
import org.example.homework.dto.response.OverloadedEmployeeItem;
import org.example.homework.dto.response.UtilizationReportItem;
import org.example.homework.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @Test
    void getUtilizationReport_ShouldReturn200() throws Exception {
        UtilizationReportItem item = UtilizationReportItem.builder()
            .employeeId(1L).employeeCode("E1").fullName("Emp 1").totalAllocation(80)
            .build();

        when(reportService.getUtilizationReport()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/reports/utilization"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].employeeCode").value("E1"))
            .andExpect(jsonPath("$[0].totalAllocation").value(80));
    }

    @Test
    void getAvailableResourcesReport_ShouldReturn200() throws Exception {
        AvailableResourceItem item = AvailableResourceItem.builder()
            .employeeId(3L).employeeCode("E3").fullName("Emp 3").availablePercent(40)
            .build();

        when(reportService.getAvailableResourcesReport()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/reports/available-resources"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].availablePercent").value(40));
    }

    @Test
    void getOverloadedReport_ShouldReturn200() throws Exception {
        OverloadedEmployeeItem item = OverloadedEmployeeItem.builder()
            .employeeId(1L).employeeCode("E1").fullName("Emp 1").totalAllocation(100)
            .build();

        when(reportService.getOverloadedEmployeesReport()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/reports/overloaded"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].totalAllocation").value(100));
    }
}
