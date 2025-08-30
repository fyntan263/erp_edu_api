package com.innoverse.erp_edu_api.provisioning.api.web;

import com.innoverse.erp_edu_api.provisioning.infrastructure.config.TenantProperties;
import com.innoverse.erp_edu_api.provisioning.infrastructure.config.TenantSchemaCache;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class TenantHeaderResolver implements TenantResolver {
    private final TenantProperties tenantProperties;
    private final TenantSchemaCache tenantSchemaCache;

    public TenantHeaderResolver(TenantProperties tenantProperties,
                                TenantSchemaCache tenantSchemaCache) {
        this.tenantProperties = tenantProperties;
        this.tenantSchemaCache = tenantSchemaCache;
    }

    @Override
    public String resolve(HttpServletRequest req) {
        String schoolIdHeader = req.getHeader(tenantProperties.getHeaderName());

        if (schoolIdHeader == null || schoolIdHeader.isBlank()) {
            return tenantProperties.getDefaultTenant();
        }

        try {
            UUID schoolId = UUID.fromString(schoolIdHeader.trim());
            String schemaName = tenantSchemaCache.getSchemaForSchool(schoolId);

            if (schemaName != null) {
                return schemaName;
            } else {
                log.warn("No schema found for school ID: {}", schoolId);
                throw new IllegalArgumentException("Schema not found for schoolId: " + schoolId);

//                return tenantProperties.getDefaultTenant();
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }
}