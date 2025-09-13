package com.innoverse.erp_edu_api.schools.services;

import com.innoverse.erp_edu_api.provisioning.ProvisioningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolAccessControlService  {
    private final ProvisioningService provisioningService;

    public boolean canSchoolAccessDatabase(UUID schoolId) {
        return this.provisioningService.getTenantCache().hasAccess(schoolId);
    }


    public void validateSchoolDatabaseAccess(UUID schoolId) {
        if (!canSchoolAccessDatabase(schoolId)) {
//            throw new SchoolAccessDeniedException(
//                    schoolId,
//                    "Database access denied. School may be suspended or not provisioned."
//            );
            throw new RuntimeException("School Access Denied");
        }
    }

    @Transactional
    public void revokeDatabaseAccess(UUID schoolId) {
        this.provisioningService.getByProvisionBySchoolId(schoolId).forEach(provision -> {
            try {
                this.provisioningService.revokeProvisionAccess(provision.getProvisionId());
                log.info("Database access revoked for school: {} from schema: {}",
                        schoolId, provision.getDbSchemaName());

                this.provisioningService.getTenantCache().invalidateCaches(schoolId);

            } catch (Exception e) {
                log.warn("Failed to revoke database access for provision: {}",
                        provision.getProvisionId(), e);
            }
        });
    }

    @Transactional
    public void grantDatabaseAccess(UUID schoolId) {
        this.provisioningService.getByProvisionBySchoolId(schoolId).forEach(provision -> {
            try {
                if (provision.isProvisioned() && provision.isAssigned()) {
                    this.provisioningService.grantProvisionAccess(provision.getProvisionId());
                    log.info("Database access granted for school: {} to schema: {}",
                            schoolId, provision.getDbSchemaName());
                    this.provisioningService.getTenantCache().updateAccessCache(schoolId, true);
                }
            } catch (Exception e) {
                log.warn("Failed to grant database access for provision: {}",
                        provision.getProvisionId(), e);
            }
        });
    }


    public void refreshAccessCache(UUID schoolId) {
        this.provisioningService.getTenantCache().invalidateCaches(schoolId);
    }
}