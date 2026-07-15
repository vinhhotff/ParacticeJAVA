package org.example.homework.dto.response;

import lombok.*;
import org.example.homework.entity.Employee;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceRecommendation {
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private String role;
    private int available;
    private double matchScore;

    public static ResourceRecommendation from(Employee employee, int available) {
        return ResourceRecommendation.builder()
            .employeeId(employee.getEmployeeId())
            .employeeName(employee.getFullName())
            .employeeCode(employee.getEmployeeCode())
            .role(employee.getRole())
            .available(available)
            .matchScore(calculateMatchScore(employee, available))
            .build();
    }

    private static double calculateMatchScore(Employee emp, int available) {
        double score = (double) available / 100.0 * 50.0;
        score += emp.getRole().toLowerCase().contains("senior") ? 30 : 15;
        score += emp.getRole().toLowerCase().contains("lead") ? 20 : 10;
        return Math.round(score * 10.0) / 10.0;
    }
}
