package com.innoverse.erp_edu_api.features.income.invoices;

import com.innoverse.erp_edu_api.features.income.invoices.domain.InvoiceItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Getter
@ToString
@Builder
@AllArgsConstructor
public class Invoice {
    private final UUID invoiceId;
    private final UUID payeeId;
    private final String payeeType;
    private final String invoiceNo;
    private final LocalDate issueDate;
    private LocalDate dueDate;
    private final String currency;
    private final String notes;

    private Status status;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private final List<InvoiceItem> lineItems;
    private final LocalDate createdAt;
    private LocalDate updatedAt;

    public enum Status {
        DRAFT, ISSUED, PARTIALLY_PAID, PAID, OVERDUE, CANCELLED, REFUNDED
    }

    private Invoice(
            UUID payeeId,
            String payeeType,
            LocalDate dueDate,
            String currency,
            String notes
    ) {
        Objects.requireNonNull(payeeId, "payeeId is required");
        if (payeeType == null || payeeType.isBlank())
            throw new IllegalArgumentException("PayeeType is required");
        if (!isValidCurrency(currency))
            throw new IllegalArgumentException("Invalid currency code");
        if (dueDate != null && dueDate.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Due date cannot be in the past");

        this.invoiceId = UUID.randomUUID();
        this.payeeId = payeeId;
        this.payeeType = payeeType;
        this.invoiceNo = generateInvoiceNo();
        this.issueDate = null; // Will be set when issued
        this.dueDate = dueDate;
        this.currency = currency;
        this.notes = notes;
        this.status = Status.DRAFT;
        this.totalAmount = BigDecimal.ZERO;
        this.amountPaid = BigDecimal.ZERO;
        this.lineItems = new ArrayList<>();
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
    }

    public static Invoice create(
            UUID payeeId,
            String payeeType,
            LocalDate dueDate,
            String currency,
            String notes
    ) {
        return new Invoice(payeeId, payeeType, dueDate, currency, notes);
    }

    // ---------------------- Business Behavior ----------------------
    public void addLineItem(InvoiceItem item) {
        ensureModifiable();
        this.lineItems.add(item);
        recalculateTotals();
        this.updatedAt = LocalDate.now();
    }

    public void removeLineItem(UUID lineItemId) {
        ensureModifiable();
        boolean removed = this.lineItems.removeIf(item -> item.getLineItemId().equals(lineItemId));
        if (removed) {
            recalculateTotals();
            this.updatedAt = LocalDate.now();
        }
    }

    public void issue() {
        ensureStatus(Status.DRAFT);
        if (lineItems.isEmpty()) throw new IllegalStateException("Invoice must have line items before issuing");
        if (dueDate == null) throw new IllegalStateException("Due date must be set before issuing");

        this.status = Status.ISSUED;
        this.updatedAt = LocalDate.now();
    }

    public void applyPayment(BigDecimal amount) {
        if (status != Status.ISSUED && status != Status.PARTIALLY_PAID && status != Status.OVERDUE) {
            throw new IllegalStateException("Payments can only be applied to ISSUED, PARTIALLY_PAID, or OVERDUE invoices");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Payment must be positive");
        if (amountPaid.add(amount).compareTo(totalAmount) > 0)
            throw new IllegalArgumentException("Payment exceeds invoice total");

        this.amountPaid = this.amountPaid.add(amount);
        updateStatus();
        this.updatedAt = LocalDate.now();
    }

    public void markAsCancelled() {
        ensureStatus(Status.DRAFT);
        this.status = Status.CANCELLED;
        this.updatedAt = LocalDate.now();
    }

    public void markAsRefunded() {
        ensureStatus(Status.PAID);
        this.status = Status.REFUNDED;
        this.updatedAt = LocalDate.now();
    }

    public void updateDueDate(LocalDate newDueDate) {
        ensureModifiable();
        if (newDueDate.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Due date cannot be in the past");
        this.dueDate = newDueDate;
        this.updatedAt = LocalDate.now();
    }

    public void checkOverdue() {
        if ((status == Status.ISSUED || status == Status.PARTIALLY_PAID) &&
                dueDate != null && LocalDate.now().isAfter(dueDate)) {
            this.status = Status.OVERDUE;
            this.updatedAt = LocalDate.now();
        }
    }

    public BigDecimal getAmountDue() {
        return totalAmount.subtract(amountPaid);
    }

    public boolean isFullyPaid() {
        return status == Status.PAID;
    }

    public boolean isOverdue() {
        return status == Status.OVERDUE;
    }

    public boolean isDraft() {
        return status == Status.DRAFT;
    }

    public boolean isPayable() {
        return status == Status.ISSUED || status == Status.PARTIALLY_PAID || status == Status.OVERDUE;
    }

    public boolean hasPayments() {
        return amountPaid.compareTo(BigDecimal.ZERO) > 0;
    }

    // ---------------------- Private Helpers ----------------------

    private void recalculateTotals() {
        this.totalAmount = lineItems.stream()
                .map(InvoiceItem::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        updateStatus();
    }

    private void updateStatus() {
        if (status == Status.CANCELLED || status == Status.REFUNDED) return;

        if (amountPaid.compareTo(BigDecimal.ZERO) == 0) {
            if (dueDate != null && LocalDate.now().isAfter(dueDate)) {
                status = Status.OVERDUE;
            } else if (status != Status.DRAFT) {
                status = Status.ISSUED;
            }
        } else if (amountPaid.compareTo(totalAmount) < 0) {
            status = Status.PARTIALLY_PAID;
            if (dueDate != null && LocalDate.now().isAfter(dueDate)) {
                status = Status.OVERDUE;
            }
        } else {
            status = Status.PAID;
        }
    }

    private void ensureStatus(Status expected) {
        if (status != expected) {
            throw new IllegalStateException("Invalid lifecycle transition. Expected " + expected + " but was " + status);
        }
    }

    private void ensureModifiable() {
        if (status != Status.DRAFT) {
            throw new IllegalStateException("Cannot modify invoice once it is issued");
        }
    }

    private static String generateInvoiceNo() {
        return "INV-" + System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private static boolean isValidCurrency(String code) {
        try {
            Currency.getInstance(code);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}