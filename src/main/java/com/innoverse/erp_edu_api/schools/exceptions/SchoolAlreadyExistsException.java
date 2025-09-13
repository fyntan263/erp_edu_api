package com.innoverse.erp_edu_api.schools.exceptions;

import com.innoverse.erp_edu_api.common.errors.DomainException;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

public class SchoolAlreadyExistsException extends DomainException {
    public SchoolAlreadyExistsException(String field, String value) {
        super("SCHOOL",
                "A school with this " + field + " already exists",
                "School already exists with " + field + ": " + value,
                Level.WARN,
                HttpStatus.CONFLICT,
                null);
    }
}
