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
        boolean isNew = !jdbcRepository.existsById(entity.getInvoiceId());

        if (isNew) {
            jdbcRepository.insertInvoice(
                    entity.getInvoiceId(),
                    entity.getPayeeId(),
                    entity.getPayeeType(),
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
        } else {
            jdbcRepository.save(entity);
            itemJdbcRepository.deleteByInvoiceId(entity.getInvoiceId());
        }
        if (invoice.getLineItems() != null && !invoice.getLineItems().isEmpty()) {
            List<InvoiceItemEntity> itemEntities = invoice.getLineItems()
                    .stream()
                    .map(x -> InvoiceItemEntity.fromDomain(invoice.getInvoiceId(),x))
                    .toList();
            for(InvoiceItemEntity itemEntity : itemEntities) {
                itemJdbcRepository.insertLineItem(
                        itemEntity.getLineItemId(),
                        itemEntity.getInvoiceId(),
                        itemEntity.getIncomeSourceId(),
                        itemEntity.getDescription(),
                        itemEntity.getQuantity(),
                        itemEntity.getUnitPrice(),
                        itemEntity.getTaxRate(),
                        itemEntity.getDiscount()
                );
            }
        }

        // Return the saved invoice by fetching it with items
        return toDomainWithItems(entity);
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
    public List<Invoice> findBypayeeIdAndName(UUID payeeId, String payeeType) {
        return List.of();
    }

    @Override
    public List<Invoice> findByType(String type) {
        return List.of();
    }

    @Override
    public List<Invoice> findBypayeeId(UUID payeeId) {
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invoice> findByPayeeType(UUID payeeId, String payeeType) {
        List<InvoiceEntity> invoices = jdbcRepository.findBypayeeIdAndPayeeType(payeeId,payeeType);
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