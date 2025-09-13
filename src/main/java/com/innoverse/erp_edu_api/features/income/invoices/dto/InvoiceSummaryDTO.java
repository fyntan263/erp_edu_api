package com.innoverse.erp_edu_api.features.income.invoices.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceSummaryDTO(
        UUID entityId,
        String entityType,
        BigDecimal totalInvoiced,
        BigDecimal totalPaid,
        BigDecimal totalOutstanding,
        long invoiceCount
) {
}
