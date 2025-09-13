package com.innoverse.erp_edu_api.features.income.income_stream.service;


import com.innoverse.erp_edu_api.features.income.income_stream.IncomeSource;
import com.innoverse.erp_edu_api.features.income.income_stream.web.dto.CreateIncomeSourceRequest;
import com.innoverse.erp_edu_api.features.income.income_stream.web.dto.IncomeSourceDto;
import com.innoverse.erp_edu_api.features.income.income_stream.web.dto.UpdateIncomeSourceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IncomeSourceFacade {

    private final IncomeSourceService incomeSourceService;

    @Transactional
    public Optional<IncomeSourceDto> createIncomeSource(CreateIncomeSourceRequest request) {
        return  incomeSourceService.createIncomeSource(request).map(IncomeSourceDto::fromDomain);
    }

    @Transactional(readOnly = true)
    public Optional<IncomeSourceDto> getIncomeSourceById(UUID id) {
        return incomeSourceService.getIncomeSourceById(id).map(IncomeSourceDto::fromDomain);
    }

    @Transactional(readOnly = true)
    public List<IncomeSourceDto> getAllIncomeSources() {
        return incomeSourceService.getAllIncomeSources().stream().map(IncomeSourceDto::fromDomain).toList();
    }

    @Transactional(readOnly = true)
    public List<IncomeSourceDto> getActiveIncomeSources() {
        return incomeSourceService.getActiveIncomeSources().stream().map(IncomeSourceDto::fromDomain).toList();
    }

    @Transactional(readOnly = true)
    public List<IncomeSourceDto> getActiveAndEffectiveIncomeSources() {
        return incomeSourceService.getActiveAndEffectiveIncomeSources().stream().map(IncomeSourceDto::fromDomain).toList();
    }

    @Transactional
    public Optional<IncomeSourceDto> updateIncomeSource(UUID id, UpdateIncomeSourceRequest request) {
        try {
            return incomeSourceService.updateIncomeSource(id, request).map(IncomeSourceDto::fromDomain);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Transactional
    public boolean deleteIncomeSource(UUID id) {
        try{
            incomeSourceService.deleteIncomeSource(id);
            return true;
        }catch (IllegalArgumentException e){
            return false;
        }
    }

    @Transactional(readOnly = true)
    public boolean existsByAccountingCode(String accountingCode) {
        return incomeSourceService.existsByAccountingCode(accountingCode);
    }
}