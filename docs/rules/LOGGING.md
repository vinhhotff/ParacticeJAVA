# Logging Rules

> Chuẩn logging tập trung, phục vụ debug, monitor, và audit.

---

## 1. Logging Framework

- **Framework**: SLF4J + Logback (mặc định với Spring Boot).
- **Annotation**: `@Slf4j` từ Lombok.

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class AllocationServiceImpl implements AllocationService {
    private final AllocationRepository allocationRepository;
    // log instance tự động được tạo bởi @Slf4j
}
```

---

## 2. Log Level Guidelines

| Level | Khi nào dùng | Ví dụ |
|-------|-------------|-------|
| **ERROR** | Lỗi hệ thống, cần can thiệp ngay | DB connection fail, unexpected exception |
| **WARN** | Vấn đề tiềm ẩn, không mong đợi nhưng không crash | Business validation fail, allocation exceed |
| **INFO** | Thông tin vận hành quan trọng | Create/Update/Delete allocation, employee |
| **DEBUG** | Debug chi tiết trong quá trình dev | Parameter values, step-by-step logic |
| **TRACE** | Rất chi tiết, chỉ dùng để debug sâu | Method entry/exit, DB query details |

### Quy tắc cốt lõi
```
ERROR  → Có bug/cần fix
WARN   → User làm sai / edge case
INFO   → Hệ thống làm gì (audit trail)
DEBUG  → Dev cần gì để debug
TRACE  → Tôi muốn biết từng dòng code chạy
```

---

## 3. Log Format

### 3.1 Console Log (Development)
```
2026-07-15 10:30:00.123 [http-nio-8080-exec-1] INFO  c.c.p.service.AllocationService - Creating allocation for employeeId=1, projectId=2, percent=60
```

### 3.2 JSON Log (Production — optional)
```json
{
    "timestamp": "2026-07-15T10:30:00.123Z",
    "level": "INFO",
    "thread": "http-nio-8080-exec-1",
    "logger": "com.company.project.service.AllocationService",
    "message": "Creating allocation",
    "context": {
        "employeeId": 1,
        "projectId": 2,
        "percent": 60
    }
}
```

### 3.3 Configuration in `application.yml`

```yaml
logging:
  level:
    com.company.project: DEBUG
    org.springframework: INFO
    org.hibernate.SQL: DEBUG      # show SQL queries
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE  # show bind params
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/application.log
    max-size: 10MB
    max-history: 7
