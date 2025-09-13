package com.innoverse.erp_edu_api.provisioning.web.resolvers;

import com.innoverse.erp_edu_api.provisioning.services.DistributedTenantCache;
import com.innoverse.erp_edu_api.provisioning.infrastructure.config.TenantProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class TenantHeaderResolver implements TenantResolver {
    private final TenantProperties tenantProperties;
    private final DistributedTenantCache tenantCache;

    public TenantHeaderResolver(TenantProperties tenantProperties, DistributedTenantCache tenantCache) {
        this.tenantProperties = tenantProperties;
        this.tenantCache = tenantCache;
    }
    @Override
    public String resolve(HttpServletRequest req) {
        String schoolIdHeader = req.getHeader(tenantProperties.getHeaderName());

        if (schoolIdHeader == null || schoolIdHeader.isBlank()) {
            return tenantProperties.getDefaultTenant();
        }

        try {
            UUID schoolId = UUID.fromString(schoolIdHeader.trim());
            String schemaName = tenantCache.getSchemaForSchool(schoolId);

            if (schemaName != null) {
                return schemaName;
            } else {
                log.warn("No schema found for school ID: {}", schoolId);
                throw new IllegalArgumentException("Schema not found for schoolId: " + schoolId);
            }
        } catch (IllegalArgumentException e) {
            log.warn("Invalid school ID format: {}", schoolIdHeader);
            return tenantProperties.getDefaultTenant();
        }
    }
}