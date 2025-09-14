package com.innoverse.erp_edu_api.features.income.payments.service;

import com.innoverse.erp_edu_api.features.income.payments.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(UUID paymentId);
    Optional<Payment> findByPaymentNo(String paymentNo);
    List<Payment> findBypayeeId(UUID payeeId);
    List<Payment> findBypayeeIdAndPayeeType(UUID payeeId, String payeeType);
    List<Payment> findByPayeeType(String payeeType);
    List<Payment> findByInvoiceId(UUID invoiceId);
    List<Payment> findByStatus(String status);
    List<Payment> findAll();
    boolean existsByPaymentNo(String paymentNo);
    long countBypayeeId(UUID payeeId);
    void softDeleteById(UUID paymentId);
    void restoreById(UUID paymentId);

    // Add these missing methods:
    List<Payment> findByPaymentMethod(String paymentMethod);
    List<Payment> findBypayeeIdAndStatus(UUID payeeId, String status);
    List<Payment> findByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    boolean existsByInvoiceId(UUID invoiceId);
    long countByPayeeType(String payeeType);
    long countByStatus(String status);
}