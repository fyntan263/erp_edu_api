package com.innoverse.erp_edu_api.features.income.payments.dto;

import com.innoverse.erp_edu_api.features.income.payments.Payment;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(
        UUID payeeId,
        String payeeType,
        BigDecimal amount,
        String currency,
        Payment.PaymentMethod paymentMethod,
        String notes
) {

}
