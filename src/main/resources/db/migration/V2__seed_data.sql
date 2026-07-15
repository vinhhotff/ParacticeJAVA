-- Seed data for employee table
INSERT INTO employee (employee_code, full_name, email, role, department) VALUES
('EMP001', 'Tuan Ho Anh', 'tuanha@company.com', 'Senior Developer', 'FSOFT-Q1'),
('EMP002', 'Nguyen Van B', 'nguyenvanb@company.com', 'Junior Developer', 'FSOFT-Q2'),
('EMP003', 'Le Thi C', 'lethic@company.com', 'Senior Java Developer', 'FSOFT-Q1'),
('EMP004', 'Tran Van D', 'tranvand@company.com', 'Project Lead', 'FSOFT-Q3'),
('EMP005', 'Pham Minh E', 'phamme@company.com', 'Lead React Architect', 'FSOFT-Q2'),
('EMP006', 'Hoang Quoc F', 'hoangqf@company.com', 'Junior Python/Django Engineer', 'FSOFT-AI'),
('EMP007', 'Vu Minh G', 'vumg@company.com', 'DevOps Cloud Engineer (AWS/Kubernetes)', 'FSOFT-CL'),
('EMP008', 'Ngo Thanh H', 'ngoth@company.com', 'Senior Backend Engineer (Go & Node.js)', 'FSOFT-Q1'),
('EMP009', 'Dang Quang I', 'dangqi@company.com', 'Automation QA Engineer (Selenium/Cypress)', 'FSOFT-Q4'),
('EMP010', 'Bui Thi J', 'buitj@company.com', 'UI/UX Designer (Figma)', 'FSOFT-DES'),
('EMP011', 'Do Hoang K', 'dohk@company.com', 'Database Administrator (PostgreSQL/Oracle)', 'FSOFT-DB'),
('EMP012', 'Lai Van L', 'laivl@company.com', 'Senior Data Scientist (Python/PyTorch)', 'FSOFT-AI'),
('EMP013', 'Mac Thi M', 'mactm@company.com', 'React Native Mobile Developer', 'FSOFT-MOB'),
('EMP014', 'Nguyen Duc N', 'nguyendn@company.com', 'Scrum Master & PM', 'FSOFT-Q3'),
('EMP015', 'Trinh Van O', 'trinhvo@company.com', 'Full Stack Javascript Engineer (Next.js/Node.js)', 'FSOFT-Q2');

-- Seed data for project table
INSERT INTO project (project_code, project_name, customer, status, start_date, end_date) VALUES
('PRJ001', 'NCG Training', 'Internal', 'ACTIVE', '2026-01-01', '2026-12-31'),
('PRJ002', 'E-Commerce System', 'Retail Corp', 'PLANNING', '2026-08-01', '2027-02-28'),
('PRJ003', 'Legacy App Migration', 'Legacy Inc', 'COMPLETED', '2025-01-01', '2025-12-31'),
('PRJ004', 'NextGen AI Platform', 'AI Tech Inc', 'ACTIVE', '2026-03-01', '2027-03-01'),
('PRJ005', 'Fintech Gateway Implementation', 'National Bank', 'ACTIVE', '2026-05-15', '2026-11-15'),
('PRJ006', 'Cloud Security Hardening', 'Secure Fin', 'ACTIVE', '2026-06-01', '2026-12-01');

-- Seed data for allocation table
INSERT INTO allocation (employee_id, project_id, allocation_percent, role_in_project, start_date, end_date) VALUES
-- EMP001 (Senior Developer): 60% + 40% = 100% (Overloaded)
(1, 1, 60, 'Developer', '2026-01-01', NULL),
(1, 2, 40, 'Tech Lead', '2026-08-01', NULL),

-- EMP002 (Junior Developer): 80% (Healthy/High)
(2, 1, 80, 'Developer', '2026-01-01', NULL),

-- EMP003 (Senior Java Developer): 40% (Healthy - 60% available)
(3, 1, 40, 'Java Developer', '2026-02-01', NULL),

-- EMP004 (Project Lead): 95% (Overloaded)
(4, 1, 95, 'Project Lead', '2026-01-01', NULL),

-- EMP005 (Lead React Architect): 50% + 50% = 100% (Overloaded)
(5, 2, 50, 'React Lead', '2026-08-01', NULL),
(5, 4, 50, 'Frontend Architect', '2026-03-01', NULL),

-- EMP006 (Junior Python/Django Engineer): 0% (Bench - 100% available)

-- EMP007 (DevOps Cloud Engineer): 50% + 30% + 30% = 110% (Critical Overload)
(7, 4, 50, 'DevOps Engineer', '2026-03-01', NULL),
(7, 5, 30, 'Cloud Infrastructure', '2026-05-15', NULL),
(7, 6, 30, 'Security Deployment', '2026-06-01', NULL),

-- EMP008 (Senior Backend Engineer): 50% (Healthy - 50% available)
(8, 5, 50, 'Go Developer', '2026-05-15', NULL),

-- EMP009 (Automation QA Engineer): 0% (Bench - 100% available)

-- EMP010 (UI/UX Designer): 40% (Healthy - 60% available)
(10, 4, 40, 'UI/UX Designer', '2026-03-01', NULL),

-- EMP011 (Database Administrator): 60% + 40% = 100% (Overloaded)
(11, 4, 60, 'DBA', '2026-03-01', NULL),
(11, 5, 40, 'DB Migration Specialist', '2026-05-15', NULL),

-- EMP012 (Senior Data Scientist): 0% (Bench - 100% available)

-- EMP013 (React Native Mobile Developer): 70% (Healthy - 30% available)
(13, 2, 70, 'Mobile Dev', '2026-08-01', NULL),

-- EMP014 (Scrum Master & PM): 20% (Healthy - 80% available)
(14, 6, 20, 'Scrum Master', '2026-06-01', NULL),

-- EMP015 (Full Stack Javascript Engineer): 60% + 40% = 100% (Overloaded)
(15, 2, 60, 'Full Stack Developer', '2026-08-01', NULL),
(15, 4, 40, 'React Dev', '2026-03-01', NULL);
