package com.innoverse.erp_edu_api.features.income.income_stream;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class TuitionIncomeSource extends IncomeSource {

    public TuitionIncomeSource(UUID incomeSourceId, String accountingCode, String feeTypeCode, String name, String description, Recurrency recurrency, Applicability applicability, String currency, BigDecimal defaultAmount, Boolean isActive, Boolean allowPartialPayment, Boolean isTaxable, BigDecimal taxRate, LocalDate effectiveFrom, LocalDate effectiveTo) {
        super(incomeSourceId, accountingCode, feeTypeCode, name, description, recurrency, applicability, currency, defaultAmount, isActive, allowPartialPayment, isTaxable, taxRate, effectiveFrom, effectiveTo);
    }
}
