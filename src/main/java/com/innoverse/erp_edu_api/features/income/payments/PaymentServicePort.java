package com.innoverse.erp_edu_api.features.income.payments;

import com.innoverse.erp_edu_api.features.income.invoices.Invoice;
import com.innoverse.erp_edu_api.features.income.invoices.dto.InvoiceRequest;
import com.innoverse.erp_edu_api.features.income.payments.dto.PaymentCreationRequest;
import com.innoverse.erp_edu_api.features.income.payments.dto.PaymentRequest;
import com.innoverse.erp_edu_api.features.income.payments.service.PaymentService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentServicePort {


    default Payment createPayment(UUID invoiceId, PaymentRequest request) {
        return Payment.create(
                request.payeeId(),
                request.payeeType(),
                invoiceId,
                request.amount(),
                request.currency(),
                request.paymentMethod(),
                request.notes()
        );
    }
    Payment applyPaymentToInvoice(UUID invoiceId, PaymentRequest request);
    Payment processPaymentWithInvoiceCreation(PaymentCreationRequest request);
    Payment createStandalonePayment(PaymentRequest request);
    Optional<Payment> getPaymentById(UUID paymentId);
    @Transactional(readOnly = true)
    List<Payment> getPaymentsByPayeeIdAndType(UUID payeeId, String payeeType);
    Optional<Payment> getPaymentByNumber(String paymentNo);

    @Transactional(readOnly = true)
    List<Payment> getPaymentsByPayeeId(UUID payeeId);
    @Transactional(readOnly = true)
    List<Payment> getPaymentsByPayeeType(String payeeType);

    @Transactional(readOnly = true)
    List<Payment> getPaymentsByInvoiceId(UUID invoiceId);

    @Transactional(readOnly = true)
    BigDecimal getTotalPaidForInvoice(UUID invoiceId);

    @Transactional(readOnly = true)
    List<Payment> getPaymentsByStatus(Payment.PaymentStatus status);

    @Transactional(readOnly = true)
    List<Payment> getAllPayments();

    @Transactional
    Payment updatePaymentStatus(UUID paymentId, Payment.PaymentStatus status, String notes);

    default void validateStatusTransition(Payment.PaymentStatus current, Payment.PaymentStatus next) {
        // Implement business rules for status transitions
        if (current == Payment.PaymentStatus.FAILED && next == Payment.PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot transition from FAILED to COMPLETED");
        }
        if (current == Payment.PaymentStatus.REFUNDED) {
            throw new IllegalStateException("Cannot modify a refunded payment");
        }
        if (current == Payment.PaymentStatus.COMPLETED &&
                (next == Payment.PaymentStatus.PENDING || next == Payment.PaymentStatus.FAILED)) {
            throw new IllegalStateException("Cannot transition from COMPLETED to " + next);
        }
    }

    @Transactional
    Payment updatePaymentNotes(UUID paymentId, String notes);

    @Transactional
    void softDeletePayment(UUID paymentId);

    @Transactional
    void restorePayment(UUID paymentId);

    @Transactional(readOnly = true)
    boolean paymentExists(String paymentNo);

    long countPaymentsByEntity(UUID payeeId);

    List<Payment> getInvoicePayments(UUID invoiceId);
}
