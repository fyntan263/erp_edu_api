CREATE TABLE classes (
     class_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
     class_name VARCHAR(50) NOT NULL,
     grade_level VARCHAR(20) NOT NULL,
     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);