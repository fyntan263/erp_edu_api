package com.innoverse.erp_edu_api.features.income.invoices.services;

import com.innoverse.erp_edu_api.features.income.invoices.InvoiceServicePort;
import com.innoverse.erp_edu_api.features.income.invoices.adapters.InvoiceRepositoryAdapter;
import com.innoverse.erp_edu_api.features.income.invoices.Invoice;
import com.innoverse.erp_edu_api.features.income.invoices.domain.InvoiceItem;
import com.innoverse.erp_edu_api.features.income.invoices.dto.ApplyPaymentRequest;
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

    @Transactional
    public Invoice createInvoice(List<InvoiceItemRequest> request){
        return null;
    }
    @Transactional
    @Override
    public Invoice createInvoice(InvoiceRequest command) {
        Invoice invoice = Invoice.create(
                command.entityId(),
                command.entityType(),
                command.description(),
                command.dueDate(),
                command.currency(),
                command.notes()
        );

        // Add line items if provided during creation
        if (command.lineItems() != null && !command.lineItems().isEmpty()) {
            for (InvoiceItemRequest itemCommand : command.lineItems()) {
                InvoiceItem lineItem = InvoiceItem.create(
                        invoice.getInvoiceId(),
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
        return invoiceRepository.save(invoice);
    }

    @Override
    public Invoice createInvoice(UUID entityId, String entityType, String description, LocalDate dueDate, String currency, List<InvoiceItemRequest> invoiceItemRequests, String notes) {
        Invoice invoice = Invoice.create(
                entityId,
                entityType,
                description,
                dueDate,
                currency,
                notes
        );

        if (invoiceItemRequests != null && !invoiceItemRequests.isEmpty()) {
            for (InvoiceItemRequest item : invoiceItemRequests) {
                InvoiceItem lineItem = InvoiceItem.create(
                        invoice.getInvoiceId(),
                        item.incomeSourceId(),
                        item.description(),
                        item.quantity(),
                        item.unitPrice(),
                        item.taxRate(),
                        item.discountPercentage()
                );
                invoice.addLineItem(lineItem);
            }
        }
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
    public List<Invoice> getInvoicesByEntityId(UUID entityId) {
        return invoiceRepository.findByEntityId(entityId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Invoice> getInvoicesByEntityIdAndType(UUID entityId, String type) {
        return invoiceRepository.findByEntityIdAndName(entityId, type);
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
    public List<Invoice> getOverdueInvoices() {
        return invoiceRepository.findOverdueInvoices(LocalDate.now());
    }

    @Transactional
    @Override
    public Invoice addLineItem(UUID invoiceId, InvoiceItemRequest command) {
        Invoice invoice = this.invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        if (invoice.getStatus() != Invoice.Status.DRAFT) {
            throw new IllegalStateException("Cannot add line items to non-draft invoice");
        }

        InvoiceItem lineItem = InvoiceItem.create(
                invoiceId,
                command.incomeSourceId(),
                command.description(),
                command.quantity(),
                command.unitPrice(),
                command.taxRate(),
                command.discountPercentage()
        );

        invoice.addLineItem(lineItem);
        return invoiceRepository.save(invoice);
    }

    @Transactional
    @Override
    public Invoice removeLineItem(UUID invoiceId, UUID lineItemId) {
        Invoice invoice = this.invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        if (invoice.getStatus() != Invoice.Status.DRAFT) {
            throw new IllegalStateException("Cannot remove line items from non-draft invoice");
        }

        invoice.removeLineItem(lineItemId);
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

        if (amount.compareTo(invoice.getAmountDue()) > 0) {
            throw new IllegalArgumentException("Payment amount exceeds amount due");
        }

//        invoice.applyPayment(request.amount());
        try {
            this.invoiceRepository.applyPayment(invoiceId, amount);
            return true;
        }catch (Exception ex){
            return false;
        }

    }

    @Transactional
    @Override
    public void updateStatus(UUID invoiceId, Invoice.Status status) {
        Invoice invoice = this.invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        validateStatusTransition(invoice.getStatus(), status);

        // Use domain method for specific status changes
        if (status == Invoice.Status.CANCELLED) {
            invoice.markAsCancelled();
        }
         this.invoiceRepository.updateStatus(invoiceId, status);
    }

    @Transactional
    @Override
    public void deleteInvoice(UUID invoiceId) {
        Invoice invoice = this.invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        if (invoice.getStatus() != Invoice.Status.DRAFT) {
            throw new IllegalStateException("Cannot delete non-draft invoice");
        }

        invoiceRepository.deleteById(invoiceId);
    }

    @Transactional(readOnly = true)
    @Override
    public BigDecimal getTotalInvoicedAmount(UUID entityId) {
        return null;//invoiceRepository.sumInvoicedAmountByEntityId(entityId);
    }

    @Transactional(readOnly = true)
    @Override
    public BigDecimal getTotalPaidAmount(UUID entityId) {
        return null; //invoiceRepository.sumPaidAmountByEntityId(entityId);
    }

    @Transactional(readOnly = true)
    @Override
    public BigDecimal getOutstandingBalance(UUID entityId) {
        return null;//invoiceRepository.sumOutstandingAmountByEntityId(entityId);
    }

    @Transactional(readOnly = true)
    @Override
    public Long getInvoiceCount(UUID entityId) {
        return null;//invoiceRepository.countByEntityId(entityId);
    }

//    @Override
//    public String generateInvoiceNo() {
//        return "INV-" + System.currentTimeMillis() + "-" +
//                UUID.randomUUID().toString().substring(0, 4).toUpperCase();
//    }

    @Override
    public boolean validateInvoiceNo(String invoiceNo) {
        return invoiceNo != null && invoiceNo.startsWith("INV-");
    }
}