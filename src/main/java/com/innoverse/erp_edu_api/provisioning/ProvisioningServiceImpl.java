package com.innoverse.erp_edu_api.provisioning;

import com.innoverse.erp_edu_api.common.domain.AcademicLevel;
import com.innoverse.erp_edu_api.provisioning.domain.DbProvision;
import com.innoverse.erp_edu_api.provisioning.services.DistributedTenantCache;
import com.innoverse.erp_edu_api.provisioning.services.ProvisioningContext;
import com.innoverse.erp_edu_api.provisioning.services.ProvisioningTrackingService;
import com.innoverse.erp_edu_api.provisioning.services.TenantProvisioningOrchestrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProvisioningServiceImpl implements ProvisioningService {
    private final TenantProvisioningOrchestrationService orchestrationService;
    private final ProvisioningTrackingService trackingService;
    private final DistributedTenantCache tenantCache;

    @Override
    public List<DbProvision> getByProvisionBySchoolId(UUID schoolId) {
        return this.trackingService.getBySchoolId(schoolId);
    }

    @Override
    public Optional<DbProvision> revokeProvisionAccess(UUID provisionId) {
        return this.trackingService.revokeProvisionAccess(provisionId);
    }

    @Override
    public Optional<DbProvision> grantProvisionAccess(UUID provisionId) {
        return this.trackingService.grantProvisionAccess(provisionId);
    }

    @Override
    public DbProvision orchestrateProvisioning(ProvisioningContext context) {
        return orchestrationService.orchestrateProvisioning(context);
    }

    @Override
    public void assignToSchool(UUID schoolId, UUID provisionId, String schemaName) {
        trackingService.assignToSchoolWithAccess(schoolId, provisionId, schemaName);
    }

    @Override
    public void unassignFromSchool(UUID schoolId) {
        trackingService.unassignFromSchoolWithAccess(schoolId);
    }

    @Override
    public DistributedTenantCache getTenantCache() {
        return this.tenantCache;
    }

    @Override
    public ProvisioningContext buildContext(String schemaName, AcademicLevel level,String assignedBy) {
        return new ProvisioningContext(schemaName, level, assignedBy);
    }

    @Override
    public Optional<DbProvision> assignToSchoolWithAccess(UUID provisionId, UUID schoolId, String assignedBy) {
        return this.trackingService.assignToSchoolWithAccess(provisionId, schoolId, assignedBy);
    }

    @Override
    public Optional<DbProvision> unassignFromSchoolWithAccess(UUID provisionId) {
        return this.trackingService.unassignFromSchoolWithAccess(provisionId);
    }
}
