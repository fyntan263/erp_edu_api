package com.innoverse.erp_edu_api.provisioning;

import com.innoverse.erp_edu_api.common.domain.AcademicLevel;
import com.innoverse.erp_edu_api.provisioning.domain.DbProvision;
import com.innoverse.erp_edu_api.provisioning.services.DistributedTenantCache;
import com.innoverse.erp_edu_api.provisioning.services.ProvisioningContext;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProvisioningService {
    List<DbProvision> getByProvisionBySchoolId(UUID schoolId);
    Optional<DbProvision> revokeProvisionAccess(UUID provisionId);
    Optional<DbProvision> grantProvisionAccess(UUID provisionId);
    DbProvision orchestrateProvisioning(ProvisioningContext context);
    void assignToSchool(UUID schoolId, UUID provisionId, String schemaName);
    void unassignFromSchool(UUID schoolId);
    DistributedTenantCache getTenantCache();
    ProvisioningContext buildContext(String schemaName, AcademicLevel level, String assignedBy);
    Optional<DbProvision> assignToSchoolWithAccess(UUID provisionId, UUID schoolId, String assignedBy);
    Optional<DbProvision> unassignFromSchoolWithAccess(UUID provisionId);


}
