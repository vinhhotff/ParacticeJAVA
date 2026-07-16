# RAMS - Backend Service Documentation
## Công nghệ: Java 17, Spring Boot 3.3.5, Hibernate, PostgreSQL + pgvector, Flyway, Maven

Hệ thống quản lý phân bổ nhân sự (Backend Service) cung cấp toàn bộ các REST APIs cho các hoạt động quản lý thực thể nhân sự, dự án, phân bổ và tích hợp trí tuệ nhân tạo (Semantic Search & Gemini Risk Analysis).

---

## 🏗️ Kiến trúc & Thiết kế hệ thống

Backend được xây dựng theo kiến trúc phân lớp (Layered Architecture):
1.  **Controller Layer**: Định nghĩa các API endpoints phục vụ Client, tích hợp Swagger API docs tự động.
2.  **Service Layer**: Xử lý logic nghiệp vụ phức tạp, tương tác với AI clients và tích hợp Transaction.
3.  **Repository Layer**: Thực thi các truy vấn CSDL PostgreSQL và các truy vấn vector so sánh tương đồng ngữ nghĩa.
4.  **Entity / DTO Layer**: Biểu diễn cấu trúc dữ liệu quan hệ (Hibernate) và các lớp vận chuyển dữ liệu an toàn.

---

## 🛡️ Business Rules & Validators (Strategy Pattern)
Để đảm bảo code sạch và dễ mở rộng, toàn bộ các ràng buộc nghiệp vụ (Business Rules) được cài đặt dưới dạng các **Validator Strategies** độc lập:
*   `MaxAllocationValidator`: Kiểm tra xem phân bổ mới có làm tổng tải của nhân viên vượt quá 100% tại bất kỳ thời điểm nào hay không (hỗ trợ thuật toán kiểm tra overlap ngày thông minh).
*   `ProjectStatusValidator`: Ngăn chặn việc phân bổ nhân sự vào dự án đã kết thúc (`COMPLETED`).
*   `AllocationPercentValidator`: Ràng buộc tỉ lệ phần trăm phân bổ nằm trong khoảng `(0, 100]`.

---

## 🧠 AI Integrations
1.  **AI Resource Recommendation (Semantic Search)**:
    *   Sinh vector nhúng từ trường `role` của nhân viên thông qua Python embedding service (`all-MiniLM-L6-v2`).
    *   Thực hiện so khớp tương đồng ngữ nghĩa trực tiếp dưới Database bằng PostgreSQL `pgvector` với khoảng cách Cosine (`<=>` operator) qua native query, kết hợp lọc capacity rảnh rỗi.
2.  **AI Risk Detection**:
    *   Gom dữ liệu workload của toàn bộ team, kết hợp prompt yêu cầu tùy chỉnh của người dùng để gọi API **Gemini 3.1 Flash Lite**.
    *   Sinh báo cáo rủi ro phân bổ dưới định dạng JSON có cấu trúc định sẵn (Structured JSON Response).

---

## 🏃 Hướng dẫn chạy Backend

### 1. Chạy thông qua Docker Compose (Khuyên dùng)
Backend sẽ tự động chạy song hành cùng PostgreSQL và AI Embedding service.
Lệnh khởi chạy tại thư mục gốc:
```bash
docker compose -f docker/docker-compose.yml up -d --build app
```

### 2. Chạy local
*   Yêu cầu đã chạy PostgreSQL (cổng 5432) và Python Embedding Service (cổng 5000).
*   Chạy lệnh:
    ```bash
    ./mvnw spring-boot:run
    ```

---

## 📚 API Specs (Swagger UI)
Sau khi ứng dụng khởi động thành công, truy cập tài liệu Swagger tại:
*   [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 🧪 Unit Testing
Để chạy toàn bộ các bài kiểm tra chất lượng code backend:
```bash
./mvnw clean test
```
