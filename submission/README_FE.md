# RAMS - Frontend Application Documentation
## Công nghệ: Angular 17+ (Standalone Components), HTML5, Vanilla CSS, Nginx

Giao diện người dùng của Hệ thống quản lý phân bổ nhân sự (Resource Allocation Management System) được thiết kế hiện đại theo phong cách **Glassmorphism**, tập trung vào trải nghiệm mượt mà, trực quan và tối giản.

---

## 🎨 Giao diện & Các trang chức năng

### 1. Dashboard (Bảng điều khiển)
*   **Bento Stats Grid**: Thống kê nhanh tổng số nhân viên, dự án đang hoạt động và tổng số phân bổ.
*   **Resource Workload Index**: Biểu đồ thanh tải công việc của nhân sự dạng progress bar, cảnh báo đỏ nếu quá tải.
*   **Bench & Overload List**: Hiển thị danh sách các nhân viên đang rảnh rỗi (`Bench`) hoặc đang bị quá tải công việc (`Overload`).

### 2. Employees (Danh sách nhân viên)
*   Hiển thị danh sách nhân sự hiện tại dưới dạng bảng phân trang (5 người/trang).
*   Chức năng CRUD (Thêm, Sửa, Xóa) tích hợp Modal điền thông tin cao cấp.
*   Nút xem nhanh tải công việc (**Workload Analysis**) dưới dạng vòng tròn tỷ lệ phần trăm trực quan.

### 3. Projects (Danh sách dự án)
*   Hiển thị danh sách dự án kèm trạng thái (`PLANNING`, `ACTIVE`, `COMPLETED`).
*   Tích hợp bộ phân trang và modal thêm dự án mới.

### 4. Allocations (Phân bổ nhân sự)
*   Hiển thị sơ đồ phân bổ nhân viên vào dự án tương ứng, vai trò trong dự án, phần trăm phân bổ và thời hạn dự kiến.
*   Form tạo mới phân bổ kéo chọn danh sách nhân viên và dự án động từ API.

### 5. AI Engine (Trung tâm Trí tuệ Nhân tạo)
*   **AI Resource Matcher**: Nhập từ khóa kỹ năng và điều chỉnh thanh trượt mức capacity tối thiểu mong muốn, hệ thống sẽ đề xuất các ứng viên tối ưu nhất (sắp xếp theo điểm tương đồng ngữ nghĩa `matchScore`).
*   **Team Risk Analysis**: Xem tóm tắt rủi ro toàn team do Gemini phân tích. Hỗ trợ gửi prompts tùy chỉnh bằng tiếng Việt (ví dụ: *"Sprint tới cần thêm 2 Java Developer"* hoặc *"Ai đang làm nhiều việc nhất và giảm tải thế nào"*) để Gemini tư vấn trực tiếp trên màn hình.

---

## 🏃 Hướng dẫn chạy Frontend local

### 1. Khởi chạy bằng Docker Compose (Khuyên dùng)
Frontend sẽ được tự động build và chạy qua Nginx (cổng `4200` của host).
Lệnh khởi chạy tại thư mục gốc:
```bash
docker compose -f docker/docker-compose.yml up -d --build frontend
```

### 2. Chạy local bằng Node.js
Yêu cầu cài đặt Node.js 18+ và Angular CLI.
```bash
cd frontend
npm install
npm start
```
Truy cập ứng dụng tại địa chỉ: [http://localhost:4200](http://localhost:4200)
