CREATE TABLE employee (
    employee_id   BIGSERIAL    PRIMARY KEY,
    employee_code VARCHAR(20)  UNIQUE NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(100) UNIQUE NOT NULL,
    role          VARCHAR(50)  NOT NULL,
    department    VARCHAR(50)  NOT NULL
);

COMMENT ON TABLE  employee        IS 'Employee information';
COMMENT ON COLUMN employee.email  IS 'Company email address';
COMMENT ON COLUMN employee.role   IS 'Job title / role';

CREATE INDEX idx_employee_department ON employee(department);
CREATE INDEX idx_employee_role ON employee(role);

CREATE TABLE project (
    project_id   BIGSERIAL    PRIMARY KEY,
    project_code VARCHAR(20)  UNIQUE NOT NULL,
    project_name VARCHAR(200) NOT NULL,
    customer     VARCHAR(100),
    status       VARCHAR(20)  NOT NULL DEFAULT 'PLANNING',
    start_date   DATE,
    end_date     DATE,
    CONSTRAINT chk_project_status CHECK (status IN ('PLANNING', 'ACTIVE', 'COMPLETED'))
);

COMMENT ON TABLE project IS 'Project information';

CREATE TABLE allocation (
    allocation_id      BIGSERIAL    PRIMARY KEY,
    employee_id        BIGINT       NOT NULL,
    project_id         BIGINT       NOT NULL,
    allocation_percent INT          NOT NULL,
    role_in_project    VARCHAR(100) NOT NULL,
    start_date         DATE         NOT NULL,
    end_date           DATE,
    CONSTRAINT fk_allocation_employee FOREIGN KEY (employee_id) REFERENCES employee(employee_id) ON DELETE CASCADE,
    CONSTRAINT fk_allocation_project FOREIGN KEY (project_id) REFERENCES project(project_id) ON DELETE CASCADE,
    CONSTRAINT chk_allocation_percent CHECK (allocation_percent >= 1 AND allocation_percent <= 100)
);

COMMENT ON TABLE allocation IS 'Employee project allocation';

CREATE INDEX idx_allocation_employee ON allocation(employee_id);
CREATE INDEX idx_allocation_project ON allocation(project_id);
CREATE INDEX idx_allocation_dates ON allocation(start_date, end_date);
