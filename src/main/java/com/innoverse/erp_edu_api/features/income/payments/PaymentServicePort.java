package com.innoverse.erp_edu_api.features.income.payments;

import com.innoverse.erp_edu_api.features.income.payments.dto.PaymentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentServicePort {
    Payment processPayment(PaymentRequest request);
    Payment createPayment(PaymentRequest command);

    Optional<Payment> getPaymentById(UUID paymentId);
    Optional<Payment> getPaymentByNumber(String paymentNo);

    List<Payment> getPaymentsByEntityId(UUID entityId);
    List<Payment> getPaymentsByEntityIdAndType(UUID entityId, String entityType);
    List<Payment> getPaymentsByEntityType(String entityType);
    List<Payment> getPaymentsByInvoiceId(UUID invoiceId);
    List<Payment> getPaymentsByStatus(Payment.PaymentStatus status);
    Page<Payment> getAllPayments(Pageable pageable);
    List<Payment> getAllPayments();

    Payment updatePaymentStatus(UUID paymentId, Payment.PaymentStatus status, String notes);
    Payment updatePaymentNotes(UUID paymentId, String notes);

    void softDeletePayment(UUID paymentId);
    void restorePayment(UUID paymentId);

    boolean paymentExists(String paymentNo);
    long countPaymentsByEntity(UUID entityId);

    Payment markAsCompleted(UUID paymentId);
    Payment markAsFailed(UUID paymentId, String reason);
    Payment markAsRefunded(UUID paymentId);
    Payment markAsPartiallyRefunded(UUID paymentId);

    Payment applyPaymentToInvoice(UUID invoiceId, PaymentRequest command);
    List<Payment> getInvoicePayments(UUID invoiceId);
    BigDecimal getTotalPaidForInvoice(UUID invoiceId);
}