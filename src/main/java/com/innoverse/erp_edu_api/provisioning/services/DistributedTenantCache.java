package com.innoverse.erp_edu_api.provisioning.services;

import com.innoverse.erp_edu_api.provisioning.SchoolAccessCheckEvent;
import com.innoverse.erp_edu_api.provisioning.domain.DbProvision;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedTenantCache {
    private final RedisTemplate<String, String> redisTemplate;
    private final ProvisioningTrackingService trackingService;
    private final ApplicationEventPublisher eventPublisher;

    private static final String SCHEMA_CACHE_PREFIX = "tenant:schema:";
    private static final String ACCESS_CACHE_PREFIX = "tenant:access:";
    private static final long ACCESS_CACHE_TTL = 1 ; // day minutes
    private static final long SCHEMA_CACHE_TTL = 356; // 1 year

    public String getSchemaForSchool(UUID schoolId) {
        if (!hasAccess(schoolId)) {
            throw new RuntimeException("School not accessible");
        }
        String cacheKey = SCHEMA_CACHE_PREFIX + schoolId;
        String schemaName = redisTemplate.opsForValue().get(cacheKey);

        if (schemaName == null) {
            schemaName = loadSchemaFromDatabase(schoolId);
            if (schemaName != null) {
                redisTemplate.opsForValue().set(cacheKey, schemaName, SCHEMA_CACHE_TTL, TimeUnit.DAYS);
            }
        }
        return schemaName;
    }

    public boolean hasAccess(UUID schoolId) {
        String cacheKey = ACCESS_CACHE_PREFIX + schoolId;
        String hasAccessStr = redisTemplate.opsForValue().get(cacheKey);

        if (hasAccessStr == null) {
            boolean hasAccess = checkSchoolAccess(schoolId);
            redisTemplate.opsForValue().set(cacheKey, String.valueOf(hasAccess), ACCESS_CACHE_TTL, TimeUnit.DAYS);
            return hasAccess;
        }

        return Boolean.parseBoolean(hasAccessStr);
    }

    private String loadSchemaFromDatabase(UUID schoolId) {
        return trackingService.getBySchoolId(schoolId)
                .stream()
                .findFirst()
                .map(DbProvision::getDbSchemaName)
                .orElse(null);
    }

    public void updateSchemaCache(UUID schoolId, String schemaName) {
        String cacheKey = SCHEMA_CACHE_PREFIX + schoolId;
        if (schemaName == null) {
            redisTemplate.delete(cacheKey);
        } else {
            redisTemplate.opsForValue().set(cacheKey, schemaName, SCHEMA_CACHE_TTL, TimeUnit.SECONDS);
        }
    }

    public void updateAccessCache(UUID schoolId, boolean hasAccess) {
        String cacheKey = ACCESS_CACHE_PREFIX + schoolId;
        redisTemplate.opsForValue().set(cacheKey, String.valueOf(hasAccess), ACCESS_CACHE_TTL, TimeUnit.SECONDS);
    }

    public void invalidateCaches(UUID schoolId) {
        redisTemplate.delete(ACCESS_CACHE_PREFIX + schoolId);
        redisTemplate.delete(SCHEMA_CACHE_PREFIX + schoolId);
    }

    public boolean checkSchoolAccess(UUID schoolId) {
        SchoolAccessCheckEvent event = new SchoolAccessCheckEvent(schoolId);
        eventPublisher.publishEvent(event);
        try {
            return event.getAccessFuture().get(5, TimeUnit.SECONDS); // 5-second timeout
        } catch (Exception e) {
            log.error("Timeout or error waiting for school access check response for school: {}", schoolId, e);
            return false; // Default to false on error
        }
    }
}