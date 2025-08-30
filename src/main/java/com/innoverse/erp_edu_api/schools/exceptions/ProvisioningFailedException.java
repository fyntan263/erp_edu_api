package com.innoverse.erp_edu_api.schools.exceptions;

import com.innoverse.erp_edu_api.common.DomainException;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

public class ProvisioningFailedException extends DomainException {
    public ProvisioningFailedException(String schemaName, Throwable cause) {
        super("PROVISION",
                "Failed to provision database for tenant",
                "Provisioning failed for schema: " + schemaName,
                Level.ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR,
                cause);
    }
}
