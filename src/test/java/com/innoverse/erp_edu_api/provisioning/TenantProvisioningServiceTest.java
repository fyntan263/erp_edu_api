package com.innoverse.erp_edu_api.provisioning;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.innoverse.erp_edu_api.provisioning.domain.AcademicLevel;
import com.innoverse.erp_edu_api.provisioning.infrastructure.datasource.TenantAwareDataSource;
import com.innoverse.erp_edu_api.provisioning.infrastructure.migration.FlywayMigrationService;
import com.innoverse.erp_edu_api.provisioning.services.TenantProvisioningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.SQLException;

@ExtendWith(MockitoExtension.class)
class TenantProvisioningServiceTest {

    @Mock
    private TenantAwareDataSource tenantAwareDataSource;

    @Mock
    private DataSource dataSource;

    @Mock
    private FlywayMigrationService migrationService;

    @InjectMocks
    private TenantProvisioningService provisioningService;

    @BeforeEach
    void setUp() {
        // Removed unnecessary stubbing that was causing the test failure
    }

    @Test
    void testProvisionTenantSuccess() throws SQLException {
        String tenantId = "test-tenant";

        when(tenantAwareDataSource.schemaExists(tenantId)).thenReturn(false);

        provisioningService.provisionTenant(tenantId, AcademicLevel.PRIMARY);

        verify(tenantAwareDataSource).createSchema(tenantId);
        verify(migrationService).migrate(tenantId, AcademicLevel.PRIMARY);
    }

    @Test
    void testProvisionTenantAlreadyExists() throws SQLException {
        String tenantId = "existing-tenant";

        when(tenantAwareDataSource.schemaExists(tenantId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            provisioningService.provisionTenant(tenantId, AcademicLevel.PRIMARY);
        });

        verify(tenantAwareDataSource, never()).createSchema(any());
        verify(migrationService, never()).migrate(anyString(), (AcademicLevel) any());
    }

    @Test
    void testTenantExists() throws SQLException {
        String tenantId = "test-tenant";

        when(tenantAwareDataSource.schemaExists(tenantId)).thenReturn(true);

        assertTrue(provisioningService.tenantExists(tenantId));
    }

    @Test
    void testTenantNotExists() throws SQLException {
        String tenantId = "non-existent-tenant";

        when(tenantAwareDataSource.schemaExists(tenantId)).thenReturn(false);

        assertFalse(provisioningService.tenantExists(tenantId));
    }

    @Test
    void testIsTenantUpToDate() {
        String tenantId = "test-tenant";

        when(migrationService.isSchemaUpToDate(tenantId, AcademicLevel.PRIMARY)).thenReturn(true);

        assertTrue(provisioningService.isTenantUpToDate(tenantId, AcademicLevel.PRIMARY));
    }
}