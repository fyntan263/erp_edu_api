package com.innoverse.erp_edu_api.features.income.payments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class Payment {
    private UUID paymentId;
    private UUID entityId; // Changed from studentId to entityId
    private String entityType; // e.g., "STUDENT", "TEACHER", "COURSE"
    private UUID invoiceId;
    private String paymentNo;
    private LocalDateTime paymentDate;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String paymentNotes;

    public enum PaymentMethod {
        CASH, BANK_TRANSFER, ECOCASH, ONEMONEY, CHEQUE, CREDIT_CARD, DEBIT_CARD
    }

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, REFUNDED, PARTIALLY_REFUNDED
    }

    public static Payment create(
            UUID entityId,
            String entityType,
            UUID invoiceId,
            BigDecimal amount,
            String currency,
            PaymentMethod paymentMethod,
            String paymentNotes) {

        Payment payment = Payment.builder()
                .paymentId(UUID.randomUUID())
                .entityId(entityId)
                .entityType(entityType)
                .invoiceId(invoiceId)
                .paymentNo(generatePaymentNo())
                .paymentDate(LocalDateTime.now())
                .amount(amount)
                .currency(currency)
                .paymentMethod(paymentMethod)
                .status(PaymentStatus.PENDING)
                .paymentNotes(paymentNotes)
                .build();

        payment.validate();
        return payment;
    }

    private static String generatePaymentNo() {
        return "PAY-" + System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    // Validation method
    public void validate() {
        if (paymentId == null) {
            throw new IllegalArgumentException("Payment ID cannot be null");
        }
        if (entityId == null) {
            throw new IllegalArgumentException("Entity ID cannot be null");
        }
        if (entityType == null || entityType.isBlank()) {
            throw new IllegalArgumentException("Entity type cannot be null or empty");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Payment method cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        if (paymentNo == null || paymentNo.length() < 5) {
            throw new IllegalArgumentException("Payment number must be at least 5 characters");
        }
        if (paymentDate == null) {
            throw new IllegalArgumentException("Payment date cannot be null");
        }
        if (paymentDate.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Payment date cannot be in the future");
        }
    }

    // ... rest of the methods remain similar but updated for entityId'
    public void markAsPending() {
        this.status = PaymentStatus.PENDING;
    }
    public void markAsCompleted() {
        this.status = PaymentStatus.COMPLETED;
    }

    public void markAsFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.paymentNotes = reason != null ? reason : this.paymentNotes;
    }

    public void markAsRefunded() {
        if (!isCompleted()) {
            throw new IllegalStateException("Payment must be completed before refunding");
        }
        this.status = PaymentStatus.REFUNDED;
    }

    public void markAsPartiallyRefunded() {
        if (!isCompleted()) {
            throw new IllegalStateException("Payment must be completed before partial refund");
        }
        this.status = PaymentStatus.PARTIALLY_REFUNDED;
    }

    public void updateNotes(String notes) {
        if (notes != null && notes.length() > 1000) {
            throw new IllegalArgumentException("Payment notes cannot exceed 1000 characters");
        }
        this.paymentNotes = notes;
    }

    public boolean isCompleted() {
        return status == PaymentStatus.COMPLETED;
    }

    public boolean isRefundable() {
        return status == PaymentStatus.COMPLETED;
    }

    public boolean hasInvoice() {
        return invoiceId != null;
    }

    public boolean isValidForProcessing() {
        return status == PaymentStatus.PENDING && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean canBeModified() {
        return status != PaymentStatus.REFUNDED && status != PaymentStatus.FAILED;
    }
}