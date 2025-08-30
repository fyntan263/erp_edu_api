package com.innoverse.erp_edu_api.schools.jdbc;


import com.innoverse.erp_edu_api.schools.School;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SchoolJdbcRepository extends CrudRepository<School, UUID> {
    Optional<School> findBySchoolEmail(String email);
    Optional<School> findByMopseNo(String mopseNo);
    List<School> findByDistrict(String district);
    List<School> findByProvince(String province);
    List<School> findByStatus(String status);
    List<School> findByDistrictAndEducationLevel(String district, String educationLevel);
    boolean existsBySchoolEmail(String email);
    boolean existsByMopseNo(String mopseNo);
    List<School> findAll();

    @Query("SELECT COUNT(*) FROM schools WHERE status = :status")
    long countByStatus(@Param("status") String status);

    @Query("SELECT * FROM schools WHERE district = :district AND province = :province")
    List<School> findByDistrictAndProvince(@Param("district") String district, @Param("province") String province);

    @Modifying
    @Query("""
        INSERT INTO schools (
            school_id,
            mopse_no,
            school_email,
            school_name,
            school_type,
            education_level,
            district,
            province,
            physical_address,
            capacity,
            status,
            contact_fullname,
            contact_position,
            contact_email,
            contact_phone,
            contact_alt_phone,
            is_provisioned,
            created_at,
            updated_at
        ) VALUES (
            :schoolId,
            :mopseNo,
            :schoolEmail,
            :schoolName,
            :schoolType,
            :educationLevel,
            :district,
            :province,
            :physicalAddress,
            :capacity,
            :status,
            :contactFullname,
            :contactPosition,
            :contactEmail,
            :contactPhone,
            :contactAltPhone,
            :isProvisioned,
            :createdAt,
            :updatedAt
        )
    """)
    void customInsert(
            @Param("schoolId") UUID schoolId,
            @Param("mopseNo") String mopseNo,
            @Param("schoolEmail") String schoolEmail,
            @Param("schoolName") String schoolName,
            @Param("schoolType") String schoolType,
            @Param("educationLevel") String educationLevel,
            @Param("district") String district,
            @Param("province") String province,
            @Param("physicalAddress") String physicalAddress,
            @Param("capacity") Integer capacity,
            @Param("status") String status,
            @Param("contactFullname") String contactFullname,
            @Param("contactPosition") String contactPosition,
            @Param("contactEmail") String contactEmail,
            @Param("contactPhone") String contactPhone,
            @Param("contactAltPhone") String contactAltPhone,
            @Param("isProvisioned") boolean isProvisioned,
            @Param("createdAt") LocalDateTime createdAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );
    @Modifying
    @Query("""
        UPDATE schools SET
            mopse_no = :mopseNo,
            school_email = :schoolEmail,
            school_name = :schoolName,
            school_type = :schoolType,
            education_level = :educationLevel,
            district = :district,
            province = :province,
            physical_address = :physicalAddress,
            capacity = :capacity,
            status = :status,
            contact_fullname = :contactFullname,
            contact_position = :contactPosition,
            contact_email = :contactEmail,
            contact_phone = :contactPhone,
            contact_alt_phone = :contactAltPhone,
            is_provisioned = :isProvisioned,
            updated_at = :updatedAt
        WHERE school_id = :schoolId
    """)
    int customUpdate(
            @Param("schoolId") UUID schoolId,
            @Param("mopseNo") String mopseNo,
            @Param("schoolEmail") String schoolEmail,
            @Param("schoolName") String schoolName,
            @Param("schoolType") String schoolType,
            @Param("educationLevel") String educationLevel,
            @Param("district") String district,
            @Param("province") String province,
            @Param("physicalAddress") String physicalAddress,
            @Param("capacity") Integer capacity,
            @Param("status") String status,
            @Param("contactFullname") String contactFullname,
            @Param("contactPosition") String contactPosition,
            @Param("contactEmail") String contactEmail,
            @Param("contactPhone") String contactPhone,
            @Param("contactAltPhone") String contactAltPhone,
            @Param("isProvisioned") boolean isProvisioned,
            @Param("updatedAt") LocalDateTime updatedAt
    );
}