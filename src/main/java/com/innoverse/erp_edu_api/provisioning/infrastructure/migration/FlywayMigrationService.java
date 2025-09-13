package com.innoverse.erp_edu_api.provisioning.infrastructure.migration;

import com.innoverse.erp_edu_api.provisioning.infrastructure.config.TenantFlywayProperties;
import com.innoverse.erp_edu_api.provisioning.infrastructure.datasource.TenantAwareDataSource;
import com.innoverse.erp_edu_api.common.domain.AcademicLevel;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.exception.FlywayValidateException;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class FlywayMigrationService implements MigrationService {
    private final DataSource underlyingDataSource;
    private final TenantFlywayProperties props;

    public FlywayMigrationService(DataSource dataSource, TenantFlywayProperties props) {
        this.underlyingDataSource = dataSource instanceof TenantAwareDataSource ?
                ((TenantAwareDataSource) dataSource).getTargetDataSource() : dataSource;
        this.props = props;
    }

    @Override
    public void migrate(String tenantId, AcademicLevel level) {
        String[] locations = getMigrationLocations(level);
        log.info("Migration locations: " + Arrays.toString(locations));
        migrate(tenantId, locations);
    }

    @Override
    public void migrate(String schemaName, String[] paths) {
        Flyway flyway = createFlywayConfig(schemaName, paths);

        try {
            flyway.migrate();
            log.info("🚀 Migration successful for tenant '{}'", schemaName);
        } catch (FlywayValidateException e) {
            log.warn("⚠️ Validation failed for tenant '{}', attempting repair...", schemaName);
            flyway.repair();
            flyway.migrate();
            log.info("🚧 Migration successful after repair for tenant '{}'", schemaName);
        }
    }

    @Override
    public boolean isSchemaUpToDate(String schemaName, AcademicLevel level) {
        String[] locations = getMigrationLocations(level);
        return isSchemaUpToDate(schemaName, locations);
    }

    @Override
    public boolean isSchemaUpToDate(String schemaName, String[] locations) {
        try {
            Flyway flyway = createFlywayConfig(schemaName, locations);
            flyway.validate();
            return true;
        } catch (FlywayValidateException e) {
            return false;
        }
    }

    private Flyway createFlywayConfig(String schemaName, String[] locations) {
        return Flyway.configure()
                .table("flyway_tenant_history")
                .dataSource(underlyingDataSource)
                .schemas(schemaName) // Set the specific schema for migration
                .locations(locations)
                .baselineOnMigrate(true)
                .cleanDisabled(true)
                .validateOnMigrate(true)
                .outOfOrder(true)
                .load();
    }

    private String[] getMigrationLocations(AcademicLevel level) {
        List<String> locations = new ArrayList<>(props.getCommon());
        switch (level) {
            case PRIMARY -> locations.addAll(props.getPrimary());
            case SECONDARY -> locations.addAll(props.getSecondary());
            default -> locations.toArray(new String[0]);
        }
        return locations.toArray(String[]::new);
    }
}