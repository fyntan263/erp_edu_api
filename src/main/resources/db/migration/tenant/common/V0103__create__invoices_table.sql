-- Invoices table (optimized)
-- Invoices table (updated to match Invoice entity)
CREATE TABLE invoices (
                          invoice_id UUID PRIMARY KEY,
                          entity_id UUID NOT NULL, -- Changed from student_id
                          entity_type VARCHAR(50) NOT NULL, -- New column
                          invoice_number VARCHAR(50) NOT NULL UNIQUE,
                          description VARCHAR(500), -- New column
                          issue_date DATE NOT NULL,
                          due_date DATE NOT NULL,
                          total_amount DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
                          amount_paid DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
                          currency VARCHAR(10) NOT NULL, -- Changed from CHAR(3)
                          status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN (
                                                                                        'DRAFT', 'ISSUED', 'PARTIALLY_PAID', 'PAID', 'OVERDUE', 'CANCELLED', 'REFUNDED'
                              )),
                          notes TEXT,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Removed student foreign key constraint
                          CONSTRAINT chk_positive_amounts CHECK (total_amount >= 0 AND amount_paid >= 0),
                          CONSTRAINT chk_amount_paid_limit CHECK (amount_paid <= total_amount),
                          CONSTRAINT chk_due_after_issue CHECK (due_date >= issue_date)
);

-- Updated indexes for invoices
CREATE INDEX idx_invoices_entity ON invoices(entity_id, entity_type);
CREATE INDEX idx_invoices_status ON invoices(status);
CREATE INDEX idx_invoices_due_date ON invoices(due_date);
CREATE INDEX idx_invoices_created_at ON invoices(created_at);

-- Invoice line items table (updated column names to match entity)
CREATE TABLE invoice_line_items (
                                    line_item_id UUID PRIMARY KEY, -- Changed from line_item_id to match entity
                                    invoice_id UUID NOT NULL,
                                    income_source_id UUID NOT NULL,
                                    description VARCHAR(500) NOT NULL,
                                    quantity INTEGER NOT NULL DEFAULT 1,
                                    unit_price DECIMAL(15, 2) NOT NULL,
                                    tax_rate DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
                                    discount_percentage DECIMAL(5, 2) NOT NULL DEFAULT 0.00,

                                    CONSTRAINT fk_line_items_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(invoice_id) ON DELETE CASCADE,
                                    CONSTRAINT fk_line_items_income_source FOREIGN KEY (income_source_id) REFERENCES income_sources(income_source_id),
                                    CONSTRAINT chk_positive_quantity CHECK (quantity > 0),
                                    CONSTRAINT chk_positive_unit_price CHECK (unit_price >= 0),
                                    CONSTRAINT chk_valid_tax_rate CHECK (tax_rate >= 0 AND tax_rate <= 100),
                                    CONSTRAINT chk_valid_discount CHECK (discount_percentage >= 0 AND discount_percentage <= 100)
);

-- Updated indexes
CREATE INDEX idx_line_items_invoice ON invoice_line_items(invoice_id);
CREATE INDEX idx_line_items_income_source ON invoice_line_items(income_source_id);