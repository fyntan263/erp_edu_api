package com.innoverse.erp_edu_api.features.income.payments.jdbc;

import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentJdbcRepository extends CrudRepository<PaymentEntity, UUID> {

    Optional<PaymentEntity> findByPaymentNo(String paymentNo);
    Optional<PaymentEntity> findByPaymentId(UUID paymentId);

    // Updated methods for entity-based queries
    List<PaymentEntity> findBypayeeId(UUID payeeId);
    List<PaymentEntity> findBypayeeIdAndPayeeType(UUID payeeId, String payeeType);
    List<PaymentEntity> findByPayeeType(String payeeType);


    List<PaymentEntity> findByInvoiceId(UUID invoiceId);
    List<PaymentEntity> findByStatus(String status);
    List<PaymentEntity> findByPaymentMethod(String paymentMethod);

    @Query("SELECT * FROM payments WHERE entity_id = :payeeId AND status = :status")
    List<PaymentEntity> findBypayeeIdAndStatus(@Param("payeeId") UUID payeeId,
                                                @Param("status") String status);

    @Query("SELECT * FROM payments WHERE payment_date BETWEEN :startDate AND :endDate")
    List<PaymentEntity> findByPaymentDateBetween(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    boolean existsByPaymentNo(String paymentNo);
    boolean existsByInvoiceId(UUID invoiceId);

    long countBypayeeId(UUID payeeId);
    long countByPayeeType(String payeeType);
    long countByStatus(String status);

    @Modifying
    @Query("UPDATE payments SET deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE payment_id = :paymentId")
    void softDeleteById(@Param("paymentId") UUID paymentId);

    @Modifying
    @Query("UPDATE payments SET deleted = false, deleted_at = null WHERE payment_id = :paymentId")
    void restoreById(@Param("paymentId") UUID paymentId);

    @Modifying
    @Query("""
        INSERT INTO payments (payment_id, entity_id, entity_type, invoice_id, 
                              payment_number, payment_date, amount, currency,
                              payment_method, status, payment_notes, deleted)
        VALUES (:paymentId, :payeeId, :payeeType, :invoiceId, :paymentNo, 
                :paymentDate, :amount, :currency, :paymentMethod, :status, 
                :paymentNotes, :deleted)""")
    void insertPayment(
            @Param("paymentId") UUID paymentId,
            @Param("payeeId") UUID payeeId,
            @Param("payeeType") String payeeType,
            @Param("invoiceId") UUID invoiceId,
            @Param("paymentNo") String paymentNo,
            @Param("paymentDate") LocalDateTime paymentDate,
            @Param("amount") BigDecimal amount,
            @Param("currency") String currency,
            @Param("paymentMethod") String paymentMethod,
            @Param("status") String status,
            @Param("paymentNotes") String paymentNotes,
            @Param("deleted") Boolean deleted
    );
}