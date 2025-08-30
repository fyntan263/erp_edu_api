package com.innoverse.erp_edu_api.common;

import lombok.Getter;
import lombok.ToString;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

import java.io.Serial;

/**
 * Abstract base class for all application-specific runtime exceptions.
 */
@Getter
@ToString(callSuper = true)
public abstract class DomainException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String code;
    private final String clientMessage;
    private final Level logLevel;
    private final HttpStatus status;

    protected DomainException(String code,
                              String clientMessage,
                              String developerMessage,
                              Level logLevel,
                              HttpStatus status,
                              Throwable cause) {
        super(developerMessage != null ? developerMessage : clientMessage, cause);
        this.code = code != null ? code : "APP_ERROR";
        this.clientMessage = clientMessage != null ? clientMessage : "An unexpected error occurred.";
        this.logLevel = logLevel != null ? logLevel : Level.ERROR;
        this.status = status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR;
    }
}