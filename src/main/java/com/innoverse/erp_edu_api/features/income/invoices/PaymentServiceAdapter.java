package com.innoverse.erp_edu_api.features.income.invoices;

import com.innoverse.erp_edu_api.features.income.payments.Payment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentServiceAdapter {
    List<Payment> getInvoicePayments(UUID invoiceId);
    Optional<Payment> getPayment(UUID paymentId);
    BigDecimal getTotalPaidAmount(UUID invoiceId);
    Payment recordPayment(Payment payment);
    void refundPayment(UUID paymentId);
}