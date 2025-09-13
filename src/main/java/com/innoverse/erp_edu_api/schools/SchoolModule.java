package com.innoverse.erp_edu_api.schools;

import com.innoverse.erp_edu_api.schools.domain.School;
import com.innoverse.erp_edu_api.schools.web.dtos.SchoolDto;

import java.util.Optional;
import java.util.UUID;

public interface SchoolModule {
    Optional<SchoolDto> getSchoolById(UUID schoolId);
}
