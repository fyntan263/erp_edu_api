package com.innoverse.erp_edu_api.provisioning.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "tenant.migration.locations")
@Getter
@Setter
public class TenantFlywayProperties {
    private List<String> common;
    private List<String> primary;
    private List<String> secondary;
}
