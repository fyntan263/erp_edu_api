package com.innoverse.erp_edu_api.provisioning.infrastructure.config;

import com.innoverse.erp_edu_api.provisioning.services.DistributedTenantCache;
import com.innoverse.erp_edu_api.provisioning.infrastructure.datasource.TenantAwareDataSource;
import com.innoverse.erp_edu_api.provisioning.web.resolvers.TenantFilter;
import com.innoverse.erp_edu_api.provisioning.web.resolvers.TenantHeaderResolver;
import com.innoverse.erp_edu_api.provisioning.web.resolvers.TenantResolver;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class TenantConfig {

    @Bean
    public DataSource dataSource(DataSourceProperties properties, TenantProperties tenantProperties) {
        HikariDataSource targetDataSource = properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();

        targetDataSource.setPoolName("TenantAwarePool");
        targetDataSource.setMaximumPoolSize(50);
        targetDataSource.setMinimumIdle(10);
        targetDataSource.setConnectionTimeout(30000);
        targetDataSource.setIdleTimeout(600000);
        targetDataSource.setMaxLifetime(1800000);
        targetDataSource.setLeakDetectionThreshold(2000);

        return new TenantAwareDataSource(targetDataSource, tenantProperties);
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public TenantFilter tenantFilter(TenantResolver tenantResolver) {
        return new TenantFilter(tenantResolver);
    }

    @Bean
    public TenantResolver tenantResolver(TenantProperties tenantProperties, DistributedTenantCache cache) {
        return new TenantHeaderResolver(tenantProperties, cache);
    }
}