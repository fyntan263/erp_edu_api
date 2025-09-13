package com.innoverse.erp_edu_api.features.income.invoices.adapters;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record InvoiceWithItemsView(
        UUID invoiceId,
        UUID entityId,
        String invoiceFor,
        String invoiceNo,
        String description,
        LocalDate issueDate,
        LocalDate dueDate,
        BigDecimal totalAmount,
        BigDecimal amountPaid,
        String currency,
        String status,
        String notes,
        List<InvoiceItemEntity> lineItems
) { }