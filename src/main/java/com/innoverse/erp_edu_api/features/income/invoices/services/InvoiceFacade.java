package com.innoverse.erp_edu_api.features.income.invoices.services;

import com.innoverse.erp_edu_api.features.income.invoices.Invoice;
import com.innoverse.erp_edu_api.features.income.invoices.InvoiceServicePort;
import com.innoverse.erp_edu_api.features.income.invoices.dto.ApplyPaymentRequest;
import com.innoverse.erp_edu_api.features.income.invoices.dto.InvoiceDTO;
import com.innoverse.erp_edu_api.features.income.invoices.dto.InvoiceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceFacade {

    private final InvoiceServicePort invoiceService;

    public InvoiceDTO createInvoice(InvoiceRequest request) {
        Invoice invoice = invoiceService.createInvoice(request);
        return InvoiceDTO.fromDomain(invoice);
    }

    public Optional<InvoiceDTO> getInvoiceById(UUID invoiceId) {
        return invoiceService.getInvoiceById(invoiceId)
                .map(InvoiceDTO::fromDomain);
    }

    public List<InvoiceDTO> getInvoicesByPayeeId(UUID payeeId) {
        List<Invoice> invoices = invoiceService.getInvoicesByPayeeId(payeeId);
        return invoices.stream().map(InvoiceDTO::fromDomain).toList();
    }

    // Add all other service methods with DTO conversion
    public List<InvoiceDTO> getInvoicesByStatus(Invoice.Status status) {
        List<Invoice> invoices = invoiceService.getInvoicesByStatus(status);
        return invoices.stream().map(InvoiceDTO::fromDomain).toList();
    }

    public boolean applyPayment(UUID invoiceId, BigDecimal amount) {
        return invoiceService.applyPayment(invoiceId, amount);
    }
}
