package com.innoverse.erp_edu_api.provisioning.exceptions;

import org.springframework.http.HttpStatus;

import java.io.Serial;

public class MigrationFailedException extends ProvisioningException {
    @Serial
    private static final long serialVersionUID = 1L;

    public MigrationFailedException(String schemaName, String reason) {
        super(
                "PROVISION",
                "Migration failed for schema: " + schemaName,
                "Database migration failed for schema " + schemaName + ": " + reason,
                HttpStatus.INTERNAL_SERVER_ERROR,
                null
        );
    }

    public MigrationFailedException(String schemaName, String reason, Throwable cause) {
        super(
                "PROVISION",
                "Migration failed for schema: " + schemaName,
                "Database migration failed for schema " + schemaName + ": " + reason,
                HttpStatus.INTERNAL_SERVER_ERROR,
                cause
        );
    }
}
