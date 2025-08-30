package com.innoverse.erp_edu_api.provisioning.exceptions;


import com.innoverse.erp_edu_api.common.DomainException;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

import java.io.Serial;

public class ProvisioningException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ProvisioningException(String code, String clientMessage, String developerMessage) {
        super(code, clientMessage, developerMessage, Level.ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
    }

    public ProvisioningException(String code, String clientMessage, String developerMessage, Throwable cause) {
        super(code, clientMessage, developerMessage, Level.ERROR, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }

    public ProvisioningException(String code, String clientMessage, String developerMessage,
                                 HttpStatus status, Throwable cause) {
        super(code, clientMessage, developerMessage, Level.ERROR, status, cause);
    }
}

