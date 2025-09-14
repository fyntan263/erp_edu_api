package com.innoverse.erp_edu_api.features.income.payments.dto;

import com.innoverse.erp_edu_api.features.income.invoices.dto.InvoiceItemRequest;
import com.innoverse.erp_edu_api.features.income.payments.Payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PaymentCreationRequest(
        UUID payeeId,
        String payeeType,
        BigDecimal amount,
        String currency,
        Payment.PaymentMethod paymentMethod,
        String notes,
        Boolean shouldCreateInvoice,
        LocalDate invoiceDueDate,
        List<InvoiceItemRequest> invoiceItems
) {
    public PaymentCreationRequest {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (payeeId == null) {
            throw new IllegalArgumentException("Entity ID is required");
        }
        if (payeeType == null || payeeType.isBlank()) {
            throw new IllegalArgumentException("Entity type is required");
        }

        if (Boolean.TRUE.equals(shouldCreateInvoice)) {
            if (invoiceDueDate == null) {
                throw new IllegalArgumentException("Invoice due date is required when creating invoice");
            }
            if (invoiceItems == null || invoiceItems.isEmpty()) {
                throw new IllegalArgumentException("Line items are required when creating invoice");
            }
        }
    }
    public PaymentRequest toPaymentRequest() {
        return new PaymentRequest(
                    payeeId,
                    payeeType,
                    amount,
                    currency,
                    paymentMethod,
                    notes
        );
    }
}