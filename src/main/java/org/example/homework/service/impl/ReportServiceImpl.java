package org.example.homework.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.homework.dto.response.AvailableResourceItem;
import org.example.homework.dto.response.OverloadedEmployeeItem;
import org.example.homework.dto.response.UtilizationReportItem;
import org.example.homework.repository.EmployeeRepository;
import org.example.homework.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UtilizationReportItem> getUtilizationReport() {
        log.info("[REPORT_UTILIZATION]");
        List<Object[]> results = employeeRepository.getEmployeeAllocationSums();

        return results.stream()
            .map(row -> UtilizationReportItem.builder()
                .employeeId((Long) row[0])
                .employeeCode((String) row[1])
                .fullName((String) row[2])
                .totalAllocation(((Number) row[3]).intValue())
                .build())
            .sorted(Comparator.comparing(UtilizationReportItem::getTotalAllocation).reversed())
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailableResourceItem> getAvailableResourcesReport() {
        log.info("[REPORT_AVAILABLE_RESOURCES]");
        List<Object[]> results = employeeRepository.getEmployeeAllocationSums();

        return results.stream()
            .map(row -> {
                int total = ((Number) row[3]).intValue();
                int available = 100 - total;
                return AvailableResourceItem.builder()
                    .employeeId((Long) row[0])
                    .employeeCode((String) row[1])
                    .fullName((String) row[2])
                    .availablePercent(available)
                    .build();
            })
            .filter(item -> item.getAvailablePercent() > 0)
            .sorted(Comparator.comparing(AvailableResourceItem::getAvailablePercent).reversed())
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OverloadedEmployeeItem> getOverloadedEmployeesReport() {
        log.info("[REPORT_OVERLOADED_EMPLOYEES]");
        List<Object[]> results = employeeRepository.getEmployeeAllocationSums();

        return results.stream()
            .map(row -> OverloadedEmployeeItem.builder()
                .employeeId((Long) row[0])
                .employeeCode((String) row[1])
                .fullName((String) row[2])
                .totalAllocation(((Number) row[3]).intValue())
                .build())
            .filter(item -> item.getTotalAllocation() > 90)
            .sorted(Comparator.comparing(OverloadedEmployeeItem::getTotalAllocation).reversed())
            .toList();
    }
}
