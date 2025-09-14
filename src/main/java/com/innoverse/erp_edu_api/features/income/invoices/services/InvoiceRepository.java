package com.innoverse.erp_edu_api.features.income.invoices.services;

import com.innoverse.erp_edu_api.features.income.invoices.Invoice;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository {
    Invoice save(Invoice invoice);
    Optional<Invoice> findById(UUID invoiceId);
    Optional<Invoice> findByInvoiceNo(String invoiceNo);
    List<Invoice> findBypayeeIdAndName(UUID payeeId, String payeeType);
    List<Invoice> findByPayeeType(UUID payeeId, String payeeType);
    List<Invoice> findByType(String type);
    List<Invoice> findBypayeeId(UUID payeeId);
    List<Invoice> findByStatus(Invoice.Status status);
    List<Invoice> findOverdueInvoices(LocalDate currentDate);
//    BigDecimal sumInvoicedAmountByStudentId(UUID studentId);
//    BigDecimal sumOutstandingAmountByStudentId(UUID studentId);
    void deleteById(UUID invoiceId);
}