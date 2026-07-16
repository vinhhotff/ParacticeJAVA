package org.example.homework.dto.response;

import lombok.*;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationResponse {
    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private Long projectId;
    private String projectCode;
    private String projectName;
    private Integer allocationPercent;
    private String roleInProject;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}
