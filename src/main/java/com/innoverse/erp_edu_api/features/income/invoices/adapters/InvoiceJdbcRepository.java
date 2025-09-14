package com.innoverse.erp_edu_api.features.income.invoices.adapters;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceJdbcRepository extends CrudRepository<InvoiceEntity, UUID> {
    @Modifying
    @Query("""
        INSERT INTO invoices (
            invoice_id, entity_id, entity_type, invoice_number, description, 
            issue_date, due_date, total_amount, amount_paid, 
            currency, status, notes
        ) VALUES (
            :invoiceId, :payeeId, :payeeType, :invoiceNo, :description,
            :issueDate, :dueDate, :totalAmount, :amountPaid,
            :currency, :status, :notes
        )
        """)
    int insertInvoice(
            @Param("invoiceId") UUID invoiceId,
            @Param("payeeId") UUID payeeId,
            @Param("payeeType") String payeeType,
            @Param("invoiceNo") String invoiceNo,
            @Param("description") String description,
            @Param("issueDate") LocalDate issueDate,
            @Param("dueDate") LocalDate dueDate,
            @Param("totalAmount") BigDecimal totalAmount,
            @Param("amountPaid") BigDecimal amountPaid,
            @Param("currency") String currency,
            @Param("status") String status,
            @Param("notes") String notes);

    @Query("""
        SELECT i.*, li.line_item_id, li.income_source_id, li.description, 
               li.quantity, li.unit_price, li.tax_rate, li.discount_percentage
        FROM invoices i 
        LEFT JOIN invoice_line_items li ON i.invoice_id = li.invoice_id 
        WHERE i.invoice_id = :invoiceId
        """)
    @MappedCollection(idColumn = "invoice_id", keyColumn = "line_item_id")
    Optional<InvoiceWithItemsView> findInvoiceWithItems(@Param("invoiceId") UUID invoiceId);

    @Query("""
        SELECT i.*, li.line_item_id, li.income_source_id, li.description, 
               li.quantity, li.unit_price, li.tax_rate, li.discount_percentage
        FROM invoices i 
        LEFT JOIN invoice_line_items li ON i.invoice_id = li.invoice_id 
        WHERE i.entity_id = :payeeId AND i.status = :status
        """)
    List<InvoiceWithItemsView> findInvoicesWithItemsByEntityAndStatus(
            @Param("payeeId") UUID payeeId,
            @Param("status") String status);

    // Search by payeeId
    @Query("""
        SELECT i.*, li.line_item_id, li.income_source_id, li.description, 
               li.quantity, li.unit_price, li.tax_rate, li.discount_percentage
        FROM invoices i 
        LEFT JOIN invoice_line_items li ON i.invoice_id = li.invoice_id 
        WHERE i.entity_id = :payeeId
        """)
    List<InvoiceWithItemsView> findInvoicesWithItemsBypayeeId(@Param("payeeId") UUID payeeId);

    // Search by invoiceFor
    @Query("""
        SELECT i.*, li.line_item_id, li.income_source_id, li.description, 
               li.quantity, li.unit_price, li.tax_rate, li.discount_percentage
        FROM invoices i 
        LEFT JOIN invoice_line_items li ON i.invoice_id = li.invoice_id 
        WHERE i.invoice_for = :invoiceFor
        """)
    List<InvoiceWithItemsView> findInvoicesWithItemsByInvoiceFor(@Param("invoiceFor") String invoiceFor);

    // Search by both payeeId and invoiceFor
    @Query("""
        SELECT i.*, li.line_item_id, li.income_source_id, li.description, 
               li.quantity, li.unit_price, li.tax_rate, li.discount_percentage
        FROM invoices i 
        LEFT JOIN invoice_line_items li ON i.invoice_id = li.invoice_id 
        WHERE i.entity_id = :payeeId AND i.invoice_for = :invoiceFor
        """)
    List<InvoiceWithItemsView> findInvoicesWithItemsByEntityAndInvoiceFor(
            @Param("payeeId") UUID payeeId,
            @Param("invoiceFor") String invoiceFor);

    Optional<InvoiceEntity> findByInvoiceNo(String InvoiceNo);

    List<InvoiceEntity> findBypayeeId(UUID payeeId);

    List<InvoiceEntity> findByPayeeType(String payeeType);

    List<InvoiceEntity> findBypayeeIdAndPayeeType(UUID payeeId, String payeeType);

    List<InvoiceEntity> findByStatus(String status);

    @Query("SELECT * FROM invoices WHERE due_date < :currentDate AND status IN ('ISSUED', 'PARTIALLY_PAID')")
    List<InvoiceEntity> findOverdueInvoices(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT COALESCE(SUM(total_amount), 0) FROM invoices WHERE entity_id = :payeeId")
    BigDecimal sumInvoicedAmountBypayeeId(@Param("payeeId") UUID payeeId);

    @Query("SELECT COALESCE(SUM(total_amount - amount_paid), 0) FROM invoices WHERE entity_id = :payeeId AND status IN ('ISSUED', 'PARTIALLY_PAID', 'OVERDUE')")
    BigDecimal sumOutstandingAmountBypayeeId(@Param("payeeId") UUID payeeId);

    @Modifying
    @Query("UPDATE invoices SET amount_paid = amount_paid + :amount WHERE invoice_id = :invoiceId")
    void applyPayment(@Param("invoiceId") UUID invoiceId, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE invoices SET status = :status WHERE invoice_id = :invoiceId")
    void updateStatus(@Param("invoiceId") UUID invoiceId, @Param("status") String status);
}
