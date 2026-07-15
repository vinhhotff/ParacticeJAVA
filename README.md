# Project Resource Allocation Management System (RAMS)

Hệ thống quản lý phân bổ nhân sự (Resource Allocation Management System) được thiết kế nhằm giúp PM hoặc Resource Manager trong các công ty outsourcing quản lý nhân viên, dự án, phân bổ nhân sự và theo dõi workload hiệu quả, tránh tình trạng overloaded.

---

## 🚀 Tính năng chính

### 1. Quản lý nhân viên (Employee Management)
- Thêm mới, cập nhật, xóa và truy vấn thông tin nhân viên.
- Lưu trữ: Employee Code, Full Name, Email, Role, Department.
- Tích hợp **Phân trang (Pagination)** mượt mà trên giao diện (5 bản ghi/trang).

### 2. Quản lý dự án (Project Management)
- Thêm mới, cập nhật, xóa và truy vấn thông tin dự án.
- Trạng thái dự án: `PLANNING`, `ACTIVE`, `COMPLETED`.
- Tích hợp **Phân trang (Pagination)** mượt mà trên giao diện.

### 3. Phân bổ nhân sự (Resource Allocation) & Ràng buộc nghiệp vụ (Business Rules)
- Phân bổ nhân sự vào dự án với tỉ lệ phần trăm (`0 < allocation_percent <= 100`).
- **Business Rule 1:** Tỷ lệ phân bổ nằm trong khoảng `(0, 100]`.
- **Business Rule 2:** Tổng allocation của một nhân viên tại bất kỳ thời điểm nào không được vượt quá 100%. (Hệ thống sử dụng thuật toán kiểm tra overlap ngày thông minh).
- **Business Rule 3:** Không cho phép phân bổ nhân sự vào dự án đã kết thúc (`COMPLETED`).
- Tích hợp **Phân trang (Pagination)** danh sách phân bổ.

### 4. Báo cáo & Thống kê (Reporting)
- **Utilization Report:** Báo cáo hiệu suất sử dụng của từng nhân sự.
- **Available Resource Report:** Tìm nhân sự còn thời gian khả dụng (Capacity < 100%).
- **Overloaded Employee Report:** Cảnh báo nhân sự bị quá tải (Workload > 90%).

### 5. Tính năng AI tích hợp (AI Advanced Features)
- **AI Resource Recommendation (Semantic Search)**: Đề xuất nhân sự phù hợp bằng Tìm kiếm ngữ nghĩa sử dụng Vector Embeddings (`all-MiniLM-L6-v2`) so khớp giữa kỹ năng/role và capacity còn lại.
- **AI Risk Detection (Gemini Cognitive Engine)**: Phân tích toàn diện rủi ro quá tải của team và mức độ sẵn sàng bằng mô hình `gemini-3.1-flash-lite`, hỗ trợ nhận lệnh Prompts tiếng Việt tùy chỉnh trên giao diện.

---

## 🛠️ Công nghệ sử dụng
- **Backend:** Java 17+, Spring Boot 3.3.5, Spring Data JPA, PostgreSQL.
- **AI/Embedding Service:** Python 3, Flask, Sentence-Transformers (`all-MiniLM-L6-v2`).
- **Database:** PostgreSQL + pgvector (để lưu trữ và so sánh vector embeddings dạng native).
- **Migration:** Flyway.
- **Frontend:** Angular 17+ (tích hợp Signal computed pagination, giao diện Glassmorphic cao cấp, tối ưu hóa hiển thị Modal).

---

## ⚙️ Hướng dẫn Cấu hình Môi trường

Để dự án hoạt động chính xác (đặc biệt là tính năng phân tích rủi ro AI bằng Gemini), bạn cần tạo các file cấu hình môi trường dưới đây:

### 1. Tạo file `.env` ở thư mục gốc (Root Directory)
Tạo file `.env` tại thư mục `/` của dự án với nội dung sau:
```env
# API Key của Google Gemini (Lấy từ Google AI Studio)
GEMINI_API_KEY=Nhập_API_Key_Gemini_Của_Bạn_Vào_Đây
```

