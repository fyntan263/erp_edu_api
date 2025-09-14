package com.innoverse.erp_edu_api.features.income.income_stream.jdbc;

import com.innoverse.erp_edu_api.features.income.income_stream.IncomeSource;
import com.innoverse.erp_edu_api.features.income.income_stream.exceptions.IncomeSourceAlreadyExistsException;
import com.innoverse.erp_edu_api.features.income.income_stream.exceptions.IncomeSourceNotFoundException;
import com.innoverse.erp_edu_api.features.income.income_stream.service.IncomeSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Repository
@RequiredArgsConstructor
public class IncomeSourceRepositoryAdapter implements IncomeSourceRepository {
    private final IncomeSourceJdbcRepository repository;

    @Transactional
    @Override
    public IncomeSource save(IncomeSource incomeSource) {
        try {
            if (incomeSource.getIncomeSourceId() != null && repository.existsById(incomeSource.getIncomeSourceId())) {
                throw new IncomeSourceAlreadyExistsException(incomeSource.getIncomeSourceId());
            }

            repository.customInsert(
                    incomeSource.getIncomeSourceId() != null ? incomeSource.getIncomeSourceId() : UUID.randomUUID(),
                    incomeSource.getAccountingCode(),
                    incomeSource.getIncomeSourceType().name(),
                    incomeSource.getName(),
                    incomeSource.getDescription(),
                    incomeSource.getRecurrency().name(),
                    incomeSource.getApplicability().name(),
                    incomeSource.getCurrency(),
                    incomeSource.getDefaultAmount(),
                    incomeSource.getIsActive(),
                    incomeSource.getAllowPartialPayment(),
                    incomeSource.getIsTaxable(),
                    incomeSource.getTaxRate(),
                    incomeSource.getEffectiveFrom(),
                    incomeSource.getEffectiveTo()
            );

            return incomeSource;

        } catch (DuplicateKeyException e) {
            throw new IncomeSourceAlreadyExistsException(incomeSource.getAccountingCode());
        } catch (DataAccessException e) {
            throw new RuntimeException("Database error while saving income source", e);
        }
    }

    @Transactional
    @Override
    public IncomeSource update(IncomeSource incomeSource) {
        try {
            if (incomeSource.getIncomeSourceId() == null || !repository.existsById(incomeSource.getIncomeSourceId())) {
                throw new IncomeSourceNotFoundException(incomeSource.getIncomeSourceId());
            }
            // For updates, we need to implement a custom update method in the JdbcRepository
            System.out.printf("Updating %s", incomeSource);
            repository.save(IncomeSourceEntity.fromDomain(incomeSource));
            return incomeSource;

        } catch (DuplicateKeyException e) {
            throw new IncomeSourceAlreadyExistsException(incomeSource.getAccountingCode());
        } catch (DataAccessException e) {
            throw new RuntimeException("Database error while updating income source", e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<IncomeSource> findById(UUID id) {
        try {
            return repository.findById(id).map(IncomeSourceEntity::toDomain);
        } catch (DataAccessException e) {
            throw new RuntimeException("Database error while finding income source by ID", e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<IncomeSource> findAll() {
        try {
            return StreamSupport.stream(repository.findAll().spliterator(), true)
                    .map(IncomeSourceEntity::toDomain)
                    .toList();
        } catch (DataAccessException e) {
            throw new RuntimeException("Database error while finding all income sources", e);
        }
    }

    @Transactional
    @Override
    public void deleteById(UUID id) {
        try {
            if (!repository.existsById(id)) {
                throw new IncomeSourceNotFoundException(id);
            }
            repository.deleteById(id);
        } catch (DataAccessException e) {
            throw new RuntimeException("Database error while deleting income source", e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<IncomeSource> findByAccountingCode(String accountingCode) {
        try {
            return repository.findByAccountingCode(accountingCode).map(IncomeSourceEntity::toDomain);
        } catch (DataAccessException e) {
            throw new RuntimeException("Database error while finding income source by accounting code", e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<IncomeSource> findByIsActive(Boolean isActive) {
        try {
            return repository.findByIsActive(isActive)
                    .stream().map(IncomeSourceEntity::toDomain).toList();
        } catch (DataAccessException e) {
            throw new RuntimeException("Database error while finding income sources by active status", e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByAccountingCode(String accountingCode) {
        try {
            return repository.existsByAccountingCode(accountingCode);
        } catch (DataAccessException e) {
            throw new RuntimeException("Database error while checking existence by accounting code", e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<IncomeSource> findByAccountingCodeAndIdNot(String accountingCode, UUID id) {
        try {
            return repository.findByAccountingCodeAndIdNot(accountingCode, id).map(IncomeSourceEntity::toDomain);
        } catch (DataAccessException e) {
            throw new RuntimeException("Database error while finding income source by accounting code excluding ID", e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<IncomeSource> findActiveAndEffective() {
        try {
            return repository.findActiveAndEffective();
        } catch (DataAccessException e) {
            throw new RuntimeException("Database error while finding active and effective income sources", e);
        }
    }
}