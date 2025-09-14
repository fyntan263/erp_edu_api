package com.innoverse.erp_edu_api.features.income.payments.dto;

import java.time.LocalDateTime;

import com.innoverse.erp_edu_api.features.income.payments.Payment;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        UUID payeeId,
        String payeeType,
        UUID invoiceId,
        String paymentNo,
        LocalDateTime paymentDate,
        BigDecimal amount,
        String currency,
        Payment.PaymentMethod paymentMethod,
        Payment.PaymentStatus status,
        String paymentNotes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PaymentResponse fromDomain(Payment payment) {
        return new PaymentResponse(
                payment.getPaymentId(),
                payment.getPayeeId(),
                payment.getPayeeType(),
                payment.getInvoiceId(),
                payment.getPaymentNo(),
                payment.getPaymentDate(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getPaymentNotes(),
                null, // These would come from a separate audit DTO
                null
        );
    }
}

