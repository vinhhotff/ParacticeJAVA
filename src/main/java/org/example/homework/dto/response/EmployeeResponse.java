package org.example.homework.dto.response;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {
    private Long id;
    private String employeeCode;
    private String fullName;
    private String email;
    private String role;
    private String department;
}
