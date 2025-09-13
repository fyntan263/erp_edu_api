package com.innoverse.erp_edu_api.schools.domain;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("schools")
public class School {
    @Id @Column("school_id") private UUID schoolId;
    @Column("mopse_no") private String mopseNo;
    @Column("school_email") private String schoolEmail;
    @Column("school_name") private String schoolName;
    @Column("school_type") private String schoolType;
    @Column("education_level") private String educationLevel;
    @Column("district") private String district;
    @Column("province") private String province;
    @Column("physical_address") private String physicalAddress;
    @Column("capacity") private Integer capacity;
    @Column("status") private String status;

    // Contact person info
    @Column("contact_fullname") private String contactFullname;
    @Column("contact_position") private String contactPosition;
    @Column("contact_email") private String contactEmail;
    @Column("contact_phone") private String contactPhone;
    @Column("contact_alt_phone") private String contactAltPhone;
    @Column("is_provisioned") private boolean isProvisioned;

    // Auditing
    @CreatedDate
    @Column("created_at") private LocalDateTime createdAt;
    @LastModifiedDate
    @Column("updated_at") private LocalDateTime updatedAt;

    public enum Status {
        ACTIVE, SUSPENDED, INACTIVE, PENDING
    }

    public enum Type {
        PRIVATE, GOVERNMENT, MISSION
    }

    public boolean isActive() {
        return Status.ACTIVE.name().equalsIgnoreCase(status);
    }

    public boolean canBeProvisioned() {
        return isActive() || Status.PENDING.name().equalsIgnoreCase(status);
    }
}