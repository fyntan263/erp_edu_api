package com.innoverse.erp_edu_api.features.income.income_stream.jdbc;

import com.innoverse.erp_edu_api.features.income.income_stream.IncomeSource;
import org.springframework.data.domain.Limit;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IncomeSourceJdbcRepository extends CrudRepository<IncomeSourceEntity, UUID> {

    Optional<IncomeSourceEntity> findByAccountingCode(String accountingCode);

    List<IncomeSourceEntity> findByIsActive(Boolean isActive);

    List<IncomeSourceEntity> findByCurrency(String currency);

    List<IncomeSourceEntity> findByFeeTypeCode(String feeTypeCode);

    @Query("SELECT * FROM income_sources WHERE is_active = true AND effective_from <= CURRENT_DATE AND (effective_to IS NULL OR effective_to >= CURRENT_DATE)")
    List<IncomeSource> findActiveAndEffective();

    @Query("SELECT * FROM income_sources WHERE accounting_code = :accountingCode AND id != :id")
    Optional<IncomeSourceEntity> findByAccountingCodeAndIdNot(@Param("accountingCode") String accountingCode, @Param("id") UUID id);

    boolean existsByAccountingCode(String accountingCode);

    @Modifying
    @Query("""
        INSERT INTO income_sources (
            income_source_id,
            accounting_code,
            fee_type_code,
            name,
            description,
            recurrency,
            applicability,
            currency,
            default_amount,
            is_active,
            allow_partial_payment,
            is_taxable,
            tax_rate,
            effective_from,
            effective_to
        ) VALUES (
            :incomeSourceId,
            :accountingCode,
            :feeTypeCode,
            :name,
            :description,
            :recurrency,
            :applicability,
            :currency,
            :defaultAmount,
            :isActive,
            :allowPartialPayment,
            :isTaxable,
            :taxRate,
            :effectiveFrom,
            :effectiveTo
        )
    """)
    void customInsert(
            @Param("incomeSourceId") UUID incomeSourceId,
            @Param("accountingCode") String accountingCode,
            @Param("feeTypeCode") String feeTypeCode,
            @Param("name") String name,
            @Param("description") String description,
            @Param("recurrency") String recurrency,
            @Param("applicability") String applicability,
            @Param("currency") String currency,
            @Param("defaultAmount") BigDecimal defaultAmount,
            @Param("isActive") Boolean isActive,
            @Param("allowPartialPayment") Boolean allowPartialPayment,
            @Param("isTaxable") Boolean isTaxable,
            @Param("taxRate") BigDecimal taxRate,
            @Param("effectiveFrom") LocalDate effectiveFrom,
            @Param("effectiveTo") LocalDate effectiveTo
    );
}