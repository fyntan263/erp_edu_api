package com.innoverse.erp_edu_api.features.income.income_stream.service;

import com.innoverse.erp_edu_api.features.income.income_stream.IncomeSource;
import com.innoverse.erp_edu_api.features.income.income_stream.IncomeSourceServicePort;
import com.innoverse.erp_edu_api.features.income.income_stream.web.dto.CreateIncomeSourceRequest;
import com.innoverse.erp_edu_api.features.income.income_stream.exceptions.IncomeSourceNotFoundException;
import com.innoverse.erp_edu_api.features.income.income_stream.exceptions.InvalidIncomeSourceException;
import com.innoverse.erp_edu_api.features.income.income_stream.web.dto.UpdateIncomeSourceRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class IncomeSourceService implements IncomeSourceServicePort {
    private final IncomeSourceRepository incomeSrcRepository;

    public IncomeSourceService(IncomeSourceRepository repositoryAdapter) {
        this.incomeSrcRepository = repositoryAdapter;
    }

    @Override
    public Optional<IncomeSource> createIncomeSource(CreateIncomeSourceRequest request) {
        try {
            IncomeSource incomeSource = IncomeSource.create(
                    request.feeTypeCode(),
                    request.name(),
                    request.description(),
                    request.recurrency(),
                    request.applicability(),
                    request.currency(),
                    request.defaultAmount(),
                    request.isActive(),
                    request.allowPartialPayment(),
                    request.isTaxable(),
                    request.taxRate(),
                    request.effectiveFrom(),
                    request.effectiveTo()
            );

            IncomeSource incomeSourceCreated =  incomeSrcRepository.save(incomeSource);
            return Optional.of(incomeSourceCreated);

        } catch (IllegalArgumentException e) {
            throw new InvalidIncomeSourceException("request", e.getMessage());
        }
    }

    @Override
    public Optional<IncomeSource> updateIncomeSource(UUID incomeSourceId, UpdateIncomeSourceRequest request) {
        try{
            IncomeSource incomeSource = this.incomeSrcRepository.findById(incomeSourceId)
                    .orElseThrow(() -> new IncomeSourceNotFoundException(incomeSourceId));
            incomeSource.update(
                    request.name(),
                    request.description(),
                    request.recurrency(),
                    request.applicability(),
                    request.currency(),
                    request.defaultAmount(),
                    request.isActive(),
                    request.allowPartialPayment(),
                    request.isTaxable(),
                    request.taxRate(),
                    request.effectiveFrom(),
                    request.effectiveTo()
            );
            IncomeSource incomeSourceUpdated =  this.incomeSrcRepository.update(incomeSource);
            return Optional.of(incomeSourceUpdated);
        } catch (IllegalArgumentException e) {
            throw new InvalidIncomeSourceException("request", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<IncomeSource> getAllIncomeSources() {
        try {
            return incomeSrcRepository.findAll();
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to retrieve income sources", e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<IncomeSource> getIncomeSourceById(UUID id) {
        IncomeSource incomeSource =  incomeSrcRepository.findById(id).orElseThrow(
                ()->  new IncomeSourceNotFoundException(id));
        return Optional.of(incomeSource);
    }

    @Override
    public void deleteIncomeSource(UUID id) {
        try {
            // Check if exists first
            if (incomeSrcRepository.findById(id).isEmpty()) {
                throw new IncomeSourceNotFoundException(id);
            }
            incomeSrcRepository.deleteById(id);
        } catch (IncomeSourceNotFoundException e) {
            throw e; // Re-throw domain exceptions
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to delete income source", e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<IncomeSource> getActiveIncomeSources() {
        try {
            return incomeSrcRepository.findByIsActive(true);
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to retrieve active income sources", e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<IncomeSource> getActiveAndEffectiveIncomeSources() {
        try {
            return incomeSrcRepository.findActiveAndEffective();
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to retrieve active and effective income sources", e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByAccountingCode(String accountingCode) {
        try {
            return incomeSrcRepository.existsByAccountingCode(accountingCode);
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to check accounting code existence", e);
        }
    }


}