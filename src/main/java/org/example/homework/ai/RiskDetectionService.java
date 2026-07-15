package org.example.homework.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.homework.dto.response.EmployeeWorkload;
import org.example.homework.dto.response.Risk;
import org.example.homework.dto.response.RiskReport;
import org.example.homework.entity.Employee;
import org.example.homework.repository.AllocationRepository;
import org.example.homework.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskDetectionService {

    private final EmployeeRepository employeeRepository;
    private final AllocationRepository allocationRepository;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${GEMINI_API_KEY:}")
    private String envGeminiApiKey;

    private String getApiKey() {
        if (geminiApiKey != null && !geminiApiKey.trim().isEmpty()) {
            return geminiApiKey;
        }
        return envGeminiApiKey;
    }

    public RiskReport detectRisks(String teamRole, String customPrompt) {
        log.info("Detecting risks for teamRole: {}, customPrompt: {}", teamRole, customPrompt);
        List<Employee> employees = teamRole != null && !teamRole.trim().isEmpty()
            ? employeeRepository.findByRoleContainingIgnoreCase(teamRole)
            : employeeRepository.findAll();

        List<EmployeeWorkload> workloads = employees.stream()
            .map(emp -> {
                int total = allocationRepository.sumAllocationByEmployeeId(emp.getEmployeeId());
                return EmployeeWorkload.builder()
                    .employeeId(emp.getEmployeeId())
                    .employeeName(emp.getFullName())
                    .totalAllocation(total)
                    .available(100 - total)
                    .build();
            })
            .sorted(Comparator.comparingInt(EmployeeWorkload::getAvailable).reversed())
            .toList();

        // Try to generate risk report using Gemini API first
        RiskReport geminiReport = callGeminiForRisks(workloads, customPrompt);
        if (geminiReport != null) {
            return geminiReport;
        }

        // Fallback: rule-based calculation
        List<Risk> risks = new ArrayList<>();
        if (!employees.isEmpty()) {
            long overloaded = workloads.stream().filter(w -> w.getTotalAllocation() > 90).count();
            if (overloaded > 0) {
                risks.add(Risk.builder()
                    .type("OVERLOADED_TEAM")
                    .severity(overloaded > employees.size() / 2 ? "HIGH" : "MEDIUM")
                    .message("Team đang sử dụng quá 90% capacity. Số lượng: " + overloaded)
                    .build());
            }

            long available = workloads.stream().filter(w -> w.getAvailable() >= 50).count();
            if (available < 2) {
                risks.add(Risk.builder()
                    .type("LOW_AVAILABILITY")
                    .severity(available == 0 ? "HIGH" : "MEDIUM")
                    .message("Chỉ còn " + available + " resource available trên 50%.")
                    .build());
            }
        }

        long availableCount = workloads.stream().filter(w -> w.getAvailable() >= 50).count();

        return RiskReport.builder()
            .risks(risks)
            .summary(generateSummary(risks, availableCount))
            .workloadSummary(workloads)
            .build();
    }

    @SuppressWarnings("unchecked")
    private RiskReport callGeminiForRisks(List<EmployeeWorkload> workloads, String customPrompt) {
        String apiKey = getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("Gemini API key is not configured. Falling back to local rule-based risk detection.");
            return null;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent?key=" + apiKey;

            // Build context string
            StringBuilder context = new StringBuilder();
            context.append("Team Workload Data:\n");
            for (EmployeeWorkload w : workloads) {
                context.append(String.format("- Employee ID %d (%s): Total Allocation %d%%, Available Capacity %d%%\n",
                    w.getEmployeeId(), w.getEmployeeName(), w.getTotalAllocation(), w.getAvailable()));
            }

            String prompt = "You are a Project Resource Risk Analyst. Analyze the following team workloads and identify allocation risks.\n"
                + "Crucial requirement: You MUST write the 'summary' and all risk 'message' fields in Vietnamese (Tiếng Việt).\n";
            if (customPrompt != null && !customPrompt.trim().isEmpty()) {
                prompt += "User's Custom Request: \"" + customPrompt + "\"\n";
            }
            prompt += "Return a JSON response containing an overall summary, a list of specific risks (with type, severity, and message), "
                + "and the workloadSummary list.\n\n"
                + context.toString();

            // Construct requests
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", prompt);

            Map<String, Object> partContainer = new HashMap<>();
            partContainer.put("parts", List.of(textPart));

            Map<String, Object> contents = new HashMap<>();
            contents.put("contents", List.of(partContainer));

            // Structured JSON response schema config
            Map<String, Object> responseSchema = new HashMap<>();
            responseSchema.put("type", "OBJECT");
            
            Map<String, Object> properties = new HashMap<>();
            
            Map<String, Object> summaryProp = new HashMap<>();
            summaryProp.put("type", "STRING");
            properties.put("summary", summaryProp);

            // risks array schema
            Map<String, Object> risksProp = new HashMap<>();
            risksProp.put("type", "ARRAY");
            Map<String, Object> riskItem = new HashMap<>();
            riskItem.put("type", "OBJECT");
            Map<String, Object> riskItemProps = new HashMap<>();
            riskItemProps.put("type", Map.of("type", "STRING"));
            riskItemProps.put("severity", Map.of("type", "STRING"));
            riskItemProps.put("message", Map.of("type", "STRING"));
            riskItem.put("properties", riskItemProps);
            riskItem.put("required", List.of("type", "severity", "message"));
            risksProp.put("items", riskItem);
            properties.put("risks", risksProp);

            // workloadSummary array schema
            Map<String, Object> workloadSummaryProp = new HashMap<>();
            workloadSummaryProp.put("type", "ARRAY");
            Map<String, Object> workloadItem = new HashMap<>();
            workloadItem.put("type", "OBJECT");
            Map<String, Object> workloadItemProps = new HashMap<>();
            workloadItemProps.put("employeeId", Map.of("type", "INTEGER"));
            workloadItemProps.put("employeeName", Map.of("type", "STRING"));
            workloadItemProps.put("totalAllocation", Map.of("type", "INTEGER"));
            workloadItemProps.put("available", Map.of("type", "INTEGER"));
            workloadItem.put("properties", workloadItemProps);
            workloadItem.put("required", List.of("employeeId", "employeeName", "totalAllocation", "available"));
            workloadSummaryProp.put("items", workloadItem);
            properties.put("workloadSummary", workloadSummaryProp);

            responseSchema.put("properties", properties);
            responseSchema.put("required", List.of("summary", "risks", "workloadSummary"));

            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("responseMimeType", "application/json");
            generationConfig.put("responseSchema", responseSchema);

            contents.put("generationConfig", generationConfig);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(contents, headers);

            Map<String, Object> response = restTemplate.postForObject(url, requestEntity, Map.class);
            if (response != null && response.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> firstCandidate = candidates.get(0);
                    if (firstCandidate.containsKey("content")) {
                        Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
                        if (content.containsKey("parts")) {
                            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                            if (!parts.isEmpty() && parts.get(0).containsKey("text")) {
                                String jsonText = (String) parts.get(0).get("text");
                                
                                // Deserialize using ObjectMapper
                                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                RiskReport report = mapper.readValue(jsonText, RiskReport.class);
                                log.info("Successfully received generated risk analysis from Gemini API.");
                                return report;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to generate risk report from Gemini API: {}. Falling back to rule-based analysis.", e.getMessage());
        }
        return null;
    }

    private String generateSummary(List<Risk> risks, long available) {
        if (risks.isEmpty()) {
            return "✅ Team đang trong trạng thái an toàn.";
        }
        return "⚠️ " + risks.size() + " rủi ro. Còn " + available + " resource khả dụng > 50%.";
    }
}
