package com.innoverse.erp_edu_api.provisioning.infrastructure.migration;

import com.innoverse.erp_edu_api.provisioning.domain.AcademicLevel;

public interface MigrationService {
    void migrate(String tenantId, AcademicLevel level);
    void migrate(String tenantId, String[] paths);
    boolean isSchemaUpToDate(String tenantId, AcademicLevel level);
    boolean isSchemaUpToDate(String tenantId, String[] locations);
}
