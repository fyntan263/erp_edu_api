package com.innoverse.erp_edu_api.features.income.income_stream.exceptions;


import com.innoverse.erp_edu_api.common.errors.DomainException;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class IncomeSourceNotFoundException extends DomainException {

    public IncomeSourceNotFoundException(UUID incomeSourceId) {
        super(
                "INCOME_SOURCE_NOT_FOUND",
                "Income source not found with ID: " + incomeSourceId,
                "Income source with ID " + incomeSourceId + " was not found in the database",
                Level.WARN,
                HttpStatus.NOT_FOUND,
                null
        );
    }

    public IncomeSourceNotFoundException(String accountingCode) {
        super(
                "INCOME_SOURCE_NOT_FOUND",
                "Income source not found with accounting code: " + accountingCode,
                "Income source with accounting code " + accountingCode + " was not found in the database",
                Level.WARN,
                HttpStatus.NOT_FOUND,
                null
        );
    }
}
