package com.innoverse.erp_edu_api.provisioning.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("db_provisions")
public class DbProvision {
    public enum Status {
        PENDING, PROVISIONED, FAILED
    }

    @Id @Column("provision_id") private UUID provisionId;
    @Column("db_schema_name") private String dbSchemaName;
    @Column("provision_status") private String provisionStatus;
    @Column("assigned_school_id") private UUID assignedSchoolId;
    @Column("assigned_education_level") private String assignedEducationLevel;
    @Column("is_accessible") private boolean isAccessible;
    @CreatedBy @Column("assigned_by") private String assignedBy;
    @Column("assigned_date") private LocalDateTime assignedDate;
    @Column("error_message") private String errorMessage;
    @Column("attempts") private Integer attempts = 0;
    @CreatedDate @Column("created_at") private LocalDateTime createdAt;
    @LastModifiedDate @Column("updated_at") private LocalDateTime updatedAt;


    public static DbProvision createNew(String dbSchemaName, String educationLevel) {
        DbProvision provision = new DbProvision();
        provision.provisionId = UUID.randomUUID();
        provision.dbSchemaName = dbSchemaName;
        provision.provisionStatus = Status.PENDING.name().toLowerCase();
        provision.assignedEducationLevel = educationLevel;
        provision.attempts = 0;
        return provision;
    }

    // Business methods
    public void grantAccess() {
        if (!isProvisioned() || !isAssigned()) {
            throw new IllegalStateException("Cannot grant access to unprovisioned or unassigned schema");
        }
        this.isAccessible = true;
    }

    public void revokeAccess() {
        this.isAccessible = false;
    }

    public boolean canAccessDatabase() {
        return Boolean.TRUE.equals(isAccessible) && isProvisioned() && isAssigned();
    }

    public void assignToSchool(UUID schoolId, String assignedBy) {
        validateAssignment();
        this.assignedSchoolId = schoolId;
        this.assignedBy = assignedBy;
        this.assignedDate = LocalDateTime.now();
        this.isAccessible = true; // Auto-grant access on assignment
    }

    public void unassignFromSchool() {
        if (this.assignedSchoolId == null) {
            throw new IllegalStateException("Provision is not currently assigned to any school");
        }
        this.assignedSchoolId = null;
        this.assignedBy = null;
        this.assignedDate = null;
        this.isAccessible = false; // Revoke access on unassignment
    }

    public void markAsProvisioned() {
        this.provisionStatus = Status.PROVISIONED.name().toLowerCase();
        this.errorMessage = null;
    }

    public void markAsFailed(String errorMessage) {
        this.provisionStatus = Status.FAILED.name().toLowerCase();
        this.errorMessage = errorMessage;
        this.attempts = (this.attempts != null ? this.attempts : 0) + 1;
    }

    public void resetForRetry() {
        this.provisionStatus = Status.PENDING.name().toLowerCase();
        this.errorMessage = null;
    }

    // Validation methods
    private void validateAssignment() {
        if (this.assignedSchoolId != null) {
            throw new IllegalStateException("Provision is already assigned to school ID: " + this.assignedSchoolId);
        }
        if (!isProvisioned()) {
            throw new IllegalStateException("Cannot assign provision. Current status: " + this.provisionStatus);
        }
    }

    public boolean canBeAssigned() {
        return isProvisioned() && !isAssigned();
    }

    public boolean canBeProcessed() {
        return Status.PENDING.name().equalsIgnoreCase(this.provisionStatus) &&
                this.assignedSchoolId == null;
    }

    public boolean canRetry(int maxAttempts) {
        return (isFailed() || isPending()) &&
                (this.attempts != null && this.attempts < maxAttempts);
    }

    // Status check methods
    public boolean isProvisioned() {
        return Status.PROVISIONED.name().equalsIgnoreCase(this.provisionStatus);
    }

    public boolean isPending() {
        return Status.PENDING.name().equalsIgnoreCase(this.provisionStatus);
    }

    public boolean isFailed() {
        return Status.FAILED.name().equalsIgnoreCase(this.provisionStatus);
    }

    public boolean isAssigned() {
        return this.assignedSchoolId != null;
    }

    public String getStatusDescription() {
        if (isAssigned()) {
            return "ASSIGNED to school: " + this.assignedSchoolId;
        }
        if (isProvisioned()) return "READY for assignment";
        if (isPending()) return "PENDING provisioning";
        if (isFailed()) return "FAILED provisioning";
        return "UNKNOWN status";
    }
}