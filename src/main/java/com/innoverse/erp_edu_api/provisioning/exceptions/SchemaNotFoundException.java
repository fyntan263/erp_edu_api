package com.innoverse.erp_edu_api.provisioning.exceptions;

import org.springframework.http.HttpStatus;

import java.io.Serial;

public class SchemaNotFoundException extends ProvisioningException {
    @Serial
    private static final long serialVersionUID = 1L;

    public SchemaNotFoundException(String schemaName) {
        super(
                "PROVISION",
                "Schema not found: " + schemaName,
                "Requested schema does not exist: " + schemaName,
                HttpStatus.NOT_FOUND,
                null
        );
    }
}
