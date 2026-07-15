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
    private Long projectId;
    private Integer allocationPercent;
    private String roleInProject;
    private LocalDate startDate;
    private LocalDate endDate;
}
