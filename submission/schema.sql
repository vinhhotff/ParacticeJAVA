-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Employee Table
CREATE TABLE employee (
    employee_id   BIGSERIAL    PRIMARY KEY,
    employee_code VARCHAR(20)  UNIQUE NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(100) UNIQUE NOT NULL,
    role          VARCHAR(50)  NOT NULL,
    department    VARCHAR(50)  NOT NULL,
    role_embedding vector(384)
);

COMMENT ON TABLE  employee        IS 'Employee information';
COMMENT ON COLUMN employee.email  IS 'Company email address';
COMMENT ON COLUMN employee.role   IS 'Job title / role';
COMMENT ON COLUMN employee.role_embedding IS 'Vector embedding of employee role for semantic search';

CREATE INDEX idx_employee_department ON employee(department);
CREATE INDEX idx_employee_role ON employee(role);
CREATE INDEX idx_employee_role_embedding ON employee USING hnsw (role_embedding vector_cosine_ops) WITH (m = 16, ef_construction = 64);

-- Project Table
CREATE TABLE project (
    project_id   BIGSERIAL    PRIMARY KEY,
    project_code VARCHAR(20)  UNIQUE NOT NULL,
    project_name VARCHAR(200) NOT NULL,
    customer     VARCHAR(100),
    status       VARCHAR(20)  NOT NULL DEFAULT 'PLANNING',
    start_date   DATE,
    end_date     DATE,
    description_embedding vector(384),
    CONSTRAINT chk_project_status CHECK (status IN ('PLANNING', 'ACTIVE', 'COMPLETED'))
);

COMMENT ON TABLE project IS 'Project information';
COMMENT ON COLUMN project.description_embedding IS 'Vector embedding of project description for semantic search';

CREATE INDEX idx_project_description_embedding ON project USING hnsw (description_embedding vector_cosine_ops) WITH (m = 16, ef_construction = 64);

-- Resource Allocation Table
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
