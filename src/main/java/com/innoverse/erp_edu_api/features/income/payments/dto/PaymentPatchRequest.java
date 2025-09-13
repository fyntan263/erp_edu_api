package com.innoverse.erp_edu_api.features.income.payments.dto;

import com.innoverse.erp_edu_api.features.income.payments.Payment;

public record PaymentPatchRequest(
        Payment.PaymentStatus status,
        String paymentNotes
) {
}
