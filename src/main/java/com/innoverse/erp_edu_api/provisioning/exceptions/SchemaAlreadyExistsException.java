package com.innoverse.erp_edu_api.provisioning.exceptions;

import org.springframework.http.HttpStatus;

import java.io.Serial;

public class SchemaAlreadyExistsException extends ProvisioningException {
    @Serial
    private static final long serialVersionUID = 1L;

    public SchemaAlreadyExistsException(String schemaName) {
        super(
                "PROVISION",
                "Schema already exists: " + schemaName,
                "Attempted to create schema that already exists: " + schemaName,
                HttpStatus.CONFLICT,
                null
        );
    }
}
