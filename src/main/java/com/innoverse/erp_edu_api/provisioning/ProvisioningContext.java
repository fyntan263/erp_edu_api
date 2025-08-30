package com.innoverse.erp_edu_api.provisioning;

import com.innoverse.erp_edu_api.provisioning.domain.AcademicLevel;

public record ProvisioningContext(String schemaName, AcademicLevel level, String assignedBy) {
    public ProvisioningContext withRole(AcademicLevel level) {
        return new ProvisioningContext(schemaName, level, assignedBy);
    }
}
