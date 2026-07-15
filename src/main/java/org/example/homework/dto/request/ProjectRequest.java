package org.example.homework.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.example.homework.entity.enums.ProjectStatus;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequest {

    @NotBlank(message = "Project code is required")
    private String projectCode;

    @NotBlank(message = "Project name is required")
    private String projectName;

    private String customer;

    private ProjectStatus status; // defaults to PLANNING if null in service

    private LocalDate startDate;

    private LocalDate endDate;
}
