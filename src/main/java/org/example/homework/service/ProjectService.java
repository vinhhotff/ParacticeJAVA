package org.example.homework.service;

import org.example.homework.dto.request.ProjectRequest;
import org.example.homework.dto.response.ProjectResponse;
import java.util.List;

public interface ProjectService {
    ProjectResponse create(ProjectRequest request);
    ProjectResponse findById(Long id);
    List<ProjectResponse> findAll();
    ProjectResponse update(Long id, ProjectRequest request);
    void delete(Long id);
}
