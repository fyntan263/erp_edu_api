package com.innoverse.erp_edu_api.features.income.payments.service;

import com.innoverse.erp_edu_api.features.income.payments.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    @Transactional
    Payment save(Payment payment);

    @Transactional(readOnly = true)
    Optional<Payment> findById(UUID paymentId);

    @Transactional(readOnly = true)
    Optional<Payment> findByPaymentNo(String paymentNo);

    @Transactional(readOnly = true)
    List<Payment> findByEntityId(UUID entityId);

    @Transactional(readOnly = true)
    List<Payment> findByEntityIdAndEntityType(UUID entityId, String entityType);

    @Transactional(readOnly = true)
    List<Payment> findByEntityType(String entityType);

    @Transactional(readOnly = true)
    List<Payment> findByInvoiceId(UUID invoiceId);

    @Transactional(readOnly = true)
    List<Payment> findByStatus(String status);

    @Transactional(readOnly = true)
    Page<Payment> findAll(Pageable pageable);

    @Transactional(readOnly = true)
    List<Payment> findAll();

    @Transactional(readOnly = true)
    boolean existsByPaymentNo(String paymentNo);

    @Transactional(readOnly = true)
    long countByEntityId(UUID entityId);

    @Transactional
    void softDeleteById(UUID paymentId);

    @Transactional
    void restoreById(UUID paymentId);
}
