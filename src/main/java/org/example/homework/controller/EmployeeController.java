package org.example.homework.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.homework.dto.request.EmployeeRequest;
import org.example.homework.dto.response.EmployeeResponse;
import org.example.homework.dto.response.WorkloadResponse;
import org.example.homework.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@RequestBody @Valid EmployeeRequest request) {
        log.info("Create employee request: {}", request.getEmployeeCode());
        EmployeeResponse response = employeeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getById(@PathVariable Long id) {
        log.info("Get employee request: {}", id);
        EmployeeResponse response = employeeService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "employeeId,asc") String sort) {
        if (page != null && size != null) {
            log.info("Get paginated employees request: page={}, size={}, sort={}", page, size, sort);
            String[] sortParts = sort.split(",");
            String sortBy = sortParts[0];
            org.springframework.data.domain.Sort.Direction direction = 
                (sortParts.length > 1 && sortParts[1].equalsIgnoreCase("desc")) 
                ? org.springframework.data.domain.Sort.Direction.DESC 
                : org.springframework.data.domain.Sort.Direction.ASC;
            
            org.springframework.data.domain.Pageable pageable = 
                org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(direction, sortBy));
            return ResponseEntity.ok(employeeService.findAll(pageable));
        }

        log.info("Get all employees request");
        List<EmployeeResponse> response = employeeService.findAll();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> update(@PathVariable Long id, @RequestBody @Valid EmployeeRequest request) {
        log.info("Update employee {}: {}", id, request.getEmployeeCode());
        EmployeeResponse response = employeeService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Delete employee: {}", id);
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/workload")
    public ResponseEntity<WorkloadResponse> getWorkload(@PathVariable Long id) {
        log.info("Get employee workload request: {}", id);
        WorkloadResponse response = employeeService.getWorkload(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/skills")
    public ResponseEntity<Void> addSkills(@PathVariable Long id, @RequestBody List<String> skills) {
        log.info("Add skills to employee {}: {}", id, skills);
        employeeService.addSkills(id, skills);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/skills")
    public ResponseEntity<List<String>> getSkills(@PathVariable Long id) {
        log.info("Get skills of employee: {}", id);
        return ResponseEntity.ok(employeeService.getSkills(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<org.example.homework.dto.response.EmployeeSkillSearchResponse>> searchBySkill(@RequestParam String skill) {
        log.info("Search employees by skill: {}", skill);
        return ResponseEntity.ok(employeeService.searchBySkill(skill));
    }
}
