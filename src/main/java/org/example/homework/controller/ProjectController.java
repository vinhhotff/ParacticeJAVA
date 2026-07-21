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
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "projectId,asc") String sort) {
        if (page != null && size != null) {
            log.info("Get paginated projects request: page={}, size={}, sort={}", page, size, sort);
            String[] sortParts = sort.split(",");
            String sortBy = sortParts[0];
            org.springframework.data.domain.Sort.Direction direction = 
                (sortParts.length > 1 && sortParts[1].equalsIgnoreCase("desc")) 
                ? org.springframework.data.domain.Sort.Direction.DESC 
                : org.springframework.data.domain.Sort.Direction.ASC;
            
            org.springframework.data.domain.Pageable pageable = 
                org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(direction, sortBy));
            return ResponseEntity.ok(projectService.findAll(pageable));
        }

        log.info("Get all projects request");
        List<ProjectResponse> response = projectService.findAll();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> update(@PathVariable Long id, @RequestBody @Valid ProjectRequest request) {
        log.info("Update project {}: {}", id, request.getProjectCode());
        ProjectResponse response = projectService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Delete project: {}", id);
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
