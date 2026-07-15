package org.example.homework.dto.response;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverloadedEmployeeItem {
    private Long employeeId;
    private String employeeCode;
    private String fullName;
    private Integer totalAllocation;
}
