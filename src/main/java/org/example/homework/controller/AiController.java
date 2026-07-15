package org.example.homework.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.homework.ai.ResourceRecommendationService;
import org.example.homework.ai.RiskDetectionService;
import org.example.homework.dto.response.ResourceRecommendation;
import org.example.homework.dto.response.RiskReport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private final ResourceRecommendationService recommendationService;
    private final RiskDetectionService riskDetectionService;

    @GetMapping("/recommend-resources")
    public ResponseEntity<List<ResourceRecommendation>> recommendResources(
            @RequestParam String role,
            @RequestParam(defaultValue = "50") int minAvailable) {
        log.info("AI recommend resources request: role={}, minAvailable={}", role, minAvailable);
        if (minAvailable < 0 || minAvailable > 100) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(recommendationService.recommend(role, minAvailable));
    }

    @GetMapping("/detect-risks")
    public ResponseEntity<RiskReport> detectRisks(
            @RequestParam(required = false) String teamRole,
            @RequestParam(required = false) String customPrompt) {
        log.info("AI detect risks request: teamRole={}, customPrompt={}", teamRole, customPrompt);
        return ResponseEntity.ok(riskDetectionService.detectRisks(teamRole, customPrompt));
    }
}
