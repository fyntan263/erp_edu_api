-- Payments table (updated to match PaymentEntity)
CREATE TABLE payments (
                          payment_id UUID PRIMARY KEY,
                          entity_id UUID NOT NULL, -- Changed from student_id
                          entity_type VARCHAR(50) NOT NULL, -- New column
                          invoice_id UUID NULL,
                          payment_number VARCHAR(50) NOT NULL UNIQUE,
                          payment_date TIMESTAMP NOT NULL,
                          amount DECIMAL(15, 2) NOT NULL,
                          currency VARCHAR(10) NOT NULL, -- Changed from CHAR(3)
                          payment_method VARCHAR(50) NOT NULL, -- Increased length
                          status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN (
                                                                                          'PENDING', 'COMPLETED', 'FAILED', 'REFUNDED', 'PARTIALLY_REFUNDED'
                              )),
                          payment_notes TEXT,
                          deleted BOOLEAN DEFAULT FALSE, -- New column
                          deleted_at TIMESTAMP NULL, -- New column
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Removed student foreign key constraint since entity_id can reference different tables
                          CONSTRAINT fk_payments_invoice FOREIGN KEY (invoice_id) REFERENCES invoices (invoice_id),
                          CONSTRAINT chk_positive_amount CHECK (amount > 0)
);

-- Updated indexes for payments
CREATE INDEX idx_payments_entity ON payments(entity_id, entity_type);
CREATE INDEX idx_payments_invoice ON payments(invoice_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_date ON payments(payment_date);
CREATE INDEX idx_payments_method ON payments(payment_method);
CREATE INDEX idx_payments_deleted ON payments(deleted);