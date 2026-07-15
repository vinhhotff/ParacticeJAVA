package org.example.homework.repository;

import org.example.homework.entity.Allocation;
import org.example.homework.entity.Employee;
import org.example.homework.entity.Project;
import org.example.homework.entity.enums.ProjectStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AllocationRepositoryTest {

    @Autowired
    private AllocationRepository allocationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void should_SumAllocationsCorrectly() {
        Employee employee = Employee.builder()
            .employeeCode("EMP99")
            .fullName("Tester")
            .email("tester@test.com")
            .role("Dev")
            .department("TestDept")
            .build();
        Employee savedEmployee = employeeRepository.save(employee);

        Project project1 = Project.builder()
            .projectCode("P1")
            .projectName("Project 1")
            .status(ProjectStatus.ACTIVE)
            .build();
        Project project2 = Project.builder()
            .projectCode("P2")
            .projectName("Project 2")
            .status(ProjectStatus.PLANNING)
            .build();
        Project savedProj1 = projectRepository.save(project1);
        Project savedProj2 = projectRepository.save(project2);

        Allocation allocation1 = Allocation.builder()
            .employee(savedEmployee)
            .project(savedProj1)
            .allocationPercent(30)
            .roleInProject("Dev")
            .startDate(LocalDate.now())
            .build();
        Allocation allocation2 = Allocation.builder()
            .employee(savedEmployee)
            .project(savedProj2)
            .allocationPercent(40)
            .roleInProject("Lead")
            .startDate(LocalDate.now())
            .build();

        Allocation savedAlloc1 = allocationRepository.save(allocation1);
        allocationRepository.save(allocation2);

        int total = allocationRepository.sumAllocationByEmployeeId(savedEmployee.getEmployeeId());
        assertThat(total).isEqualTo(70);

        int totalExcluding = allocationRepository.sumAllocationByEmployeeIdExcluding(
            savedEmployee.getEmployeeId(), savedAlloc1.getAllocationId()
        );
        assertThat(totalExcluding).isEqualTo(40);
    }
}
