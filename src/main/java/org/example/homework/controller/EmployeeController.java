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
    public ResponseEntity<List<EmployeeResponse>> getAll() {
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
}
