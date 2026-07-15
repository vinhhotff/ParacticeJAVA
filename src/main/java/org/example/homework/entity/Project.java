package org.example.homework.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.homework.entity.enums.ProjectStatus;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "project_code", nullable = false, unique = true, length = 20)
    private String projectCode;

    @Column(name = "project_name", nullable = false, length = 200)
    private String projectName;

    @Column(name = "customer", length = 100)
    private String customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProjectStatus status;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Builder.Default
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Allocation> allocations = new ArrayList<>();
}
