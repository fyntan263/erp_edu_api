package com.innoverse.erp_edu_api.features.income.invoices;

import com.innoverse.erp_edu_api.features.income.invoices.domain.InvoiceItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class Invoice {
    private UUID invoiceId;
    private UUID entityId;
    private String entityType;
    private String description;
    private String invoiceNo;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private String currency;
    private Status status;
    private String notes;
    private List<InvoiceItem> lineItems;

    public enum Status {
        DRAFT, ISSUED, PARTIALLY_PAID, PAID, OVERDUE, CANCELLED, REFUNDED
    }

    // What we can CREATE - Factory method for new invoices
    public static Invoice create(
            UUID entityId,
            String entityType,
            String description,
            LocalDate dueDate,
            String currency,
            String notes) {

        return Invoice.builder()
                .invoiceId(UUID.randomUUID())
                .entityId(entityId)
                .entityType(entityType)
                .invoiceNo(generateInvoiceNo())
                .issueDate(LocalDate.now())
                .dueDate(dueDate)
                .description(description)
                .totalAmount(BigDecimal.ZERO)
                .amountPaid(BigDecimal.ZERO)
                .currency(currency)
                .status(Status.DRAFT)
                .notes(notes)
                .lineItems(List.of())
                .build();
    }

    // What we can DERIVE - Business logic methods
    private static String generateInvoiceNo() {
        return "INV-" + System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    public BigDecimal getAmountDue() {
        return totalAmount.subtract(amountPaid);
    }

    public boolean isOverdue() {
        return status == Status.OVERDUE ||
                (status == Status.ISSUED && LocalDate.now().isAfter(dueDate));
    }

    public boolean isFullyPaid() {
        return amountPaid.compareTo(totalAmount) >= 0;
    }

    // Domain behavior methods
    public void addLineItem(InvoiceItem lineItem) {
        this.lineItems.add(lineItem);
        recalculateTotals();
    }

    public void removeLineItem(UUID lineItemId) {
        this.lineItems.removeIf(item -> item.getLineItemId().equals(lineItemId));
        recalculateTotals();
    }

    public void applyPayment(BigDecimal paymentAmount) {
        this.amountPaid = this.amountPaid.add(paymentAmount);
        updateStatus();
    }

    public void markAsCancelled() {
        if (this.status != Status.DRAFT) {
            throw new IllegalStateException("Only draft invoices can be cancelled");
        }
        this.status = Status.CANCELLED;
    }

    private void recalculateTotals() {
        this.totalAmount = lineItems.stream()
                .map(InvoiceItem::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        updateStatus();
    }

    private void updateStatus() {
        if (status == Status.CANCELLED || status == Status.REFUNDED) {
            return; // Don't change status for cancelled/refunded invoices
        }

        BigDecimal amountDue = getAmountDue();
        if (amountDue.compareTo(BigDecimal.ZERO) == 0) {
            status = Status.PAID;
        } else if (amountPaid.compareTo(BigDecimal.ZERO) > 0) {
            status = Status.PARTIALLY_PAID;
        } else {
            status = LocalDate.now().isAfter(dueDate) ? Status.OVERDUE : Status.ISSUED;
        }
    }

    public boolean isPayable() {
        // Basic status check
        boolean validStatus = status == Status.ISSUED ||
                status == Status.PARTIALLY_PAID ||
                status == Status.OVERDUE;

        // Business logic checks
        boolean hasBalance = getAmountDue().compareTo(BigDecimal.ZERO) > 0;
        boolean notFinalized = status != Status.PAID &&
                status != Status.CANCELLED &&
                status != Status.REFUNDED;

        return validStatus && hasBalance && notFinalized;
    }

    public boolean isFullyPayable() {
        return isPayable() && !isOverdue();
    }

    public boolean isOverduePayable() {
        return isPayable() && isOverdue();
    }
}