```

---

## 4. Logging Patterns by Layer

### 4.1 Controller — Log Request & Response

```java
@PostMapping
public ResponseEntity<AllocationResponse> create(@RequestBody @Valid AllocationRequest request) {
    log.info("Creating allocation: employeeId={}, projectId={}, percent={}",
        request.getEmployeeId(), request.getProjectId(), request.getAllocationPercent());

    AllocationResponse response = allocationService.createAllocation(request);

    log.info("Allocation created successfully: id={}", response.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

### 4.2 Service — Log Business Logic

```java
public AllocationResponse createAllocation(AllocationRequest request) {
    log.debug("Validating allocation request: {}", request);

    // validate
    validationOrchestrator.validate(request);

    // fetch
    Employee employee = employeeRepository.findById(request.getEmployeeId())
        .orElseThrow(() -> {
            log.warn("Employee not found: id={}", request.getEmployeeId());
            return new EmployeeNotFoundException(request.getEmployeeId());
        });

    Project project = projectRepository.findById(request.getProjectId())
        .orElseThrow(() -> {
            log.warn("Project not found: id={}", request.getProjectId());
            return new ProjectNotFoundException(request.getProjectId());
        });

    // check allocation limit
    int currentTotal = calculateCurrentAllocation(employee.getEmployeeId());
    log.debug("Employee {} current allocation: {}%, requesting: {}%",
        employee.getEmployeeCode(), currentTotal, request.getAllocationPercent());

    if (currentTotal + request.getAllocationPercent() > MAX_ALLOCATION) {
        log.warn("Allocation would exceed 100%: employee={}, current={}, request={}",
            employee.getEmployeeCode(), currentTotal, request.getAllocationPercent());
        throw new AllocationExceededException(currentTotal, request.getAllocationPercent());
    }

    // build & save
    Allocation allocation = Allocation.builder()
        .employee(employee)
        .project(project)
        .allocationPercent(request.getAllocationPercent())
        .roleInProject(request.getRoleInProject())
        .startDate(request.getStartDate())
        .endDate(request.getEndDate())
        .build();

    allocation = allocationRepository.save(allocation);
    log.info("Allocation saved: id={}, employee={}, project={}, percent={}",
        allocation.getAllocationId(), employee.getEmployeeCode(),
        project.getProjectCode(), request.getAllocationPercent());

    return mapToResponse(allocation);
}
```

### 4.3 Repository — Let Hibernate Handle It

Repository không cần log riêng. Spring Data JPA + Hibernate SQL logging là đủ.

---

## 5. What to Log & What NOT to Log

### ✅ SHOULD LOG
- **Create/Update/Delete** operations (who did what)
- **Business validation failures** (why it was rejected)
- **Resource not found** (with the searched ID)
- **Unexpected errors** (with full stack trace)
- **Performance-critical operations** (time taken)

### ❌ SHOULD NOT LOG
- **Passwords** (even masked — don't log at all)
- **Full request body containing sensitive data**
- **Stack trace cho expected exception** (business validation)
- **Debug info trong production** (use WARN/INFO level)
- **Entity dump với relationships** (tránh lazy loading trigger)

---

## 6. Logging Best Practices

### 6.1 Structured Logging (Context)
Dùng MDC để thêm context đồng bộ:

```java
// Trong filter hoặc interceptor (khi có auth)
MDC.put("userId", authentication.getName());
MDC.put("requestId", request.getHeader("X-Request-Id"));

// Sau đó trong log
log.info("Creating allocation"); // Tự động có userId và requestId

// Clean up
MDC.clear();
```

### 6.2 Parameterized Logging (Performance)
Luôn dùng **parameterized logging** — không dùng string concatenation:

```java
// ✅ ĐÚNG — parameterized, không tính toán nếu level không enabled
log.info("Creating allocation for employee {} on project {}", empId, projId);

// ❌ SAI — concat luôn chạy, kể cả khi log level tắt
log.info("Creating allocation for employee " + empId + " on project " + projId);
```

### 6.3 Guarded Logging (for expensive computation)
```java
// Khi message cần compute tốn kém
if (log.isDebugEnabled()) {
    log.debug("Detailed analysis: {}", expensiveToString(someObject));
}
```

### 6.4 Audit Trail (Business Critical)
```java
// Các operation quan trọng cần log rõ:
log.info("ALLOCATION_CREATED | employeeId={} | projectId={} | percent={} | allocId={}",
    employeeId, projectId, percent, allocationId);

// Pattern: "ACTION | key=value | key=value"
// Dễ dàng grep khi cần audit
```

---

## 7. Business Operation Logging Pattern

```java
// Log mọi business operation với format nhất quán:
// [OPERATION] | param=value...

public AllocationResponse createAllocation(AllocationRequest request) {
    log.info("[CREATE_ALLOCATION] | employeeId={} | projectId={} | percent={}",
        request.getEmployeeId(), request.getProjectId(), request.getAllocationPercent());

    // ... business logic ...

    log.info("[CREATE_ALLOCATION_SUCCESS] | allocId={} | employeeId={} | projectId={}",
        saved.getId(), saved.getEmployee().getEmployeeId(), saved.getProject().getProjectId());

    return response;
}
```

---

## 8. application.yml Logging Config (Full)

```yaml
logging:
  level:
    root: INFO
    com.company.project: DEBUG
    com.company.project.controller: INFO   # controller chỉ INFO, tránh log quá nhiều
    com.company.project.service: DEBUG
    com.company.project.repository: TRACE
    org.springframework.web: INFO
    org.hibernate:
      SQL: DEBUG
      type.descriptor.sql.BasicBinder: TRACE
      
  pattern:
    console: "%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){cyan} %clr([%thread]){magenta} %clr(%-5level) %clr(%logger{36}){blue} - %msg%n"
```
