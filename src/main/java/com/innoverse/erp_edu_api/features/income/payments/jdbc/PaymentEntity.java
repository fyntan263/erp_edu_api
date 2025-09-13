package com.innoverse.erp_edu_api.features.income.payments.jdbc;

import com.innoverse.erp_edu_api.features.income.payments.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.UUID;

@Table("payments")
@Getter
@Builder
@AllArgsConstructor
public class PaymentEntity {

    @Id
    @Column("payment_id")
    private UUID paymentId;

    @Column("entity_id") // Changed from student_id
    private UUID entityId;

    @Column("entity_type") // New field
    private String entityType;

    @Column("invoice_id")
    private UUID invoiceId;

    @Column("payment_number")
    private String paymentNo;

    @Column("payment_date")
    private LocalDateTime paymentDate;

    @Column("amount")
    private BigDecimal amount;

    @Column("currency")
    private String currency;

    @Column("payment_method")
    private String paymentMethod;

    @Column("status")
    private String status;

    @Column("payment_notes")
    private String paymentNotes;

    @Column("deleted")
    private Boolean deleted;

    @Column("deleted_at")
    private Instant deletedAt;

    @CreatedDate
    @Column("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private Instant updatedAt;

    public static PaymentEntity fromDomain(Payment domain) {
        return PaymentEntity.builder()
                .paymentId(domain.getPaymentId())
                .entityId(domain.getEntityId())
                .entityType(domain.getEntityType())
                .invoiceId(domain.getInvoiceId())
                .paymentNo(domain.getPaymentNo())
                .paymentDate(domain.getPaymentDate())
                .amount(domain.getAmount())
                .currency(domain.getCurrency())
                .paymentMethod(domain.getPaymentMethod().name())
                .status(domain.getStatus().name())
                .paymentNotes(domain.getPaymentNotes())
                .deleted(false)
                .deletedAt(null)
                .build();
    }

    public Payment toDomain() {
        return Payment.builder()
                .paymentId(paymentId)
                .entityId(entityId)
                .entityType(entityType)
                .invoiceId(invoiceId)
                .paymentNo(paymentNo)
                .paymentDate(paymentDate)
                .amount(amount)
                .currency(currency)
                .paymentMethod(Payment.PaymentMethod.valueOf(paymentMethod))
                .status(Payment.PaymentStatus.valueOf(status))
                .paymentNotes(paymentNotes)
                .build();
    }

    public void updateFromDomain(Payment domain) {
        this.status = domain.getStatus().name();
        this.paymentNotes = domain.getPaymentNotes();
    }
}