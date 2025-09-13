package com.innoverse.erp_edu_api.features.income.invoices.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceItemRequest(
        @NotNull UUID incomeSourceId,
        @NotBlank String description,
        @Min(1) Integer quantity,
        @Positive BigDecimal unitPrice,
        @DecimalMin("0.0") BigDecimal taxRate,
        @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal discountPercentage
) { }
