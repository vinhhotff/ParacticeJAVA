# Business Rules

> Đặc tả chi tiết business rules cho hệ thống quản lý phân bổ nhân sự.

---

## 1. Allocation Business Rules

### Rule 1: Allocation Percent Range

```
Mô tả:  Allocation percent phải nằm trong khoảng (0, 100]
Input:  allocationPercent (Integer)
Điều kiện:
  - allocationPercent > 0
  - allocationPercent <= 100
Output: Hợp lệ nếu thỏa cả 2 điều kiện
```

**Implementation**:

```java
// Dùng Jakarta Validation trên class
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationRequest {
    @Min(value = 1, message = "Allocation must be at least 1%")
    @Max(value = 100, message = "Allocation cannot exceed 100%")
    private Integer allocationPercent;
    // ...
}
```

| Input | Result |
|-------|--------|
| 0 | ❌ Reject: "Allocation must be at least 1%" |
| 50 | ✅ OK |
| 100 | ✅ OK |
| 150 | ❌ Reject: "Allocation cannot exceed 100%" |
| -10 | ❌ Reject: "Allocation must be at least 1%" |

---

### Rule 2: Total Allocation ≤ 100%

```
Mô tả:  Tổng allocation của một employee (bao gồm allocation hiện tại + mới)
        không được vượt quá 100%.
Input:  employeeId, newAllocationPercent
Process:
  1. Query tổng allocation hiện tại của employee (SUM)
  2. Nếu currentTotal + newAllocationPercent > 100 → REJECT
Output: Hợp lệ / Reject với message
```

**Flow**:
```
currentTotal = SELECT SUM(allocation_percent) FROM allocation WHERE employee_id = ?
     │
     ├── currentTotal + newPercent > 100 → ❌ throw AllocationExceededException
     │
     └── currentTotal + newPercent <= 100 → ✅ OK
```

**Ví dụ**:

| Current Allocations | New Request | Total | Result |
|-------------------|------------|-------|--------|
| A: 60% | B: 40% | 100% | ✅ Hợp lệ |
| A: 60% | B: 50% | 110% | ❌ Reject |
| A: 100% | B: 10% | 110% | ❌ Reject |
| A: 30%, B: 30% | C: 40% | 100% | ✅ Hợp lệ |

**Update Allocation** — Khi update, cần exclude allocation hiện tại:

```java
int currentTotal = allocationRepository
    .sumAllocationByEmployeeIdExcluding(employeeId, excludeAllocationId);
// hoặc: tính tổng, trừ đi allocationPercent của allocation đang update
```

---

### Rule 3: Cannot Allocate to Completed Project

```
Mô tả:  Không cho phép tạo allocation cho project có status = 'COMPLETED'
Input:  projectId
Process:
  1. Check project.status
  2. Nếu status == COMPLETED → REJECT
Output: Hợp lệ / Reject với message
```

```java
Project project = projectRepository.findById(projectId)
    .orElseThrow(() -> new ProjectNotFoundException(projectId));

if (ProjectStatus.COMPLETED == project.getStatus()) {
    throw new ProjectCompletedException(project.getProjectCode());
}
```

---

## 2. Validation Order

Khi create/update allocation, thực hiện validation theo thứ tự:

```
1. Input Validation (Jakarta Validation)
   ├── @NotNull: employeeId, projectId, allocationPercent, startDate
   ├── @NotBlank: roleInProject
   ├── @Min(1) @Max(100): allocationPercent
   └── @NotNull: startDate

2. Existence Validation
   ├── employeeId tồn tại trong DB? → không → EmployeeNotFoundException
   └── projectId tồn tại trong DB? → không → ProjectNotFoundException

3. Business Rule Validation
   ├── Project status COMPLETED? → ProjectCompletedException
   └── Total > 100%? → AllocationExceededException

4. Save
```

> **Tại sao theo thứ tự này?** — Validated input trước → kiểm tra tồn tại → business logic. Tránh lãng phí tài nguyên kiểm tra business logic trên dữ liệu không tồn tại.

---

## 3. Report Business Logic

### 3.1 Employee Utilization

```
Mục đích:   Hiển thị tổng allocation của từng nhân viên
Công thức:  totalAllocation = SUM(allocation_percent) GROUP BY employee_id
Filter:     Không — tất cả nhân viên
Sort:       totalAllocation DESC
```

