package com.innoverse.erp_edu_api.features.income.income_stream.web.dto;

import com.innoverse.erp_edu_api.features.income.income_stream.IncomeSource;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateIncomeSourceRequest(
        @Size(max = 50) String feeTypeCode,
        @Size(max = 255) String name,
        String description,
        IncomeSource.Recurrency recurrency,
        IncomeSource.Applicability applicability,
        @Size(max = 10) String currency,
        @DecimalMin("0.00") BigDecimal defaultAmount,
        Boolean isActive,
        Boolean allowPartialPayment,
        boolean isTaxable,
        @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal taxRate,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {
}
