package com.innoverse.erp_edu_api.features.income.invoices.dto;

import com.innoverse.erp_edu_api.features.income.invoices.Invoice;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

public record InvoiceRequest(
        UUID payeeId,
        String payeeType,
        LocalDate dueDate,
        String currency,
        String notes,
        List<InvoiceItemRequest>lineItems
) { }
