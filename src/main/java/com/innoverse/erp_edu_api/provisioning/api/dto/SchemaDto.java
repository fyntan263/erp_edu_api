package com.innoverse.erp_edu_api.provisioning.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SchemaDto(UUID schemaId,String schemaName, boolean isAssignedToSchool, UUID schoolId, LocalDateTime assignedDate, String AssignedBy) {
}
