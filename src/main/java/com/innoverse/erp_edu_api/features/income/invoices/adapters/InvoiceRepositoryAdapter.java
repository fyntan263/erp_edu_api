package com.innoverse.erp_edu_api.features.income.invoices.adapters;

import com.innoverse.erp_edu_api.features.income.invoices.Invoice;
import com.innoverse.erp_edu_api.features.income.invoices.domain.InvoiceItem;
import com.innoverse.erp_edu_api.features.income.invoices.services.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class InvoiceRepositoryAdapter implements InvoiceRepository {

    private final InvoiceJdbcRepository jdbcRepository;
    private final InvoiceItemJdbcRepository itemJdbcRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public Invoice save(Invoice invoice) {
        InvoiceEntity entity = InvoiceEntity.fromDomain(invoice);
        InvoiceEntity savedEntity = null;
        if(this.jdbcRepository.existsById(entity.getInvoiceId())) {
           savedEntity = jdbcRepository.save(entity);
        } else {
             jdbcRepository.insertInvoice(
                    entity.getInvoiceId(),
                    entity.getEntityId(),
                    entity.getEntityType(),
                    entity.getInvoiceNo(),
                    entity.getDescription(),
                    entity.getIssueDate(),
                    entity.getDueDate(),
                    entity.getTotalAmount(),
                    entity.getAmountPaid(),
                    entity.getCurrency(),
                    entity.getStatus(),
                    entity.getNotes()
            );
        }


        // Save line items
        if (invoice.getLineItems() != null && !invoice.getLineItems().isEmpty()) {
            this.itemJdbcRepository.saveAll(invoice.getLineItems()
                    .stream().map(InvoiceItemEntity::fromDomain).collect(Collectors.toList()));
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Invoice> findById(UUID invoiceId) {
        return jdbcRepository.findById(invoiceId)
                .map(this::toDomainWithItems);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Invoice> findByInvoiceNo(String invoiceNo) {
        return jdbcRepository.findByInvoiceNo(invoiceNo)
                .map(this::toDomainWithItems);
    }

    @Override
    public List<Invoice> findByEntityIdAndName(UUID entityId, String entityType) {
        return List.of();
    }

    @Override
    public List<Invoice> findByType(String type) {
        return List.of();
    }

    @Override
    public List<Invoice> findByEntityId(UUID entityId) {
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invoice> findByEntityType(UUID entityId, String entityType) {
        List<InvoiceEntity> invoices = jdbcRepository.findByEntityIdAndEntityType(entityId,entityType);
        return invoices.stream()
                .map(this::toDomainWithItems)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invoice> findByStatus(Invoice.Status status) {
        List<InvoiceEntity> invoices = jdbcRepository.findByStatus(status.name());
        return invoices.stream()
                .map(this::toDomainWithItems)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invoice> findOverdueInvoices(LocalDate currentDate) {
        List<InvoiceEntity> invoices = jdbcRepository.findOverdueInvoices(currentDate);
        return invoices.stream()
                .map(this::toDomainWithItems)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(UUID invoiceId) {
        itemJdbcRepository.deleteByInvoiceId(invoiceId);
        jdbcRepository.deleteById(invoiceId);
    }

    @Transactional
    public void applyPayment(UUID invoiceId, BigDecimal amount) {
        jdbcRepository.applyPayment(invoiceId, amount);
    }

    @Transactional
    public void updateStatus(UUID invoiceId, Invoice.Status status) {
        jdbcRepository.updateStatus(invoiceId, status.name());
    }

    private Invoice toDomainWithItems(InvoiceEntity entity) {
        List<InvoiceItemEntity> itemEntities = itemJdbcRepository.findByInvoiceId(entity.getInvoiceId());
        List<InvoiceItem> lineItems = itemEntities.stream()
                .map(InvoiceItemEntity::toDomain)
                .collect(Collectors.toList());

        Invoice invoice = entity.toDomain();
        return invoice.builder().lineItems(lineItems).build();
    }
}