package com.innoverse.erp_edu_api.provisioning.infrastructure.jdbc;


import com.innoverse.erp_edu_api.provisioning.domain.DbProvision;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DbProvisioningRepository {

    @Transactional
    DbProvision save(DbProvision provision);

    @Transactional
    Optional<DbProvision> update(DbProvision provision);

    @Transactional
    boolean delete(UUID provisionId);

    Optional<DbProvision> findById(UUID provisionId);

    Optional<DbProvision> findBySchemaName(String schemaName);

    List<DbProvision> findAll();

    List<DbProvision> findByStatus(String status);

    List<DbProvision> findBySchoolId(UUID schoolId);

    List<DbProvision> findFailedProvisionsOlderThanDays(int days);

    boolean existsBySchemaName(String schemaName);

    boolean existsById(UUID provisionId);

    long countByStatus(String status);
}
