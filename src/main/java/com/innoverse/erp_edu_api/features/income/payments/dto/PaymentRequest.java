package com.innoverse.erp_edu_api.features.income.payments.dto;

import com.innoverse.erp_edu_api.features.income.invoices.dto.InvoiceItemRequest;
import com.innoverse.erp_edu_api.features.income.payments.Payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PaymentRequest(
        UUID entityId,
        String entityType,
        UUID invoiceId,
        BigDecimal amount,
        String currency,
        Payment.PaymentMethod paymentMethod,
        String paymentNotes,
        Boolean createInvoice,
        String invoiceDescription,
        LocalDate invoiceDueDate,
        List<InvoiceItemRequest> lineItems
) {
    public PaymentRequest {
        // Validation logic
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (entityId == null) {
            throw new IllegalArgumentException("Entity ID is required");
        }
        if (entityType == null || entityType.isBlank()) {
            throw new IllegalArgumentException("Entity type is required");
        }

        // Validate invoice creation parameters if needed
        if (Boolean.TRUE.equals(createInvoice)) {
            if (invoiceDescription == null || invoiceDescription.isBlank()) {
                throw new IllegalArgumentException("Invoice description is required when creating invoice");
            }
            if (invoiceDueDate == null) {
                throw new IllegalArgumentException("Invoice due date is required when creating invoice");
            }
            if (lineItems == null || lineItems.isEmpty()) {
                throw new IllegalArgumentException("Line items are required when creating invoice");
            }
        }
    }
}