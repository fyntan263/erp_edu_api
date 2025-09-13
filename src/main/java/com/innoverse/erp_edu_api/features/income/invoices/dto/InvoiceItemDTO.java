package com.innoverse.erp_edu_api.features.income.invoices.dto;

import com.innoverse.erp_edu_api.features.income.invoices.domain.InvoiceItem;

import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceItemDTO(
        UUID lineItemId,
        UUID incomeSourceId,
        String description,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal taxRate,
        BigDecimal discountPercentage,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal taxableAmount,
        BigDecimal taxAmount,
        BigDecimal totalAmount
) {
    public static InvoiceItemDTO fromDomain(InvoiceItem item) {
        return new InvoiceItemDTO(
                item.getLineItemId(),
                item.getIncomeSourceId(),
                item.getDescription(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTaxRate(),
                item.getDiscountPercentage(),
                item.getSubtotal(),
                item.getDiscountAmount(),
                item.getTaxableAmount(),
                item.getTaxAmount(),
                item.getTotalAmount()
        );
    }
}
