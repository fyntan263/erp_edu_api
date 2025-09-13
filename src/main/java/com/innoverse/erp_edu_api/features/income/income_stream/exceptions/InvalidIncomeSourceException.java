package com.innoverse.erp_edu_api.features.income.income_stream.exceptions;


import com.innoverse.erp_edu_api.common.errors.DomainException;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

public class InvalidIncomeSourceException extends DomainException {

    public InvalidIncomeSourceException(String field, String reason) {
        super(
                "INVALID_INCOME_SOURCE",
                "Invalid income source data: " + reason,
                "Income source validation failed for field '" + field + "': " + reason,
                Level.WARN,
                HttpStatus.BAD_REQUEST,
                null
        );
    }
}
