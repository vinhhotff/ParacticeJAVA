# Coding Standards

> **MỘT quy tắc duy nhất** cho toàn bộ project. Viết chuẩn senior, gọn, dễ maintain.

---

## 1. Naming Conventions (BẮT BUỘC)

| Element | Rule | Example |
|---------|------|---------|
| Class | PascalCase | `EmployeeService`, `AllocationController` |
| Interface | PascalCase | `EmployeeRepository`, `AllocationValidator` |
| Method | camelCase | `findByEmployeeCode()`, `calculateWorkload()` |
| Variable | camelCase | `employeeList`, `totalAllocation` |
| Constant | UPPER_SNAKE_CASE | `MAX_ALLOCATION_PERCENT` |
| Package | lowercase | `com.company.project.service` |
| Enum | PascalCase | `ProjectStatus.ACTIVE` |

> **Luật sắt**: Không `I` prefix, không `Impl` suffix cho interface name. Interface là `EmployeeRepository`, implementation là `EmployeeRepositoryImpl`.

---

## 2. SINGLE LOMBOK PATTERN (BẮT BUỘC)

Chỉ dùng **4 annotation** này, không thêm không bớt:

| Annotation | Áp dụng cho | Ghi chú |
|-----------|-------------|---------|
| `@RequiredArgsConstructor` | **MỌI CLASS có field `final`** | Controller, Service, Validator, Orchestrator |
| `@Getter` / `@Setter` | Entity, DTO | Riêng biệt, không gộp `@Data` |
| `@Builder` | DTO (Request + Response) | Kèm `@NoArgsConstructor @AllArgsConstructor` |
| `@Slf4j` | Controller, Service | Logger chuẩn |

### 2.1 Pattern cho từng loại class

```java
// ===== CONTROLLER =====
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {
    private final EmployeeService employeeService;
    // KHÔNG có @Autowired, KHÔNG có manual constructor
}

// ===== SERVICE =====
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
}

// ===== VALIDATOR =====
@Component
@RequiredArgsConstructor
public class MaxAllocationValidator implements AllocationValidator {
    private final AllocationRepository allocationRepository;
}

// ===== DTO REQUEST =====
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequest {
    @NotBlank(message = "Employee code is required")
    private String employeeCode;
    // ...
}

// ===== DTO RESPONSE =====
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {
    private Long id;
    private String employeeCode;
}

// ===== ENTITY =====
@Entity
@Table(name = "employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeId;
    // ...
}
```

### 2.2 ⚠️ CẤM tuyệt đối

```java
// ❌ CẤM — Field injection
@Autowired private EmployeeRepository employeeRepository;

// ❌ CẤM — @Data trên entity (gây circular + lazy loading lỗi)
@Data @Entity public class Employee { ... }

// ❌ CẤM — Manual constructor khi có @RequiredArgsConstructor
public EmployeeService(EmployeeRepository repo) { this.repo = repo; }
// → Chỉ cần: @RequiredArgsConstructor + private final EmployeeRepository repo;

// ❌ CẤM — Setter injection
@Autowired public void setRepo(EmployeeRepository repo) { ... }

// ❌ CẤM — record + @Builder (Lombok builder không support record)
public record EmployeeRequest(@NotBlank String name) {} // Nếu cần builder thì dùng class
```

---

## 3. Layer Dependency Rules

```
Controller (DTO)
     ↓
Service (DTO → Entity)
     ↓
Repository (Entity)
```

| Layer | Chỉ được gọi | Không được gọi |
|-------|-------------|----------------|
| Controller | Service | Repository, Entity |
| Service | Repository, Validator | Controller, HttpServletRequest |
| Repository | Entity (qua JPA) | Service, Controller |
| Validator | Repository (chỉ READ) | Service, Controller |

**Luồng data một chiều, không vòng tròn.**

---

## 4. Exception Handling (CHUẨN CHUNG)

Xem [EXCEPTION_HANDLING.md](./EXCEPTION_HANDLING.md). Quy tắc tóm gọn:

```
Service layer → throw CustomException (extends RuntimeException)
Controller → không catch gì cả
GlobalExceptionHandler → catch tất cả, trả ErrorResponse
```

**Không try-catch ở Controller. Không try-catch rồi làm ngơ.**

---

## 5. Method & Class Size

- **Method**: tối đa **20 dòng** — nếu dài hơn, tách method.
- **Class**: tối đa **300 dòng** — nếu dài hơn, tách class.
- **1 method = 1 responsibility** — đặt tên method đọc vào biết nó làm gì.
- **KHÔNG copy-paste code** — nếu lặp lại > 2 lần, extract ra method riêng.

---

## 6. Import Rules

```java
// ✅ ĐÚNG
import java.util.List;
import java.util.Optional;

// ❌ SAI
import java.util.*;
```

Import từng cái cụ thể, không wildcard. IDE tự optimize.

---

## 7. Package Structure

```
com.company.project/
├── controller/
├── service/
│   └── impl/
├── repository/
├── entity/
├── enums/
├── dto/
├── exception/
├── handler/
├── validator/
└── config/
```

**KHÔNG** tạo thêm package nếu không thực sự cần. Giữ flat structure.

---

## 8. Comment Rules

- **Comment WHY, không comment WHAT**.
- Code phải tự giải thích được WHAT.
- Chỉ comment khi: business decision, workaround, TODO.

```java
// ✅ TỐT
// Cần tolerance 1% vì PostgreSQL làm tròn số thập phân
private static final double ALLOCATION_TOLERANCE = 0.01;

// ❌ TỆ — code đã tự giải thích
employee.setName(name);  // Set employee name
```
