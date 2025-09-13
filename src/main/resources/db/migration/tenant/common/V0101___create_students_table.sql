CREATE TABLE students (
     student_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
     first_name VARCHAR(100) NOT NULL,
     last_name VARCHAR(100) NOT NULL,
     date_of_birth DATE NOT NULL,
     gender VARCHAR(10) CHECK (gender IN ('MALE', 'FEMALE')),
     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);