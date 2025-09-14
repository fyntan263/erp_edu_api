package com.innoverse.erp_edu_api.features.income.income_stream;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class IncomeSource {
    private UUID incomeSourceId;
    private String accountingCode;
    private IncomeSourceType incomeSourceType;
    private String name;
    private String description;
    private Recurrency recurrency;
    private Applicability applicability;
    private String currency;
    private BigDecimal defaultAmount;
    private Boolean isActive;
    private Boolean allowPartialPayment;
    private Boolean isTaxable;
    private BigDecimal taxRate;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    public enum Recurrency {
        ONE_TIME, DAILY, WEEKLY, MONTHLY, QUARTERLY, ANNUAL, TERMLY, SEMESTERLY, CUSTOM
    }

    public enum Applicability {
        PER_STUDENT, PER_CLASS, PER_GRADE, PER_HOUSE, SCHOOL_WIDE, PER_EVENT
    }

    public static IncomeSource create(
            IncomeSourceType incomeSourceType,
            String name,
            String description,
            Recurrency recurrency,
            Applicability applicability,
            String currency,
            BigDecimal defaultAmount,
            Boolean isActive,
            Boolean allowPartialPayment,
            Boolean isTaxable,
            BigDecimal taxRate,
            LocalDate effectiveFrom,
            LocalDate effectiveTo) {

        return IncomeSource.builder()
                .incomeSourceId(UUID.randomUUID())
                .accountingCode(generateAccountingCode())
                .incomeSourceType(incomeSourceType)
                .name(name)
                .description(description)
                .recurrency(recurrency)
                .applicability(applicability)
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

    private static String generateAccountingCode() {
        return "INC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public boolean isCurrentlyEffective() {
        LocalDate today = LocalDate.now();
        return isActive &&
                !today.isBefore(effectiveFrom) &&
                (effectiveTo == null || !today.isAfter(effectiveTo));
    }

    public boolean update(String name, String description, Recurrency recurrency,
                       Applicability applicability, String currency, BigDecimal defaultAmount,
                       Boolean isActive, Boolean allowPartialPayment, Boolean isTaxable,
                       BigDecimal taxRate, LocalDate effectiveFrom, LocalDate effectiveTo) {
        boolean changed = false;
        if(name !=null && name.isBlank() && !this.name.equals(name) ) {
            this.name = name;
            changed = true;
        }if(description != null && description.isBlank() && !this.description.equals(description) ) {
            this.description = description;
            changed = true;
        }if (recurrency != null && this.recurrency != recurrency){
            this.recurrency = recurrency;
            changed = true;
        }if(applicability != null && this.applicability != applicability){
            this.applicability = applicability;
            changed = true;
        }if(currency != null && currency.isBlank() && !this.currency.equals(currency) ) {
            this.currency = currency;
            changed = true;
        }if (defaultAmount != null && this.defaultAmount != defaultAmount){
            this.defaultAmount = defaultAmount;
            changed = true;
        }if(isActive != null && isActive){
            this.isActive = isActive;
            changed = true;
        }if(allowPartialPayment != null && allowPartialPayment){
            this.allowPartialPayment = allowPartialPayment;
            changed = true;
        }if(isTaxable != null && isTaxable){
            this.isTaxable = isTaxable;
            changed = true;
        }if(taxRate != null && taxRate.compareTo(BigDecimal.ZERO) == 0){
            this.taxRate = taxRate;
            changed = true;
        }if(effectiveFrom != null && effectiveTo != null){
            this.effectiveFrom = effectiveFrom;
            changed = true;
        }if(effectiveTo != null && effectiveFrom != null){
            this.effectiveTo = effectiveTo;
            changed = true;
        }
       return changed;
    }

    @Override
    public String toString() {
        return "IncomeSource{" +
                "incomeSourceId=" + incomeSourceId +
                ", accountingCode='" + accountingCode + '\'' +
                ", incomeSourceType='" + incomeSourceType + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", recurrency=" + recurrency +
                ", applicability=" + applicability +
                ", currency='" + currency + '\'' +
                ", defaultAmount=" + defaultAmount +
                ", isActive=" + isActive +
                ", allowPartialPayment=" + allowPartialPayment +
                ", isTaxable=" + isTaxable +
                ", taxRate=" + taxRate +
                ", effectiveFrom=" + effectiveFrom +
                ", effectiveTo=" + effectiveTo +
                '}';
    }
}