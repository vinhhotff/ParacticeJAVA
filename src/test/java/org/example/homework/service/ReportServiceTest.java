package org.example.homework.service;

import org.example.homework.dto.response.AvailableResourceItem;
import org.example.homework.dto.response.OverloadedEmployeeItem;
import org.example.homework.dto.response.UtilizationReportItem;
import org.example.homework.repository.EmployeeRepository;
import org.example.homework.service.impl.ReportServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Test
    void should_CalculateUtilizationReport() {
        Object[] row1 = new Object[]{1L, "E1", "Emp 1", 80};
        Object[] row2 = new Object[]{2L, "E2", "Emp 2", 100};

        when(employeeRepository.getEmployeeAllocationSums()).thenReturn(List.of(row1, row2));

        List<UtilizationReportItem> result = reportService.getUtilizationReport();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEmployeeCode()).isEqualTo("E2"); // 100% first
        assertThat(result.get(0).getTotalAllocation()).isEqualTo(100);
        assertThat(result.get(1).getEmployeeCode()).isEqualTo("E1"); // 80% second
    }

    @Test
    void should_CalculateAvailableResourcesReport() {
        Object[] row1 = new Object[]{1L, "E1", "Emp 1", 60}; // available: 40%
        Object[] row2 = new Object[]{2L, "E2", "Emp 2", 100}; // available: 0%

        when(employeeRepository.getEmployeeAllocationSums()).thenReturn(List.of(row1, row2));

        List<AvailableResourceItem> result = reportService.getAvailableResourcesReport();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmployeeCode()).isEqualTo("E1");
        assertThat(result.get(0).getAvailablePercent()).isEqualTo(40);
    }

    @Test
    void should_CalculateOverloadedReport() {
        Object[] row1 = new Object[]{1L, "E1", "Emp 1", 95}; // overloaded
        Object[] row2 = new Object[]{2L, "E2", "Emp 2", 60}; // not overloaded

        when(employeeRepository.getEmployeeAllocationSums()).thenReturn(List.of(row1, row2));

        List<OverloadedEmployeeItem> result = reportService.getOverloadedEmployeesReport();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmployeeCode()).isEqualTo("E1");
    }
}
