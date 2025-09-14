package com.innoverse.erp_edu_api.features.income.income_stream.web.dto;

import com.innoverse.erp_edu_api.features.income.income_stream.IncomeSource;
import com.innoverse.erp_edu_api.features.income.income_stream.IncomeSourceType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record IncomeSourceDto(
        UUID id,
        String accountingCode,
        IncomeSourceType incomeSourceType,
        String name,
        String description,
        String recurrency,
        String applicability,
        String currency,
        BigDecimal defaultAmount,
        Boolean isActive,
        Boolean allowPartialPayment,
        Boolean isTaxable,
        BigDecimal taxRate,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {
    @Builder
    public IncomeSourceDto {}

    public static IncomeSourceDto fromDomain(IncomeSource source) {
        if (source == null) {
            return null;
        }

        return IncomeSourceDto.builder()
                .id(source.getIncomeSourceId())
                .accountingCode(source.getAccountingCode())
                .incomeSourceType(source.getIncomeSourceType())
                .name(source.getName())
                .description(source.getDescription())
                .recurrency(source.getRecurrency() != null ? source.getRecurrency().name() : null)
                .applicability(source.getApplicability() != null ? source.getApplicability().name() : null)
                .currency(source.getCurrency() != null ? source.getCurrency() : null)
                .defaultAmount(source.getDefaultAmount())
                .isActive(source.getIsActive())
                .allowPartialPayment(source.getAllowPartialPayment())
                .isTaxable(source.getIsTaxable())
                .taxRate(source.getTaxRate())
                .effectiveFrom(source.getEffectiveFrom())
                .effectiveTo(source.getEffectiveTo())
                .build();
    }
}
