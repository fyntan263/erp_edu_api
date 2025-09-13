package com.innoverse.erp_edu_api.features.income.income_stream.exceptions;


import com.innoverse.erp_edu_api.common.errors.DomainException;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class IncomeSourceAlreadyExistsException extends DomainException {

    public IncomeSourceAlreadyExistsException(UUID incomeSourceId) {
        super(
                "INCOME_SOURCE_ALREADY_EXISTS",
                "Income source already exists with ID: " + incomeSourceId,
                "Income source with ID " + incomeSourceId + " already exists in the database",
                Level.WARN,
                HttpStatus.CONFLICT,
                null
        );
    }

    public IncomeSourceAlreadyExistsException(String accountingCode) {
        super(
                "INCOME_SOURCE_ALREADY_EXISTS",
                "Income source already exists with accounting code: " + accountingCode,
                "Income source with accounting code " + accountingCode + " already exists in the database",
                Level.WARN,
                HttpStatus.CONFLICT,
                null
        );
    }
}