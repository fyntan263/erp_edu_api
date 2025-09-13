package com.innoverse.erp_edu_api.features.income.invoices.dto;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ApplyPaymentRequest(
        @Positive BigDecimal amount
) {
}
