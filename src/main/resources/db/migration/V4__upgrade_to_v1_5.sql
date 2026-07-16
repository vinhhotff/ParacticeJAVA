-- Create Skill Table
CREATE TABLE skill (
    skill_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

-- Create Employee-Skill Relation Table (Many-to-Many)
CREATE TABLE employee_skill (
    employee_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    PRIMARY KEY (employee_id, skill_id),
    CONSTRAINT fk_employee_skill_employee FOREIGN KEY (employee_id) REFERENCES employee(employee_id) ON DELETE CASCADE,
    CONSTRAINT fk_employee_skill_skill FOREIGN KEY (skill_id) REFERENCES skill(skill_id) ON DELETE CASCADE
);

-- Add status column with constraint to allocation table
ALTER TABLE allocation ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING';
ALTER TABLE allocation ADD CONSTRAINT chk_allocation_status CHECK (status IN ('PENDING', 'ACTIVE', 'ENDED'));

-- Update existing allocations to ACTIVE status to maintain data consistency
UPDATE allocation SET status = 'ACTIVE';

-- Seed Skills
INSERT INTO skill (name) VALUES 
('Java'),
('Spring Boot'),
('PostgreSQL'),
('Angular'),
('Python'),
('React'),
('TypeScript'),
('Docker');

-- Link skills to seed employees
-- Employee 1: Nguyen Van A -> Java, Spring Boot, PostgreSQL, Docker
INSERT INTO employee_skill (employee_id, skill_id) VALUES 
(1, 1), (1, 2), (1, 3), (1, 8);

-- Employee 2: Tran Thi B -> React, TypeScript
INSERT INTO employee_skill (employee_id, skill_id) VALUES 
(2, 6), (2, 7);

-- Employee 3: Le Thi C -> Java, Spring Boot, Angular
INSERT INTO employee_skill (employee_id, skill_id) VALUES 
(3, 1), (3, 2), (3, 4);

-- Employee 6: Hoang Quoc F -> Python, Docker
INSERT INTO employee_skill (employee_id, skill_id) VALUES 
(6, 5), (6, 8);
