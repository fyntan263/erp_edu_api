package com.innoverse.erp_edu_api.features.income.invoices.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@ToString
@AllArgsConstructor
public class InvoiceItem {
    private UUID lineItemId;
    private UUID incomeSourceId;
    private String description;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal taxRate; // percentage
    private BigDecimal discount; // percentage

    public static InvoiceItem of(
            UUID incomeSourceId,
            String description,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal taxRate,
            BigDecimal discount) {

        return InvoiceItem.builder()
                .lineItemId(UUID.randomUUID())
                .incomeSourceId(incomeSourceId)
                .description(description)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .taxRate(taxRate != null ? taxRate : BigDecimal.ZERO)
                .discount(discount != null ? discount : BigDecimal.ZERO)
                .build();
    }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public BigDecimal getDiscountAmount() {
        return getSubtotal().multiply(discount.divide(BigDecimal.valueOf(100)));
    }

    public BigDecimal getTaxableAmount() {
        return getSubtotal().subtract(getDiscountAmount());
    }

    public BigDecimal getTaxAmount() {
        return getTaxableAmount().multiply(taxRate.divide(BigDecimal.valueOf(100)));
    }

    public BigDecimal getTotalAmount() {
        return getTaxableAmount().add(getTaxAmount());
    }
}