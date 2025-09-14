package com.innoverse.erp_edu_api.features.income.payments.exceptions;

import com.innoverse.erp_edu_api.common.errors.DomainException;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class PaymentAlreadyExistsException extends DomainException {

    public PaymentAlreadyExistsException(UUID paymentId) {
        super(
                "PAYMENT_ALREADY_EXISTS",
                "Payment already exists with ID: " + paymentId,
                "Payment with ID " + paymentId + " already exists in the database",
                Level.WARN,
                HttpStatus.CONFLICT,
                null
        );
    }
}