**Response mẫu**:
```json
[
    { "employeeId": 1, "employeeCode": "EMP001", "fullName": "Tuan Ho Anh", "totalAllocation": 100 },
    { "employeeId": 2, "employeeCode": "EMP002", "fullName": "Nguyen Van B", "totalAllocation": 80 },
    { "employeeId": 3, "employeeCode": "EMP003", "fullName": "Le Thi C", "totalAllocation": 40 }
]
```

### 3.2 Available Resources

```
Mục đích:   Tìm nhân viên còn thời gian khả dụng
Công thức:  available = 100 - SUM(allocation_percent) GROUP BY employee_id
Điều kiện:  available > 0
Sort:       available DESC
```

**Response mẫu**:
```json
[
    { "employeeId": 3, "employeeCode": "EMP003", "fullName": "Le Thi C", "availablePercent": 60 },
    { "employeeId": 2, "employeeCode": "EMP002", "fullName": "Nguyen Van B", "availablePercent": 20 }
]
```

### 3.3 Overloaded Employees

```
Mục đích:   Tìm nhân viên có workload cao
Công thức:  totalAllocation = SUM(allocation_percent) GROUP BY employee_id
Điều kiện:  totalAllocation > 90
Sort:       totalAllocation DESC
```

**Response mẫu**:
```json
[
    { "employeeId": 1, "employeeCode": "EMP001", "fullName": "Tuan Ho Anh", "totalAllocation": 100 },
    { "employeeId": 4, "employeeCode": "EMP004", "fullName": "Tran Van D", "totalAllocation": 95 }
]
```

### 3.4 Employee Workload

```
Mục đích:   Xem workload cụ thể của một nhân viên
Công thức:  totalAllocation = SUM(allocation_percent) WHERE employee_id = ?
            available = 100 - totalAllocation
Endpoint:   GET /api/employees/{id}/workload
```

**Response mẫu**:
```json
{
    "employeeId": 1,
    "employeeName": "Tuan Ho Anh",
    "totalAllocation": 80,
    "available": 20
}
```

---

## 4. Data Integrity Rules

### 4.1 Cascade Behavior
- Xóa employee → Xóa tất cả allocation liên quan (CASCADE).
- Xóa project → Xóa tất cả allocation liên quan (CASCADE).
- Xóa allocation → Chỉ xóa bản ghi đó — không ảnh hưởng employee/project.

```sql
ALTER TABLE allocation
ADD CONSTRAINT fk_allocation_employee
FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
ON DELETE CASCADE;

ALTER TABLE allocation
ADD CONSTRAINT fk_allocation_project
FOREIGN KEY (project_id) REFERENCES project(project_id)
ON DELETE CASCADE;
```

### 4.2 Unique Constraints
- `employee.employee_code`: UNIQUE
- `employee.email`: UNIQUE
- `project.project_code`: UNIQUE
- `allocation`: Không có UNIQUE cho cặp (employee_id, project_id) — vì một employee có thể allocate vào project với nhiều role khác nhau trong các giai đoạn khác nhau.

### 4.3 Date Overlap (Future Enhancement)
Khi cần, thêm business rule: không cho phép allocation overlap date range trên cùng một employee.

---

## 5. Error Messages Reference

| Scenario | HTTP Status | Message |
|----------|-------------|---------|
| Allocation > 100% | 400 | `Employee allocation exceeds 100%` |
| Project COMPLETED | 400 | `Cannot allocate to completed project: {code}` |
| Employee not found | 404 | `Employee not found with id: {id}` |
| Project not found | 404 | `Project not found with id: {id}` |
| Allocation not found | 404 | `Allocation not found with id: {id}` |
| Employee code duplicate | 409 | `Employee code already exists` |
| Email duplicate | 409 | `Email already exists` |
| Project code duplicate | 409 | `Project code already exists` |

---

## 6. Business Edge Cases

| Tình huống | Expected Behavior |
|-----------|------------------|
| Allocation = 100%, không còn dự án nào | Hợp lệ — nhân viên full-time 1 dự án |
| Allocation = 100%, sau đó allocation mới | Reject — AllocationExceededException |
| Update allocation lên 100% (đang ở 60%) | Valid nếu tổng <= 100% |
| Delete allocation | Không cần check rule — chỉ xóa |
| Multiple allocations cùng % | Hợp lệ — chỉ cần tổng <= 100% |
| Employee 0 allocation | Hợp lệ — available = 100% |
