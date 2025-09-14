package com.innoverse.erp_edu_api.features.income.invoices.services;

import com.innoverse.erp_edu_api.features.income.invoices.InvoiceServicePort;
import com.innoverse.erp_edu_api.features.income.invoices.adapters.InvoiceRepositoryAdapter;
import com.innoverse.erp_edu_api.features.income.invoices.Invoice;
import com.innoverse.erp_edu_api.features.income.invoices.domain.InvoiceItem;
import com.innoverse.erp_edu_api.features.income.invoices.dto.InvoiceRequest;
import com.innoverse.erp_edu_api.features.income.invoices.dto.InvoiceItemRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService implements InvoiceServicePort {
    private final InvoiceRepositoryAdapter invoiceRepository;
//    private final PaymentServiceAdapter paymentServiceAdapter;

    @Transactional
    @Override
    public Invoice createInvoice(InvoiceRequest command) {
        Invoice invoice = Invoice.create(
                command.payeeId(),
                command.payeeType(),
                command.dueDate(),
                command.currency(),
                command.notes()
        );

        // Add line items if provided during creation
        if (command.lineItems() != null && !command.lineItems().isEmpty()) {
            for (InvoiceItemRequest itemCommand : command.lineItems()) {
                InvoiceItem lineItem = InvoiceItem.of(
                        itemCommand.incomeSourceId(),
                        itemCommand.description(),
                        itemCommand.quantity(),
                        itemCommand.unitPrice(),
                        itemCommand.taxRate(),
                        itemCommand.discountPercentage()
                );
                invoice.addLineItem(lineItem);
            }

            // Auto-issue if line items are provided and due date is set
            if (command.dueDate() != null) {
                invoice.issue();
            }
        }
        log.info("Creating invoice: {}", invoice);
        return invoiceRepository.save(invoice);
    }

    @Transactional
    @Override
    public Invoice createDraftInvoice(UUID payeeId, String payeeType, String currency, String notes) {
        Invoice invoice = Invoice.create(
                payeeId,
                payeeType,
                null, // dueDate can be null for drafts
                currency,
                notes
        );

        log.info("Creating draft invoice: {}", invoice);
        return invoiceRepository.save(invoice);
    }

    @Transactional
    @Override
    public Invoice issueInvoice(UUID invoiceId) {
        Invoice invoice = getInvoiceById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        invoice.issue();
        log.info("Issuing invoice: {}", invoiceId);
        return invoiceRepository.save(invoice);
    }

    @Transactional
    @Override
    public Invoice updateDraftInvoice(UUID invoiceId, InvoiceRequest command) {
        Invoice invoice = getInvoiceById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        if (!invoice.isDraft()) {
            throw new IllegalStateException("Only draft invoices can be updated");
        }

        // Update due date if provided
        if (command.dueDate() != null) {
            // Create a method in Invoice to update due date
            invoice.updateDueDate(command.dueDate());
        }

        // Clear existing line items and add new ones
        // Note: This is simplified - you might want more granular control
        if (command.lineItems() != null) {
            // Remove all existing line items
            invoice.getLineItems().clear();

            // Add new line items
            for (InvoiceItemRequest itemCommand : command.lineItems()) {
                InvoiceItem lineItem = InvoiceItem.of(
                        itemCommand.incomeSourceId(),
                        itemCommand.description(),
                        itemCommand.quantity(),
                        itemCommand.unitPrice(),
                        itemCommand.taxRate(),
                        itemCommand.discountPercentage()
                );
                invoice.addLineItem(lineItem);
            }
        }

        log.info("Updating draft invoice: {}", invoiceId);
        return invoiceRepository.save(invoice);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Invoice> getInvoiceById(UUID invoiceId) {
        return invoiceRepository.findById(invoiceId);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Invoice> getInvoiceByNo(String invoiceNo) {
        return invoiceRepository.findByInvoiceNo(invoiceNo);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Invoice> getInvoicesByPayeeId(UUID payeeId) {
        return invoiceRepository.findBypayeeId(payeeId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Invoice> getInvoicesByPayeeIdAndType(UUID payeeId, String type) {
        return invoiceRepository.findBypayeeIdAndName(payeeId, type);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Invoice> getInvoicesByType(String invoiceFor) {
        return invoiceRepository.findByType(invoiceFor);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Invoice> getInvoicesByStatus(Invoice.Status status) {
        return invoiceRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Invoice> getDraftInvoices() {
        return invoiceRepository.findByStatus(Invoice.Status.DRAFT);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Invoice> getOverdueInvoices() {
        return invoiceRepository.findOverdueInvoices(LocalDate.now());
    }

    @Transactional
    @Override
    public Invoice addLineItem(UUID invoiceId, InvoiceItemRequest command) {
        Invoice invoice = this.invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        if (!invoice.isDraft()) {
            throw new IllegalStateException("Cannot add line items to non-draft invoice");
        }

        InvoiceItem lineItem = InvoiceItem.of(
                command.incomeSourceId(),
                command.description(),
                command.quantity(),
                command.unitPrice(),
                command.taxRate(),
                command.discountPercentage()
        );

        invoice.addLineItem(lineItem);
        log.info("Adding line item to invoice: {}", invoiceId);
        return invoiceRepository.save(invoice);
    }

    @Transactional
    @Override
    public Invoice removeLineItem(UUID invoiceId, UUID lineItemId) {
        Invoice invoice = this.invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        if (!invoice.isDraft()) {
            throw new IllegalStateException("Cannot remove line items from non-draft invoice");
        }

        invoice.removeLineItem(lineItemId);
        log.info("Removing line item {} from invoice: {}", lineItemId, invoiceId);
        return invoiceRepository.save(invoice);
    }

    @Transactional
    @Override
    public boolean applyPayment(UUID invoiceId, BigDecimal amount) {
        Invoice invoice = this.invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        if (invoice.getStatus() == Invoice.Status.CANCELLED || invoice.getStatus() == Invoice.Status.REFUNDED) {
            throw new IllegalStateException("Cannot apply payment to cancelled or refunded invoice");
        }

        if (invoice.isDraft()) {
            throw new IllegalStateException("Cannot apply payment to draft invoice");
        }

        if (amount.compareTo(invoice.getAmountDue()) > 0) {
            throw new IllegalArgumentException("Payment amount exceeds amount due");
        }

        try {
            invoice.applyPayment(amount);
            invoiceRepository.save(invoice);
            log.info("Applied payment of {} to invoice: {}", amount, invoiceId);
            return true;
        } catch (Exception ex) {
            log.error("Failed to apply payment to invoice: {}", invoiceId, ex);
            return false;
        }
    }

    @Transactional
    @Override
    public void cancelInvoice(UUID invoiceId) {
        Invoice invoice = this.invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        if (!invoice.isDraft()) {
            throw new IllegalStateException("Only draft invoices can be cancelled directly");
        }

        invoice.markAsCancelled();
        invoiceRepository.save(invoice);
        log.info("Cancelled invoice: {}", invoiceId);
    }

    @Transactional
    @Override
    public void refundInvoice(UUID invoiceId) {
        Invoice invoice = this.invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        if (invoice.getStatus() != Invoice.Status.PAID) {
            throw new IllegalStateException("Only paid invoices can be refunded");
        }

        invoice.markAsRefunded();
        invoiceRepository.save(invoice);
        log.info("Refunded invoice: {}", invoiceId);
    }

    @Transactional
    @Override
    public void deleteInvoice(UUID invoiceId) {
        Invoice invoice = this.invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        if (!invoice.isDraft()) {
            throw new IllegalStateException("Cannot delete non-draft invoice");
        }

        invoiceRepository.deleteById(invoiceId);
        log.info("Deleted draft invoice: {}", invoiceId);
    }
}