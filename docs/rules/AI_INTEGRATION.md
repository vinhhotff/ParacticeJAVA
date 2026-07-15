# AI Integration Rules (Bonus)

> AI features: Resource Recommendation + Risk Detection.

---

## 1. Resource Recommendation

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceRecommendationService {

    private final EmployeeRepository employeeRepository;
    private final AllocationRepository allocationRepository;

    public List<ResourceRecommendation> recommend(String role, int minAvailable) {
        log.info("Recommend resources: role={}, minAvailable={}", role, minAvailable);

        List<Employee> employees = employeeRepository.findByRoleContainingIgnoreCase(role);

        return employees.stream()
            .map(emp -> {
                Integer total = allocationRepository.sumAllocationByEmployeeId(emp.getEmployeeId());
                int available = 100 - (total != null ? total : 0);
                return ResourceRecommendation.from(emp, available);
            })
            .filter(rec -> rec.getAvailable() >= minAvailable)
            .sorted(Comparator.comparingInt(ResourceRecommendation::getAvailable).reversed())
            .toList();
    }
}
```

### DTO

```java
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceRecommendation {
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private String role;
    private int available;
    private double matchScore;

    public static ResourceRecommendation from(Employee employee, int available) {
        return ResourceRecommendation.builder()
            .employeeId(employee.getEmployeeId())
            .employeeName(employee.getFullName())
            .employeeCode(employee.getEmployeeCode())
            .role(employee.getRole())
            .available(available)
            .matchScore(calculateMatchScore(employee, available))
            .build();
    }

    private static double calculateMatchScore(Employee emp, int available) {
        double score = (double) available / 100.0 * 50.0;
        score += emp.getRole().toLowerCase().contains("senior") ? 30 : 15;
        score += emp.getRole().toLowerCase().contains("lead") ? 20 : 10;
        return Math.round(score * 10.0) / 10.0;
    }
}
```

---

## 2. Risk Detection

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class RiskDetectionService {

    private final EmployeeRepository employeeRepository;
    private final AllocationRepository allocationRepository;

    public RiskReport detectRisks(String teamRole) {
        List<Employee> employees = teamRole != null
            ? employeeRepository.findByRoleContainingIgnoreCase(teamRole)
            : employeeRepository.findAll();

        List<EmployeeWorkload> workloads = employees.stream()
            .map(emp -> {
                Integer total = allocationRepository.sumAllocationByEmployeeId(emp.getEmployeeId());
                int t = total != null ? total : 0;
                return new EmployeeWorkload(emp.getEmployeeId(), emp.getFullName(), t, 100 - t);
            })
            .sorted(Comparator.comparingInt(EmployeeWorkload::getAvailable).reversed())
            .toList();

        List<Risk> risks = new ArrayList<>();

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

        return RiskReport.builder()
            .risks(risks)
            .summary(generateSummary(risks, available))
            .workloadSummary(workloads)
            .build();
    }

    private String generateSummary(List<Risk> risks, long available) {
        if (risks.isEmpty()) return "✅ Team đang trong trạng thái an toàn.";
        return "⚠️ " + risks.size() + " rủi ro. Còn " + available + " resource khả dụng > 50%.";
    }
}
```

### DTOs

```java
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class RiskReport {
    private List<Risk> risks;
    private String summary;
    private List<EmployeeWorkload> workloadSummary;
}

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class Risk {
    private String type;
    private String severity;
    private String message;
}

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class EmployeeWorkload {
    private Long employeeId;
    private String employeeName;
    private int totalAllocation;
    private int available;
}
```

---

## 3. Controller

```java
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
        if (minAvailable < 0 || minAvailable > 100) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(recommendationService.recommend(role, minAvailable));
    }

    @GetMapping("/detect-risks")
    public ResponseEntity<RiskReport> detectRisks(@RequestParam(required = false) String teamRole) {
        return ResponseEntity.ok(riskDetectionService.detectRisks(teamRole));
    }
}
```

---

## 4. Response Examples

```json
// GET /api/ai/recommend-resources?role=Java Developer&minAvailable=50
[{
    "employeeId": 3,
    "employeeName": "Nguyen Van A",
    "employeeCode": "EMP003",
    "role": "Senior Java Developer",
    "available": 60,
    "matchScore": 95.0
}]

// GET /api/ai/detect-risks
{
    "risks": [{
        "type": "OVERLOADED_TEAM",
        "severity": "HIGH",
        "message": "Team đang sử dụng quá 90% capacity. Số lượng: 2"
    }],
    "summary": "⚠️ 1 rủi ro. Còn 1 resource khả dụng > 50%.",
    "workloadSummary": [
        { "employeeId": 1, "employeeName": "Tuan Ho Anh", "totalAllocation": 100, "available": 0 },
        { "employeeId": 3, "employeeName": "Nguyen Van A", "totalAllocation": 50, "available": 50 }
    ]
}
```

---

## 5. Test

```java
@ExtendWith(MockitoExtension.class)
class ResourceRecommendationServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private AllocationRepository allocationRepository;
    @InjectMocks private ResourceRecommendationService service;

    @Test
    void recommend_WithAvailableResources_ShouldReturnSorted() {
        List<Employee> employees = List.of(
            Employee.builder().employeeId(1L).fullName("Dev A").role("Java Developer").build(),
            Employee.builder().employeeId(2L).fullName("Dev B").role("Senior Java Developer").build()
        );
        when(employeeRepository.findByRoleContainingIgnoreCase("Java")).thenReturn(employees);
        when(allocationRepository.sumAllocationByEmployeeId(1L)).thenReturn(40);
        when(allocationRepository.sumAllocationByEmployeeId(2L)).thenReturn(60);

        List<ResourceRecommendation> result = service.recommend("Java", 30);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAvailable()).isGreaterThanOrEqualTo(result.get(1).getAvailable());
    }

    @Test
    void recommend_WithNoMatch_ShouldReturnEmpty() {
        when(employeeRepository.findByRoleContainingIgnoreCase("Python")).thenReturn(List.of());
        assertThat(service.recommend("Python", 50)).isEmpty();
    }
}
```
