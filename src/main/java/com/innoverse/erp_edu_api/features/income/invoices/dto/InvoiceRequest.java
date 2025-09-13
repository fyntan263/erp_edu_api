package com.innoverse.erp_edu_api.features.income.invoices.dto;

import com.innoverse.erp_edu_api.features.income.invoices.Invoice;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

public record InvoiceRequest(
        UUID entityId,
        String entityType,
        String description,
        LocalDate dueDate,
        String currency,
        Invoice.Status status,
        String notes,
        List<InvoiceItemRequest>lineItems
) { }
