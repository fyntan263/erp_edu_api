package com.innoverse.erp_edu_api.features.income.payments.jdbc;

import com.innoverse.erp_edu_api.features.income.payments.Payment;
import com.innoverse.erp_edu_api.features.income.payments.exceptions.PaymentAlreadyExistsException;
import com.innoverse.erp_edu_api.features.income.payments.service.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final PaymentJdbcRepository jdbcRepository;

    @Transactional
    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = PaymentEntity.fromDomain(payment);
        try {
            if (jdbcRepository.existsById(payment.getPaymentId())) {
                throw new PaymentAlreadyExistsException(payment.getPaymentId());
            }
            // Use the correct method name and parameters
            jdbcRepository.insertPayment(
                    entity.getPaymentId(),
                    entity.getPayeeId(),
                    entity.getPayeeType(),
                    entity.getInvoiceId(),
                    entity.getPaymentNo(),
                    entity.getPaymentDate(),
                    entity.getAmount(),
                    entity.getCurrency(),
                    entity.getPaymentMethod(),
                    entity.getStatus(),
                    entity.getPaymentNotes(),
                    false // deleted flag
            );
            // Retrieve the saved entity to return
            return jdbcRepository.findByPaymentId(entity.getPaymentId())
                    .map(PaymentEntity::toDomain)
                    .orElseThrow(() -> new RuntimeException("Failed to retrieve saved payment"));

        } catch (DuplicateKeyException e) {
            throw new PaymentAlreadyExistsException(entity.getPaymentId());
        } catch (DataAccessException e) {
            throw new RuntimeException("Database error while saving payment", e);
        }
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
    public List<Payment> findBypayeeId(UUID payeeId) {
        return jdbcRepository.findBypayeeId(payeeId).stream()
                .map(PaymentEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<Payment> findBypayeeIdAndPayeeType(UUID payeeId, String payeeType) {
        return jdbcRepository.findBypayeeIdAndPayeeType(payeeId, payeeType).stream()
                .map(PaymentEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<Payment> findByPayeeType(String payeeType) {
        return jdbcRepository.findByPayeeType(payeeType).stream()
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
    public List<Payment> findAll() {
        try {
            return StreamSupport.stream(jdbcRepository.findAll().spliterator(), true)
                    .map(PaymentEntity::toDomain)
                    .toList();
        } catch (DataAccessException e) {
            throw new RuntimeException("Database error while finding all payments", e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByPaymentNo(String paymentNo) {
        return jdbcRepository.existsByPaymentNo(paymentNo);
    }

    @Transactional(readOnly = true)
    @Override
    public long countBypayeeId(UUID payeeId) {
        return jdbcRepository.countBypayeeId(payeeId);
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
    // Add these methods to the adapter:
    @Transactional(readOnly = true)
    @Override
    public List<Payment> findByPaymentMethod(String paymentMethod) {
        return jdbcRepository.findByPaymentMethod(paymentMethod).stream()
                .map(PaymentEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<Payment> findBypayeeIdAndStatus(UUID payeeId, String status) {
        return jdbcRepository.findBypayeeIdAndStatus(payeeId, status).stream()
                .map(PaymentEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<Payment> findByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return jdbcRepository.findByPaymentDateBetween(startDate, endDate).stream()
                .map(PaymentEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByInvoiceId(UUID invoiceId) {
        return jdbcRepository.existsByInvoiceId(invoiceId);
    }

    @Transactional(readOnly = true)
    @Override
    public long countByPayeeType(String entityType) {
        return jdbcRepository.countByPayeeType(entityType);
    }

    @Transactional(readOnly = true)
    @Override
    public long countByStatus(String status) {
        return jdbcRepository.countByStatus(status);
    }
}