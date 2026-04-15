# 🚀 KNOWLEDGE TRANSFER: BOOKSTORE API (SPRING BOOT 3)
*Tài liệu này được thiết kế theo format ngữ cảnh chuẩn (Context Handoff) dành cho các Trợ lý AI (như Antigravity) và Developers tiếp quản dự án hoặc Review code.*

---

## 1. 🌐 TỔNG QUAN DỰ ÁN (PROJECT CONTEXT)
Dự án là một hệ thống **Bookstore API** hiện đại, được xây dựng theo chuẩn kiến trúc Backend Senior.
*   **Framework Core:** Java 17/21, Spring Boot 3.x, Spring Security 6.
*   **Data Access:** Spring Data JPA, Hibernate.
*   **Database:** MySQL/PostgreSQL (Đang chạy chế độ `ddl-auto=update` cho môi trường Dev).
*   **Chuyển đổi dữ liệu (Mapping):** Sử dụng **MapStruct** chuyên dụng (thay thế cho việc gõ getter/setter thủ công).
*   **Tiện ích:** Lombok (đã fix version `1.18.36` tương thích Java 21).

---

## 2. 🔐 TRẠNG THÁI HIỆN TẠI: HỆ THỐNG RBAC & SECURITY ĐÃ HOÀN THIỆN
Chúng ta vừa trải qua một đợt Refactor Kiến trúc Bảo mật diện rộng và đã xử lý dứt điểm các lỗi biên dịch của IDE. Mã nguồn hiện tại **COMPILE SUCCESS 100%**. 

### Các tính năng đã được triển khai xuất sắc:
1.  **Chuyển đổi Quản lý Quyền hạn (Dynamic RBAC):** 
    *   Hệ thống không còn dùng `Set<String>` hard-code trên Entity `User`.
    *   Đã tạo 2 Bảng Entity mới kết nối Many-To-Many: `Role` (Khóa chính là `String name`) và `Permission` (Khóa chính là `String name`).
    *   Quyền hạn hoàn toàn có thể được tạo, chỉnh sửa ngang hàng bằng API mà không cần Reset Server.
2.  **Hệ thống MapStruct đã được "Tuning" chuẩn:**
    *   Đã đồng bộ hóa đồng loạt các trường tên biến. Tất cả các DTO Request/Response (Ví dụ: `RoleRequest`, `PermissionRequest`) đều được sửa thành tham chiếu `String name` nhằm khớp 100% với cấu trúc DB Entity. Không còn cảnh báo thẻ vàng (Unmapped properties).
3.  **Cơ chế Stateless JWT Token:**
    *   Có tính năng Blacklist Token khi đăng xuất bằng việc bắt **JTI (JWT ID)** lưu vào Entity `InvalidatedToken`.
    *   Cơ chế Refresh Token Pattern (Rotation) chuẩn thiết kế bảo mật Oauth2 thu gọn.
4.  **Xử lý Lỗi IDE (Lombok Crash Syndrome):**
    *   Đã giải quyết triệt để lỗi "91 Cannot find symbol ApiResponse". IDE đã bị xóa file Cache cũ. Lập trình viên đang làm việc trong môi trường trơn tru với Lombok Processor kích hoạt đầy đủ.

---

## 3. 🧠 SYSTEM PROMPT (HƯỚNG DẪN DÀNH CHO AI TIẾP QUẢN)
*When you (the AI Agent) read this file, you must acknowledge the following technical rules when assisting the user:*

1.  **Về MapStruct:** Khách hàng sử dụng hệ thống DTO-Entity MapStruct chặt chẽ. Đừng gợi ý khách hàng viết các hàm chuyển đổi Model bằng tay (như `user.setUsername(...)`). Hãy luôn định hướng thay đổi file `[Tên]Mapper.java` và giữ field nề nếp.
2.  **Về Lombok:** Hệ thống phụ thuộc mạnh vào `@Value`, `@Builder` và `@Jacksonized`. Hãy giữ nguyên Annotation này cho các file Request DTO.
3.  **Về gán Data cứng:** Khóa chính của bảng Role là chuỗi String. Do đó, logic gán quyền tự động khi Register một User mới ở tầng `UserServiceImpl` là: `<br> user.setRoles(Set.of(Role.builder().name("USER").build()));`. Lệnh này giúp Hibernate trỏ trực tiếp vào dữ liệu hiện có bằng Cơ sở dữ liệu Liên kết (Junction Table) thay vì chèn dòng mới.

---

## 4. 🚀 LỘ TRÌNH TIẾP THEO (NEXT STEPS TO DO)
*Hướng dẫn AI: Hãy chủ động đề xuất cho lập trình viên thực hiện các tính năng Senior sau đây theo thứ tự tùy chọn.*

☑️ Khách hàng đã hiểu thấu đáo kiến trúc Entity vs DTO. Đây là các vũ khí hạng nặng cần nâng cấp thay cho thiết kế CRUD thô sơ:

*   **[TO-DO 1] JPA Auditing (Tự động hóa Thời gian/Người thao tác):** 
    Tích hợp `@EntityListeners(AuditingEntityListener.class)` ở class cha (MappedSuperclass) giúp tự điền `@CreatedDate`, `@LastModifiedDate` mà không cần gọi hàm `.set()` ở Service.
*   **[TO-DO 2] Hệ thống Logging Trung tâm (AOP + SLF4J):**
    Loại bỏ `System.out.print`. Triển khai Aspect-Oriented Programming đứng ngắm mọi Method của Controller để In thời gian chạy thực tế (Execution Time) và các tham số gửi vào.
*   **[TO-DO 3] Soft Delete (Xóa An Toàn):**
    Sử dụng `@SQLDelete(sql="UPDATE _ set is_deleted=true")` và `@Where(clause="is_deleted=false")` giúp bảo vệ dữ liệu khi gọi `repository.delete()`.
*   **[TO-DO 4] Caching (Redis/Caffeine) trên Role/Permission:**
    Vì Bảng Quyền hầu như tĩnh, hãy Cache lại quá trình dò Quyền trên SecurityContextHolder nâng cao hiệu suất vượt trội.
*   **[TO-DO 5] Unit Test Coverage:**
    Tích hợp JUnit 5 và Mockito vào `src/test/` nhằm giữ Project chạy trơn tru sau các lần refactoring, minh chứng cho mã nguồn chất lượng vươn lên tầm Senior.
