package com.innoverse.erp_edu_api.features.income.income_stream.jdbc;

import com.innoverse.erp_edu_api.features.income.income_stream.IncomeSource;
import com.innoverse.erp_edu_api.features.income.income_stream.IncomeSourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;

@Table("income_sources")
@Getter
@Builder
@AllArgsConstructor
@ToString
public class IncomeSourceEntity {

    @Id
    @Column("income_source_id")
    private UUID incomeSourceId;

    @Column("accounting_code")
    private String accountingCode;

    @Column("income_source_type")
    private String incomeSourceType;

    @Column("name")
    private String name;

    @Column("description")
    private String description;

    @Column("recurrency")
    private String recurrency;

    @Column("applicability")
    private String applicability;

    @Column("currency")
    private String currency;

    @Column("default_amount")
    private BigDecimal defaultAmount;

    @Column("is_active")
    private Boolean isActive;

    @Column("allow_partial_payment")
    private Boolean allowPartialPayment;

    @Column("is_taxable")
    private Boolean isTaxable;

    @Column("tax_rate")
    private BigDecimal taxRate;

    @Column("effective_from")
    private LocalDate effectiveFrom;

    @Column("effective_to")
    private LocalDate effectiveTo;

    @CreatedDate
    @Column("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private Instant updatedAt;

    @CreatedBy
    @Column("created_by")
    private String createdBy;

    @LastModifiedBy
    @Column("updated_by")
    private String updatedBy;

    public static IncomeSourceEntity fromDomain(IncomeSource domain) {
        return IncomeSourceEntity.builder()
                .incomeSourceId(domain.getIncomeSourceId())
                .accountingCode(domain.getAccountingCode())
                .incomeSourceType(domain.getIncomeSourceType().name())
                .name(domain.getName())
                .description(domain.getDescription())
                .recurrency(domain.getRecurrency().name())
                .applicability(domain.getApplicability().name())
                .currency(domain.getCurrency())
                .defaultAmount(domain.getDefaultAmount())
                .isActive(domain.getIsActive())
                .allowPartialPayment(domain.getAllowPartialPayment())
                .isTaxable(domain.getIsTaxable())
                .taxRate(domain.getTaxRate())
                .effectiveFrom(domain.getEffectiveFrom())
                .effectiveTo(domain.getEffectiveTo())
                .build();
    }

    public IncomeSource toDomain() {
        return IncomeSource.builder()
                .incomeSourceId(incomeSourceId)
                .accountingCode(accountingCode)
                .incomeSourceType(IncomeSourceType.valueOf(incomeSourceType))
                .name(name)
                .description(description)
                .recurrency(IncomeSource.Recurrency.valueOf(recurrency))
                .applicability(IncomeSource.Applicability.valueOf(applicability))
                .currency(currency)
                .defaultAmount(defaultAmount)
                .isActive(isActive)
                .allowPartialPayment(allowPartialPayment)
                .isTaxable(isTaxable)
                .taxRate(taxRate)
                .effectiveFrom(effectiveFrom)
                .effectiveTo(effectiveTo)
                .build();
    }

    public void updateFromDomain(IncomeSource domain) {
        this.incomeSourceType = domain.getIncomeSourceType().name();
        this.name = domain.getName();
        this.description = domain.getDescription();
        this.recurrency = domain.getRecurrency().name();
        this.applicability = domain.getApplicability().name();
        this.currency = domain.getCurrency();
        this.defaultAmount = domain.getDefaultAmount();
        this.isActive = domain.getIsActive();
        this.allowPartialPayment = domain.getAllowPartialPayment();
        this.isTaxable = domain.getIsTaxable();
        this.taxRate = domain.getTaxRate();
        this.effectiveFrom = domain.getEffectiveFrom();
        this.effectiveTo = domain.getEffectiveTo();
        this.updatedAt = Instant.now();
    }
}