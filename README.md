# Project Resource Allocation Management System

Hệ thống quản lý phân bổ nhân sự (Resource Allocation Management System) được thiết kế nhằm giúp PM hoặc Resource Manager trong các công ty outsourcing quản lý nhân viên, dự án, phân bổ nhân sự và theo dõi workload hiệu quả, tránh tình trạng overloaded.

---

## 🚀 Tính năng chính

### 1. Quản lý nhân viên (Employee Management)
- Thêm mới, cập nhật, xóa và truy vấn thông tin nhân viên.
- Lưu trữ: Employee Code, Full Name, Email, Role, Department.

### 2. Quản lý dự án (Project Management)
- Thêm mới, cập nhật, xóa và truy vấn thông tin dự án.
- Trạng thái dự án: `PLANNING`, `ACTIVE`, `COMPLETED`.

### 3. Phân bổ nhân sự (Resource Allocation) & Ràng buộc nghiệp vụ (Business Rules)
- Phân bổ nhân sự vào dự án với tỉ lệ phần trăm (`0 < allocation_percent <= 100`).
- **Business Rule 1:** Tỷ lệ phân bổ nằm trong khoảng `(0, 100]`.
- **Business Rule 2:** Tổng allocation của một nhân viên tại bất kỳ thời điểm nào không được vượt quá 100%. (Hệ thống sử dụng thuật toán kiểm tra overlap ngày thông minh).
- **Business Rule 3:** Không cho phép phân bổ nhân sự vào dự án đã kết thúc (`COMPLETED`).

### 4. Báo cáo & Thống kê (Reporting)
- **Utilization Report:** Báo cáo hiệu suất sử dụng của từng nhân sự.
- **Available Resource Report:** Tìm nhân sự còn thời gian khả dụng (Capacity < 100%).
- **Overloaded Employee Report:** Cảnh báo nhân sự bị quá tải (Workload > 90%).

### 5. Tính năng AI tích hợp (AI Bonus Features)
- **AI Resource Recommendation:** Đề xuất nhân sự phù hợp bằng Semantic Search sử dụng Vector Embeddings (MiniLM-L6-v2) so khớp giữa kỹ năng/role và capacity còn lại.
- **AI Risk Detection:** Phân tích rủi ro quá tải của team và mức độ sẵn sàng cho các sprint tiếp theo.

---

## 🛠️ Công nghệ sử dụng
- **Backend:** Java 17+, Spring Boot 3.3.5, Spring Data JPA, PostgreSQL.
- **AI/Embedding Service:** Python 3, Flask, Sentence-Transformers (`all-MiniLM-L6-v2`).
- **Database:** PostgreSQL + pgvector (để lưu trữ và so sánh vector embeddings).
- **Migration:** Flyway.
- **Frontend:** Angular.

---

## 🏃 Hướng dẫn chạy dự án

### Cách 1: Chạy bằng Docker Compose (Khuyên dùng)
Dự án đã được cấu hình đầy đủ trong `docker/docker-compose.yml`. Bạn chỉ cần khởi chạy:
```bash
docker-compose -f docker/docker-compose.yml up --build
```
Lệnh này sẽ khởi tạo:
1. **PostgreSQL** (cổng 5432) tích hợp sẵn extension `pgvector`.
2. **Embedding Service** (cổng 5000) chạy Flask và mô hình AI.
3. **Backend Service** (cổng 8080) kết nối DB và Embedding Service.
4. **Frontend Service** (cổng 4200) giao diện Angular.

### Cách 2: Chạy local từng phần

#### 1. Khởi động PostgreSQL và cài pgvector
Tạo database `resource_allocation`.

#### 2. Chạy Python Embedding Service
```bash
cd embedding-service
pip install -r requirements.txt
python app.py
```

#### 3. Chạy Java Spring Boot Backend
```bash
./mvnw spring-boot:run
```

#### 4. Chạy Frontend (Angular)
```bash
cd frontend
npm install
npm start
```
Truy cập: `http://localhost:4200`

---

## 📚 API Documentation & Swagger
Sau khi khởi động Backend thành công, bạn có thể truy cập tài liệu API tự động tại:
- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **API Docs JSON:** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

---

## 🧪 Chạy Unit Test
Để kiểm tra tính toàn vẹn và đúng đắn của logic backend:
```bash
./mvnw clean test
```
