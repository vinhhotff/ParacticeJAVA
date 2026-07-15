package org.example.homework.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationRequest {

    @NotNull(message = "Employee ID is required")
    @Positive(message = "Employee ID must be positive")
    private Long employeeId;

    @NotNull(message = "Project ID is required")
    @Positive(message = "Project ID must be positive")
    private Long projectId;

    @NotNull(message = "Allocation percent is required")
    @Min(value = 1, message = "Allocation must be at least 1%")
    @Max(value = 100, message = "Allocation cannot exceed 100%")
    private Integer allocationPercent;

    @NotBlank(message = "Role in project is required")
    private String roleInProject;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;
}
