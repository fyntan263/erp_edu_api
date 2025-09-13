package com.innoverse.erp_edu_api.provisioning.web;

import com.innoverse.erp_edu_api.provisioning.web.dto.SchemaDto;
import com.innoverse.erp_edu_api.common.domain.AcademicLevel;
import com.innoverse.erp_edu_api.provisioning.domain.DbProvision;
import com.innoverse.erp_edu_api.provisioning.exceptions.*;
import com.innoverse.erp_edu_api.provisioning.services.ProvisioningContext;
import com.innoverse.erp_edu_api.provisioning.services.ProvisioningTrackingService;
import com.innoverse.erp_edu_api.provisioning.services.TenantProvisioningOrchestrationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/platform/provisioning")
@RequiredArgsConstructor
@Tag(name = "Provisions", description = "API for managing provisions")
public class ProvisioningController {

    private final TenantProvisioningOrchestrationService orchestrationService;
    private final ProvisioningTrackingService trackingService;

    @PostMapping("/provisions")
    public ResponseEntity<ProvisionResponse> createProvision(
            @Valid @RequestBody CreateProvisionRequest request) {
        try {
            log.info("Creating new provision with prefix: {}, level: {}",
                    request.getPrefix(), request.getLevel());

            ProvisioningContext ctx = new ProvisioningContext(
                    request.getPrefix().trim(),
                    request.getLevel(),
                    request.getAssignedBy().trim()
            );

            DbProvision provision = orchestrationService.orchestrateProvisioning(ctx);

            return ResponseEntity.accepted()
                    .body(new ProvisionResponse(
                            provision.getProvisionId(),
                            provision.getDbSchemaName(),
                            "Provisioning started successfully"
                    ));
        } catch (SchemaAlreadyExistsException e) {
            throw e; // Let global handler catch it
        } catch (Exception e) {
            throw new ProvisioningException(
                    "PROVISION",
                    "Failed to create provision",
                    "Unexpected error during provision creation: " + e.getMessage(),
                    e
            );
        }
    }

//    @PostMapping("/provisions/async")
//    public ResponseEntity<ProvisionResponse> createProvisionAsync(
//            @Valid @RequestBody CreateProvisionRequest request) {
//        try {
//            log.info("Creating async provision with prefix: {}", request.getPrefix());
//
//            ProvisioningContext ctx = new ProvisioningContext(
//                    request.getPrefix().trim(),
//                    request.getLevel(),
//                    request.getAssignedBy().trim()
//            );
//
//            DbProvision provision = trackingService.createProvision(ctx);
//
//            orchestrationService.asyncProvisionTenant(provision.getProvisionId(), ctx);
//
//            return ResponseEntity.accepted()
//                    .body(new ProvisionResponse(
//                            provision.getProvisionId(),
//                            provision.getDbSchemaName(),
//                            "Async provisioning started successfully"
//                    ));
//        } catch (SchemaAlreadyExistsException e) {
//            throw e;
//        } catch (Exception e) {
//            throw new ProvisioningException(
//                    "PROVISION",
//                    "Failed to create async provision",
//                    "Unexpected error during async provision creation: " + e.getMessage(),
//                    e
//            );
//        }
//    }

    @PostMapping("/provisions/{provisionId}/retry")
    public ResponseEntity<ApiResponse> retryProvisioning(
            @PathVariable UUID provisionId) {
        try {
            log.info("Retrying provision: {}", provisionId);


            orchestrationService.retryProvisioning(provisionId);

            return ResponseEntity.accepted()
                    .body(new ApiResponse(
                            "SUCCESS",
                            "Retry started for provision: " + provisionId
                    ));
        } catch (ProvisionNotFoundException | InvalidProvisionStateException e) {
            throw e;
        } catch (Exception e) {
            throw new ProvisioningException(
                    "PROVISION",
                    "Failed to retry provision",
                    "Unexpected error during provision retry: " + e.getMessage(),
                    e
            );
        }
    }

    // Migration Operations
    @PostMapping("/migrations/schemas/{schemaName}")
    public ResponseEntity<ApiResponse> migrateSingleTenant(
            @PathVariable String schemaName,
            @RequestParam(required = false) AcademicLevel level) {
        try {
            log.info("Migrating single tenant: {}", schemaName);

            orchestrationService.migrateSingleTenant(schemaName, level);

            return ResponseEntity.accepted()
                    .body(new ApiResponse(
                            "SUCCESS",
                            "Migration started for schema: " + schemaName
                    ));
        } catch (SchemaNotFoundException | MigrationFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new ProvisioningException(
                    "PROVISION",
                    "Failed to migrate tenant",
                    "Unexpected error during tenant migration: " + e.getMessage(),
                    e
            );
        }
    }

