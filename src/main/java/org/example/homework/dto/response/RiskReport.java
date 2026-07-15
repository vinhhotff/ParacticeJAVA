package org.example.homework.dto.response;

import lombok.*;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskReport {
    private List<Risk> risks;
    private String summary;
    private List<EmployeeWorkload> workloadSummary;
}
