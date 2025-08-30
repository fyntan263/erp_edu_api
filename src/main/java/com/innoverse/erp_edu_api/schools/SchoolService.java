package com.innoverse.erp_edu_api.schools;


import com.innoverse.erp_edu_api.provisioning.domain.AcademicLevel;
import com.innoverse.erp_edu_api.provisioning.TenantProvisioningOrchestrationService;
import com.innoverse.erp_edu_api.provisioning.ProvisioningTrackingService;
import com.innoverse.erp_edu_api.provisioning.domain.DbProvision;
import com.innoverse.erp_edu_api.provisioning.ProvisioningContext;
import com.innoverse.erp_edu_api.schools.api.dtos.CreateSchoolRequest;
import com.innoverse.erp_edu_api.schools.api.dtos.SchoolDto;
import com.innoverse.erp_edu_api.schools.api.dtos.UpdateSchoolRequest;
import com.innoverse.erp_edu_api.schools.exceptions.SchoolAlreadyExistsException;
import com.innoverse.erp_edu_api.schools.exceptions.SchoolNotFoundException;
import com.innoverse.erp_edu_api.schools.ports.SchoolServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolService  implements SchoolServicePort {

    private final SchoolRepository schoolRepository;
    private final TenantProvisioningOrchestrationService provisioningService;
    private final ProvisioningTrackingService trackingService;

    @Transactional
    @Override
    public SchoolDto registerSchool(CreateSchoolRequest request) {
        validateSchoolRequest(request);

        School school = mapToEntity(request);
        school.setStatus(School.Status.PENDING.name().toLowerCase());

        School savedSchool = schoolRepository.save(school);
        log.info("School registered successfully: {}", savedSchool.getSchoolId());

        try {
            // Auto-provision if school is active
            if (School.Status.PENDING.name().equalsIgnoreCase(savedSchool.getStatus())) {
                this.assignProvision(savedSchool.getSchoolId());

                savedSchool.setProvisioned(true);
            }
        } catch (Exception e) {
            log.warn("Auto-provisioning failed for school: {}, error: {}", savedSchool.getSchoolId(), e.getMessage());
            // Continue without failing the registration
            savedSchool.setProvisioned(false);
        }
        this.schoolRepository.update(savedSchool);
        return mapToDto(savedSchool);

//        return enrichWithProvisionInfo(mapToDto(savedSchool), savedSchool.getSchoolId());
    }

    @Transactional(readOnly = true)
    @Override
    public SchoolDto getSchoolById(UUID schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new SchoolNotFoundException(schoolId));

        return mapToDto(school);
//        enrichWithProvisionInfo(dto, schoolId);
//        return dto;
    }

    @Transactional
    @Override
    public SchoolDto updateSchool(UUID schoolId, UpdateSchoolRequest request) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new SchoolNotFoundException(schoolId));

        updateSchoolFromRequest(school, request);
        School updatedSchool = schoolRepository.save(school);

       return mapToDto(updatedSchool);
