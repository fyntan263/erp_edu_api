package com.innoverse.erp_edu_api.features.income.invoices;

import com.innoverse.erp_edu_api.features.income.invoices.dto.*;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceServicePort {
    @Transactional
    Invoice createInvoice(InvoiceRequest request);

    @Transactional
    Invoice createInvoice(
            UUID entityId,
            String entityType,
            String description,
            LocalDate dueDate,
            String currency,
            List<InvoiceItemRequest> invoiceItemRequests,
            String notes
    );

    @Transactional(readOnly = true)
    Optional<Invoice> getInvoiceById(UUID invoiceId);
    @Transactional(readOnly = true)
    List<Invoice> getInvoicesByEntityId(UUID entityId);



    @Transactional(readOnly = true)
    List<Invoice> getInvoicesByEntityIdAndType(UUID entityId, String invoiceFor);

    @Transactional(readOnly = true)
    List<Invoice> getInvoicesByType(String invoiceFor);

    @Transactional(readOnly = true)
    List<Invoice> getInvoicesByStatus(Invoice.Status status);

    @Transactional(readOnly = true)
    List<Invoice> getOverdueInvoices();

    @Transactional
    Invoice addLineItem(UUID invoiceId, InvoiceItemRequest request);

    @Transactional
    Invoice removeLineItem(UUID invoiceId, UUID lineItemId);

    @Transactional
    boolean applyPayment(UUID invoiceId, BigDecimal amount);

    @Transactional(readOnly = true)
    Optional<Invoice> getInvoiceByNo(String invoiceNo);
    @Transactional
    void updateStatus(UUID invoiceId, Invoice.Status status);

    default void validateStatusTransition(Invoice.Status current, Invoice.Status next) {
        // Validation logic here
    }

    @Transactional
    void deleteInvoice(UUID invoiceId);

    @Transactional(readOnly = true)
    BigDecimal getTotalInvoicedAmount(UUID entityId);

    @Transactional(readOnly = true)
    BigDecimal getTotalPaidAmount(UUID entityId);

    @Transactional(readOnly = true)
    BigDecimal getOutstandingBalance(UUID entityId);

    @Transactional(readOnly = true)
    Long getInvoiceCount(UUID entityId);

    boolean validateInvoiceNo(String invoiceNo);

}
