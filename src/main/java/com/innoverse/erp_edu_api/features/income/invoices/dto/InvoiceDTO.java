package com.innoverse.erp_edu_api.features.income.invoices.dto;

import com.innoverse.erp_edu_api.features.income.invoices.Invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record InvoiceDTO(
        UUID invoiceId,
        UUID payeeId,
        String payeeType,
        String invoiceNo,
        LocalDate issueDate,
        LocalDate dueDate,
        BigDecimal totalAmount,
        BigDecimal amountPaid,
        String currency,
        Invoice.Status status,
        String notes,
        List<InvoiceItemDTO> lineItems
) {
    public static InvoiceDTO fromDomain(Invoice invoice) {
        return new InvoiceDTO(
                invoice.getInvoiceId(),
                invoice.getPayeeId(),
                invoice.getPayeeType(),
                invoice.getInvoiceNo(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getTotalAmount(),
                invoice.getAmountPaid(),
                invoice.getCurrency(),
                invoice.getStatus(),
                invoice.getNotes(),
                invoice.getLineItems().stream()
                        .map(InvoiceItemDTO::fromDomain)
                        .collect(Collectors.toList())
        );
    }
}
