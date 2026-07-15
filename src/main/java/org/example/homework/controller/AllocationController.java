package org.example.homework.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.homework.dto.request.AllocationRequest;
import org.example.homework.dto.response.AllocationResponse;
import org.example.homework.service.AllocationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/allocations")
@RequiredArgsConstructor
@Slf4j
public class AllocationController {

    private final AllocationService allocationService;

    @PostMapping
    public ResponseEntity<AllocationResponse> create(@RequestBody @Valid AllocationRequest request) {
        log.info("Create allocation: employee={}, project={}, percent={}",
            request.getEmployeeId(), request.getProjectId(), request.getAllocationPercent());
        AllocationResponse response = allocationService.createAllocation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AllocationResponse> update(
            @PathVariable Long id, @RequestBody @Valid AllocationRequest request) {
        log.info("Update allocation {}: percent={}", id, request.getAllocationPercent());
        AllocationResponse response = allocationService.updateAllocation(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable Long id) {
        log.info("Remove allocation: {}", id);
        allocationService.removeAllocation(id);
        return ResponseEntity.noContent().build();
    }
}
