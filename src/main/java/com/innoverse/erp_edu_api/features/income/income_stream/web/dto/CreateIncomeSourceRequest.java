package com.innoverse.erp_edu_api.features.income.income_stream.web.dto;

import com.innoverse.erp_edu_api.features.income.income_stream.IncomeSource;
import com.innoverse.erp_edu_api.features.income.income_stream.IncomeSourceType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

public record CreateIncomeSourceRequest(
        @NotBlank @Size(max = 50) IncomeSourceType incomeSourceType,
        @NotBlank @Size(max = 255) String name,
        String description,

        @NotNull IncomeSource.Recurrency recurrency,
        @NotNull IncomeSource.Applicability applicability,
        @NotBlank @Size(max = 10) String currency,

        @DecimalMin("0.00") BigDecimal defaultAmount,
        Boolean isActive,
        Boolean allowPartialPayment,
        Boolean isTaxable,

        @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal taxRate,

        @NotNull LocalDate effectiveFrom,
        LocalDate effectiveTo
) {
}
