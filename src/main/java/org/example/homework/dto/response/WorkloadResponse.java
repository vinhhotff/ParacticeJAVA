package org.example.homework.dto.response;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadResponse {
    private Long employeeId;
    private String employeeName;
    private Integer totalAllocation;
    private Integer allocated;
    private Integer available;
}
