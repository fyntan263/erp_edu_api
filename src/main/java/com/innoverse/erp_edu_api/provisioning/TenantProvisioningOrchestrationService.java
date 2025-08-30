package com.innoverse.erp_edu_api.provisioning;

import com.innoverse.erp_edu_api.provisioning.api.dto.SchemaDto;
import com.innoverse.erp_edu_api.provisioning.domain.AcademicLevel;
import com.innoverse.erp_edu_api.provisioning.domain.DbProvision;
import com.innoverse.erp_edu_api.provisioning.exceptions.*;
import com.innoverse.erp_edu_api.provisioning.infrastructure.migration.FlywayMigrationService;
import com.innoverse.erp_edu_api.provisioning.infrastructure.migration.SchemaManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantProvisioningOrchestrationService {
    private final ProvisioningTrackingService trackingService;
    private final SchemaManagerService schemaManagerService;
    private final FlywayMigrationService flywayMigrationService;

    @Transactional
    public DbProvision orchestrateProvisioning(ProvisioningContext ctx) {
        log.info("Starting provisioning for schema: {}", ctx.schemaName());

        // Check if schema truly exists (both in DB and provision record)
        if (trackingService.doesSchemaTrulyExist(ctx.schemaName())) {
            throw new SchemaAlreadyExistsException(ctx.schemaName());
        }

        // Check for orphaned schema (exists in DB but no provision record)
        if (trackingService.isSchemaOrphaned(ctx.schemaName())) {
            log.warn("Orphaned schema detected: {}, cleaning up before provisioning", ctx.schemaName());
            cleanupOrphanedSchema(ctx.schemaName());
        }

        DbProvision provision = trackingService.createProvision(ctx);

        try {
            executeProvisioning(provision, ctx);
            return provision;
        } catch (Exception e) {
            handleProvisioningFailure(provision, e);
            throw new MigrationFailedException(ctx.schemaName(), "Provisioning failed", e);
        }
    }

    @Async("provisioningTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void asyncProvisionTenant(UUID provisionId, ProvisioningContext ctx) {
        log.info("Starting async provisioning for ID: {}", provisionId);

        DbProvision provision = trackingService.getProvisionById(provisionId)
                .orElseThrow(() -> new ProvisionNotFoundException(provisionId));

        try {
            executeProvisioning(provision, ctx);
        } catch (Exception e) {
            handleProvisioningFailure(provision, e);
            log.error("Async provisioning failed for provision ID: {}", provisionId, e);
        }
    }

    @Transactional
    public void migrateSingleTenant(String schemaName, AcademicLevel level) {
        log.info("Migrating schema: {}", schemaName);

        DbProvision provision = trackingService.getProvisionBySchemaName(schemaName)
                .orElseThrow(() -> new SchemaNotFoundException(schemaName));

        try {
            executeMigration(provision, level);
        } catch (SQLException e) {
            throw new MigrationFailedException(schemaName, "Database migration failed", e);
        }
    }

    @Transactional
    public void migrateAllTenants() {
        log.info("Migrating all tenants");

        List<DbProvision> provisions = trackingService.getProvisionsByStatus("provisioned");
        MigrationResult result = new MigrationResult();

        for (DbProvision provision : provisions) {
            if (provision.getDbSchemaName() != null) {
                try {
                    AcademicLevel level = AcademicLevel.valueOf(provision.getAssignedEducationLevel());
                    executeMigration(provision, level);
                    result.incrementSuccess();
                } catch (Exception e) {
                    log.error("Migration failed for: {}", provision.getDbSchemaName(), e);
                    result.incrementFailure();
                }
            }
        }

        log.info("Migration completed. {}", result);
    }

    @Transactional
    public void retryProvisioning(UUID provisionId) {
        log.info("Retrying provision: {}", provisionId);

        DbProvision provision = getProvisionOrThrow(provisionId);
        validateRetry(provision);
        var  level= AcademicLevel.valueOf(provision.getAssignedEducationLevel());
        ProvisioningContext ctx = new ProvisioningContext(provision.getDbSchemaName(), level, null);
        // Check if this is an orphaned provision (provision exists but schema doesn't)
        if (trackingService.isProvisionOrphaned(provisionId)) {
            log.warn("Orphaned provision detected: {}, attempting recovery", provisionId);
            recoverOrphanedProvision(provision, ctx);
        } else {
            // For non-orphaned provisions, drop the existing schema and recreate from scratch
            dropSchemaForRetry(provision.getDbSchemaName());
            trackingService.updateProvisionStatus(provisionId, "pending", "Retrying provisioning");
            executeProvisioning(provision, ctx);
        }
    }

    private void dropSchemaForRetry(String schemaName) {
        try {
            log.info("Dropping schema for retry: {}", schemaName);
            if (schemaManagerService.schemaExists(schemaName)) {
                schemaManagerService.dropSchema(schemaName);
                log.info("Successfully dropped schema for retry: {}", schemaName);
            } else {
                log.info("Schema already dropped: {}", schemaName);
            }
        } catch (SQLException e) {
            throw new ProvisioningException(
                    "PROVISION_015",
                    "Failed to drop schema for retry",
                    "Error dropping schema '" + schemaName + "' for retry: " + e.getMessage(),
                    e
            );
        }
    }
    private void cleanupOrphanedSchema(String schemaName) {
        try {
            log.info("Cleaning up orphaned schema: {}", schemaName);
            schemaManagerService.dropSchema(schemaName);
            log.info("Successfully cleaned up orphaned schema: {}", schemaName);
        } catch (SQLException e) {
            throw new ProvisioningException(
                    "PROVISION_012",
                    "Failed to cleanup orphaned schema",
                    "Error cleaning up orphaned schema '" + schemaName + "': " + e.getMessage(),
                    e
            );
        }
    }

    private void recoverOrphanedProvision(DbProvision provision, ProvisioningContext ctx) {
        try {
            log.info("Recovering orphaned provision: {}", provision.getProvisionId());

            // Update status to indicate recovery
            trackingService.updateProvisionStatus(provision.getProvisionId(), "pending", "Recovering orphaned provision");

            // Ensure any existing schema is dropped before recovery
            dropSchemaForRetry(provision.getDbSchemaName());

            // Execute the provisioning process to recreate the schema
            executeProvisioning(provision, ctx);

            log.info("Successfully recovered orphaned provision: {}", provision.getProvisionId());

        } catch (Exception e) {
            trackingService.updateProvisionStatus(
                    provision.getProvisionId(),
                    "failed",
                    "Recovery failed: " + e.getMessage()
            );
            throw new ProvisioningException(
                    "PROVISION_013",
                    "Failed to recover orphaned provision",
                    "Error recovering provision '" + provision.getProvisionId() + "': " + e.getMessage(),
                    e
            );
        }
    }

    @Transactional
    public List<DbProvision> findOrphanedProvisions() {
        return trackingService.getAllProvisions().stream()
                .filter(provision -> trackingService.isProvisionOrphaned(provision.getProvisionId()))
                .toList();
    }

    @Transactional
    public List<String> findOrphanedSchemas() {
        try {
            // Get all schemas from database
            List<String> allSchemas = schemaManagerService.getAllTenantSchemas();

            return allSchemas.stream()
                    .filter(schema -> trackingService.isSchemaOrphaned(schema))
                    .toList();
        } catch (SQLException e) {
            throw new ProvisioningException(
                    "PROVISION_014",
                    "Failed to find orphaned schemas",
                    "Database error while finding orphaned schemas: " + e.getMessage(),
                    e
            );
        }
    }

    @Transactional
    public void cleanupAllOrphanedSchemas() {
        List<String> orphanedSchemas = findOrphanedSchemas();
        log.info("Found {} orphaned schemas to cleanup", orphanedSchemas.size());

        int cleanedCount = 0;
        for (String schema : orphanedSchemas) {
            try {
                cleanupOrphanedSchema(schema);
                cleanedCount++;
            } catch (Exception e) {
                log.error("Failed to cleanup orphaned schema {}: {}", schema, e.getMessage());
            }
        }
        log.info("Cleaned up {} orphaned schemas", cleanedCount);
    }

    @Transactional
    public void recoverAllOrphanedProvisions() {
        List<DbProvision> orphanedProvisions = findOrphanedProvisions();
        log.info("Found {} orphaned provisions to recover", orphanedProvisions.size());

        int recoveredCount = 0;
        for (DbProvision provision : orphanedProvisions) {
            try {
                // Create a context for recovery
                ProvisioningContext ctx = new ProvisioningContext(
                        provision.getDbSchemaName(),
                        AcademicLevel.valueOf(provision.getAssignedEducationLevel()),
                        provision.getAssignedBy() != null ? provision.getAssignedBy() : "system-recovery"
                );
                recoverOrphanedProvision(provision, ctx);
                recoveredCount++;
            } catch (Exception e) {
                log.error("Failed to recover orphaned provision {}: {}", provision.getProvisionId(), e.getMessage());
            }
        }
        log.info("Recovered {} orphaned provisions", recoveredCount);
    }

    @Transactional
    public void cleanupFailedProvisions(int maxAgeDays) {
        log.info("Cleaning up failed provisions older than {} days", maxAgeDays);

        List<DbProvision> failedProvisions = trackingService.getFailedProvisionsOlderThanDays(maxAgeDays);
        int cleanedCount = 0;

        for (DbProvision provision : failedProvisions) {
            try {
                cleanupProvision(provision);
                cleanedCount++;
            } catch (Exception e) {
                log.error("Cleanup failed for provision: {}", provision.getProvisionId(), e);
            }
        }

        log.info("Cleanup completed. Removed {} provisions", cleanedCount);
    }

    @Transactional(readOnly = true)
    public List<SchemaDto> getAllTenantSchemas() {
        return trackingService.getAllProvisions().stream()
                .map(p -> new SchemaDto(p.getProvisionId(), p.getDbSchemaName(), p.isAssigned(), p.getAssignedSchoolId(), p.getAssignedDate(), p.getAssignedBy()))
                .toList();
    }

    @Transactional
    public void assignProvisionToSchool(UUID provisionId, UUID schoolId, String assignedBy) {
        DbProvision provision = getProvisionOrThrow(provisionId);

        if (!provision.canBeAssigned()) {
            throw new InvalidProvisionStateException(
                    "Provision cannot be assigned. Current status: " + provision.getStatusDescription()
            );
        }

        try {
            trackingService.assignToSchoolWithPassword(provisionId, schoolId, assignedBy);
        } catch (Exception e) {
            throw new TenantRoleException(
                    provision.getDbSchemaName(),
                    "assign to school",
                    "Failed to update tenant role password",
                    e
            );
        }
    }

    @Transactional
    public void unassignProvision(UUID provisionId) {
        log.info("Unassigning provision {}", provisionId);
        trackingService.unassignFromSchoolWithPassword(provisionId);
    }

    @Transactional(readOnly = true)
    public ProvisioningStats getProvisioningStats() {
        List<DbProvision> allProvisions = trackingService.getAllProvisions();
        return new ProvisioningStats(
                allProvisions.size(),
                countByStatus(allProvisions, "provisioned"),
                countByStatus(allProvisions, "pending"),
                countByStatus(allProvisions, "failed")
        );
    }

    @Transactional
    public int deleteAllOrphanedSchemas() {
        List<String> orphanedSchemas = findOrphanedSchemas();
        int deletedCount = 0;
        if (orphanedSchemas.isEmpty()) {
            log.info("No orphaned schemas found to delete");
        }

        log.info("Found {} orphaned schemas to delete in batch: {}", orphanedSchemas.size(), orphanedSchemas);

        for (int i = 0; i < orphanedSchemas.size(); i++) {
            String schemaName = orphanedSchemas.get(i);
            try {
                // Delete the orphaned schema
                schemaManagerService.dropSchemaIfExists(schemaName);
                deletedCount++;
                log.info("Successfully deleted orphaned schema: {}", schemaName);

            } catch (SQLException e) {
                log.error("Failed to delete orphaned schema: {}", schemaName, e);
            } catch (Exception e) {
                log.error("Unexpected error deleting orphaned schema {}: {}", schemaName, e.getMessage());
            }
        }

        log.info("Deleted {} out of {} orphaned schemas", deletedCount, orphanedSchemas.size());
        return deletedCount;
    }

    // Private helper methods
    private void executeProvisioning(DbProvision provision, ProvisioningContext ctx) {
        try {
            updateStatus(provision, "pending", "Starting schema creation");
            createSchemaSafely(ctx.schemaName());
            updateStatus(provision, "pending", "Schema created");

            updateStatus(provision, "pending", "Starting migrations");
            flywayMigrationService.migrate(ctx.schemaName(), ctx.level());
            updateStatus(provision, "pending", "Migrations completed");

            updateStatus(provision, "pending", "Validating schema");
            validateSchema(ctx.schemaName());
            updateStatus(provision, "provisioned", "Provisioning successful");

            log.info("Successfully provisioned schema: {}", ctx.schemaName());
        } catch (Exception e) {
            updateStatus(provision, "failed", "Execution failed: " + e.getMessage());
            throw new ProvisioningException(
                    "PROVISION",
                    "Provisioning execution failed",
                    "Error during provisioning: " + e.getMessage(),
                    e
            );
        }
    }

    private void executeMigration(DbProvision provision, AcademicLevel level) throws SQLException {
        updateStatus(provision, "pending", "Starting migration");

        if (!schemaManagerService.schemaExists(provision.getDbSchemaName())) {
            throw new SchemaNotFoundException(provision.getDbSchemaName());
        }

        try {
            flywayMigrationService.migrate(provision.getDbSchemaName(), level);
            updateStatus(provision, "provisioned", "Migration completed");
        } catch (Exception e) {
            throw new MigrationFailedException(provision.getDbSchemaName(), "Migration execution failed", e);
        }
    }

    private void cleanupProvision(DbProvision provision) throws SQLException {
        if (provision.getDbSchemaName() != null &&
                schemaManagerService.schemaExists(provision.getDbSchemaName())) {
            schemaManagerService.dropSchema(provision.getDbSchemaName());
        }
        trackingService.deleteProvision(provision.getProvisionId());
    }

    private void createSchemaSafely(String schemaName) {
        try {
            // Double-check that schema doesn't exist before creating
            if (schemaManagerService.schemaExists(schemaName)) {
                // This shouldn't happen if we properly dropped it, but if it does, drop it now
                log.warn("Schema {} still exists during create operation, dropping it first", schemaName);
                schemaManagerService.dropSchema(schemaName);
            }

            schemaManagerService.createSchema(schemaName);
            log.info("Successfully created schema: {}", schemaName);

        } catch (SQLException e) {
            throw new ProvisioningException(
                    "PROVISION_016",
                    "Failed to create schema",
                    "Database error while creating schema: " + e.getMessage(),
                    e
            );
        }
    }

    private void validateSchema(String schemaName) {
        try {
            if (!schemaManagerService.schemaExists(schemaName)) {
                throw new SchemaNotFoundException(schemaName);
            }

//            if (!schemaManagerService.tableExists(schemaName, "flyway_schema_history")) {
//                throw new ValidationException("Missing migrations table");
//            }
        } catch (SQLException e) {
            throw new ProvisioningException(
                    "PROVISION",
                    "Schema validation failed",
                    "Database error during validation: " + e.getMessage(),
                    e
            );
        }
    }

    private void updateStatus(DbProvision provision, String status, String message) {
        trackingService.updateProvisionStatus(provision.getProvisionId(), status, message);
    }

    private void handleProvisioningFailure(DbProvision provision, Exception e) {
        trackingService.updateProvisionStatus(
                provision.getProvisionId(),
                "failed",
                "Provisioning failed: " + e.getMessage()
        );
    }

    private DbProvision getProvisionOrThrow(UUID provisionId) {
        return trackingService.getProvisionById(provisionId)
                .orElseThrow(() -> new ProvisionNotFoundException(provisionId));
    }

    private void validateRetry(DbProvision provision) {
//        if (!provision.canRetry(3)) {
//            throw new InvalidProvisionStateException("Max retry attempts reached");
//        }
    }

    private long countByStatus(List<DbProvision> provisions, String status) {
        return provisions.stream()
                .filter(p -> status.equalsIgnoreCase(p.getProvisionStatus()))
                .count();
    }

    // Records
    public record ProvisioningStats(long total, long provisioned, long pending, long failed) {
        public double getSuccessRate() {
            return total > 0 ? (double) provisioned / total * 100 : 0;
        }
    }

    private static class MigrationResult {
        private int success = 0;
        private int failure = 0;

        void incrementSuccess() { success++; }
        void incrementFailure() { failure++; }

        @Override
        public String toString() {
            return String.format("Success: %d, Failures: %d", success, failure);
        }
    }
}