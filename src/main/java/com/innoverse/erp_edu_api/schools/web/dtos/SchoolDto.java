package com.innoverse.erp_edu_api.schools.web.dtos;


import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record SchoolDto(
     UUID schoolId,
     String mopseNo,
     String schoolEmail,
     String schoolName,
     String schoolType,
     String educationLevel,
     String district,
     String province,
     String physicalAddress,
     Integer capacity,
     String status,

    // Contact person info
     String contactFullname,
     String contactPosition,
     String contactEmail,
     String contactPhone,
     String contactAltPhone,
    boolean isProvisioned,
    // Auditing
     LocalDateTime createdAt,
     LocalDateTime updatedAt
     ){

}
