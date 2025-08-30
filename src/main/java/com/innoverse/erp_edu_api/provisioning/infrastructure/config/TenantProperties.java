package com.innoverse.erp_edu_api.provisioning.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("multitenancy")
public class TenantProperties {
    private String adminRole;
    private String headerName = "X-Tenant-ID";
    private String defaultTenant = "public";
    private boolean autoCreateSchema = true;
    private boolean createTenantRoles = true;
}
