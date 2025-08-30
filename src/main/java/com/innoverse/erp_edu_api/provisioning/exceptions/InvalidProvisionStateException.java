package com.innoverse.erp_edu_api.provisioning.exceptions;

import org.springframework.http.HttpStatus;

import java.io.Serial;

public class InvalidProvisionStateException extends ProvisioningException {
    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidProvisionStateException(String message) {
        super(
                "PROVISION",
                "Invalid provision state: " + message,
                "Provision is in an invalid state for operation: " + message,
                HttpStatus.CONFLICT,
                null
        );
    }
}
