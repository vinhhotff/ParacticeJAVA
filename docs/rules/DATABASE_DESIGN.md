# Database Design Rules

> Chuẩn hóa database design cho hệ thống quản lý phân bổ nhân sự.

---

## 1. Naming Conventions

| Object | Convention | Example |
|--------|-----------|---------|
| Table | `snake_case`, số nhiều | `employees`, `projects`, `allocations` |
| Column | `snake_case` | `employee_code`, `allocation_percent` |
| PK | `{table}_id` | `employee_id` |
| FK | `{referenced_table}_id` | `employee_id` (trong allocation) |
| Index | `idx_{table}_{column}` | `idx_employee_department` |
| Unique Constraint | `uq_{table}_{column}` | `uq_employee_code` |
| Sequence | `{table}_seq` | `employee_seq` |

> **Quy tắc**: Tất cả tên đều viết thường, dùng underscore. PostgreSQL tự động lowercase khi không có quote.

---

## 2. Entity Relationship Diagram (ERD)

```
┌─────────────────┐       ┌──────────────────┐       ┌─────────────────┐
│    employee     │       │   allocation     │       │    project      │
├─────────────────┤       ├──────────────────┤       ├─────────────────┤
│ PK employee_id  │◀──────│ FK employee_id   │──────▶│ PK project_id   │
│ employee_code   │       │ FK project_id    │       │ project_code    │
│ full_name       │       │ allocation_percent│      │ project_name    │
│ email           │       │ role_in_project  │       │ customer        │
│ role            │       │ start_date       │       │ status          │
│ department      │       │ end_date         │       │ start_date      │
└─────────────────┘       └──────────────────┘       │ end_date        │
                                                      └─────────────────┘
```

### 2.1 Relationships
- **Employee 1:N Allocation**: Một nhân viên có nhiều allocation.
- **Project 1:N Allocation**: Một dự án có nhiều allocation.
- **Allocation N:1 Employee**: Mỗi allocation thuộc về một nhân viên.
- **Allocation N:1 Project**: Mỗi allocation thuộc về một dự án.

---

## 3. Table Specifications

### 3.1 `employee`

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| `employee_id` | `BIGSERIAL` | `PK` | Auto-increment |
| `employee_code` | `VARCHAR(20)` | `UNIQUE NOT NULL` | Mã nhân viên |
| `full_name` | `VARCHAR(100)` | `NOT NULL` | Họ tên |
| `email` | `VARCHAR(100)` | `UNIQUE NOT NULL` | Email |
| `role` | `VARCHAR(50)` | `NOT NULL` | Vai trò (Senior Developer, ...) |
| `department` | `VARCHAR(50)` | `NOT NULL` | Phòng ban (FSOFT-Q1, ...) |

**Indexes**:
```sql
CREATE INDEX idx_employee_department ON employee(department);
CREATE INDEX idx_employee_role ON employee(role);
```

### 3.2 `project`

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| `project_id` | `BIGSERIAL` | `PK` | Auto-increment |
| `project_code` | `VARCHAR(20)` | `UNIQUE NOT NULL` | Mã dự án |
| `project_name` | `VARCHAR(200)` | `NOT NULL` | Tên dự án |
| `customer` | `VARCHAR(100)` | | Khách hàng |
| `status` | `VARCHAR(20)` | `NOT NULL DEFAULT 'PLANNING'` | `PLANNING`, `ACTIVE`, `COMPLETED` |
| `start_date` | `DATE` | | Ngày bắt đầu |
| `end_date` | `DATE` | | Ngày kết thúc |

**Status Constraint**:
```sql
CHECK (status IN ('PLANNING', 'ACTIVE', 'COMPLETED'))
```

### 3.3 `allocation`

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| `allocation_id` | `BIGSERIAL` | `PK` | Auto-increment |
| `employee_id` | `BIGINT` | `FK → employee(employee_id) NOT NULL` | |
| `project_id` | `BIGINT` | `FK → project(project_id) NOT NULL` | |
| `allocation_percent` | `INTEGER` | `CHECK (1 <= percent <= 100)` | |
| `role_in_project` | `VARCHAR(100)` | `NOT NULL` | |
| `start_date` | `DATE` | `NOT NULL` | |
| `end_date` | `DATE` | | Có thể null (chưa xác định) |

**Indexes**:
```sql
CREATE INDEX idx_allocation_employee ON allocation(employee_id);
CREATE INDEX idx_allocation_project ON allocation(project_id);
CREATE INDEX idx_allocation_dates ON allocation(start_date, end_date);
```

---

## 4. SQL Script Standards

### 4.1 File Structure
```
sql/
├── 01-create-schema.sql       # CREATE TABLE statements
├── 02-seed-data.sql           # Sample data
└── 03-report-queries.sql      # Report queries
```

### 4.2 Script Template

