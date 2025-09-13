package com.innoverse.erp_edu_api.features.income.payments.dto;

import java.time.LocalDateTime;

public record PaymentAuditView(
        PaymentResponse payment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean deleted,
        LocalDateTime deletedAt
) {
}
