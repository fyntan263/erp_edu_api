package com.innoverse.erp_edu_api.features.income.invoices;

import com.innoverse.erp_edu_api.features.income.invoices.dto.InvoiceItemRequest;
import com.innoverse.erp_edu_api.features.income.invoices.dto.InvoiceRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceServicePort {
    @Transactional
    Invoice createInvoice(InvoiceRequest command);

    @Transactional
    Invoice createDraftInvoice(UUID payeeId, String payeeType, String currency, String notes);

    @Transactional
    Invoice issueInvoice(UUID invoiceId);

    @Transactional
    Invoice updateDraftInvoice(UUID invoiceId, InvoiceRequest command);

    @Transactional(readOnly = true)
    Optional<Invoice> getInvoiceById(UUID invoiceId);

    @Transactional(readOnly = true)
    Optional<Invoice> getInvoiceByNo(String invoiceNo);

    @Transactional(readOnly = true)
    List<Invoice> getInvoicesByPayeeId(UUID payeeId);

    @Transactional(readOnly = true)
    List<Invoice> getInvoicesByPayeeIdAndType(UUID payeeId, String type);

    @Transactional(readOnly = true)
    List<Invoice> getInvoicesByType(String invoiceFor);

    @Transactional(readOnly = true)
    List<Invoice> getInvoicesByStatus(Invoice.Status status);

    @Transactional(readOnly = true)
    List<Invoice> getDraftInvoices();

    @Transactional(readOnly = true)
    List<Invoice> getOverdueInvoices();

    @Transactional
    Invoice addLineItem(UUID invoiceId, InvoiceItemRequest command);

    @Transactional
    Invoice removeLineItem(UUID invoiceId, UUID lineItemId);

    @Transactional
    boolean applyPayment(UUID invoiceId, BigDecimal amount);

    @Transactional
    void cancelInvoice(UUID invoiceId);

    @Transactional
    void refundInvoice(UUID invoiceId);

    @Transactional
    void deleteInvoice(UUID invoiceId);
}
