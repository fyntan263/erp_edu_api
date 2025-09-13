package com.innoverse.erp_edu_api.provisioning.services;

import com.innoverse.erp_edu_api.provisioning.ProvisioningService;
import com.innoverse.erp_edu_api.provisioning.domain.DbProvision;
import com.innoverse.erp_edu_api.provisioning.exceptions.ProvisioningException;
import com.innoverse.erp_edu_api.provisioning.exceptions.SchemaNotFoundException;
import com.innoverse.erp_edu_api.provisioning.infrastructure.migration.SchemaManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProvisioningTrackingService{
    private final SchemaManagerService schemaManagerService;
    private final DbProvisioningRepository repository;

    @Transactional(readOnly = true)
    public List<DbProvision> getAllProvisions() {
        return repository.findAll();
    }

//    @Cacheable(value = "provisionById", key = "#provisionId")
    @Transactional(readOnly = true)
    public Optional<DbProvision> getProvisionById(UUID provisionId) {
        return repository.findById(provisionId);
    }

//    @Cacheable(value = "provisionsBySchool", key = "#schoolId")
    @Transactional(readOnly = true)
    public List<DbProvision> getBySchoolId(UUID schoolId) {
        return repository.findBySchoolId(schoolId);
    }

//    @Cacheable(value = "provisionBySchema", key = "#schemaName")
    @Transactional(readOnly = true)
    public Optional<DbProvision> getProvisionBySchemaName(String schemaName) {
        return repository.findBySchemaName(schemaName);
    }

//    @Cacheable(value = "provisionsByStatus", key = "#status")
    @Transactional(readOnly = true)
    public List<DbProvision> getProvisionsByStatus(String status) {
        return repository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<DbProvision> getFailedProvisionsOlderThanDays(int days) {
        return repository.findFailedProvisionsOlderThanDays(days);
    }

    @Transactional(readOnly = true)
    public boolean isSchemaAvailable(String schemaName) {
        return !repository.existsBySchemaName(schemaName);
    }

    @Transactional(readOnly = true)
    public boolean doesSchemaTrulyExist(String schemaName) {
        try {
            // Check both: schema exists in database AND has a provision record
            boolean schemaExistsInDB = schemaManagerService.schemaExists(schemaName);
            boolean hasProvisionRecord = repository.existsBySchemaName(schemaName);

            return schemaExistsInDB && hasProvisionRecord;
        } catch (SQLException e) {
            throw new ProvisioningException(
                    "PROVISION",
                    "Failed to check schema existence",
                    "Database error while checking schema consistency: " + e.getMessage(),
                    e
            );
        }
    }

    @Transactional
    public Optional<DbProvision> grantProvisionAccess(UUID provisionId) {
        return repository.findById(provisionId).map(provision -> {
            provision.grantAccess();
            return repository.update(provision).orElse(provision);
        });
    }

    @Transactional
    public Optional<DbProvision> revokeProvisionAccess(UUID provisionId) {
        return repository.findById(provisionId).map(provision -> {
            provision.revokeAccess();
            return repository.update(provision).orElse(provision);
        });
    }

    @Transactional
    public Optional<DbProvision> assignToSchoolWithAccess(UUID provisionId, UUID schoolId, String assignedBy) {
        return repository.findById(provisionId).map(provision -> {
            verifySchemaExists(provision.getDbSchemaName());
            provision.assignToSchool(schoolId, assignedBy); // This now sets isAccessible=true
            return repository.update(provision).orElse(provision);
        });
    }

    @Transactional
    public Optional<DbProvision> unassignFromSchoolWithAccess(UUID provisionId) {
        return repository.findById(provisionId).map(provision -> {
            provision.unassignFromSchool(); // This now sets isAccessible=false
            return repository.update(provision).orElse(provision);
        });
    }


    @Transactional(readOnly = true)
    public boolean isSchemaOrphaned(String schemaName) {
        try {
            // Schema exists in database but no provision record
            boolean schemaExistsInDB = schemaManagerService.schemaExists(schemaName);
            boolean hasProvisionRecord = repository.existsBySchemaName(schemaName);

            return schemaExistsInDB && !hasProvisionRecord;
        } catch (SQLException e) {
            throw new ProvisioningException(
                    "PROVISION_010",
                    "Failed to check orphaned schema",
                    "Database error while checking orphaned schema: " + e.getMessage(),
                    e
            );
        }
    }

    @Transactional(readOnly = true)
    public boolean isProvisionOrphaned(UUID provisionId) {
        return repository.findById(provisionId)
                .map(provision -> {
                    try {
                        // Provision exists but schema doesn't exist in database
                        boolean schemaExistsInDB = schemaManagerService.schemaExists(provision.getDbSchemaName());
                        return !schemaExistsInDB;
                    } catch (SQLException e) {
                        throw new ProvisioningException(
                                "PROVISION_011",
                                "Failed to check orphaned provision",
                                "Database error while checking provision consistency: " + e.getMessage(),
                                e
                        );
                    }
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public long getProvisionCountByStatus(String status) {
        return repository.countByStatus(status);
    }

    @Transactional
    public DbProvision createProvision(ProvisioningContext ctx) {
        // Check if schema already exists in database
        try {
            if (schemaManagerService.schemaExists(ctx.schemaName())) {
                throw new ProvisioningException(
                        "PROVISION",
                        "Schema already exists in database: " + ctx.schemaName(),
                        "Cannot create provision for existing schema: " + ctx.schemaName()
                );
            }
        } catch (SQLException e) {
            throw new ProvisioningException(
                    "PROVISION",
                    "Failed to check schema existence: " + ctx.schemaName(),
                    "Database error while checking schema existence: " + e.getMessage(),
                    e
            );
        }

        DbProvision provision = DbProvision.createNew(ctx.schemaName(), ctx.level().name());
        provision.setAssignedBy(ctx.assignedBy());
        return repository.save(provision);
    }


    @Transactional
    public Optional<DbProvision> updateProvisionStatus(UUID provisionId, String status, String message) {
        return repository.findById(provisionId).map(provision -> {
            updateProvisionStatus(provision, status, message);
            return repository.update(provision).orElse(provision);
        });
    }

    @Transactional
    public boolean deleteProvision(UUID provisionId) {
        return repository.findById(provisionId).map(provision -> {
            // Delete the schema first, then the provision
            try {
                if (provision.getDbSchemaName() != null) {
                    schemaManagerService.dropSchemaIfExists(provision.getDbSchemaName());
                    log.info("Dropped schema: {}", provision.getDbSchemaName());
                }
            } catch (SQLException e) {
                log.warn("Failed to drop schema {}: {}", provision.getDbSchemaName(), e.getMessage());
                // Continue with provision deletion even if schema drop fails
            }

            return repository.delete(provisionId);
        }).orElse(false);
    }

    @Transactional
    public void cleanupOldFailedProvisions(int days) {
        repository.findFailedProvisionsOlderThanDays(days)
                .forEach(provision -> deleteProvision(provision.getProvisionId()));
    }

    @Transactional
    public Optional<DbProvision> assignToSchoolWithPassword(UUID provisionId, UUID schoolId, String assignedBy) {
        return repository.findById(provisionId).map(provision -> {
            // Verify schema exists before assignment
            verifySchemaExists(provision.getDbSchemaName());

            provision.assignToSchool(schoolId, assignedBy);
            DbProvision updatedProvision = repository.update(provision).orElse(provision);

            // Update the tenant role password
            try {
                schemaManagerService.assignToSchool(provision.getDbSchemaName(), schoolId);
            } catch (SQLException e) {
                log.error("Failed to update tenant role password for schema: {}", provision.getDbSchemaName(), e);
                // Continue anyway - this is not a critical failure
            }

            return updatedProvision;
        });
    }

    @Transactional
    public Optional<DbProvision> unassignFromSchoolWithPassword(UUID provisionId) {
        return repository.findById(provisionId).map(provision -> {
            provision.unassignFromSchool();
            DbProvision updatedProvision = repository.update(provision).orElse(provision);

            // Reset the tenant role password to default
            try {
                schemaManagerService.unassignFromSchool(provision.getDbSchemaName());
            } catch (SQLException e) {
                log.error("Failed to reset tenant role password for schema: {}", provision.getDbSchemaName(), e);
                // Continue anyway - this is not a critical failure
            }

            return updatedProvision;
        });
    }

    @Transactional
    public void cleanupSchema(String schemaName) throws SQLException {
        Optional<DbProvision> provisionOpt = getProvisionBySchemaName(schemaName);
        if (provisionOpt.isPresent()) {
            throw new IllegalStateException(
                    "Schema has active provision. Delete provision first: " +
                            provisionOpt.get().getProvisionId()
            );
        }

        schemaManagerService.dropSchemaIfExists(schemaName);
    }

    @Transactional(readOnly = true)
    public boolean isProvisionConsistent(UUID provisionId) {
        return repository.findById(provisionId).map(provision -> {
            try {
                boolean schemaExists = schemaManagerService.schemaExists(provision.getDbSchemaName());
                boolean provisionExists = repository.existsById(provisionId);

                // Consistency check: both should exist or both should not exist
                return schemaExists == provisionExists;
            } catch (SQLException e) {
                log.warn("Failed to check schema existence for consistency: {}", provision.getDbSchemaName(), e);
                return false;
            }
        }).orElse(false);
    }

    @Transactional(readOnly = true)
    public List<DbProvision> findInconsistentProvisions() {
        return repository.findAll().stream()
                .filter(provision -> !isProvisionConsistent(provision.getProvisionId()))
                .toList();
    }

    @Transactional
    public void repairInconsistentProvision(UUID provisionId) {
        repository.findById(provisionId).ifPresent(provision -> {
            try {
                boolean schemaExists = schemaManagerService.schemaExists(provision.getDbSchemaName());
                boolean provisionExists = repository.existsById(provisionId);

                if (provisionExists && !schemaExists) {
                    // Provision exists but schema is missing - recreate schema
                    log.warn("Repairing inconsistent provision: schema missing for {}", provisionId);
                    schemaManagerService.createSchema(provision.getDbSchemaName());
                } else if (!provisionExists && schemaExists) {
                    // Schema exists but provision is missing - delete orphaned schema
                    log.warn("Repairing inconsistent provision: orphaned schema for {}", provisionId);
                    schemaManagerService.dropSchema(provision.getDbSchemaName());
                }
            } catch (SQLException e) {
                throw new ProvisioningException(
                        "PROVISION",
                        "Failed to repair inconsistent provision: " + provisionId,
                        "Database error during repair: " + e.getMessage(),
                        e
                );
            }
        });
    }

    private void updateProvisionStatus(DbProvision provision, String status, String message) {
        switch (status.toLowerCase()) {
            case "provisioned":
                provision.markAsProvisioned();
                break;
            case "failed":
                provision.markAsFailed(message);
                break;
            case "pending":
                provision.resetForRetry();
                break;
            default:
                throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    private void verifySchemaExists(String schemaName) {
        try {
            if (!schemaManagerService.schemaExists(schemaName)) {
                throw new SchemaNotFoundException(schemaName);
            }
        } catch (SQLException e) {
            throw new ProvisioningException(
                    "PROVISION",
                    "Failed to verify schema existence: " + schemaName,
                    "Database error while verifying schema: " + e.getMessage(),
                    e
            );
        }
    }
}