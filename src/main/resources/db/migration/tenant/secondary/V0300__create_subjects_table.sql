CREATE TABLE subjects (
        subject_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        subject_name VARCHAR(100) NOT NULL,
        subject_code VARCHAR(20) UNIQUE NOT NULL,
        credits INT DEFAULT 0
);