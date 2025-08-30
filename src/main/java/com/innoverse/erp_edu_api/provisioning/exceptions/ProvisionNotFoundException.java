package com.innoverse.erp_edu_api.provisioning.exceptions;

import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.util.UUID;

public class ProvisionNotFoundException extends ProvisioningException {
    @Serial
    private static final long serialVersionUID = 1L;

    public ProvisionNotFoundException(UUID provisionId) {
        super(
                "PROVISION",
                "Provision not found with ID: " + provisionId,
                "Provision record not found for ID: " + provisionId,
                HttpStatus.NOT_FOUND,
                null
        );
    }
}
