package com.innoverse.erp_edu_api.features.income.income_stream;

import com.innoverse.erp_edu_api.features.income.income_stream.web.dto.CreateIncomeSourceRequest;
import com.innoverse.erp_edu_api.features.income.income_stream.web.dto.UpdateIncomeSourceRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncomeSourceServicePort {
    Optional<IncomeSource> createIncomeSource(CreateIncomeSourceRequest request);
    Optional<IncomeSource> updateIncomeSource(UUID incomeSourceId, UpdateIncomeSourceRequest request);

    @Transactional(readOnly = true)
    List<IncomeSource> getAllIncomeSources();

    @Transactional(readOnly = true)
    Optional<IncomeSource> getIncomeSourceById(UUID id);

    void deleteIncomeSource(UUID id);

    @Transactional(readOnly = true)
    List<IncomeSource> getActiveIncomeSources();

    @Transactional(readOnly = true)
    List<IncomeSource> getActiveAndEffectiveIncomeSources();

    @Transactional(readOnly = true)
    boolean existsByAccountingCode(String accountingCode);


}
