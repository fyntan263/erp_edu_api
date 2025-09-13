package com.innoverse.erp_edu_api.features.income.payments.dto;

import com.innoverse.erp_edu_api.features.income.payments.Payment;

import java.time.LocalDateTime;
import java.util.UUID;

// Filter DTO
public record PaymentFilter(
        UUID studentId,
        UUID invoiceId,
        Payment.PaymentStatus status,
        Payment.PaymentMethod paymentMethod,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String paymentNo,
        Boolean includeDeleted
) {
}