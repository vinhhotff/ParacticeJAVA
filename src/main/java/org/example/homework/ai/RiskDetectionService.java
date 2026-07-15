package org.example.homework.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.homework.dto.response.EmployeeWorkload;
import org.example.homework.dto.response.Risk;
import org.example.homework.dto.response.RiskReport;
import org.example.homework.entity.Employee;
import org.example.homework.repository.AllocationRepository;
import org.example.homework.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskDetectionService {

    private final EmployeeRepository employeeRepository;
    private final AllocationRepository allocationRepository;

    public RiskReport detectRisks(String teamRole) {
        log.info("Detecting risks for teamRole: {}", teamRole);
        List<Employee> employees = teamRole != null && !teamRole.trim().isEmpty()
            ? employeeRepository.findByRoleContainingIgnoreCase(teamRole)
            : employeeRepository.findAll();

        List<EmployeeWorkload> workloads = employees.stream()
            .map(emp -> {
                int total = allocationRepository.sumAllocationByEmployeeId(emp.getEmployeeId());
                return EmployeeWorkload.builder()
                    .employeeId(emp.getEmployeeId())
                    .employeeName(emp.getFullName())
                    .totalAllocation(total)
                    .available(100 - total)
                    .build();
            })
            .sorted(Comparator.comparingInt(EmployeeWorkload::getAvailable).reversed())
            .toList();

        List<Risk> risks = new ArrayList<>();

        if (!employees.isEmpty()) {
            long overloaded = workloads.stream().filter(w -> w.getTotalAllocation() > 90).count();
            if (overloaded > 0) {
                risks.add(Risk.builder()
                    .type("OVERLOADED_TEAM")
                    .severity(overloaded > employees.size() / 2 ? "HIGH" : "MEDIUM")
                    .message("Team đang sử dụng quá 90% capacity. Số lượng: " + overloaded)
                    .build());
            }

            long available = workloads.stream().filter(w -> w.getAvailable() >= 50).count();
            if (available < 2) {
                risks.add(Risk.builder()
                    .type("LOW_AVAILABILITY")
                    .severity(available == 0 ? "HIGH" : "MEDIUM")
                    .message("Chỉ còn " + available + " resource available trên 50%.")
                    .build());
            }
        }

        long availableCount = workloads.stream().filter(w -> w.getAvailable() >= 50).count();

        return RiskReport.builder()
            .risks(risks)
            .summary(generateSummary(risks, availableCount))
            .workloadSummary(workloads)
            .build();
    }

    private String generateSummary(List<Risk> risks, long available) {
        if (risks.isEmpty()) {
            return "✅ Team đang trong trạng thái an toàn.";
        }
        return "⚠️ " + risks.size() + " rủi ro. Còn " + available + " resource khả dụng > 50%.";
    }
}
