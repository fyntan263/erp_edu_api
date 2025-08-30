package com.innoverse.erp_edu_api.provisioning.exceptions;

import org.springframework.http.HttpStatus;

import java.io.Serial;

public class TenantRoleException extends ProvisioningException {
    @Serial
    private static final long serialVersionUID = 1L;

    public TenantRoleException(String schemaName, String operation, String reason) {
        super(
                "PROVISION",
                "Tenant role operation failed for schema: " + schemaName,
                "Failed to " + operation + " tenant role for schema " + schemaName + ": " + reason,
                HttpStatus.INTERNAL_SERVER_ERROR,
                null
        );
    }

    public TenantRoleException(String schemaName, String operation, String reason, Throwable cause) {
        super(
                "PROVISION_006",
                "Tenant role operation failed for schema: " + schemaName,
                "Failed to " + operation + " tenant role for schema " + schemaName + ": " + reason,
                HttpStatus.INTERNAL_SERVER_ERROR,
                cause
        );
    }
}
