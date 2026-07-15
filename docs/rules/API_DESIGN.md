# API Design Rules

> REST API conventions cho Resource Allocation System.

---

## 1. Endpoints

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| `POST` | `/api/employees` | Create employee |
| `GET` | `/api/employees` | List employees |
| `GET` | `/api/employees/{id}` | Get employee |
| `GET` | `/api/employees/{id}/workload` | Get workload |
| `POST` | `/api/projects` | Create project |
| `GET` | `/api/projects` | List projects |
| `GET` | `/api/projects/{id}` | Get project |
| `POST` | `/api/allocations` | Create allocation |
| `PUT` | `/api/allocations/{id}` | Update allocation |
| `DELETE` | `/api/allocations/{id}` | Remove allocation |
| `GET` | `/api/reports/utilization` | Utilization report |
| `GET` | `/api/reports/available-resources` | Available resources |
| `GET` | `/api/reports/overloaded` | Overloaded employees |

### Quy tắc URL
- Prefix `/api/` — phân biệt với static resource.
- **Số nhiều**: `/employees`, không `/employee`.
- **Danh từ**: `/allocations`, không `/createAllocation`.
- **Lồng resource**: `/employees/{id}/workload`.
- **kebab-case**: `available-resources`, không `availableResources`.

---

## 2. HTTP Status Codes

| Scenario | Code |
|----------|------|
| Create thành công | `201 Created` |
| GET/PUT thành công | `200 OK` |
| DELETE thành công | `204 No Content` |
| Input/Business error | `400 Bad Request` |
| Not found | `404 Not Found` |
| Duplicate | `409 Conflict` |
| Server error | `500 Internal Server Error` |

---

## 3. Request/Response Mẫu

### POST /api/employees
```json
// REQUEST
{
    "employeeCode": "EMP001",
    "fullName": "Tuan Ho Anh",
    "email": "tuanha@company.com",
    "role": "Senior Developer",
    "department": "FSOFT-Q1"
}

// RESPONSE 201
{
    "id": 1,
    "employeeCode": "EMP001",
    "fullName": "Tuan Ho Anh",
    "email": "tuanha@company.com",
    "role": "Senior Developer",
    "department": "FSOFT-Q1"
}
```

### GET /api/employees/{id}/workload
```json
// RESPONSE 200
{
    "employeeId": 1,
    "employeeName": "Tuan Ho Anh",
    "totalAllocation": 80,
    "available": 20
}
```

### Error Response (xem [EXCEPTION_HANDLING.md](./EXCEPTION_HANDLING.md))
```json
// 400
{ "status": 400, "error": "Bad Request", "message": "Employee allocation exceeds 100%", "timestamp": "2026-07-15T10:30:00Z" }

// 404
{ "status": 404, "error": "Not Found", "message": "Employee not found with id: 999", "timestamp": "2026-07-15T10:30:00Z" }

// 400 — validation
{
    "status": 400, "error": "Validation Failed", "message": "Input validation failed",
    "timestamp": "2026-07-15T10:30:00Z",
    "details": [{ "field": "email", "message": "Email should be valid" }]
}
```

---

## 4. Controller Pattern (BẮT BUỘC)

```java
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
```

**Controller KHÔNG được**:
- ❌ Gọi repository trực tiếp.
- ❌ Chứa if/else business logic.
- ❌ Catch exception.
- ❌ Field injection (`@Autowired`).

---

## 5. Validation Annotations

| Annotation | Dùng cho | Message |
|-----------|---------|---------|
| `@NotBlank` | String | `"{field} is required"` |
| `@NotNull` | Number/Object | `"{field} is required"` |
| `@Email` | `email` | `"Email should be valid"` |
| `@Min(1)` | `allocationPercent` | `"Allocation must be at least 1%"` |
| `@Max(100)` | `allocationPercent` | `"Allocation cannot exceed 100%"` |
| `@Positive` | ID fields | `"ID must be positive"` |
| `@NotNull` | `startDate` | `"Start date is required"` |

### Custom Validation (chỉ khi cần)

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AllocationDateValidator.class)
public @interface ValidAllocationDates {
    String message() default "End date must be after start date";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

---

## 6. Swagger/OpenAPI Config

```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI api() {
        return new OpenAPI()
            .info(new Info()
                .title("Resource Allocation API")
                .version("1.0")
                .description("API for managing employee-project allocation"));
    }
}
```

> Config tối thiểu, không thêm security scheme nếu chưa có auth.

---

## 7. Postman Collection

Cấu trúc:
```
Resource Allocation API
├── Employees (POST, GET, GET/{id})
├── Projects (POST, GET, GET/{id})
├── Allocations (POST, PUT/{id}, DELETE/{id})
├── Reports (utilization, available-resources, overloaded)
└── Workload (GET employee/{id}/workload)
```

Variables: `{{baseUrl}}` = `http://localhost:8080/api`.
