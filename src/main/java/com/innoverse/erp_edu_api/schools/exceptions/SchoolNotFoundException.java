package com.innoverse.erp_edu_api.schools.exceptions;

import com.innoverse.erp_edu_api.common.DomainException;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class SchoolNotFoundException extends DomainException {
    public SchoolNotFoundException(UUID schoolId) {
        super("SCHOOL",
                "The requested school was not found",
                "School not found: " + schoolId,
                Level.WARN,
                HttpStatus.NOT_FOUND,
                null);
    }
}
