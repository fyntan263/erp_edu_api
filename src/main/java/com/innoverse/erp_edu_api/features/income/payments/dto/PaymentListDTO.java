package com.innoverse.erp_edu_api.features.income.payments.dto;

import java.util.List;

public record PaymentListDTO(
        List<PaymentDTO> payments,
        long totalCount,
        int page,
        int size
) {
}