    @PostMapping(value = "/migrations/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> migrateAllTenants() {
        try {
            log.info("Migrating all tenants");

            orchestrationService.migrateAllTenants();

            return ResponseEntity.accepted()
                    .body(new ApiResponse(
                            "SUCCESS",
                            "Bulk migration started for all tenants"
                    ));
        } catch (Exception e) {
            log.error("Migration failed: {}", e.getMessage(), e);
            throw new ProvisioningException(
                    "PROVISION_013",
                    "Failed to migrate all tenants",
                    "Unexpected error during bulk migration: " + e.getMessage(),
                    e
            );
        }
    }

    @GetMapping("/provisions")
    public ResponseEntity<List<DbProvision>> getAllProvisions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID schoolId) {
        try {
            log.info("Fetching provisions with status: {}, schoolId: {}", status, schoolId);
            status = status.trim().toLowerCase();
            List<DbProvision> provisions;
            if (!status.isBlank()) {
                provisions = trackingService.getProvisionsByStatus(status);
            } else {
                provisions = trackingService.getAllProvisions();
            }

            return ResponseEntity.ok(provisions);
        } catch (Exception e) {
            throw new ProvisioningException(
                    "PROVISION",
                    "Failed to fetch provisions",
                    "Unexpected error while fetching provisions: " + e.getMessage(),
                    e
            );
        }
    }


    @GetMapping("/provisions/{provisionId}")
    public ResponseEntity<DbProvision> getProvisionById(@PathVariable UUID provisionId) {
        try {
            log.info("Fetching provision: {}", provisionId);

            return trackingService.getProvisionById(provisionId)
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new ProvisionNotFoundException(provisionId));
        } catch (ProvisionNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ProvisioningException(
                    "PROVISION",
                    "Failed to fetch provision",
                    "Unexpected error while fetching provision: " + e.getMessage(),
                    e
            );
        }
    }
