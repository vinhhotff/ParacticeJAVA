package org.example.homework.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.homework.entity.Employee;
import org.example.homework.entity.Project;
import org.example.homework.entity.enums.ProjectStatus;

public class TestUtils {

    public static Employee createSampleEmployee() {
        return Employee.builder()
            .employeeId(1L)
            .employeeCode("EMP001")
            .fullName("Tuan Ho Anh")
            .email("tuanha@company.com")
            .role("Senior Developer")
            .department("FSOFT-Q1")
            .build();
    }

    public static Project createSampleProject() {
        return Project.builder()
            .projectId(1L)
            .projectCode("NCG")
            .projectName("NCG Training")
            .customer("Internal")
            .status(ProjectStatus.ACTIVE)
            .build();
    }

    public static String asJsonString(Object obj) {
        try {
            return new ObjectMapper().findAndRegisterModules().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
