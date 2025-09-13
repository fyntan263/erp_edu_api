CREATE TABLE db_provisions (
                               provision_id UUID PRIMARY KEY,
                               db_schema_name VARCHAR(255) NOT NULL,
                               provision_status VARCHAR(50) NOT NULL,
                               assigned_school_id UUID,
                               assigned_education_level VARCHAR(100),
                               is_accessible BOOLEAN NOT NULL DEFAULT false,
                               assigned_by VARCHAR(255),
                               assigned_date TIMESTAMP,
                               error_message TEXT,
                               attempts INTEGER NOT NULL DEFAULT 0,
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for db_provisions
CREATE INDEX idx_db_provisions_status ON db_provisions(provision_status);
CREATE INDEX idx_db_provisions_school_id ON db_provisions(assigned_school_id);
CREATE INDEX idx_db_provisions_schema_name ON db_provisions(db_schema_name);
CREATE INDEX idx_db_provisions_accessible ON db_provisions(is_accessible);
CREATE INDEX idx_db_provisions_education_level ON db_provisions(assigned_education_level);
CREATE UNIQUE INDEX idx_db_provisions_schema_unique ON db_provisions(LOWER(db_schema_name));