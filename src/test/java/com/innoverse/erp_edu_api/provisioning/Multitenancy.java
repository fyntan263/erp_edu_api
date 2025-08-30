package com.innoverse.erp_edu_api.provisioning;

import com.innoverse.erp_edu_api.provisioning.api.web.TenantContext;
import com.innoverse.erp_edu_api.provisioning.infrastructure.config.TenantProperties;
import com.innoverse.erp_edu_api.provisioning.infrastructure.datasource.TenantAwareDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MultiTenancyIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private DataSource dataSource;
    private TenantAwareDataSource tenantAwareDataSource;

    @BeforeAll
    void setup() {
        // Start the container if not already started
        postgres.start();

        // Create HikariCP data source pointing to test container (consistent with your config)
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl(postgres.getJdbcUrl());
        hikariDataSource.setUsername(postgres.getUsername());
        hikariDataSource.setPassword(postgres.getPassword());
        hikariDataSource.setDriverClassName(postgres.getDriverClassName());
        hikariDataSource.setMaximumPoolSize(5);
        hikariDataSource.setMinimumIdle(1);
        hikariDataSource.setPoolName("TestContainerPool");

        this.dataSource = hikariDataSource;

        TenantProperties tenantProperties = new TenantProperties();
        tenantProperties.setDefaultTenant("public");
        tenantProperties.setCreateTenantRoles(true);

        this.tenantAwareDataSource = new TenantAwareDataSource(dataSource, tenantProperties);
    }

    @Test
    void testTenantIsolation() throws Exception {
        // Create two tenant schemas
        String tenant1 = "tenant1";
        String tenant2 = "tenant2";

        // Create schemas
        tenantAwareDataSource.createSchema(tenant1);
        tenantAwareDataSource.createSchema(tenant2);

        // Create tables in each schema
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // Create table in tenant1
        TenantContext.set(tenant1);
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS test_data (id SERIAL PRIMARY KEY, value VARCHAR(100))");
        jdbcTemplate.execute("INSERT INTO test_data (value) VALUES ('tenant1-data')");

        // Create table in tenant2
        TenantContext.set(tenant2);
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS test_data (id SERIAL PRIMARY KEY, value VARCHAR(100))");
        jdbcTemplate.execute("INSERT INTO test_data (value) VALUES ('tenant2-data')");

        // Verify tenant1 can only see its own data
        TenantContext.set(tenant1);
        String tenant1Data = jdbcTemplate.queryForObject(
                "SELECT value FROM test_data", String.class);
        assertEquals("tenant1-data", tenant1Data);

        // Verify tenant2 can only see its own data
        TenantContext.set(tenant2);
        String tenant2Data = jdbcTemplate.queryForObject(
                "SELECT value FROM test_data", String.class);
        assertEquals("tenant2-data", tenant2Data);

        // Verify cross-tenant access is prevented
        try {
            TenantContext.set(tenant1);
            jdbcTemplate.queryForObject(
                    "SELECT value FROM " + tenant2 + ".test_data", String.class);
            fail("Should not be able to access other tenant's schema");
        } catch (Exception e) {
            // Expected - should not be able to access other tenant's data
            assertTrue(e.getMessage().contains("does not exist") ||
                    e.getMessage().contains("permission denied") ||
                    e.getMessage().contains("schema") || e.getMessage().contains("not found"));
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    void testPublicSchemaAccess() throws Exception {
        String tenant = "test-tenant";
        tenantAwareDataSource.createSchema(tenant);

        // Create table in public schema
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS public.shared_data (id SERIAL PRIMARY KEY, value VARCHAR(100))");
        jdbcTemplate.execute("INSERT INTO public.shared_data (value) VALUES ('public-data')");

        // Verify tenant can access public schema
        TenantContext.set(tenant);
        String publicData = jdbcTemplate.queryForObject(
                "SELECT value FROM public.shared_data", String.class);
        assertEquals("public-data", publicData);

        TenantContext.clear();
    }

    @Test
    void testSchemaCreationAndDeletion() throws Exception {
        String tenant = "temp-tenant";

        // Test schema creation
        assertDoesNotThrow(() -> tenantAwareDataSource.createSchema(tenant));

        // Verify schema exists
        TenantContext.set(tenant);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS temp_table (id SERIAL PRIMARY KEY)");

        TenantContext.clear();
    }

    @Test
    void testConnectionPoolIntegration() throws Exception {
        // Test that the HikariCP connection pool works correctly with tenant awareness
        String tenant = "pool-test-tenant";
        tenantAwareDataSource.createSchema(tenant);

        TenantContext.set(tenant);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // Execute multiple queries to test connection pooling
        for (int i = 0; i < 5; i++) {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS pool_test_" + i + " (id SERIAL PRIMARY KEY)");
        }

        TenantContext.clear();
    }
}