package com.innoverse.erp_edu_api.features.income.payments.jdbc;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentJdbcRepository extends PagingAndSortingRepository<PaymentEntity, UUID> {

    Optional<PaymentEntity> findByPaymentNo(String paymentNo);
    Optional<PaymentEntity> findByPaymentId(UUID paymentId);

    // Updated methods for entity-based queries
    List<PaymentEntity> findByEntityId(UUID entityId);
    List<PaymentEntity> findByEntityIdAndEntityType(UUID entityId, String entityType);
    List<PaymentEntity> findByEntityType(String entityType);

    List<PaymentEntity> findAll();
    PaymentEntity save(PaymentEntity paymentEntity);

    List<PaymentEntity> findByInvoiceId(UUID invoiceId);
    List<PaymentEntity> findByStatus(String status);
    List<PaymentEntity> findByPaymentMethod(String paymentMethod);

    @Query("SELECT * FROM payments WHERE entity_id = :entityId AND status = :status")
    List<PaymentEntity> findByEntityIdAndStatus(@Param("entityId") UUID entityId,
                                                @Param("status") String status);

    @Query("SELECT * FROM payments WHERE payment_date BETWEEN :startDate AND :endDate")
    List<PaymentEntity> findByPaymentDateBetween(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    boolean existsByPaymentNo(String paymentNo);
    boolean existsByInvoiceId(UUID invoiceId);

    long countByEntityId(UUID entityId);
    long countByEntityType(String entityType);
    long countByStatus(String status);

    @Modifying
    @Query("UPDATE payments SET deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE payment_id = :paymentId")
    void softDeleteById(@Param("paymentId") UUID paymentId);

    @Modifying
    @Query("UPDATE payments SET deleted = false, deleted_at = null WHERE payment_id = :paymentId")
    void restoreById(@Param("paymentId") UUID paymentId);
}