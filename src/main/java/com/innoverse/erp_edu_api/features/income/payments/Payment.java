package com.innoverse.erp_edu_api.features.income.payments;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Payment {
    private final UUID paymentId;
    private final UUID payeeId;
    private final String payeeType;
    private final UUID invoiceId;
    private final String paymentNo;
    private final LocalDateTime paymentDate;
    private final BigDecimal amount;
    private final String currency;
    private final PaymentMethod paymentMethod;
    private final PaymentStatus status;
    private final String paymentNotes;

    public enum PaymentMethod {
        CASH, BANK_TRANSFER, ECOCASH, ONEMONEY, CHEQUE, CREDIT_CARD, DEBIT_CARD
    }

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, REFUNDED, PARTIALLY_REFUNDED
    }

    public static Payment create(
            UUID payeeId,
            String payeeType,
            UUID invoiceId,
            BigDecimal amount,
            String currency,
            PaymentMethod paymentMethod,
            String paymentNotes) {

        Payment payment = Payment.builder()
                .paymentId(UUID.randomUUID())
                .payeeId(payeeId)
                .payeeType(payeeType)
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

    public void validate() {
        if (paymentId == null) {
            throw new IllegalArgumentException("Payment ID cannot be null");
        }
        if (payeeId == null) {
            throw new IllegalArgumentException("Payee ID cannot be null");
        }
        if (payeeType == null || payeeType.isBlank()) {
            throw new IllegalArgumentException("Payee type cannot be null or empty");
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

    // State transition methods with domain validation
    public Payment markAsCompleted() {
        validateTransition(PaymentStatus.COMPLETED);
        return this.toBuilder()
                .status(PaymentStatus.COMPLETED)
                .build();
    }

    public Payment markAsFailed(String reason) {
        validateTransition(PaymentStatus.FAILED);
        String updatedNotes = reason != null ? reason : this.paymentNotes;
        return this.toBuilder()
                .status(PaymentStatus.FAILED)
                .paymentNotes(updatedNotes)
                .build();
    }

    public Payment markAsRefunded() {
        validateTransition(PaymentStatus.REFUNDED);
        return this.toBuilder()
                .status(PaymentStatus.REFUNDED)
                .build();
    }

    public Payment markAsPartiallyRefunded() {
        validateTransition(PaymentStatus.PARTIALLY_REFUNDED);
        return this.toBuilder()
                .status(PaymentStatus.PARTIALLY_REFUNDED)
                .build();
    }

    public Payment markAsPending() {
        validateTransition(PaymentStatus.PENDING);
        return this.toBuilder()
                .status(PaymentStatus.PENDING)
                .build();
    }

    public Payment updateNotes(String notes) {
        if (notes != null && notes.length() > 1000) {
            throw new IllegalArgumentException("Payment notes cannot exceed 1000 characters");
        }
        return this.toBuilder()
                .paymentNotes(notes)
                .build();
    }

    // Core state transition validation logic
    private void validateTransition(PaymentStatus newStatus) {
        if (this.status == newStatus) {
            throw new IllegalStateException("Payment is already in status: " + newStatus);
        }

        switch (this.status) {
            case REFUNDED:
                throw new IllegalStateException("Cannot modify a refunded payment");

            case FAILED:
                if (newStatus != PaymentStatus.PENDING) {
                    throw new IllegalStateException("Failed payments can only be retried to PENDING");
                }
                break;

            case COMPLETED:
                if (newStatus != PaymentStatus.REFUNDED && newStatus != PaymentStatus.PARTIALLY_REFUNDED) {
                    throw new IllegalStateException("Completed payments can only be refunded or partially refunded");
                }
                break;

            case PARTIALLY_REFUNDED:
                if (newStatus != PaymentStatus.REFUNDED) {
                    throw new IllegalStateException("Partially refunded payments can only be fully refunded");
                }
                break;

            case PENDING:
                // PENDING can transition to COMPLETED or FAILED
                if (newStatus != PaymentStatus.COMPLETED && newStatus != PaymentStatus.FAILED) {
                    throw new IllegalStateException("Pending payments can only transition to COMPLETED or FAILED");
                }
                break;
        }

        // Additional business rules
        if ((newStatus == PaymentStatus.REFUNDED || newStatus == PaymentStatus.PARTIALLY_REFUNDED) && !isRefundable()) {
            throw new IllegalStateException("Payment is not refundable");
        }
    }

    // Query methods
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

    // Helper method to get allowed transitions (useful for UI)
    public PaymentStatus[] getAllowedTransitions() {
        switch (this.status) {
            case PENDING:
                return new PaymentStatus[]{PaymentStatus.COMPLETED, PaymentStatus.FAILED};
            case COMPLETED:
                return new PaymentStatus[]{PaymentStatus.REFUNDED, PaymentStatus.PARTIALLY_REFUNDED};
            case FAILED:
                return new PaymentStatus[]{PaymentStatus.PENDING};
            case PARTIALLY_REFUNDED:
                return new PaymentStatus[]{PaymentStatus.REFUNDED};
            case REFUNDED:
                return new PaymentStatus[]{};
            default:
                return new PaymentStatus[]{};
        }
    }

    public boolean canTransitionTo(PaymentStatus newStatus) {
        for (PaymentStatus allowed : getAllowedTransitions()) {
            if (allowed == newStatus) {
                return true;
            }
        }
        return false;
    }
}