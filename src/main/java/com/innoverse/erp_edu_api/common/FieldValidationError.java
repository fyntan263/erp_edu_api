package com.innoverse.erp_edu_api.common;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FieldValidationError {
    private String field;
    private Object rejectedValue;
    private String message;
}