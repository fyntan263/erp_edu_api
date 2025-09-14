package com.innoverse.erp_edu_api.features.income.invoices.adapters;

import com.innoverse.erp_edu_api.features.income.invoices.domain.InvoiceItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Table("invoice_line_items")
@Getter
@Builder
@AllArgsConstructor
public class InvoiceItemEntity {

    @Id
    @Column("line_item_id")
    private UUID lineItemId;

    @Column("invoice_id")
    private UUID invoiceId;

    @Column("income_source_id")
    private UUID incomeSourceId;

    @Column("description")
    private String description;

    @Column("quantity")
    private Integer quantity;

    @Column("unit_price")
    private BigDecimal unitPrice;

    @Column("tax_rate")
    private BigDecimal taxRate;

    @Column("discount_percentage")
    private BigDecimal discount;

    public static InvoiceItemEntity fromDomain(UUID invoiceId, InvoiceItem domain) {
        return InvoiceItemEntity.builder()
                .lineItemId(domain.getLineItemId())
                .invoiceId(invoiceId)
                .incomeSourceId(domain.getIncomeSourceId())
                .description(domain.getDescription())
                .quantity(domain.getQuantity())
                .unitPrice(domain.getUnitPrice())
                .taxRate(domain.getTaxRate())
                .discount(domain.getDiscount())
                .build();
    }

    public InvoiceItem toDomain() {
        return com.innoverse.erp_edu_api.features.income.invoices.domain.InvoiceItem.builder()
                .lineItemId(lineItemId)
                .incomeSourceId(incomeSourceId)
                .description(description)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .taxRate(taxRate)
                .discount(discount)
                .build();
    }
}