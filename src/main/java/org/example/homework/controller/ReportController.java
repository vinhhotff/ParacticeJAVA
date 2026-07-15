package org.example.homework.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.homework.dto.response.AvailableResourceItem;
import org.example.homework.dto.response.OverloadedEmployeeItem;
import org.example.homework.dto.response.UtilizationReportItem;
import org.example.homework.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/utilization")
    public ResponseEntity<List<UtilizationReportItem>> getUtilizationReport() {
        log.info("Request utilization report");
        return ResponseEntity.ok(reportService.getUtilizationReport());
    }

    @GetMapping("/available-resources")
    public ResponseEntity<List<AvailableResourceItem>> getAvailableResourcesReport() {
        log.info("Request available resources report");
        return ResponseEntity.ok(reportService.getAvailableResourcesReport());
    }

    @GetMapping("/overloaded")
    public ResponseEntity<List<OverloadedEmployeeItem>> getOverloadedReport() {
        log.info("Request overloaded employees report");
        return ResponseEntity.ok(reportService.getOverloadedEmployeesReport());
    }
}
