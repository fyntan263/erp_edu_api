package com.innoverse.erp_edu_api.features.income.invoices.adapters;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface InvoiceItemJdbcRepository extends CrudRepository<InvoiceItemEntity, UUID> {
    @Modifying
    @Query("""
        INSERT INTO invoice_line_items (
            line_item_id, invoice_id, income_source_id, description,
            quantity, unit_price, tax_rate, discount_percentage, created_at
        ) VALUES (
            :lineItemId, :invoiceId, :incomeSourceId, :description,
            :quantity, :unitPrice, :taxRate, :discountPercentage, CURRENT_TIMESTAMP
        )
        """)
    int insertLineItem(
            @Param("lineItemId") UUID lineItemId,
            @Param("invoiceId") UUID invoiceId,
            @Param("incomeSourceId") UUID incomeSourceId,
            @Param("description") String description,
            @Param("quantity") Integer quantity,
            @Param("unitPrice") BigDecimal unitPrice,
            @Param("taxRate") BigDecimal taxRate,
            @Param("discountPercentage") BigDecimal discountPercentage);


    // Existing methods
    List<InvoiceItemEntity> findByInvoiceId(UUID invoiceId);
    List<InvoiceItemEntity> findByIncomeSourceId(UUID incomeSourceId);

    @Modifying
    @Query("DELETE FROM invoice_line_items WHERE invoice_id = :invoiceId")
    void deleteByInvoiceId(@Param("invoiceId") UUID invoiceId);

    @Query("SELECT * FROM invoice_line_items WHERE invoice_id IN (:invoiceIds)")
    List<InvoiceItemEntity> findByInvoiceIdIn(@Param("invoiceIds") List<UUID> invoiceIds);
}