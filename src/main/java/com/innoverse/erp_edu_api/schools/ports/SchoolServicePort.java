package com.innoverse.erp_edu_api.schools.ports;



import com.innoverse.erp_edu_api.provisioning.domain.AcademicLevel;
import com.innoverse.erp_edu_api.schools.School;
import com.innoverse.erp_edu_api.schools.api.dtos.CreateSchoolRequest;
import com.innoverse.erp_edu_api.schools.api.dtos.SchoolDto;
import com.innoverse.erp_edu_api.schools.api.dtos.UpdateSchoolRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface SchoolServicePort {
    @Transactional
    SchoolDto registerSchool(CreateSchoolRequest request);

    @Transactional(readOnly = true)
    SchoolDto getSchoolById(UUID schoolId);

    @Transactional
    SchoolDto updateSchool(UUID schoolId, UpdateSchoolRequest request);

    @Transactional
    void deleteSchool(UUID schoolId);

    @Transactional(readOnly = true)
    List<SchoolDto> getAllSchools();

    @Transactional(readOnly = true)
    List<SchoolDto> getSchoolsByDistrict(String district);

    @Transactional(readOnly = true)
    List<SchoolDto> getSchoolsByProvince(String province);

    @Transactional(readOnly = true)
    List<SchoolDto> getSchoolsByStatus(String status);

    @Transactional
    SchoolDto activateSchool(UUID schoolId);

    @Transactional
    SchoolDto suspendSchool(UUID schoolId);

    @Transactional
    SchoolDto assignProvision(UUID schoolId);

    @Transactional(readOnly = true)
    boolean isSchoolProvisioned(UUID schoolId, AcademicLevel academicLevel);



    default School mapToEntity(CreateSchoolRequest request) {
        return School.builder()
                .schoolName(request.getSchoolName())
                .schoolType(request.getSchoolType())
                .educationLevel(request.getEducationLevel())
                .district(request.getDistrict())
                .province(request.getProvince())
                .physicalAddress(request.getPhysicalAddress())
                .capacity(request.getCapacity())
                .schoolEmail(request.getSchoolEmail())
                .mopseNo(request.getMopseNo())
                .contactFullname(request.getContactFullname())
                .contactPosition(request.getContactPosition())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .contactAltPhone(request.getContactAltPhone())
                .build();
    }

    default SchoolDto mapToDto(School school) {
        return SchoolDto.builder()
                .schoolId(school.getSchoolId())
                .schoolName(school.getSchoolName())
                .schoolType(school.getSchoolType())
                .educationLevel(school.getEducationLevel())
                .district(school.getDistrict())
                .province(school.getProvince())
                .physicalAddress(school.getPhysicalAddress())
                .capacity(school.getCapacity())
                .schoolEmail(school.getSchoolEmail())
                .mopseNo(school.getMopseNo())
                .contactFullname(school.getContactFullname())
                .contactPosition(school.getContactPosition())
                .contactEmail(school.getContactEmail())
                .contactPhone(school.getContactPhone())
                .contactAltPhone(school.getContactAltPhone())
                .status(school.getStatus())
                .isProvisioned(school.isProvisioned())
                .createdAt(school.getCreatedAt())
                .updatedAt(school.getUpdatedAt())
                .build();
    }

    default void updateSchoolFromRequest(School school, UpdateSchoolRequest request) {
        if (request.getSchoolName() != null) school.setSchoolName(request.getSchoolName());
        if (request.getSchoolType() != null) school.setSchoolType(request.getSchoolType());
        if (request.getEducationLevel() != null) school.setEducationLevel(request.getEducationLevel());
        if (request.getDistrict() != null) school.setDistrict(request.getDistrict());
        if (request.getProvince() != null) school.setProvince(request.getProvince());
        if (request.getPhysicalAddress() != null) school.setPhysicalAddress(request.getPhysicalAddress());
        if (request.getCapacity() != null) school.setCapacity(request.getCapacity());
        if (request.getSchoolEmail() != null) school.setSchoolEmail(request.getSchoolEmail());
        if (request.getMopseNo() != null) school.setMopseNo(request.getMopseNo());
        if (request.getStatus() != null) school.setStatus(request.getStatus());
        if (request.getContactFullname() != null) school.setContactFullname(request.getContactFullname());
        if (request.getContactPosition() != null) school.setContactPosition(request.getContactPosition());
        if (request.getContactEmail() != null) school.setContactEmail(request.getContactEmail());
        if (request.getContactPhone() != null) school.setContactPhone(request.getContactPhone());
        if (request.getContactAltPhone() != null) school.setContactAltPhone(request.getContactAltPhone());
    }

    default String generateSchemaName(String schoolName,String level, UUID schoolId) {
        // Generate schema name from school name and ID
        String baseName = schoolName.toLowerCase()
                .replaceAll(" ", "_");
        String uniqueSuffix = schoolId.toString().substring(0, 8);
        if( schoolName.contains(level.toLowerCase())) return uniqueSuffix+"_"+baseName;
        return uniqueSuffix+"_"+level.toLowerCase()+"_"+baseName;
    }
}