// Add these endpoints to your ProvisioningController

    @GetMapping("/provisions/orphaned")
    public ResponseEntity<List<DbProvision>> getOrphanedProvisions() {
        log.info("Fetching orphaned provisions");

        List<DbProvision> orphanedProvisions = orchestrationService.findOrphanedProvisions();
        return ResponseEntity.ok(orphanedProvisions);
    }

    @GetMapping("/schemas/orphaned")
    public ResponseEntity<List<String>> getOrphanedSchemas() {
        log.info("Fetching orphaned schemas");

        List<String> orphanedSchemas = orchestrationService.findOrphanedSchemas();
        return ResponseEntity.ok(orphanedSchemas);
    }

    @DeleteMapping("/maintenance/cleanup/orphaned-schemas")
    public ResponseEntity<ApiResponse> cleanupOrphanedSchemas() {
        log.info("Cleaning up all orphaned schemas");

        orchestrationService.cleanupAllOrphanedSchemas();

        return ResponseEntity.ok()
                .body(new ApiResponse(
                        "SUCCESS",
                        "Orphaned schemas cleanup completed"
                ));
    }

    @PostMapping("/maintenance/recover/orphaned-provisions")
    public ResponseEntity<ApiResponse> recoverOrphanedProvisions() {
        log.info("Recovering all orphaned provisions");

        orchestrationService.recoverAllOrphanedProvisions();

        return ResponseEntity.ok()
                .body(new ApiResponse(
                        "SUCCESS",
                        "Orphaned provisions recovery completed"
                ));
    }

    @GetMapping("/schemas/{schemaName}/consistency")
    public ResponseEntity<ApiResponse> checkSchemaConsistency(@PathVariable String schemaName) {
        log.info("Checking consistency for schema: {}", schemaName);

        boolean trulyExists = trackingService.doesSchemaTrulyExist(schemaName);
        boolean isOrphaned = trackingService.isSchemaOrphaned(schemaName);

        if (trulyExists) {
            return ResponseEntity.ok()
                    .body(new ApiResponse("CONSISTENT", "Schema is consistent with provision record"));
        } else if (isOrphaned) {
            return ResponseEntity.ok()
                    .body(new ApiResponse("ORPHANED", "Schema exists but has no provision record"));
        } else {
            return ResponseEntity.ok()
                    .body(new ApiResponse("MISSING", "Schema does not exist in database"));
        }
    }
    @GetMapping("/provisions/schemas/{schemaName}")
    public ResponseEntity<?> getProvisionBySchemaName(@PathVariable String schemaName) {
        log.info("Fetching provision for schema: {}", schemaName);

        return trackingService.getProvisionBySchemaName(schemaName)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new SchemaNotFoundException(schemaName));
    }

    @DeleteMapping("/schemas/{schemaName}")
    public ResponseEntity<?> dropSchema(@PathVariable String schemaName) {
        log.info("Dropping schema: {}", schemaName);

        // Check if schema exists first
        if (!trackingService.getProvisionBySchemaName(schemaName).isPresent()) {
            throw new SchemaNotFoundException(schemaName);
        }

        try {
            trackingService.cleanupSchema(schemaName);
            return ResponseEntity.ok(new ApiResponse(
                    "SUCCESS",
                    "Schema dropped successfully: " + schemaName
            ));
        } catch (Exception e) {
            throw new ProvisioningException(
                    "PROVISION",
                    "Failed to drop schema: " + schemaName,
                    "Error during schema deletion: " + e.getMessage(),
                    e
            );
        }
    }


    // Assignment Operations
    @PostMapping("/provisions/{provisionId}/assignments/schools/{schoolId}")
    public ResponseEntity<ApiResponse> assignToSchool(
            @PathVariable UUID provisionId,
            @PathVariable UUID schoolId,
            @RequestParam String assignedBy) {
        log.info("Assigning provision {} to school {}", provisionId, schoolId);

        orchestrationService.assignProvisionToSchool(provisionId, schoolId, assignedBy);

        return ResponseEntity.ok()
                .body(new ApiResponse(
                        "SUCCESS",
                        "Provision assigned to school successfully"
                ));
    }

    @DeleteMapping("/provisions/{provisionId}/assignments")
    public ResponseEntity<ApiResponse> unassignFromSchool(@PathVariable UUID provisionId) {
        log.info("Unassigning provision {}", provisionId);

        orchestrationService.unassignProvision(provisionId);

        return ResponseEntity.ok()
                .body(new ApiResponse(
                        "SUCCESS",
                        "Provision unassigned successfully"
                ));
    }

    // Schema Operations
    @GetMapping("/schemas")
    public ResponseEntity<List<SchemaDto>> getAllSchemas() {
        log.info("Fetching all tenant schemas");

        List<SchemaDto> schemas = orchestrationService.getAllTenantSchemas();
        return ResponseEntity.ok(schemas);
    }


    // Maintenance Operations
    @DeleteMapping("/maintenance/cleanup")
    public ResponseEntity<ApiResponse> cleanupFailedProvisions(
            @RequestParam(defaultValue = "30") int maxAgeDays) {
        log.info("Cleaning up failed provisions older than {} days", maxAgeDays);

        orchestrationService.cleanupFailedProvisions(maxAgeDays);

        return ResponseEntity.accepted()
                .body(new ApiResponse(
                        "SUCCESS",
                        "Cleanup process started for provisions older than " + maxAgeDays + " days"
                ));
    }

    @GetMapping("/maintenance/stats")
    public ResponseEntity<ProvisioningStatsResponse> getProvisioningStats() {
        log.info("Fetching provisioning statistics");

        var stats = orchestrationService.getProvisioningStats();

        return ResponseEntity.ok(new ProvisioningStatsResponse(
                stats.total(),
                stats.provisioned(),
                stats.pending(),
                stats.failed(),
                stats.getSuccessRate()
        ));
    }

    // Request/Response DTOs
    public static class CreateProvisionRequest {
        private String prefix;
        private AcademicLevel level;
        private String assignedBy;

        // Getters and setters with validation
        public String getPrefix() { return prefix; }
        public void setPrefix(String prefix) {
            this.prefix = prefix != null ? prefix.trim() : null;
        }

        public AcademicLevel getLevel() { return level; }
        public void setLevel(AcademicLevel level) {
            this.level = level;
        }

        public String getAssignedBy() { return assignedBy; }
        public void setAssignedBy(String assignedBy) {
            if (assignedBy == null || assignedBy.trim().isEmpty()) {
                throw new IllegalArgumentException("assignedBy is required");
            }
            this.assignedBy = assignedBy.trim();
        }
    }

    public record RetryProvisionRequest ( String schemaName,
                                          String assignedBy) { }

    public record  ProvisionResponse(UUID provisioningId, String schemaName, String message) {}
    public record ApiResponse (String status, String message) { }

    public record ProvisioningStatsResponse(long total, long provisioned, long pending, long failed, double successRate) { }

}