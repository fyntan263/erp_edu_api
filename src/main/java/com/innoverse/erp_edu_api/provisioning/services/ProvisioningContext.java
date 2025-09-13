package com.innoverse.erp_edu_api.provisioning.services;

import com.innoverse.erp_edu_api.common.domain.AcademicLevel;

public record ProvisioningContext(String schemaName, AcademicLevel level, String assignedBy) {
    public ProvisioningContext withRole(AcademicLevel level) {
        return new ProvisioningContext(schemaName, level, assignedBy);
    }
}
