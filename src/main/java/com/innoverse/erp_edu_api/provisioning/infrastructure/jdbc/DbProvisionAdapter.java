package com.innoverse.erp_edu_api.provisioning.infrastructure.jdbc;

import com.innoverse.erp_edu_api.provisioning.domain.DbProvision;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DbProvisionAdapter implements DbProvisioningRepository {

    private final DbProvisionJdbcRepository jpaRepository;


//    @Transactional
//    public DbProvision createProvision(DbProvisioningRequest request, String assignedBy) {
//        // Check if schema name already exists
//        if (jpaRepository.existsByDbSchemaName(request.schemaName())) {
//            throw new IllegalArgumentException("Database schema '" + request.schemaName() + "' already exists");
//        }
//
//        DbProvision provision = DbProvision.createNew(request.schemaName(), request.level().name());
//        provision.setAssignedBy(assignedBy);
//
//        // Convert enum to lowercase string for database compatibility
//        String dbStatus = provision.getProvisionStatus();
//
//        // Use custom insert
//        jpaRepository.customInsert(
//                provision.getProvisionId(),
//                provision.getDbSchemaName(),
//                dbStatus,
//                provision.getAssignedSchoolId(),
//                provision.getAssignedEducationLevel(),
//                provision.getAssignedBy(),
//                provision.getAssignedDate(),
//                provision.getErrorMessage(),
//                provision.getAttempts(),
//                provision.getCreatedAt(),
//                provision.getUpdatedAt()
//        );
//
//        log.info("Created new database provision with ID: {}", provision.getProvisionId());
//        return provision;
//    }
    @Override
    @Transactional
    public Optional<DbProvision> update(DbProvision provision) {
        if (!jpaRepository.existsById(provision.getProvisionId())) {
            log.warn("Provision with ID {} not found for update", provision.getProvisionId());
            return Optional.empty();
        }

        provision.setUpdatedAt(LocalDateTime.now());
        return Optional.of(jpaRepository.save(provision));
    }

    @Override
    @Transactional
    public DbProvision save(DbProvision provision) {
        // Check if entity already exists by ID
        if (jpaRepository.existsById(provision.getProvisionId())) {
            throw new IllegalArgumentException("Provision with ID '" + provision.getProvisionId() + "' already exists");
        }

        // Check if schema name already exists
        if (jpaRepository.existsByDbSchemaName(provision.getDbSchemaName())) {
            throw new IllegalArgumentException("Database schema '" + provision.getDbSchemaName() + "' already exists");
        }

        // Update timestamps
        provision.setUpdatedAt(LocalDateTime.now());
        if (provision.getCreatedAt() == null) {
            provision.setCreatedAt(LocalDateTime.now());
        }

        // Convert enum to lowercase string for database compatibility
        String dbStatus = provision.getProvisionStatus();

        // Use custom insert
        jpaRepository.customInsert(
                provision.getProvisionId(),
                provision.getDbSchemaName(),
                dbStatus,
                provision.getAssignedSchoolId(),
                provision.getAssignedEducationLevel(),
                provision.getAssignedBy(),
                provision.getAssignedDate(),
                provision.getErrorMessage(),
                provision.getAttempts(),
                provision.getCreatedAt(),
                provision.getUpdatedAt()
        );

        log.info("Saved database provision with ID: {}", provision.getProvisionId());
        return provision;
    }

    @Override
    @Transactional
    public boolean delete(UUID provisionId) {
        if (!jpaRepository.existsById(provisionId)) {
            log.warn("Provision with ID {} not found for deletion", provisionId);
            return false;
        }

        jpaRepository.deleteById(provisionId);
        return true;
    }

    @Override
    public Optional<DbProvision> findById(UUID provisionId) {
        return jpaRepository.findById(provisionId);
    }

    @Override
    public Optional<DbProvision> findBySchemaName(String schemaName) {
        return jpaRepository.findByDbSchemaName(schemaName);
    }

    @Override
    public List<DbProvision> findAll() {
        return (List<DbProvision>) jpaRepository.findAll();
    }

    @Override
    public List<DbProvision> findByStatus(String status) {
        return jpaRepository.findByProvisionStatus(status.toLowerCase());
    }

    @Override
    public List<DbProvision> findBySchoolId(UUID schoolId) {
        return jpaRepository.findByAssignedSchoolId(schoolId);
    }

    @Override
    public List<DbProvision> findFailedProvisionsOlderThanDays(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        return jpaRepository.findFailedProvisionsBefore(cutoffDate);
    }

    @Override
    public boolean existsBySchemaName(String schemaName) {
        return jpaRepository.existsByDbSchemaName(schemaName);
    }

    @Override
    public boolean existsById(UUID provisionId) {
        return jpaRepository.existsById(provisionId);
    }

    @Override
    public long countByStatus(String status) {
        return jpaRepository.countByProvisionStatus(status.toLowerCase());
    }

    private void validateProvision(DbProvision provision) {
        if (provision.getDbSchemaName() == null || provision.getDbSchemaName().trim().isEmpty()) {
            throw new IllegalArgumentException("Schema name is required");
        }

        if (existsBySchemaName(provision.getDbSchemaName())) {
            throw new IllegalArgumentException("Schema name already exists: " + provision.getDbSchemaName());
        }
    }

    // Additional helper methods for better performance
    public List<DbProvision> findReadyForAssignment() {
        return jpaRepository.findByProvisionStatus("provisioned");
    }

    public List<DbProvision> findPendingProvisions() {
        return jpaRepository.findByProvisionStatus("pending");
    }

    public Optional<DbProvision> findFirstAvailableProvision() {
        return jpaRepository.findByProvisionStatus("provisioned")
                .stream()
                .filter(provision -> provision.getAssignedSchoolId() == null)
                .findFirst();
    }
}