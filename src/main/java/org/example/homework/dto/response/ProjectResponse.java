package org.example.homework.dto.response;

import lombok.*;
import org.example.homework.entity.enums.ProjectStatus;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private Long id;
    private String projectCode;
    private String projectName;
    private String customer;
    private ProjectStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
}
