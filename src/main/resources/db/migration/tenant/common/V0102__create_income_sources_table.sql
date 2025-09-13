-- Income sources table (updated to match IncomeSource entity)
CREATE TABLE income_sources (
                                income_source_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), -- Changed from 'id'
                                accounting_code VARCHAR(50) NOT NULL UNIQUE,
                                fee_type_code VARCHAR(50) NOT NULL,
                                name VARCHAR(255) NOT NULL,
                                description TEXT,
                                recurrency VARCHAR(50) NOT NULL, -- Changed to match enum
                                applicability VARCHAR(50) NOT NULL, -- Changed to match enum
                                currency VARCHAR(10) NOT NULL,
                                default_amount NUMERIC(12,2) DEFAULT 0 CHECK (default_amount >= 0),
                                is_active BOOLEAN DEFAULT TRUE,
                                allow_partial_payment BOOLEAN DEFAULT FALSE,
                                is_taxable BOOLEAN DEFAULT FALSE,
                                tax_rate NUMERIC(5,2) DEFAULT 0 CHECK (tax_rate >= 0),
                                effective_from DATE NOT NULL,
                                effective_to DATE NULL,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                created_by VARCHAR(150),
                                updated_by VARCHAR(150)
);

-- Updated indexes for income_sources
CREATE INDEX idx_income_sources_active ON income_sources(is_active);
CREATE INDEX idx_income_sources_currency ON income_sources(currency);
CREATE INDEX idx_income_sources_fee_type ON income_sources(fee_type_code);
CREATE INDEX idx_income_sources_effective ON income_sources(effective_from, effective_to);
CREATE INDEX idx_income_sources_active_effective ON income_sources(is_active, effective_from, effective_to);