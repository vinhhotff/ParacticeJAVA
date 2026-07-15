-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Add embedding column to employee table for semantic search
ALTER TABLE employee
    ADD COLUMN IF NOT EXISTS role_embedding vector(384);

-- Add embedding column to project table for semantic search
ALTER TABLE project
    ADD COLUMN IF NOT EXISTS description_embedding vector(384);

-- Add index for vector similarity search on employee
CREATE INDEX IF NOT EXISTS idx_employee_role_embedding
    ON employee USING hnsw (role_embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);

-- Add index for vector similarity search on project
CREATE INDEX IF NOT EXISTS idx_project_description_embedding
    ON project USING hnsw (description_embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);

-- Add comment
COMMENT ON COLUMN employee.role_embedding IS 'Vector embedding of employee role for semantic search';
COMMENT ON COLUMN project.description_embedding IS 'Vector embedding of project description for semantic search';