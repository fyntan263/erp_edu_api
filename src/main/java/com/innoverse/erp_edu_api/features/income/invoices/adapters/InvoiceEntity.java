package com.innoverse.erp_edu_api.features.income.invoices.adapters;

import com.innoverse.erp_edu_api.features.income.invoices.Invoice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

@Table("invoices")
@Getter
@Builder
@AllArgsConstructor
public class InvoiceEntity {
    @Id
    @Column("invoice_id")
    private UUID invoiceId;

    @Column("entity_id")
    private UUID entityId;

    @Column("entity_type")
    private String entityType;

    @Column("invoice_number")
    private String invoiceNo;

    @Column("description")
    private String description;

    @Column("issue_date")
    private LocalDate issueDate;

    @Column("due_date")
    private LocalDate dueDate;

    @Column("total_amount")
    private BigDecimal totalAmount;

    @Column("amount_paid")
    private BigDecimal amountPaid;

    @Column("currency")
    private String currency;

    @Column("status")
    private String status;

    @Column("notes")
    private String notes;

    @CreatedDate
    @Column("created_at")
    private LocalDate createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDate updatedAt;

    public static InvoiceEntity fromDomain(Invoice domain) {
        return InvoiceEntity.builder()
                .invoiceId(domain.getInvoiceId())
                .entityId(domain.getEntityId())
                .entityType(domain.getEntityType())
                .invoiceNo(domain.getInvoiceNo())
                .description(domain.getDescription())
                .issueDate(domain.getIssueDate())
                .dueDate(domain.getDueDate())
                .totalAmount(domain.getTotalAmount())
                .amountPaid(domain.getAmountPaid())
                .currency(domain.getCurrency())
                .status(domain.getStatus().name())
                .notes(domain.getNotes())
                .build();
    }

    public Invoice toDomain() {
        return Invoice.builder()
                .invoiceId(invoiceId)
                .entityId(entityId)
                .entityType(entityType)
                .invoiceNo(invoiceNo)
                .description(description)
                .issueDate(issueDate)
                .dueDate(dueDate)
                .totalAmount(totalAmount)
                .amountPaid(amountPaid)
                .currency(currency)
                .status(Invoice.Status.valueOf(status))
                .notes(notes)
                .lineItems(List.of()) // Line items are loaded separately
                .build();
    }

    public void updateFromDomain(Invoice domain) {
        this.totalAmount = domain.getTotalAmount();
        this.amountPaid = domain.getAmountPaid();
        this.status = domain.getStatus().name();
        this.notes = domain.getNotes();
        this.updatedAt = LocalDate.now();
    }
}