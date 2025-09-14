package com.innoverse.erp_edu_api.features.income.invoices.services;

import com.innoverse.erp_edu_api.features.income.invoices.PaymentServiceAdapter;
import com.innoverse.erp_edu_api.features.income.payments.Payment;
import com.innoverse.erp_edu_api.features.income.payments.PaymentServicePort;
import com.innoverse.erp_edu_api.features.income.payments.dto.PaymentCreationRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentServiceAdapterImpl implements PaymentServiceAdapter {

    private final PaymentServicePort paymentServicePort;

    public PaymentServiceAdapterImpl(PaymentServicePort paymentServicePort) {
        this.paymentServicePort = paymentServicePort;
    }

    @Override
    public List<Payment> getInvoicePayments(UUID invoiceId) {
        return paymentServicePort.getPaymentsByInvoiceId(invoiceId);
    }

    @Override
    public Optional<Payment> getPayment(UUID paymentId) {
        return paymentServicePort.getPaymentById(paymentId);
    }

    @Override
    public BigDecimal getTotalPaidAmount(UUID invoiceId) {
        return paymentServicePort.getTotalPaidForInvoice(invoiceId);
    }

    @Override
    public Payment recordPayment(Payment payment) {
        // Convert to PaymentRequest and use the service
        // This is simplified - you'd need proper conversion
        return null;//paymentServicePort.createPayment(convertToPaymentRequest(payment));
    }

    @Override
    public void refundPayment(UUID paymentId) {
        //paymentServicePort.markAsRefunded(paymentId);
    }

    private PaymentCreationRequest convertToPaymentRequest(Payment payment) {
        // Implementation depends on your PaymentRequest structure
        return null; // Placeholder
    }
}
