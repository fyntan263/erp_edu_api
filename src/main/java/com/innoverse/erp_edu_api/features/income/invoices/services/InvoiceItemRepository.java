package com.innoverse.erp_edu_api.features.income.invoices.services;


import com.innoverse.erp_edu_api.features.income.invoices.domain.InvoiceItem;

import java.util.List;
import java.util.UUID;

public interface InvoiceItemRepository {
    InvoiceItem save(UUID invoiceId, InvoiceItem item);
    List<InvoiceItem> findByInvoiceId(UUID invoiceId);
    List<InvoiceItem> findByIncomeSourceId(UUID incomeSourceId);
    void deleteByInvoiceId(UUID invoiceId);
    void deleteById(UUID lineItemId);
}