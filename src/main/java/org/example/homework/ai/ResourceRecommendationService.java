package org.example.homework.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.homework.dto.response.ResourceRecommendation;
import org.example.homework.entity.Employee;
import org.example.homework.repository.AllocationRepository;
import org.example.homework.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceRecommendationService {

    private final EmployeeRepository employeeRepository;
    private final AllocationRepository allocationRepository;

    public List<ResourceRecommendation> recommend(String role, int minAvailable) {
        log.info("Recommend resources: role={}, minAvailable={}", role, minAvailable);

        List<Employee> employees = employeeRepository.findByRoleContainingIgnoreCase(role);

        return employees.stream()
            .map(emp -> {
                int total = allocationRepository.sumAllocationByEmployeeId(emp.getEmployeeId());
                int available = 100 - total;
                return ResourceRecommendation.from(emp, available);
            })
            .filter(rec -> rec.getAvailable() >= minAvailable)
            .sorted(Comparator.comparingInt(ResourceRecommendation::getAvailable).reversed())
            .toList();
    }
}
