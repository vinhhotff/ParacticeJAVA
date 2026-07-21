package org.example.homework.dto.response;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSkillSearchResponse {
    private String employeeName;
    private int available;
}
