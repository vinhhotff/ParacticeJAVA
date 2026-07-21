package org.example.homework.dto.response;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentReportItem {
    private String department;
    private Long headcount;
    private Double averageAllocation;
    private Integer maxAllocation;
    private Integer minAllocation;
}
