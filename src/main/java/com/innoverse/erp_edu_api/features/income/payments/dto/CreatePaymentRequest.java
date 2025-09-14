package com.innoverse.erp_edu_api.features.income.payments.dto;

import com.innoverse.erp_edu_api.features.income.payments.Payment;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

// Request DTOs
public record CreatePaymentRequest(
        UUID payeeId,
        String payeeType,
        UUID invoiceId,
        BigDecimal amount,
        String currency,
        Payment.PaymentMethod paymentMethod,
        String paymentNotes
) {}
