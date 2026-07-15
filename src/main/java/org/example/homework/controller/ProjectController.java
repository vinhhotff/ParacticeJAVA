package org.example.homework.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.homework.dto.request.ProjectRequest;
import org.example.homework.dto.response.ProjectResponse;
import org.example.homework.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Slf4j
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> create(@RequestBody @Valid ProjectRequest request) {
        log.info("Create project request: {}", request.getProjectCode());
        ProjectResponse response = projectService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getById(@PathVariable Long id) {
        log.info("Get project request: {}", id);
        ProjectResponse response = projectService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAll() {
        log.info("Get all projects request");
        List<ProjectResponse> response = projectService.findAll();
        return ResponseEntity.ok(response);
    }
}
