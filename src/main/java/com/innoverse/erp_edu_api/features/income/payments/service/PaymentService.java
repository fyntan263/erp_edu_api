package com.innoverse.erp_edu_api.features.income.payments.service;

import com.innoverse.erp_edu_api.features.income.invoices.Invoice;
import com.innoverse.erp_edu_api.features.income.invoices.InvoiceServicePort;
import com.innoverse.erp_edu_api.features.income.invoices.dto.InvoiceRequest;
import com.innoverse.erp_edu_api.features.income.payments.Payment;
import com.innoverse.erp_edu_api.features.income.payments.PaymentServicePort;
import com.innoverse.erp_edu_api.features.income.payments.dto.PaymentRequest;
import com.innoverse.erp_edu_api.features.income.payments.dto.PaymentCreationRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    @Transactional(rollbackFor = Exception.class)
    public  Payment applyPaymentToInvoice(UUID invoiceId, PaymentRequest request) {
        invoiceService.getInvoiceById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
        Payment payment = createPayment(invoiceId, request);
        Payment savedPayment = paymentRepository.save(payment);

        invoiceService.applyPayment(invoiceId, request.amount());
        savedPayment.markAsCompleted();
        return paymentRepository.save(savedPayment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
     public Payment processPaymentWithInvoiceCreation(PaymentCreationRequest request) {
        // Create invoice first
        InvoiceRequest invoiceRequest = new InvoiceRequest(
                request.payeeId(),
                request.payeeType(),
                request.invoiceDueDate(),
                request.currency(),
                request.notes(),
                request.invoiceItems()
        );
        Invoice invoice = invoiceService.createInvoice(invoiceRequest);
        // Issue the invoice immediately since we're processing payment
        if (invoice.isDraft()) {
            invoice = invoiceService.issueInvoice(invoice.getInvoiceId());
        }
        PaymentService.log.info("Created and issued invoice for payment: {}", invoice.getInvoiceId());

        return applyPaymentToInvoice(invoice.getInvoiceId(), request.toPaymentRequest());
    }

    @Override
    public  Payment createStandalonePayment(PaymentRequest request) {
        Payment payment = createPayment(null, request);
        if (request.notes() == null || request.notes().isBlank()) {
            throw new IllegalArgumentException("Notes are required when creating standalone payment");
        }
        if (paymentRepository.existsByPaymentNo(payment.getPaymentNo())) {
            throw new IllegalStateException("Payment number already exists: " + payment.getPaymentNo());
        }
        payment.markAsCompleted(); // Standalone payments are immediately completed
        return paymentRepository.save(payment);
    }

    @Override
    public Optional<Payment> getPaymentById(UUID paymentId) {
        return paymentRepository.findById(paymentId);
    }

    @Override
    public Optional<Payment> getPaymentByNumber(String paymentNo) {
        return paymentRepository.findByPaymentNo(paymentNo);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Payment> getPaymentsByPayeeId(UUID payeeId) {
        return paymentRepository.findBypayeeId(payeeId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Payment> getPaymentsByPayeeIdAndType(UUID payeeId, String payeeType) {
        return paymentRepository.findBypayeeIdAndPayeeType(payeeId, payeeType);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Payment> getPaymentsByPayeeType(String payeeType) {
        return paymentRepository.findByPayeeType(payeeType);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Payment> getPaymentsByInvoiceId(UUID invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId);
    }

    @Transactional(readOnly = true)
    @Override
    public BigDecimal getTotalPaidForInvoice(UUID invoiceId) {
        List<Payment> payments = getPaymentsByInvoiceId(invoiceId);
        return payments.stream()
                .filter(Payment::isCompleted)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Payment> getPaymentsByStatus(Payment.PaymentStatus status) {
        return paymentRepository.findByStatus(status.name());
    }

    @Transactional(readOnly = true)
    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    @Transactional
    public Payment updatePaymentStatus(UUID paymentId, Payment.PaymentStatus newStatus, String notes) {
        Payment payment = getPaymentById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        Payment updatedPayment;

        switch (newStatus) {
            case COMPLETED -> updatedPayment = payment.markAsCompleted();
            case FAILED -> updatedPayment = payment.markAsFailed(notes);
            case REFUNDED -> updatedPayment = payment.markAsRefunded();
            case PARTIALLY_REFUNDED -> updatedPayment = payment.markAsPartiallyRefunded();
            case PENDING -> updatedPayment = payment.markAsPending();
            default -> throw new IllegalArgumentException("Unknown status: " + newStatus);
        }
        if (notes != null && !notes.isBlank()) {
            updatedPayment = updatedPayment.updateNotes(notes);
        }

        return paymentRepository.save(updatedPayment);
    }

    @Transactional
    @Override
    public Payment updatePaymentNotes(UUID paymentId, String notes) {
        Payment payment = getPaymentById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
        payment.updateNotes(notes);
        return paymentRepository.save(payment);
    }

    @Transactional
    @Override
    public void softDeletePayment(UUID paymentId) {
        Payment payment = getPaymentById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        if (payment.isCompleted() && !payment.isRefundable()) {
            throw new IllegalStateException("Cannot delete a completed payment that is not refundable");
        }

        paymentRepository.softDeleteById(payment.getPaymentId());
    }

    @Transactional
    @Override
    public void restorePayment(UUID paymentId) {
        Payment payment = getPaymentById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
        paymentRepository.restoreById(payment.getPaymentId());
    }

    @Transactional(readOnly = true)
    @Override
    public boolean paymentExists(String paymentNo) {
        return paymentRepository.existsByPaymentNo(paymentNo);
    }

    @Override
    public long countPaymentsByEntity(UUID payeeId) {
        return paymentRepository.countBypayeeId(payeeId);
    }

    @Override
    public List<Payment> getInvoicePayments(UUID invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId);
    }

}