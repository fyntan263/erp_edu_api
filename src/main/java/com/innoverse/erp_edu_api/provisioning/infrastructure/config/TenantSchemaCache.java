package com.innoverse.erp_edu_api.provisioning.infrastructure.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.innoverse.erp_edu_api.provisioning.domain.DbProvision;
import com.innoverse.erp_edu_api.provisioning.ProvisioningTrackingService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class TenantSchemaCache {
    private final Cache<UUID, String> schoolToSchemaCache;
    private final ProvisioningTrackingService trackingService;

    public TenantSchemaCache(ProvisioningTrackingService trackingService) {
        this.trackingService = trackingService;
        this.schoolToSchemaCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(1000)
                .build();
    }

    @PostConstruct
    public void preloadCache() {
        // Preload cache with existing assignments
        trackingService.getAllProvisions().stream()
                .filter(DbProvision::isAssigned)
                .forEach(provision -> {
                    schoolToSchemaCache.put(provision.getAssignedSchoolId(), provision.getDbSchemaName());
                });
    }

    public String getSchemaForSchool(UUID schoolId) {
        return schoolToSchemaCache.get(schoolId, key -> {
            Optional<DbProvision> provision = trackingService.getBySchoolId(key)
                    .stream()
                    .findFirst();
            return provision.map(DbProvision::getDbSchemaName).orElse(null);
        });
    }

    public void updateCache(UUID schoolId, String schemaName) {
        if (schemaName == null) {
            schoolToSchemaCache.invalidate(schoolId);
        } else {
            schoolToSchemaCache.put(schoolId, schemaName);
        }
    }
}