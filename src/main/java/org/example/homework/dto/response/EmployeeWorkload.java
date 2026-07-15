package org.example.homework.dto.response;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeWorkload {
    private Long employeeId;
    private String employeeName;
    private int totalAllocation;
    private int available;
}