```sql
-- =============================================
-- Table: employee
-- Description: Store employee information
-- =============================================
CREATE TABLE employee (
    employee_id   BIGSERIAL    PRIMARY KEY,
    employee_code VARCHAR(20)  UNIQUE NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(100) UNIQUE NOT NULL,
    role          VARCHAR(50)  NOT NULL,
    department    VARCHAR(50)  NOT NULL
);

COMMENT ON TABLE  employee        IS 'Employee information';
COMMENT ON COLUMN employee.email  IS 'Company email address';
COMMENT ON COLUMN employee.role   IS 'Job title / role';
```

### 4.3 CREATE TABLE Rules
1. **Luôn** có `PRIMARY KEY` với `BIGSERIAL`.
2. **Luôn** có `UNIQUE` cho business code.
3. **Luôn** có `NOT NULL` cho field bắt buộc.
4. **Luôn** có `COMMENT ON` cho table và column quan trọng.
5. **ForeignKey** khai báo rõ ràng với `REFERENCES`.
6. **CHECK constraint** cho business rule đơn giản.
7. **DROP IF EXISTS** cho idempotent script:
```sql
DROP TABLE IF EXISTS allocation CASCADE;
```

---

## 5. Key Queries

### 5.1 Employee Utilization
```sql
SELECT
    e.employee_code,
    e.full_name,
    COALESCE(SUM(a.allocation_percent), 0) AS total_allocation
FROM employee e
LEFT JOIN allocation a ON e.employee_id = a.employee_id
GROUP BY e.employee_id, e.employee_code, e.full_name
ORDER BY total_allocation DESC;
```

### 5.2 Available Resources
```sql
SELECT
    e.employee_code,
    e.full_name,
    e.role,
    100 - COALESCE(SUM(a.allocation_percent), 0) AS available_percent
FROM employee e
LEFT JOIN allocation a ON e.employee_id = a.employee_id
GROUP BY e.employee_id, e.employee_code, e.full_name, e.role
HAVING 100 - COALESCE(SUM(a.allocation_percent), 0) > 0
ORDER BY available_percent DESC;
```

### 5.3 Overloaded Employees
```sql
SELECT
    e.employee_code,
    e.full_name,
    COALESCE(SUM(a.allocation_percent), 0) AS total_allocation
FROM employee e
LEFT JOIN allocation a ON e.employee_id = a.employee_id
GROUP BY e.employee_id, e.employee_code, e.full_name
HAVING COALESCE(SUM(a.allocation_percent), 0) > 90
ORDER BY total_allocation DESC;
```

### 5.4 Overlap Check (Validation Query)
```sql
-- Kiểm tra allocation overlap cho một employee
SELECT COUNT(*) AS conflict_count
FROM allocation
WHERE employee_id = :employeeId
  AND project_id != :excludeProjectId  -- khi update, exclude chính nó
  AND (start_date, end_date) OVERLAPS (:newStartDate, :newEndDate);
```

---

## 6. JPA Entity Mapping Rules

### 6.1 Entity Class Pattern

```java
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
    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "employee_code", nullable = false, unique = true, length = 20)
    private String employeeCode;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // relationships
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Allocation> allocations = new ArrayList<>();
}
```

### 6.2 JPA Entity Rules (BẮT BUỘC)

**Annotation set duy nhất cho Entity**:
```java
@Entity
@Table(name = "employee")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Employee { ... }
```

**Luật**:
1. **`@Table(name = "{table_name}")`** — mapping rõ ràng, không mặc định.
2. **`@Column(name = "{column_name}")`** — mapping rõ ràng từng field.
3. **`GenerationType.IDENTITY`** — khớp với `BIGSERIAL` của PostgreSQL.
4. **LAZY loading mặc định** — chỉ dùng EAGER khi performance đo đạc chứng minh cần.
5. **KHÔNG `@Data`** — gây circular reference với `@OneToMany`.
6. **KHÔNG `@EqualsAndHashCode`** — tự sinh gây lỗi lazy loading.
7. **Collection field** luôn khởi tạo: `private List<Allocation> allocations = new ArrayList<>();`.

---

## 7. Entity ↔ DTO Mapping Example

```java
// Entity → DTO
public EmployeeResponse toResponse(Employee entity) {
    return EmployeeResponse.builder()
        .id(entity.getEmployeeId())
        .code(entity.getEmployeeCode())
        .fullName(entity.getFullName())
        .email(entity.getEmail())
        .role(entity.getRole())
        .department(entity.getDepartment())
        .build();
}

// DTO → Entity (for creation)
public Employee toEntity(EmployeeRequest request) {
    return Employee.builder()
        .employeeCode(request.getEmployeeCode())
        .fullName(request.getFullName())
        .email(request.getEmail())
        .role(request.getRole())
        .department(request.getDepartment())
        .build();
}
```

> **Note**: Luôn dùng mapping thủ công với Builder pattern — đơn giản, dễ debug, không thêm dependency.
