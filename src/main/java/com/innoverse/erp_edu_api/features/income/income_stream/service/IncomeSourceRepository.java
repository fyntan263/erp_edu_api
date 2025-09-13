package com.innoverse.erp_edu_api.features.income.income_stream.service;

import com.innoverse.erp_edu_api.features.income.income_stream.IncomeSource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncomeSourceRepository {
    @Transactional
    IncomeSource save(IncomeSource incomeSource);

    @Transactional
    IncomeSource update(IncomeSource incomeSource);

    @Transactional(readOnly = true)
    Optional<IncomeSource> findById(UUID id);

    @Transactional(readOnly = true)
    List<IncomeSource> findAll();

    @Transactional
    void deleteById(UUID id);

    @Transactional(readOnly = true)
    Optional<IncomeSource> findByAccountingCode(String accountingCode);

    @Transactional(readOnly = true)
    List<IncomeSource> findByIsActive(Boolean isActive);

    @Transactional(readOnly = true)
    boolean existsByAccountingCode(String accountingCode);

    @Transactional(readOnly = true)
    Optional<IncomeSource> findByAccountingCodeAndIdNot(String accountingCode, UUID id);

    @Transactional(readOnly = true)
    List<IncomeSource> findActiveAndEffective();
}
