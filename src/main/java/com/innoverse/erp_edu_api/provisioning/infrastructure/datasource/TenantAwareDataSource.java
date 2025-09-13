package com.innoverse.erp_edu_api.provisioning.infrastructure.datasource;

import com.innoverse.erp_edu_api.provisioning.infrastructure.config.TenantProperties;
import com.innoverse.erp_edu_api.provisioning.web.resolvers.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

@Slf4j
public class TenantAwareDataSource extends DelegatingDataSource {
    private final TenantProperties tenantProperties;

    public TenantAwareDataSource(DataSource targetDataSource, TenantProperties tenantProperties) {
        super(targetDataSource);
        this.tenantProperties = tenantProperties;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = super.getConnection();
        setSchemaForConnection(connection);
        return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = super.getConnection(username, password);
        setSchemaForConnection(connection);
        return connection;
    }

    private void setSchemaForConnection(Connection connection) throws SQLException {
        String tenant = TenantContext.get();
        String schema = (tenant == null || tenant.isBlank()) ?
                tenantProperties.getDefaultTenant() : tenant;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("SET search_path TO \"" + schema + "\", public");
        }
    }

//    // SECURITY ENHANCEMENT: Secure public schema on initialization
//    public void securePublicSchema() throws SQLException {
//        try (Connection conn = super.getConnection();
//             Statement stmt = conn.createStatement()) {
//
//            // Revoke all privileges from public role on public schema
//            stmt.execute("REVOKE ALL ON SCHEMA public FROM PUBLIC");
//            stmt.execute("REVOKE ALL ON ALL TABLES IN SCHEMA public FROM PUBLIC");
//            stmt.execute("REVOKE ALL ON ALL SEQUENCES IN SCHEMA public FROM PUBLIC");
//            stmt.execute("REVOKE ALL ON ALL FUNCTIONS IN SCHEMA public FROM PUBLIC");
//            stmt.execute("REVOKE ALL ON ALL ROUTINES IN SCHEMA public FROM PUBLIC");
//
//            // Grant minimal privileges to public role
//            stmt.execute("GRANT USAGE ON SCHEMA public TO PUBLIC");
//
//            log.info("Public schema secured with minimal privileges");
//        }
//    }

    // Schema management methods
    public boolean schemaExists(String schema) throws SQLException {
        if (schema.equals(tenantProperties.getDefaultTenant())) return true;

        try (Connection conn = super.getConnection();
             Statement stmt = conn.createStatement();
             var rs = stmt.executeQuery(
                     "SELECT schema_name FROM information_schema.schemata WHERE schema_name = '" + schema + "'")) {
            return rs.next();
        }
    }

    public void createSchema(String schema) throws SQLException {
        createSchema(schema, "password");
    }

    public void createSchema(String schema, String password) throws SQLException {
        if (schema.equals(tenantProperties.getDefaultTenant()) || schemaExists(schema)) return;

        try (Connection conn = super.getConnection();
             Statement stmt = conn.createStatement()) {

            // Create schema with secure ownership
            stmt.execute("CREATE SCHEMA \"" + schema + "\"");

            // Set schema owner to admin role (not the tenant role)
            stmt.execute("ALTER SCHEMA \"" + schema + "\" OWNER TO " + getAdminRole());

            if (tenantProperties.isCreateTenantRoles()) {
                createTenantRole(stmt, schema, password);
            }

            log.info("Created secure schema: {}", schema);
        }
    }

    public void dropSchema(String schema) throws SQLException {
        if (schema.equals(tenantProperties.getDefaultTenant())) {
            throw new SQLException("Cannot drop default tenant schema");
        }

        try (Connection conn = super.getConnection();
             Statement stmt = conn.createStatement()) {

            // Drop schema first (cascade will handle dependencies)
            if (schemaExists(schema)) {
                stmt.execute("DROP SCHEMA \"" + schema + "\" CASCADE");
                log.info("Dropped schema: {}", schema);
            }

            // Then drop the tenant role
            if (tenantProperties.isCreateTenantRoles()) {
                dropTenantRole(schema);
            }
        }
    }

    public void dropSchemaIfExists(String schema) throws SQLException {
        if (schema.equals(tenantProperties.getDefaultTenant())) return;

        try (Connection conn = super.getConnection();
             Statement stmt = conn.createStatement()) {

            if (schemaExists(schema)) {
                stmt.execute("DROP SCHEMA \"" + schema + "\" CASCADE");
                log.info("Dropped schema if exists: {}", schema);
            }

            if (tenantProperties.isCreateTenantRoles()) {
                dropTenantRoleIfExists(schema);
            }
        }
    }

    /**
     * Update tenant role password when assigned to a school
     */
    public void assignToSchool(String schema, UUID schoolId) throws SQLException {
        if (!schemaExists(schema)) {
            throw new SQLException("Schema does not exist: " + schema);
        }

        if (tenantProperties.isCreateTenantRoles()) {
            try (Connection conn = super.getConnection();
                 Statement stmt = conn.createStatement()) {

                String tenantRole = schema + "_user";
                String password = schoolId.toString();

                if (tenantRoleExists(schema)) {
                    stmt.execute("ALTER ROLE \"" + tenantRole + "\" WITH PASSWORD '" + password + "'");
                    log.info("Updated password for role {} to school ID: {}", tenantRole, schoolId);
                } else {
                    createTenantRole(stmt, schema, password);
                }
            }
        }
    }

    /**
     * Reset tenant role password to default when unassigned from school
     */
    public void unassignFromSchool(String schema) throws SQLException {
        if (!schemaExists(schema)) {
            throw new SQLException("Schema does not exist: " + schema);
        }

        if (tenantProperties.isCreateTenantRoles()) {
            try (Connection conn = super.getConnection();
                 Statement stmt = conn.createStatement()) {

                String tenantRole = schema + "_user";
                String defaultPassword = "password";

                if (tenantRoleExists(schema)) {
                    stmt.execute("ALTER ROLE \"" + tenantRole + "\" WITH PASSWORD '" + defaultPassword + "'");
                    log.info("Reset password for role {} to default", tenantRole);
                }
            }
        }
    }

    /**
     * Get the current password for a tenant role
     */
    public String getTenantRolePassword(String schema) throws SQLException {
        if (!tenantProperties.isCreateTenantRoles()) {
            return "tenant_roles_disabled";
        }

        try (Connection conn = super.getConnection();
             Statement stmt = conn.createStatement();
             var rs = stmt.executeQuery(
                     "SELECT rolpassword FROM pg_authid WHERE rolname = '" + schema + "_user'")) {

            if (rs.next()) {
                return rs.getString("rolpassword");
            }
            return "role_not_found";
        }
    }

    private void createTenantRole(Statement stmt, String schema, String password) throws SQLException {
        String tenantRole = schema + "_user";

        try {
            if (tenantRoleExistsInternal(stmt, tenantRole)) {
                stmt.execute("ALTER ROLE \"" + tenantRole + "\" WITH PASSWORD '" + password + "'");
                log.info("Updated password for existing role: {}", tenantRole);
            } else {
                stmt.execute("CREATE ROLE \"" + tenantRole + "\" LOGIN PASSWORD '" + password + "' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT NOREPLICATION");
                log.info("Created secure tenant role: {} with password: {}", tenantRole, password);
            }

            grantTenantRolePrivileges(stmt, schema, tenantRole);

        } catch (SQLException e) {
            throw new SQLException("Failed to create/update tenant role '" + tenantRole + "': " + e.getMessage(), e);
        }
    }

    // SECURITY ENHANCEMENT: Secure privilege granting
    private void grantTenantRolePrivileges(Statement stmt, String schema, String tenantRole) throws SQLException {
        // Grant full access to their own schema
        stmt.execute("GRANT USAGE ON SCHEMA \"" + schema + "\" TO \"" + tenantRole + "\"");
        stmt.execute("GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA \"" + schema + "\" TO \"" + tenantRole + "\"");
        stmt.execute("GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA \"" + schema + "\" TO \"" + tenantRole + "\"");
        stmt.execute("GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA \"" + schema + "\" TO \"" + tenantRole + "\"");

        // Set default privileges for future objects in their schema
        stmt.execute("ALTER DEFAULT PRIVILEGES IN SCHEMA \"" + schema + "\" GRANT ALL ON TABLES TO \"" + tenantRole + "\"");
        stmt.execute("ALTER DEFAULT PRIVILEGES IN SCHEMA \"" + schema + "\" GRANT ALL ON SEQUENCES TO \"" + tenantRole + "\"");
        stmt.execute("ALTER DEFAULT PRIVILEGES IN SCHEMA \"" + schema + "\" GRANT ALL ON FUNCTIONS TO \"" + tenantRole + "\"");

        // Grant READ-ONLY access to public schema
        stmt.execute("GRANT USAGE ON SCHEMA public TO \"" + tenantRole + "\"");
        stmt.execute("GRANT SELECT ON ALL TABLES IN SCHEMA public TO \"" + tenantRole + "\"");

        // Set default privileges for future objects in public schema (READ-ONLY)
        stmt.execute("ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO \"" + tenantRole + "\"");

        // Explicitly REVOKE write privileges from public schema
        stmt.execute("REVOKE INSERT, UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER ON ALL TABLES IN SCHEMA public FROM \"" + tenantRole + "\"");
        stmt.execute("ALTER DEFAULT PRIVILEGES IN SCHEMA public REVOKE INSERT, UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER ON TABLES FROM \"" + tenantRole + "\"");

        // Revoke all privileges on sequences and functions in public schema
        stmt.execute("REVOKE ALL ON ALL SEQUENCES IN SCHEMA public FROM \"" + tenantRole + "\"");
        stmt.execute("REVOKE ALL ON ALL FUNCTIONS IN SCHEMA public FROM \"" + tenantRole + "\"");
        stmt.execute("ALTER DEFAULT PRIVILEGES IN SCHEMA public REVOKE ALL ON SEQUENCES FROM \"" + tenantRole + "\"");
        stmt.execute("ALTER DEFAULT PRIVILEGES IN SCHEMA public REVOKE ALL ON FUNCTIONS FROM \"" + tenantRole + "\"");
    }

    /**
     * Drop tenant role for a schema
     */
    public void dropTenantRole(String schema) throws SQLException {
        if (!tenantProperties.isCreateTenantRoles()) return;

        String tenantRole = schema + "_user";

        try (Connection conn = super.getConnection();
             Statement stmt = conn.createStatement()) {

            if (tenantRoleExistsInternal(stmt, tenantRole)) {
                revokeAllRolePrivileges(stmt, schema, tenantRole);
                stmt.execute("DROP ROLE \"" + tenantRole + "\"");
                log.info("Dropped tenant role: {}", tenantRole);
            }
        } catch (SQLException e) {
            log.warn("Could not drop role {}: {}", tenantRole, e.getMessage());
        }
    }

    public void dropTenantRoleIfExists(String schema) throws SQLException {
        if (!tenantProperties.isCreateTenantRoles()) return;

        String tenantRole = schema + "_user";

        try (Connection conn = super.getConnection();
             Statement stmt = conn.createStatement()) {

            if (tenantRoleExistsInternal(stmt, tenantRole)) {
                try {
                    revokeAllRolePrivileges(stmt, schema, tenantRole);
                    stmt.execute("DROP ROLE IF EXISTS \"" + tenantRole + "\"");
                    log.info("Dropped tenant role if exists: {}", tenantRole);
                } catch (SQLException e) {
                    log.warn("Could not drop role {}: {}", tenantRole, e.getMessage());
                }
            }
        }
    }

    // SECURITY ENHANCEMENT: Comprehensive privilege revocation
    private void revokeAllRolePrivileges(Statement stmt, String schema, String tenantRole) {
        try {
            // Revoke privileges from the specific schema
            if (schemaExists(schema)) {
                stmt.execute("REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA \"" + schema + "\" FROM \"" + tenantRole + "\"");
                stmt.execute("REVOKE ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA \"" + schema + "\" FROM \"" + tenantRole + "\"");
                stmt.execute("REVOKE ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA \"" + schema + "\" FROM \"" + tenantRole + "\"");
                stmt.execute("REVOKE USAGE ON SCHEMA \"" + schema + "\" FROM \"" + tenantRole + "\"");

                // Revoke default privileges
                stmt.execute("ALTER DEFAULT PRIVILEGES IN SCHEMA \"" + schema + "\" REVOKE ALL ON TABLES FROM \"" + tenantRole + "\"");
                stmt.execute("ALTER DEFAULT PRIVILEGES IN SCHEMA \"" + schema + "\" REVOKE ALL ON SEQUENCES FROM \"" + tenantRole + "\"");
                stmt.execute("ALTER DEFAULT PRIVILEGES IN SCHEMA \"" + schema + "\" REVOKE ALL ON FUNCTIONS FROM \"" + tenantRole + "\"");
            }

            // Revoke privileges from public schema
            stmt.execute("REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA public FROM \"" + tenantRole + "\"");
            stmt.execute("REVOKE ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public FROM \"" + tenantRole + "\"");
            stmt.execute("REVOKE ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public FROM \"" + tenantRole + "\"");
            stmt.execute("REVOKE USAGE ON SCHEMA public FROM \"" + tenantRole + "\"");

        } catch (SQLException e) {
            log.warn("Could not revoke privileges from role {}: {}", tenantRole, e.getMessage());
        }
    }

    private boolean tenantRoleExistsInternal(Statement stmt, String tenantRole) throws SQLException {
        try (var rs = stmt.executeQuery("SELECT 1 FROM pg_roles WHERE rolname = '" + tenantRole + "'")) {
            return rs.next();
        }
    }

    /**
     * Verify if a tenant role exists
     */
    public boolean tenantRoleExists(String schema) throws SQLException {
        if (!tenantProperties.isCreateTenantRoles()) return false;

        try (Connection conn = super.getConnection();
             Statement stmt = conn.createStatement()) {
            return tenantRoleExistsInternal(stmt, schema + "_user");
        }
    }

    /**
     * Validate tenant credentials
     */
    public boolean validateTenantCredentials(String schema, String password) throws SQLException {
        if (!tenantProperties.isCreateTenantRoles() || !tenantRoleExists(schema)) {
            return false;
        }

        try (Connection conn = super.getConnection();
             Statement stmt = conn.createStatement();
             var rs = stmt.executeQuery(
                     "SELECT rolpassword FROM pg_authid WHERE rolname = '" + schema + "_user'")) {

            if (rs.next()) {
                String storedPassword = rs.getString("rolpassword");
                return storedPassword != null && storedPassword.contains(password);
            }
            return false;
        }
    }

    /**
     * Cleanup orphaned roles (roles without corresponding schemas)
//     */
//    public void cleanupOrphanedRoles() throws SQLException {
//        if (!tenantProperties.isCreateTenantRoles()) return;
//
//        try (Connection conn = super.getConnection();
//             Statement stmt = conn.createStatement();
//             var rs = stmt.executeQuery(
//                     "SELECT rolname FROM pg_roles WHERE rolname LIKE '%_user' AND rolname != 'postgres_user'")) {
//
//            while (rs.next()) {
//                String roleName = rs.getString("rolname");
//                String schemaName = roleName.replace("_user", "");
//
//                if (!schemaExists(schemaName)) {
//                    try {
//                        dropTenantRoleIfExists(schemaName);
//                    } catch (SQLException e) {
//                        log.warn("Could not drop orphaned role {}: {}", roleName, e.getMessage());
//                    }
//                }
//            }
//        }
//    }
//
//    public void forceDropSchema(String schema) throws SQLException {
//        if (schema.equals(tenantProperties.getDefaultTenant())) {
//            throw new SQLException("Cannot drop default tenant schema");
//        }
//
//        try (Connection conn = super.getConnection();
//             Statement stmt = conn.createStatement()) {
//
//            // Drop schema with cascade first
//            if (schemaExists(schema)) {
//                stmt.execute("DROP SCHEMA \"" + schema + "\" CASCADE");
//                log.info("Force-dropped schema: {}", schema);
//            }
//
//            // Then drop the tenant role with force
//            if (tenantProperties.isCreateTenantRoles()) {
//                forceDropTenantRole(schema);
//            }
//        }
//    }

    private void forceDropTenantRole(String schema) throws SQLException {
        if (!tenantProperties.isCreateTenantRoles()) return;

        String tenantRole = schema + "_user";

        try (Connection conn = super.getConnection();
             Statement stmt = conn.createStatement()) {

            if (tenantRoleExistsInternal(stmt, tenantRole)) {
                try {
                    // Forcefully revoke all privileges
                    revokeAllRolePrivilegesForcefully(stmt, schema, tenantRole);

                    // Drop the role with force
                    stmt.execute("DROP ROLE IF EXISTS \"" + tenantRole + "\"");
                    log.info("Force-dropped tenant role: {}", tenantRole);
                } catch (SQLException e) {
                    log.warn("Could not force-drop role {}: {}", tenantRole, e.getMessage());
                }
            }
        }
    }

    private void revokeAllRolePrivilegesForcefully(Statement stmt, String schema, String tenantRole) {
        try {
            // Revoke all privileges from all schemas
            stmt.execute("REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA \"" + schema + "\" FROM \"" + tenantRole + "\"");
            stmt.execute("REVOKE ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA \"" + schema + "\" FROM \"" + tenantRole + "\"");
            stmt.execute("REVOKE ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA \"" + schema + "\" FROM \"" + tenantRole + "\"");
            stmt.execute("REVOKE USAGE ON SCHEMA \"" + schema + "\" FROM \"" + tenantRole + "\"");

            // Revoke from public schema
            stmt.execute("REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA public FROM \"" + tenantRole + "\"");
            stmt.execute("REVOKE ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public FROM \"" + tenantRole + "\"");
            stmt.execute("REVOKE ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public FROM \"" + tenantRole + "\"");
            stmt.execute("REVOKE USAGE ON SCHEMA public FROM \"" + tenantRole + "\"");

            // Revoke default privileges
            stmt.execute("ALTER DEFAULT PRIVILEGES FOR ROLE \"" + tenantRole + "\" IN SCHEMA \"" + schema + "\" REVOKE ALL ON TABLES FROM \"" + tenantRole + "\"");
            stmt.execute("ALTER DEFAULT PRIVILEGES FOR ROLE \"" + tenantRole + "\" IN SCHEMA \"" + schema + "\" REVOKE ALL ON SEQUENCES FROM \"" + tenantRole + "\"");
            stmt.execute("ALTER DEFAULT PRIVILEGES FOR ROLE \"" + tenantRole + "\" IN SCHEMA \"" + schema + "\" REVOKE ALL ON FUNCTIONS FROM \"" + tenantRole + "\"");

        } catch (SQLException e) {
            log.warn("Could not forcefully revoke privileges from role {}: {}", tenantRole, e.getMessage());
        }
    }

