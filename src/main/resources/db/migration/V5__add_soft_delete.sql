-- Add deleted_at column to employee and project tables
ALTER TABLE employee ADD COLUMN deleted_at BIGINT NOT NULL DEFAULT 0;
ALTER TABLE project ADD COLUMN deleted_at BIGINT NOT NULL DEFAULT 0;

-- Drop existing unique constraints
ALTER TABLE employee DROP CONSTRAINT IF EXISTS employee_employee_code_key;
ALTER TABLE employee DROP CONSTRAINT IF EXISTS employee_email_key;
ALTER TABLE project DROP CONSTRAINT IF EXISTS project_project_code_key;

-- Add new composite unique constraints including deleted_at
ALTER TABLE employee ADD CONSTRAINT uq_employee_code_deleted UNIQUE (employee_code, deleted_at);
ALTER TABLE employee ADD CONSTRAINT uq_employee_email_deleted UNIQUE (email, deleted_at);
ALTER TABLE project ADD CONSTRAINT uq_project_code_deleted UNIQUE (project_code, deleted_at);
