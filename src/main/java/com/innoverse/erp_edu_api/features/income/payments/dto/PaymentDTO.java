package com.innoverse.erp_edu_api.features.income.payments.dto;

import com.innoverse.erp_edu_api.features.income.payments.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.UUID;

// Response DTOs
public record PaymentDTO(
        UUID paymentId,
        UUID entityId,
        String entityType,
        UUID invoiceId,
        String paymentNo,
        LocalDateTime paymentDate,
        BigDecimal amount,
        String currency,
        Payment.PaymentMethod paymentMethod,
        Payment.PaymentStatus status,
        String paymentNotes
) {
    public static PaymentDTO fromDomain(Payment payment) {
        return new PaymentDTO(
                payment.getPaymentId(),
                payment.getEntityId(),
                payment.getEntityType(),
                payment.getInvoiceId(),
                payment.getPaymentNo(),
                payment.getPaymentDate(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getPaymentNotes()
        );
    }
}

