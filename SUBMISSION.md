# BÁO CÁO NỘP BÀI TẬP VỀ NHÀ
## Đề tài: Resource Allocation Management System (RAMS)

---

### 👤 Thông tin học viên
*   **Họ và tên**: Thân Đức Quang Vinh
*   **Mã học viên / MSSV**: VinhTDQ1
*   **Lớp**: Fresher Java

---

### 🔗 Đường dẫn mã nguồn (GitHub Repository)
*   **Link GitHub**: [https://github.com/vinhhotff/ParacticeJAVA](https://github.com/vinhhotff/ParacticeJAVA)

---

### 🛠️ Tóm tắt các nội dung đã thực hiện
Dự án **RAMS** đã được phát triển hoàn thiện cả về cấu trúc Backend, dịch vụ AI thông minh và giao diện người dùng tối ưu:

1.  **Quản lý Nghiệp vụ lõi (Core Business Rules)**:
    *   Hoàn thành các chức năng CRUD cho **Nhân sự (Employees)**, **Dự án (Projects)** và **Phân bổ nhân sự (Allocations)**.
    *   Áp dụng các ràng buộc chặt chẽ: Kiểm tra chồng chéo ngày phân bổ (Date overlapping check), chặn phân bổ quá 100% capacity của mỗi nhân sự, chặn phân bổ vào các dự án đã kết thúc (`COMPLETED`).
2.  **Tích hợp Tìm kiếm ngữ nghĩa AI (Semantic Resource Matcher)**:
    *   Tích hợp dịch vụ Python sinh vector nhúng (`all-MiniLM-L6-v2`) để biểu diễn năng lực nhân sự.
    *   Lưu trữ và truy vấn vector ngữ nghĩa trực tiếp trên **PostgreSQL + pgvector** bằng Native SQL Casting. Cho phép tìm kiếm nhân sự phù hợp theo từ khóa ngữ nghĩa và phần trăm capacity rảnh rỗi.
3.  **Tích hợp Phân tích Rủi ro AI (Risk Analysis Engine)**:
    *   Kết nối với **Google Gemini API** (mô hình `gemini-3.1-flash-lite`) để phân tích quá tải của team và hỗ trợ tìm giải pháp thông qua Prompts tùy chỉnh bằng tiếng Việt.
4.  **Cải tiến Giao diện Người dùng (UI/UX)**:
    *   Thiết kế giao diện tối tối giản (Glassmorphic Dark Mode) trực quan, hiện đại.
    *   Bổ sung tính năng **Phân trang (Pagination)** toàn diện cho tất cả các bảng danh sách để tối ưu hóa chiều dài trang.
    *   Sửa lỗi hiển thị mờ/đục của Modal, giúp các Form điền thông tin rõ ràng, không bị chồng chéo chữ từ bảng phía dưới.

---

### 🏃 Hướng dẫn khởi chạy nhanh (Docker Compose)
Học viên đã cấu hình toàn bộ hệ thống chạy qua Docker Compose đồng bộ.

1.  **Cấu hình API Key**:
    Tạo file `.env` tại thư mục gốc và thư mục `docker/` chứa key Gemini:
    ```env
    GEMINI_API_KEY=Nhập_API_Key_Gemini_Của_Bạn
    ```
2.  **Khởi chạy**:
    ```bash
    docker compose -f docker/docker-compose.yml up --build -d
    ```
3.  **Truy cập ứng dụng**:
    *   Giao diện người dùng: [http://localhost:4200](http://localhost:4200)
    *   Tài liệu API Swagger: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
