# Architecture Rules

> Kiến trúc 4-layer chuẩn cho Spring Boot project.

---

## 1. Layered Architecture

```
┌──────────────────────────────────────┐
│  Presentation Layer  (Controller)    │ DTO vào/ra
├──────────────────────────────────────┤
│  Business Layer      (Service)       │ Business logic + @Transactional
├──────────────────────────────────────┤
│  Persistence Layer   (Repository)    │ JPA queries
├──────────────────────────────────────┤
│  Domain Layer        (Entity)        │ JPA mapping
└──────────────────────────────────────┘
```

### 1.1 Controller Layer
- **Chỉ**: nhận request → gọi service → trả response.
- **Không**: business logic, gọi repository, thao tác entity.
- **Validation**: `@RequestBody @Valid` + Jakarta Validation.
- **Response**: luôn DTO, không entity.

```java
@PostMapping
public ResponseEntity<EmployeeResponse> create(@RequestBody @Valid EmployeeRequest request) {
    EmployeeResponse response = employeeService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

### 1.2 Service Layer
- **Chứa**: business logic, orchestration, transaction.
- **Pattern**: Interface + Impl (Impl trong `impl/` package).
- **KHÔNG** tự gọi chính mình (circular), KHÔNG gọi controller.
- **@Transactional** trên method, không trên class.

```java
public interface EmployeeService {
    EmployeeResponse create(EmployeeRequest request);
    EmployeeResponse findById(Long id);
    List<EmployeeResponse> findAll();
    WorkloadResponse getWorkload(Long id);
}
```

### 1.3 Repository Layer
- Kế thừa `JpaRepository<T, Long>`.
- Chỉ chứa query methods. KHÔNG chứa business logic.

### 1.4 Entity Layer
- `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder`.
- **Không** `@Data`.
- `@OneToMany` dùng LAZY — không EAGER.
- `@ManyToOne` mặc định LAZY cũng được, Spring Data JPA handle.

---

## 2. DTO Pattern (BẮT BUỘC)

**1 class = 1 annotation set duy nhất**:

```java
// === REQUEST DTO ===
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequest {
    @NotBlank(message = "Employee code is required")
    private String employeeCode;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Role is required")
    private String role;

    @NotBlank(message = "Department is required")
    private String department;
}

// === RESPONSE DTO ===
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {
    private Long id;
    private String employeeCode;
    private String fullName;
    private String email;
    private String role;
    private String department;
}
```

### Mapping DTO ↔ Entity

```java
// Trong Service Impl — map thủ công, không thư viện
private EmployeeResponse toResponse(Employee entity) {
    return EmployeeResponse.builder()
        .id(entity.getEmployeeId())
        .employeeCode(entity.getEmployeeCode())
        .fullName(entity.getFullName())
        .email(entity.getEmail())
        .role(entity.getRole())
        .department(entity.getDepartment())
        .build();
}
```

> **KHÔNG** dùng ModelMapper, MapStruct, Dozer, Orika. Mapping thủ công với Builder là đủ.

---

## 3. Validator Pattern

```java
@FunctionalInterface
public interface AllocationValidator {
    void validate(AllocationRequest request, Long excludeAllocationId);
}

@Component
@RequiredArgsConstructor
public class MaxAllocationValidator implements AllocationValidator {
    private final AllocationRepository allocationRepository;
    private final AllocationProperties allocationProperties;

    @Override
    public void validate(AllocationRequest request, Long excludeAllocationId) {
        int currentTotal = excludeAllocationId != null
            ? allocationRepository.sumAllocationByEmployeeIdExcluding(request.getEmployeeId(), excludeAllocationId)
            : allocationRepository.sumAllocationByEmployeeId(request.getEmployeeId());
        if (currentTotal + request.getAllocationPercent() > allocationProperties.getMaxPercent()) {
            throw new AllocationExceededException(currentTotal, request.getAllocationPercent());
        }
    }
}

@Component
@RequiredArgsConstructor
public class ProjectStatusValidator implements AllocationValidator {
    private final ProjectRepository projectRepository;

    @Override
    public void validate(AllocationRequest request, Long excludeAllocationId) {
        Project project = projectRepository.findById(request.getProjectId())
            .orElseThrow(() -> new ProjectNotFoundException(request.getProjectId()));
        if (project.getStatus() == ProjectStatus.COMPLETED) {
            throw new ProjectCompletedException(project.getProjectCode());
        }
    }
}

@Service
@RequiredArgsConstructor
public class AllocationValidationOrchestrator {
    private final List<AllocationValidator> validators;

    public void validate(AllocationRequest request, Long excludeAllocationId) {
        validators.forEach(v -> v.validate(request, excludeAllocationId));
    }
}
```

**Cách validator hoạt động**: Mỗi `@Component` implements `AllocationValidator`, Spring auto-inject tất cả vào `List<AllocationValidator>` trong orchestrator. Muốn thêm validator mới: chỉ cần tạo class mới, không sửa code cũ. Đây là OCP.

---

## 4. Exception Handling

Xem chi tiết [EXCEPTION_HANDLING.md](./EXCEPTION_HANDLING.md).

Tóm gọn:
```
Service throw → GlobalExceptionHandler catch → ErrorResponse JSON
Controller: NEVER catch
```

---

## 5. Transaction Management

```java
@Service
@RequiredArgsConstructor
public class AllocationServiceImpl implements AllocationService {

    @Override
    @Transactional
    public AllocationResponse createAllocation(AllocationRequest request) {
        // tất cả repository calls trong 1 transaction
    }
}
```

- Đặt `@Transactional` trên **method của Service impl**.
- **KHÔNG** đặt trên Controller.
- **KHÔNG** đặt trên Repository (JpaRepository đã có sẵn).

---

## 6. Component Diagram

```
AllocationController → AllocationService → AllocationRepository
                              ↓
                    AllocationValidationOrchestrator
                              ↓
                    ┌──── MaxAllocationValidator
                    └──── ProjectStatusValidator
```

---

## 7. Design Decisions

| Decision | Choice | Lý do |
|----------|--------|-------|
| Injection | `@RequiredArgsConstructor` | Immutable, testable, no boilerplate |
| DTO Mapping | Thủ công với Builder | 0 dependency, dễ debug, type-safe |
| Entity annotation | `@Getter @Setter @Builder @NoArgs @AllArgs` | Đủ, không thừa |
| Validation | Jakarta Validation + Strategy pattern | Input validation tự động, business rule linh hoạt |
| Exception | Custom hierarchy + GlobalHandler | Response format đồng nhất |
