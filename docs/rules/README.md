# Resource Allocation Management System — Rule Documents

> Bộ quy chuẩn phát triển cho hệ thống quản lý phân bổ nhân sự.
> Mục tiêu: Clean Code, SOLID, chuẩn doanh nghiệp, dễ maintain và scale.

---

## 📋 Danh sách tài liệu

| # | File | Mô tả | Đối tượng |
|---|------|-------|-----------|
| 1 | [CODING_STANDARDS.md](./CODING_STANDARDS.md) | Chuẩn coding, OOP, SOLID, Clean Code | Tất cả developer |
| 2 | [ARCHITECTURE.md](./ARCHITECTURE.md) | Kiến trúc layered, component diagram | Senior/Architect |
| 3 | [DATABASE_DESIGN.md](./DATABASE_DESIGN.md) | Thiết kế DB, ERD, JPA mapping | Backend, DBA |
| 4 | [API_DESIGN.md](./API_DESIGN.md) | REST API conventions, request/response | Full-stack |
| 5 | [EXCEPTION_HANDLING.md](./EXCEPTION_HANDLING.md) | Exception hierarchy, GlobalHandler | Backend |
| 6 | [BUSINESS_RULES.md](./BUSINESS_RULES.md) | Đặc tả business rules, validation | BA, Developer |
| 7 | [LOGGING.md](./LOGGING.md) | Chuẩn logging, audit trail | Tất cả developer |
| 8 | [PROJECT_STRUCTURE.md](./PROJECT_STRUCTURE.md) | Cấu trúc project, setup, build | Tất cả |
| 9 | [UNIT_TEST.md](./UNIT_TEST.md) | Testing standards, coverage | QA, Backend |
| 10 | [DOCKER_DEPLOYMENT.md](./DOCKER_DEPLOYMENT.md) | Docker, deployment, production | DevOps, Backend |
| 11 | [AI_INTEGRATION.md](./AI_INTEGRATION.md) | AI Bonus features | Backend (bonus) |

---

## 📊 Layer Mapping

| Layer | Rules | Key Patterns |
|-------|-------|-------------|
| **Controller** | API_DESIGN, CODING_STANDARDS | DTO in/out, @Valid, 201/200/400/404 |
| **Service** | BUSINESS_RULES, EXCEPTION_HANDLING, LOGGING | Interface + Impl, @Transactional |
| **Repository** | DATABASE_DESIGN | JpaRepository, @Query, LAZY |
| **Entity** | DATABASE_DESIGN | @Getter @Setter, IDENTITY |
| **Validator** | ARCHITECTURE, BUSINESS_RULES | Strategy pattern, interface |
| **Exception** | EXCEPTION_HANDLING | Custom hierarchy, GlobalHandler |

---

## 🚀 Quy trình phát triển đề xuất

```
1. Đọc BUSINESS_RULES.md → Hiểu nghiệp vụ
2. Đọc DATABASE_DESIGN.md → Tạo SQL schema
3. Đọc ARCHITECTURE.md + CODING_STANDARDS.md → Setup project structure
4. Code Entity → Repository → Service → Controller (theo layer)
5. Đọc EXCEPTION_HANDLING.md → Custom exceptions + handler
6. Đọc API_DESIGN.md → REST endpoints + validation
7. Đọc LOGGING.md → Thêm logging
8. Đọc UNIT_TEST.md → Viết test
9. Đọc DOCKER_DEPLOYMENT.md → Containerize
```

---

## 🔗 Relation giữa các tài liệu

```
CODING_STANDARDS (nền tảng chung)
        │
        ▼
ARCHITECTURE (cấu trúc tổng thể)
        │
        ├──► DATABASE_DESIGN (Entity ↔ DB)
        ├──► API_DESIGN (Controller ↔ HTTP)
        ├──► EXCEPTION_HANDLING (Exception ↔ Response)
        ├──► BUSINESS_RULES (Service logic)
        ├──► LOGGING (Xuyên suốt)
        │
        ▼
PROJECT_STRUCTURE (triển khai cụ thể)
        │
        ├──► UNIT_TEST (kiểm thử)
        ├──► DOCKER_DEPLOYMENT (deploy)
        └──► AI_INTEGRATION (bonus)
```

---

> **Nguyên tắc vàng**: Code như thể người bảo trì tiếp theo là một kẻ tâm thần biết địa chỉ nhà bạn.
> *Clean Code luôn quan trọng hơn code "chạy được".*
