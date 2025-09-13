package com.innoverse.erp_edu_api.features.income.payments.jdbc;

import com.innoverse.erp_edu_api.features.income.payments.Payment;
import com.innoverse.erp_edu_api.features.income.payments.service.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final PaymentJdbcRepository jdbcRepository;

    @Transactional
    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = PaymentEntity.fromDomain(payment);
        PaymentEntity savedEntity = jdbcRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Payment> findById(UUID paymentId) {
        return jdbcRepository.findByPaymentId(paymentId)
                .map(PaymentEntity::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Payment> findByPaymentNo(String paymentNo) {
        return jdbcRepository.findByPaymentNo(paymentNo)
                .map(PaymentEntity::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Payment> findByEntityId(UUID entityId) {
        return jdbcRepository.findByEntityId(entityId).stream()
                .map(PaymentEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<Payment> findByEntityIdAndEntityType(UUID entityId, String entityType) {
        return jdbcRepository.findByEntityIdAndEntityType(entityId, entityType).stream()
                .map(PaymentEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<Payment> findByEntityType(String entityType) {
        return jdbcRepository.findByEntityType(entityType).stream()
                .map(PaymentEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<Payment> findByInvoiceId(UUID invoiceId) {
        return jdbcRepository.findByInvoiceId(invoiceId).stream()
                .map(PaymentEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<Payment> findByStatus(String status) {
        return jdbcRepository.findByStatus(status).stream()
                .map(PaymentEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Payment> findAll(Pageable pageable) {
        return jdbcRepository.findAll(pageable)
                .map(PaymentEntity::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Payment> findAll() {
        return jdbcRepository.findAll().stream()
                .map(PaymentEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByPaymentNo(String paymentNo) {
        return jdbcRepository.existsByPaymentNo(paymentNo);
    }

    @Transactional(readOnly = true)
    @Override
    public long countByEntityId(UUID entityId) {
        return jdbcRepository.countByEntityId(entityId);
    }

    @Transactional
    @Override
    public void softDeleteById(UUID paymentId) {
        jdbcRepository.softDeleteById(paymentId);
    }

    @Transactional
    @Override
    public void restoreById(UUID paymentId) {
        jdbcRepository.restoreById(paymentId);
    }
}