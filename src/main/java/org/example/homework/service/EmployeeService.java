package org.example.homework.service;

import org.example.homework.dto.request.EmployeeRequest;
import org.example.homework.dto.response.EmployeeResponse;
import org.example.homework.dto.response.WorkloadResponse;
import java.util.List;

public interface EmployeeService {
    EmployeeResponse create(EmployeeRequest request);
    EmployeeResponse findById(Long id);
    List<EmployeeResponse> findAll();
    org.example.homework.dto.response.PageResponse<EmployeeResponse> findAll(org.springframework.data.domain.Pageable pageable);
    EmployeeResponse update(Long id, EmployeeRequest request);
    void delete(Long id);
    WorkloadResponse getWorkload(Long id);
    List<String> getSkills(Long employeeId);
    void addSkills(Long employeeId, List<String> skillNames);
    List<org.example.homework.dto.response.EmployeeSkillSearchResponse> searchBySkill(String skillName);
}
