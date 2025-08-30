package com.innoverse.erp_edu_api.provisioning.infrastructure.jdbc;

import com.innoverse.erp_edu_api.provisioning.domain.DbProvision;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DbProvisionJdbcRepository extends CrudRepository<DbProvision, UUID> {

    Optional<DbProvision> findByDbSchemaName(String dbSchemaName);

    List<DbProvision> findByProvisionStatus(String provisionStatus);

    List<DbProvision> findByAssignedSchoolId(UUID assignedSchoolId);

    boolean existsByDbSchemaName(String dbSchemaName);

    @Query("SELECT * FROM db_provisions WHERE provision_status = 'failed' AND created_at < :cutoffDate")
    List<DbProvision> findFailedProvisionsBefore(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT COUNT(*) FROM db_provisions WHERE provision_status = :status")
    long countByProvisionStatus(@Param("status") String status);

    @Modifying
    @Query("""
        INSERT INTO db_provisions (
            provision_id,
            db_schema_name,
            provision_status,
            assigned_school_id,
            assigned_education_level,
            assigned_by,
            assigned_date,
            error_message,
            attempts,
            created_at,
            updated_at
        ) VALUES (
            :provisionId,
            :dbSchemaName,
            :provisionStatus,
            :assignedSchoolId,
            :assignedEducationLevel,
            :assignedBy,
            :assignedDate,
            :errorMessage,
            :attempts,
            :createdAt,
            :updatedAt
        )
    """)
    void customInsert(
            @Param("provisionId") UUID provisionId,
            @Param("dbSchemaName") String dbSchemaName,
            @Param("provisionStatus") String provisionStatus, // Changed to String
            @Param("assignedSchoolId") UUID assignedSchoolId,
            @Param("assignedEducationLevel") String assignedEducationLevel,
            @Param("assignedBy") String assignedBy,
            @Param("assignedDate") LocalDateTime assignedDate,
            @Param("errorMessage") String errorMessage,
            @Param("attempts") Integer attempts,
            @Param("createdAt") LocalDateTime createdAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );
}