//    // SECURITY ENHANCEMENT: Validate tenant permissions
//    public boolean validateTenantPermissions(String schema) throws SQLException {
//        try (Connection conn = super.getConnection();
//             Statement stmt = conn.createStatement()) {
//
//            String tenantRole = schema + "_user";
//
//            // Check public schema permissions (should be read-only)
//            boolean publicWriteAllowed = checkPublicSchemaWritePermissions(stmt, tenantRole);
//
//            // Check own schema permissions (should be full access)
//            boolean ownSchemaFullAccess = checkOwnSchemaFullAccess(stmt, tenantRole, schema);
//
//            return !publicWriteAllowed && ownSchemaFullAccess;
//        }
//    }

//    private boolean checkPublicSchemaWritePermissions(Statement stmt, String tenantRole) throws SQLException {
//        try (var rs = stmt.executeQuery(
//                "SELECT EXISTS (" +
//                        "  SELECT 1 FROM information_schema.role_table_grants " +
//                        "  WHERE grantee = '" + tenantRole + "' " +
//                        "  AND table_schema = 'public' " +
//                        "  AND privilege_type IN ('INSERT', 'UPDATE', 'DELETE', 'TRUNCATE')" +
//                        ") as has_write_privileges")) {
//
//            return rs.next() && rs.getBoolean("has_write_privileges");
//        }
//    }
//
//    private boolean checkOwnSchemaFullAccess(Statement stmt, String tenantRole, String schema) throws SQLException {
//        try (var rs = stmt.executeQuery(
//                "SELECT COUNT(*) as table_count, " +
//                        "       SUM(CASE WHEN privilege_type = 'SELECT' THEN 1 ELSE 0 END) as select_count, " +
//                        "       SUM(CASE WHEN privilege_type = 'INSERT' THEN 1 ELSE 0 END) as insert_count, " +
//                        "       SUM(CASE WHEN privilege_type = 'UPDATE' THEN 1 ELSE 0 END) as update_count, " +
//                        "       SUM(CASE WHEN privilege_type = 'DELETE' THEN 1 ELSE 0 END) as delete_count " +
//                        "FROM information_schema.role_table_grants " +
//                        "WHERE grantee = '" + tenantRole + "' " +
//                        "AND table_schema = '" + schema + "'")) {
//
//            if (rs.next()) {
//                int tableCount = rs.getInt("table_count");
//                int selectCount = rs.getInt("select_count");
//                int insertCount = rs.getInt("insert_count");
//                int updateCount = rs.getInt("update_count");
//                int deleteCount = rs.getInt("delete_count");
//
//                return tableCount > 0 &&
//                        selectCount == tableCount &&
//                        insertCount == tableCount &&
//                        updateCount == tableCount &&
//                        deleteCount == tableCount;
//            }
//            return false;
//        }
//    }

    private String getAdminRole() {
        return tenantProperties.getAdminRole() != null ? tenantProperties.getAdminRole() : "postgres";
    }
}