### 2. Tạo file `.env` ở thư mục `docker/`
Tạo file `.env` tại thư mục `/docker/` của dự án để Docker Compose có thể đọc biến môi trường:
```env
# API Key của Google Gemini
GEMINI_API_KEY=Nhập_API_Key_Gemini_Của_Bạn_Vào_Đây
```

---

## 🏃 Hướng dẫn chạy dự án bằng Docker Compose

Dự án đã được thiết lập đồng bộ hóa thứ tự khởi chạy (Backend chỉ khởi động sau khi Database và AI Embedding Service đã khỏe mạnh - `service_healthy`).

### Khởi chạy dự án
Chạy lệnh sau tại thư mục gốc của dự án:
```bash
docker compose -f docker/docker-compose.yml up --build -d
```

Lệnh này sẽ tự động tải các dependencies, build ứng dụng và chạy:
1. **PostgreSQL Container** (cổng `5432`): Chạy cơ sở dữ liệu và kích hoạt sẵn extension `vector`.
2. **AI Embedding Service Container** (cổng `5001` host, `5000` internal): Chạy API Python sinh vector.
3. **Backend Service Container** (cổng `8080`): Tự động tạo bảng qua Flyway, gọi AI service sinh vector ban đầu cho 15 nhân sự mẫu lúc khởi động.
4. **Frontend Service Container** (cổng `4200`): Giao diện web Angular.

### Dọn dẹp & Reset Database
Nếu bạn muốn reset lại toàn bộ dữ liệu database để chạy lại Flyway và sinh mới vector embeddings cho dữ liệu mẫu:
```bash
docker compose -f docker/docker-compose.yml down -v
docker compose -f docker/docker-compose.yml up --build -d
```

---

## 🏃 Chạy local từng phần (Không qua Docker)

### 1. Khởi động PostgreSQL và cài pgvector
Tạo database tên là `resource_allocation`. Cấu hình thông tin kết nối trong `src/main/resources/application.yml`.

### 2. Chạy Python Embedding Service
```bash
cd embedding-service
pip install -r requirements.txt
python app.py
```

### 3. Chạy Java Spring Boot Backend
```bash
./mvnw spring-boot:run
```

### 4. Chạy Frontend (Angular)
```bash
cd frontend
npm install
npm start
```
Truy cập giao diện: [http://localhost:4200](http://localhost:4200)

---

## 📚 Tài liệu API (Swagger UI)
Sau khi Backend khởi động thành công, bạn có thể xem danh sách và thử nghiệm các API trực tiếp tại:
- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **API Docs JSON:** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

---

## 🧪 Chạy Unit Test
Chạy lệnh sau ở thư mục gốc để chạy toàn bộ unit test của backend:
```bash
./mvnw clean test
```

---

## 📬 Postman Collection (API Testing)
Dự án đi kèm với bộ Postman Collection được cấu hình sẵn để Mentor hoặc Học viên dễ dàng thử nghiệm toàn bộ API của hệ thống:
- **Đường dẫn file collection:** [postman/ResourceAllocation.postman_collection.json](postman/ResourceAllocation.postman_collection.json)

### Hướng dẫn Import & Sử dụng:
1. Mở ứng dụng **Postman** trên máy tính -> Chọn **Import** ở góc trên cùng bên trái.
2. Chọn hoặc kéo thả file `ResourceAllocation.postman_collection.json` (nằm trong thư mục `postman/` của dự án) vào vùng import.
3. Sau khi Import thành công, thư mục **Resource Allocation API** sẽ xuất hiện ở tab *Collections*.
4. Bộ collection đã được thiết lập sẵn biến môi trường `{{baseUrl}}` trỏ tới `http://localhost:8080/api` và các nhóm request (CRUD, Báo cáo và các API AI nâng cao). Chỉ cần chọn request và bấm **Send** để chạy thử nghiệm!
