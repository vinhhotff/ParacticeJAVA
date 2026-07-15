-- Seed data for employee table
INSERT INTO employee (employee_code, full_name, email, role, department) VALUES
('EMP001', 'Tuan Ho Anh', 'tuanha@company.com', 'Senior Developer', 'FSOFT-Q1'),
('EMP002', 'Nguyen Van B', 'nguyenvanb@company.com', 'Junior Developer', 'FSOFT-Q2'),
('EMP003', 'Le Thi C', 'lethic@company.com', 'Senior Java Developer', 'FSOFT-Q1'),
('EMP004', 'Tran Van D', 'tranvand@company.com', 'Project Lead', 'FSOFT-Q3');

-- Seed data for project table
INSERT INTO project (project_code, project_name, customer, status, start_date, end_date) VALUES
('PRJ001', 'NCG Training', 'Internal', 'ACTIVE', '2026-01-01', '2026-12-31'),
('PRJ002', 'E-Commerce System', 'Retail Corp', 'PLANNING', '2026-08-01', '2027-02-28'),
('PRJ003', 'Legacy App Migration', 'Legacy Inc', 'COMPLETED', '2025-01-01', '2025-12-31');

-- Seed data for allocation table
INSERT INTO allocation (employee_id, project_id, allocation_percent, role_in_project, start_date, end_date) VALUES
(1, 1, 60, 'Developer', '2026-01-01', NULL),
(1, 2, 40, 'Tech Lead', '2026-08-01', NULL),
(2, 1, 80, 'Developer', '2026-01-01', NULL),
(3, 1, 40, 'Java Developer', '2026-02-01', NULL),
(4, 1, 95, 'Project Lead', '2026-01-01', NULL);