//        enrichWithProvisionInfo(dto, schoolId);
//        return dto;
    }

    @Transactional
    @Override
    public void deleteSchool(UUID schoolId) {
        if (!schoolRepository.existsById(schoolId)) {
            throw new SchoolNotFoundException(schoolId);
        }

        // Unassign provision if exists
        try {
            trackingService.unassignFromSchoolWithPassword(schoolId);
        } catch (Exception e) {
            log.warn("Failed to unassign provision during school deletion: {}", e.getMessage());
        }

        schoolRepository.deleteById(schoolId);
        log.info("School deleted successfully: {}", schoolId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<SchoolDto> getAllSchools() {
        //                    enrichWithProvisionInfo(dto, school.getSchoolId());
        //                    return dto;
        return schoolRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<SchoolDto> getSchoolsByDistrict(String district) {
        return schoolRepository.findByDistrict(district).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<SchoolDto> getSchoolsByProvince(String province) {
        return schoolRepository.findByProvince(province).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<SchoolDto> getSchoolsByStatus(String status) {
        return schoolRepository.findByStatus(status).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public SchoolDto activateSchool(UUID schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new SchoolNotFoundException(schoolId));

        school.setStatus(School.Status.ACTIVE.name().toLowerCase());
        School updatedSchool = schoolRepository.save(school);

        // Auto-provision on activation
        assignProvision(schoolId);

        return mapToDto(updatedSchool);
//        enrichWithProvisionInfo(dto, schoolId);
//        return dto;
    }

    @Transactional
    @Override
    public SchoolDto suspendSchool(UUID schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new SchoolNotFoundException(schoolId));

        school.setStatus(School.Status.SUSPENDED.name().toLowerCase());
        School updatedSchool = schoolRepository.save(school);

        // Unassign provision on suspension
        try {
            trackingService.unassignFromSchoolWithPassword(schoolId);
        } catch (Exception e) {
            log.warn("Failed to unassign provision during school suspension: {}", e.getMessage());
        }

        return  mapToDto(updatedSchool);
//        enrichWithProvisionInfo(dto, schoolId);
//        return dto;
    }

    @Transactional
    @Override
    public SchoolDto assignProvision(UUID schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new SchoolNotFoundException(schoolId));

        if (!school.canBeProvisioned()) {
//            throw new SchoolNotEligibleForProvisioningException(schoolId, school.getStatus());
            throw new IllegalArgumentException("School already has been assigned to this School");
        }

        // Generate schema name from school name
        String schemaName = generateSchemaName(school.getSchoolName(),school.getEducationLevel(), schoolId);
        boolean isProvisionSuccessful = false;
        try {
            // Create provisioning context
            ProvisioningContext ctx =
                    new ProvisioningContext(
                            schemaName,
                            AcademicLevel.valueOf(school.getEducationLevel().toUpperCase()),
                            "system"
                    );

            // Orchestrate provisioning
            DbProvision provision = provisioningService.orchestrateProvisioning(ctx);

            // Assign provision to school
            provisioningService.assignProvisionToSchool(provision.getProvisionId(), schoolId, "system");

            log.info("Provision assigned successfully to school: {}", schoolId);
            isProvisionSuccessful = true;
        } catch (Exception e) {
//            throw new ProvisioningAssignmentException(schoolId, e);
            throw new RuntimeException("Failed to assign provision to school: " + schoolId, e);
        }


        return mapToDto(school);
//        return enrichWithProvisionInfo(dto, schoolId);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean isSchoolProvisioned(UUID schoolId, AcademicLevel academicLevel) {
        return trackingService.getBySchoolId(schoolId)
                .stream().anyMatch(p -> p.getAssignedEducationLevel().equalsIgnoreCase(academicLevel.name()));
    }
    // Helper methods
    private void validateSchoolRequest(CreateSchoolRequest request) {
        if (schoolRepository.existsByEmail(request.getSchoolEmail())) {
            throw new SchoolAlreadyExistsException("email", request.getSchoolEmail());
        }

        if (request.getMopseNo() != null && schoolRepository.existsByMopseNo(request.getMopseNo())) {
            throw new SchoolAlreadyExistsException("MoPSE number", request.getMopseNo());
        }
    }

//    private SchoolDto enrichWithProvisionInfo(SchoolDto dto, UUID schoolId) {
//        try {
//            Optional<DbProvision> provisionOpt = trackingService.getBySchoolId(schoolId)
//                    .stream()
//                    .filter(provision -> Objects.equals(provision.getAssignedEducationLevel().toLowerCase(),
//                            dto.educationLevel().toLowerCase()))
//                    .findFirst();
//            String status = provisionOpt.get().getProvisionStatus();
//
//            log.info("School enriched successfully: {}",status );
//            var result =  dto.withProvisionedStatus(status);
//            log.info("School enriched successfully: {}",result);
//            return result;
//        } catch (Exception e) {
//            SchoolService.log.warn("Failed to fetch provision info for school: {}", schoolId, e);
//            return dto.withProvisionedStatus("ERROR ON FETCHING PROVISION INFO");
//        }
//    }
}