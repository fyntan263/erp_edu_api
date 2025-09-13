package com.innoverse.erp_edu_api.features.income.payments.service;

import com.innoverse.erp_edu_api.features.income.invoices.Invoice;
import com.innoverse.erp_edu_api.features.income.invoices.InvoiceServicePort;
import com.innoverse.erp_edu_api.features.income.payments.Payment;
import com.innoverse.erp_edu_api.features.income.payments.PaymentServicePort;
import com.innoverse.erp_edu_api.features.income.payments.dto.PaymentRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService implements PaymentServicePort {

    private final PaymentRepository paymentRepository;
    private final InvoiceServicePort invoiceService;

    @Override
    @Transactional
    public Payment processPayment(PaymentRequest request) {
        validatePaymentRequest(request);

        try {
            // Scenario 1: Payment for existing invoice
            if (request.invoiceId() != null) {
                return processPaymentForExistingInvoice(request);
            }
            // Scenario 2: Payment with invoice creation
            else if (Boolean.TRUE.equals(request.createInvoice())) {
                return processPaymentWithInvoiceCreation(request);
            }
            // Scenario 3: Standalone payment (no invoice)
            else {
                return createStandalonePayment(request);
            }
        } catch (Exception ex) {
            log.error("Error processing payment: {}", ex.getMessage(), ex);
            throw new IllegalStateException("Failed to process payment: " + ex.getMessage(), ex);
        }
    }

    private Payment processPaymentForExistingInvoice(PaymentRequest request) {
        Invoice invoice = invoiceService.getInvoiceById(request.invoiceId())
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));

        validateInvoiceForPayment(invoice, request.amount(), request.currency());

        Payment payment = createPaymentEntity(request, request.invoiceId());
        Payment savedPayment = paymentRepository.save(payment);

        invoiceService.applyPayment(request.invoiceId(), request.amount());
        savedPayment.markAsCompleted();

        return paymentRepository.save(savedPayment);
    }

    private Payment processPaymentWithInvoiceCreation(PaymentRequest request) {
        // Create invoice first
        Invoice invoice = invoiceService.createInvoice(
                request.entityId(),
                request.entityType(),
                request.invoiceDescription(),
                request.invoiceDueDate(),
                request.currency(),
                request.lineItems(),
                request.paymentNotes()
        );

        // Process payment for the newly created invoice
        PaymentRequest invoicePaymentRequest = new PaymentRequest(
                request.entityId(),
                request.entityType(),
                invoice.getInvoiceId(),
                request.amount(),
                request.currency(),
                request.paymentMethod(),
                request.paymentNotes(),
                false,
                null,
                null,
                null
        );

        return processPaymentForExistingInvoice(invoicePaymentRequest);
    }

    private Payment createStandalonePayment(PaymentRequest request) {
        Payment payment = createPaymentEntity(request, null);

        if (paymentRepository.existsByPaymentNo(payment.getPaymentNo())) {
            throw new IllegalStateException("Payment number already exists: " + payment.getPaymentNo());
        }

        payment.markAsCompleted(); // Standalone payments are immediately completed
        return paymentRepository.save(payment);
    }

    private Payment createPaymentEntity(PaymentRequest request, UUID invoiceId) {
        return Payment.create(
                request.entityId(),
                request.entityType(),
                invoiceId,
                request.amount(),
                request.currency(),
                request.paymentMethod(),
                request.paymentNotes()
        );
    }

    private void validatePaymentRequest(PaymentRequest request) {
        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (request.entityId() == null) {
            throw new IllegalArgumentException("Entity ID is required");
        }
        if (request.entityType() == null || request.entityType().isBlank()) {
            throw new IllegalArgumentException("Entity type is required");
        }

//        CurrencyUtils.validateCurrency(request.currency());

        // Validate invoice creation parameters if needed
        if (Boolean.TRUE.equals(request.createInvoice())) {
            if (request.invoiceDescription() == null || request.invoiceDescription().isBlank()) {
                throw new IllegalArgumentException("Invoice description is required when creating invoice");
            }
            if (request.invoiceDueDate() == null) {
                throw new IllegalArgumentException("Invoice due date is required when creating invoice");
            }
            if (request.lineItems() == null || request.lineItems().isEmpty()) {
                throw new IllegalArgumentException("Line items are required when creating invoice");
            }
        }
    }

    private void validateInvoiceForPayment(Invoice invoice, BigDecimal paymentAmount, String paymentCurrency) {
        if (!invoice.isPayable()) {
            throw new IllegalStateException("Invoice is not payable");
        }

        if (!invoice.getCurrency().equals(paymentCurrency)) {
            throw new IllegalArgumentException("Payment currency does not match invoice currency");
        }

        BigDecimal amountDue = invoice.getTotalAmount().subtract(invoice.getAmountPaid());
        if (paymentAmount.compareTo(amountDue) > 0) {
            throw new IllegalArgumentException("Payment amount exceeds invoice balance");
        }
    }

    @Override
    public Optional<Payment> getPaymentById(UUID paymentId) {
        return paymentRepository.findById(paymentId);
    }

    @Override
    public Optional<Payment> getPaymentByNumber(String paymentNo) {
        return paymentRepository.findByPaymentNo(paymentNo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByEntityId(UUID entityId) {
        return paymentRepository.findByEntityId(entityId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByEntityIdAndType(UUID entityId, String entityType) {
        return paymentRepository.findByEntityIdAndEntityType(entityId, entityType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByEntityType(String entityType) {
        return paymentRepository.findByEntityType(entityType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByInvoiceId(UUID invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalPaidForInvoice(UUID invoiceId) {
        List<Payment> payments = getPaymentsByInvoiceId(invoiceId);
        return payments.stream()
                .filter(Payment::isCompleted)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByStatus(Payment.PaymentStatus status) {
        return paymentRepository.findByStatus(status.name());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Payment> getAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    @Transactional
    public Payment updatePaymentStatus(UUID paymentId, Payment.PaymentStatus status, String notes) {
        Payment payment = getPaymentById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        validateStatusTransition(payment.getStatus(), status);

        if (notes != null) {
            payment.updateNotes(notes);
        }

        switch (status) {
            case COMPLETED -> payment.markAsCompleted();
            case FAILED -> payment.markAsFailed(notes);
            case REFUNDED -> payment.markAsRefunded();
            case PARTIALLY_REFUNDED -> payment.markAsPartiallyRefunded();
            case PENDING -> payment.markAsPending();
        }

        return paymentRepository.save(payment);
    }

    private void validateStatusTransition(Payment.PaymentStatus current, Payment.PaymentStatus next) {
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

    @Override
    @Transactional
    public Payment updatePaymentNotes(UUID paymentId, String notes) {
        Payment payment = getPaymentById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
        payment.updateNotes(notes);
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public void softDeletePayment(UUID paymentId) {
        Payment payment = getPaymentById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        if (payment.isCompleted() && !payment.isRefundable()) {
            throw new IllegalStateException("Cannot delete a completed payment that is not refundable");
        }

        paymentRepository.softDeleteById(payment.getPaymentId());
    }

    @Override
    @Transactional
    public void restorePayment(UUID paymentId) {
        Payment payment = getPaymentById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
        paymentRepository.restoreById(payment.getPaymentId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean paymentExists(String paymentNo) {
        return paymentRepository.existsByPaymentNo(paymentNo);
    }

    @Override
    public long countPaymentsByEntity(UUID entityId) {
        return paymentRepository.countByEntityId(entityId);
    }

    @Override
    @Transactional
    public Payment markAsCompleted(UUID paymentId) {
        return updatePaymentStatus(paymentId, Payment.PaymentStatus.COMPLETED, "Payment completed successfully");
    }

    @Override
    @Transactional
    public Payment markAsFailed(UUID paymentId, String reason) {
        return updatePaymentStatus(paymentId, Payment.PaymentStatus.FAILED, reason);
    }

    @Override
    @Transactional
    public Payment markAsRefunded(UUID paymentId) {
        Payment payment = getPaymentById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        if (!payment.isRefundable()) {
            throw new IllegalStateException("Payment is not refundable");
        }

        return updatePaymentStatus(paymentId, Payment.PaymentStatus.REFUNDED, "Payment refunded");
    }

    @Override
    @Transactional
    public Payment markAsPartiallyRefunded(UUID paymentId) {
        Payment payment = getPaymentById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        if (!payment.isRefundable()) {
            throw new IllegalStateException("Payment is not refundable");
        }

        return updatePaymentStatus(paymentId, Payment.PaymentStatus.PARTIALLY_REFUNDED, "Payment partially refunded");
    }

    @Override
    @Transactional
    public Payment applyPaymentToInvoice(UUID invoiceId, PaymentRequest request) {
        // Validate invoice exists
        Invoice invoice = invoiceService.getInvoiceById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        // Create payment request with entity info from invoice
        PaymentRequest invoicePaymentReq = new PaymentRequest(
                invoice.getEntityId(),
                invoice.getEntityType(),
                invoiceId,
                request.amount(),
                request.currency(),
                request.paymentMethod(),
                request.paymentNotes(),
                false,
                null,
                null,
                null
        );

        return processPaymentForExistingInvoice(invoicePaymentReq);
    }

    @Override
    public List<Payment> getInvoicePayments(UUID invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId);
    }

    @Override
    public Payment createPayment(PaymentRequest command) {
        return processPayment(command);
    }
}