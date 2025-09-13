package com.innoverse.erp_edu_api.features.income.invoices.adapters;

import com.innoverse.erp_edu_api.features.income.invoices.domain.InvoiceItem;
import com.innoverse.erp_edu_api.features.income.invoices.services.InvoiceItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class InvoiceItemRepositoryAdapter implements InvoiceItemRepository {
    private final InvoiceItemJdbcRepository jdbcRepository;

    @Override
    public InvoiceItem save(InvoiceItem item) {
        InvoiceItemEntity entity = InvoiceItemEntity.fromDomain(item);
        InvoiceItemEntity savedEntity = jdbcRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public List<InvoiceItem> findByInvoiceId(UUID invoiceId) {
        return jdbcRepository.findByInvoiceId(invoiceId).stream()
                .map(InvoiceItemEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<InvoiceItem> findByIncomeSourceId(UUID incomeSourceId) {
        return jdbcRepository.findByIncomeSourceId(incomeSourceId).stream()
                .map(InvoiceItemEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByInvoiceId(UUID invoiceId) {
        jdbcRepository.deleteByInvoiceId(invoiceId);
    }

    @Override
    public void deleteById(UUID lineItemId) {
        jdbcRepository.deleteById(lineItemId);
    }
}