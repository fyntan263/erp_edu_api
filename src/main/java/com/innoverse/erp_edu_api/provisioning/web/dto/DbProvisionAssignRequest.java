package com.innoverse.erp_edu_api.provisioning.web.dto;

import java.util.UUID;

public record DbProvisionAssignRequest(UUID schoolId,  String assignedBy){ }
