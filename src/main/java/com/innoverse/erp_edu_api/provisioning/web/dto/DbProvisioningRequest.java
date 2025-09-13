package com.innoverse.erp_edu_api.provisioning.web.dto;

import com.innoverse.erp_edu_api.common.domain.AcademicLevel;

public record DbProvisioningRequest(String schemaName, AcademicLevel level) { }
