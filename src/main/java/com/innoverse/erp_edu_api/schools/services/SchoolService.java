package com.innoverse.erp_edu_api.schools.services;

import com.innoverse.erp_edu_api.common.domain.AcademicLevel;
import com.innoverse.erp_edu_api.provisioning.services.DistributedTenantCache;
import com.innoverse.erp_edu_api.provisioning.ProvisioningService;
import com.innoverse.erp_edu_api.schools.SchoolModule;
import com.innoverse.erp_edu_api.schools.domain.School;
import com.innoverse.erp_edu_api.schools.web.dtos.CreateSchoolRequest;
import com.innoverse.erp_edu_api.schools.web.dtos.SchoolDto;
import com.innoverse.erp_edu_api.schools.web.dtos.UpdateSchoolRequest;
import com.innoverse.erp_edu_api.schools.exceptions.SchoolAlreadyExistsException;
import com.innoverse.erp_edu_api.schools.exceptions.SchoolNotFoundException;
import com.innoverse.erp_edu_api.schools.services.ports.SchoolServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolService implements SchoolServicePort, SchoolModule {
    private final SchoolRepository schoolRepository;
  private final ProvisioningService provisioningService;
    private final SchoolAccessControlService accessControlService;
    private final DistributedTenantCache tenantSchemaCache;

    @Transactional
    @Override
    public SchoolDto registerSchool(CreateSchoolRequest request) {
        validateSchoolRequest(request);

        School school = mapToEntity(request);
        school.setStatus(School.Status.PENDING.name().toLowerCase());

        School savedSchool = schoolRepository.save(school);
        log.info("School registered successfully: {}", savedSchool.getSchoolId());

        try {
            this.assignProvision(savedSchool.getSchoolId());
            school.setStatus(School.Status.ACTIVE.name().toLowerCase());
            savedSchool.setProvisioned(true);
        } catch (Exception e) {
            log.warn("Auto-provisioning failed for school: {}, error: {}", savedSchool.getSchoolId(), e.getMessage());
            savedSchool.setProvisioned(false);
        }
        this.schoolRepository.update(savedSchool);
        return mapToDto(savedSchool);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<SchoolDto> getSchoolById(UUID schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new SchoolNotFoundException(schoolId));
        return Optional.of(mapToDto(school));
    }

    @Transactional
    @Override
    public SchoolDto updateSchool(UUID schoolId, UpdateSchoolRequest request) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new SchoolNotFoundException(schoolId));

        updateSchoolFromRequest(school, request);
        School updatedSchool = schoolRepository.update(school);

        return mapToDto(updatedSchool);
    }

    @Transactional
    @Override
    public void deleteSchool(UUID schoolId) {
        if (!schoolRepository.existsById(schoolId)) {
            throw new SchoolNotFoundException(schoolId);
        }

        // Revoke all database access first
        accessControlService.revokeDatabaseAccess(schoolId);

        // Unassign provisions
        try {
            provisioningService.getByProvisionBySchoolId(schoolId).forEach(provision -> {
                provisioningService.unassignFromSchoolWithAccess(provision.getProvisionId());
            });
        } catch (Exception e) {
            log.warn("Failed to unassign provisions during school deletion: {}", e.getMessage());
        }
        tenantSchemaCache.invalidateCaches(schoolId);
        tenantSchemaCache.updateSchemaCache(schoolId, null);
        schoolRepository.deleteById(schoolId);
        log.info("School deleted successfully: {}", schoolId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<SchoolDto> getAllSchools() {
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
        School updatedSchool = schoolRepository.update(school);

        // Grant database access upon activation
        accessControlService.grantDatabaseAccess(schoolId);

        // Auto-provision if needed
        if (!isSchoolProvisioned(schoolId, AcademicLevel.valueOf(school.getEducationLevel().toUpperCase()))) {
            assignProvision(schoolId);
        }

        tenantSchemaCache.updateAccessCache(schoolId, true);
        return mapToDto(updatedSchool);
    }

    @Transactional
    @Override
    public SchoolDto suspendSchool(UUID schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new SchoolNotFoundException(schoolId));

        school.setStatus(School.Status.SUSPENDED.name().toLowerCase());
        School updatedSchool = schoolRepository.update(school);

        // Revoke database access immediately upon suspension
        accessControlService.revokeDatabaseAccess(schoolId);
        // Invalidate cache
        tenantSchemaCache.invalidateCaches(schoolId);
        return mapToDto(updatedSchool);
    }

    @Transactional
    @Override
    public SchoolDto assignProvision(UUID schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new SchoolNotFoundException(schoolId));

        if (!school.canBeProvisioned()) {
            throw new IllegalArgumentException("School is not eligible for provisioning");
        }

        // Generate schema name from school name
        String schemaName = generateSchemaName(school.getSchoolName(), school.getEducationLevel(), schoolId);

        try {
            // Create provisioning context
            var ctx = this.provisioningService.buildContext(
                    schemaName,
                    AcademicLevel.valueOf(school.getEducationLevel().toUpperCase()),
                    "system"
            );

            // Orchestrate provisioning
            var provision = provisioningService.orchestrateProvisioning(ctx);

            // Assign provision to school with automatic access grant
            this.provisioningService.assignToSchoolWithAccess(provision.getProvisionId(), schoolId, "system");
            tenantSchemaCache.updateSchemaCache(schoolId, schemaName);
            tenantSchemaCache.updateAccessCache(schoolId, true);
            log.info("Provision assigned successfully to school: {}", schoolId);

        } catch (Exception e) {
            throw new RuntimeException("Failed to assign provision to school: " + schoolId, e);
        }

        return mapToDto(school);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean isSchoolProvisioned(UUID schoolId, AcademicLevel academicLevel) {
        return this.provisioningService.getByProvisionBySchoolId(schoolId)
                .stream()
                .anyMatch(p -> p.getAssignedEducationLevel().equalsIgnoreCase(academicLevel.name()));
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

}