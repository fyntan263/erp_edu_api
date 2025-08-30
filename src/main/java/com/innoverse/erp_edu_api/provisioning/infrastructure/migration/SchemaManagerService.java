package com.innoverse.erp_edu_api.provisioning.infrastructure.migration;

import com.innoverse.erp_edu_api.provisioning.infrastructure.datasource.TenantAwareDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SchemaManagerService {
    private final DataSource dataSource;

    private TenantAwareDataSource getTenantAwareDataSource() {
        return (TenantAwareDataSource) dataSource;
    }

    public boolean schemaExists(String schema) throws SQLException {
        return getTenantAwareDataSource().schemaExists(schema);
    }

    public void createSchema(String schema) throws SQLException {
        getTenantAwareDataSource().createSchema(schema);
    }

    public void createSchema(String schema, String password) throws SQLException {
        getTenantAwareDataSource().createSchema(schema, password);
    }

    public void dropSchema(String schema) throws SQLException {
        getTenantAwareDataSource().dropSchema(schema);
    }

    public void dropSchemaIfExists(String schema) throws SQLException {
        getTenantAwareDataSource().dropSchemaIfExists(schema);
    }

    public void assignToSchool(String schema, UUID schoolId) throws SQLException {
        getTenantAwareDataSource().assignToSchool(schema, schoolId);
    }

    public void unassignFromSchool(String schema) throws SQLException {
        getTenantAwareDataSource().unassignFromSchool(schema);
    }

    public String getTenantRolePassword(String schema) throws SQLException {
        return getTenantAwareDataSource().getTenantRolePassword(schema);
    }

    public boolean tenantRoleExists(String schema) throws SQLException {
        return getTenantAwareDataSource().tenantRoleExists(schema);
    }

    public boolean validateTenantCredentials(String schema, String password) throws SQLException {
        return getTenantAwareDataSource().validateTenantCredentials(schema, password);
    }
    public List<String> getAllTenantSchemas() throws SQLException {
        try (var conn = dataSource.getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery(
                     "SELECT schema_name FROM information_schema.schemata " +
                             "WHERE schema_name NOT IN ('information_schema', 'pg_catalog', 'pg_toast', 'public') " +
                             "ORDER BY schema_name")) {

            List<String> schemas = new ArrayList<>();
            while (rs.next()) {
                schemas.add(rs.getString("schema_name"));
            }
            return schemas;
        }
    }

    public boolean tableExists(String schema, String tableName) throws SQLException {
        try (var conn = dataSource.getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery(
                     "SELECT COUNT(*) FROM information_schema.tables " +
                             "WHERE table_schema = '" + schema + "' AND table_name = '" + tableName + "'")) {

            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public int getTableCount(String schema) throws SQLException {
        try (var conn = dataSource.getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery(
                     "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '" + schema + "'")) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}