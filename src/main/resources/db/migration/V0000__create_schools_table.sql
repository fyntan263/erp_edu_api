CREATE TABLE schools (
                         school_id UUID PRIMARY KEY,
                         mopse_no VARCHAR(100) NOT NULL,
                         school_email VARCHAR(255),
                         school_name VARCHAR(255) NOT NULL,
                         school_type VARCHAR(100) NOT NULL,
                         education_level VARCHAR(100) NOT NULL,
                         district VARCHAR(255) NOT NULL,
                         province VARCHAR(255) NOT NULL,
                         physical_address TEXT,
                         capacity INTEGER,
                         status VARCHAR(50) NOT NULL DEFAULT 'PENDING',

    -- Contact person info
                         contact_fullname VARCHAR(255),
                         contact_position VARCHAR(255),
                         contact_email VARCHAR(255),
                         contact_phone VARCHAR(50),
                         contact_alt_phone VARCHAR(50),
                         is_provisioned BOOLEAN NOT NULL DEFAULT false,

    -- Auditing
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for schools
CREATE UNIQUE INDEX idx_schools_mopse_no ON schools(mopse_no);
CREATE INDEX idx_schools_status ON schools(status);
CREATE INDEX idx_schools_provisioned ON schools(is_provisioned);
CREATE INDEX idx_schools_education_level ON schools(education_level);
CREATE INDEX idx_schools_province_district ON schools(province, district);
CREATE INDEX idx_schools_school_type ON schools(school_type);
CREATE INDEX idx_schools_email ON schools(school_email);
CREATE INDEX idx_schools_contact_email ON schools(contact_email);
CREATE INDEX idx_schools_contact_phone ON schools(contact_phone);