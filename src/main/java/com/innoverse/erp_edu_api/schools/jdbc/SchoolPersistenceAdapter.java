package com.innoverse.erp_edu_api.schools.jdbc;

import com.innoverse.erp_edu_api.schools.domain.School;
import com.innoverse.erp_edu_api.schools.services.SchoolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SchoolPersistenceAdapter implements SchoolRepository {
    private final SchoolJdbcRepository jpaRepository;

    @Override
    public School save(School school) {
//        if (school.getSchoolId() != null && jpaRepository.existsById(school.getSchoolId())) {
//            throw new IllegalArgumentException("School with ID '" + school.getSchoolId() + "' already exists");
//        }
//
//        if (school.getSchoolId() == null && school.getSchoolEmail() != null &&
//                jpaRepository.existsBySchoolEmail(school.getSchoolEmail())) {
//            throw new IllegalArgumentException("School with email '" + school.getSchoolEmail() + "' already exists");
//        }
//
//        // Check if MoPSE number already exists (for new schools)
//        if (school.getSchoolId() == null && school.getMopseNo() != null &&
//                jpaRepository.existsByMopseNo(school.getMopseNo())) {
//            throw new IllegalArgumentException("School with MoPSE number '" + school.getMopseNo() + "' already exists");
//        }

        LocalDateTime now = LocalDateTime.now();
        if (school.getCreatedAt() == null) {
            school.setCreatedAt(now);
        }
        school.setUpdatedAt(now);
        if (school.getSchoolId() == null) {
            school.setSchoolId(UUID.randomUUID());
        }
        jpaRepository.customInsert(
                school.getSchoolId(),
                school.getMopseNo(),
                school.getSchoolEmail(),
                school.getSchoolName(),
                school.getSchoolType(),
                school.getEducationLevel(),
                school.getDistrict(),
                school.getProvince(),
                school.getPhysicalAddress(),
                school.getCapacity(),
                school.getStatus(),
                school.getContactFullname(),
                school.getContactPosition(),
                school.getContactEmail(),
                school.getContactPhone(),
                school.getContactAltPhone(),
                school.isProvisioned(),
                school.getCreatedAt(),
                school.getUpdatedAt()
        );

        log.info("Saved school with ID: {}", school.getSchoolId());
        return school;
    }

    @Override
    public Optional<School> findById(UUID schoolId) {
        return jpaRepository.findById(schoolId);
    }

    @Override
    public Optional<School> findByEmail(String email) {
        return jpaRepository.findBySchoolEmail(email);
    }

    @Override
    public Optional<School> findByMopseNo(String mopseNo) {
        return jpaRepository.findByMopseNo(mopseNo);
    }

    @Override
    public List<School> findByDistrict(String district) {
        return jpaRepository.findByDistrict(district);
    }

    @Override
    public List<School> findByProvince(String province) {
        return jpaRepository.findByProvince(province);
    }

    @Override
    public List<School> findByStatus(String status) {
        return jpaRepository.findByStatus(status);
    }

    @Override
    public List<School> findByDistrictAndLevel(String district, String educationLevel) {
        return jpaRepository.findByDistrictAndEducationLevel(district, educationLevel);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsBySchoolEmail(email);
    }

    @Override
    public boolean existsById(UUID id) {
        return this.jpaRepository.existsById(id);
    }

    @Override
    public boolean existsByMopseNo(String mopseNo) {
        return jpaRepository.existsByMopseNo(mopseNo);
    }

    @Override
    public List<School> findAll() {
        return (List<School>) jpaRepository.findAll();
    }

    @Override
    public void deleteById(UUID schoolId) {
        jpaRepository.deleteById(schoolId);
        log.info("Deleted school with ID: {}", schoolId);
    }

    public School update(School school) {
        if (school.getSchoolId() == null) {
            throw new IllegalArgumentException("School ID cannot be null for update");
        }

        if (!jpaRepository.existsById(school.getSchoolId())) {
            throw new IllegalArgumentException("School with ID '" + school.getSchoolId() + "' does not exist");
        }

        // Check for email conflicts (excluding current school)
        if (school.getSchoolEmail() != null) {
            Optional<School> existingWithEmail = jpaRepository.findBySchoolEmail(school.getSchoolEmail());
            if (existingWithEmail.isPresent() && !existingWithEmail.get().getSchoolId().equals(school.getSchoolId())) {
                throw new IllegalArgumentException("School with email '" + school.getSchoolEmail() + "' already exists");
            }
        }

        // Check for MoPSE number conflicts (excluding current school)
        if (school.getMopseNo() != null) {
            Optional<School> existingWithMopse = jpaRepository.findByMopseNo(school.getMopseNo());
            if (existingWithMopse.isPresent() && !existingWithMopse.get().getSchoolId().equals(school.getSchoolId())) {
                throw new IllegalArgumentException("School with MoPSE number '" + school.getMopseNo() + "' already exists");
            }
        }

        // Update timestamp
        school.setUpdatedAt(LocalDateTime.now());

        // Use custom update
        int updatedRows = jpaRepository.customUpdate(
                school.getSchoolId(),
                school.getMopseNo(),
                school.getSchoolEmail(),
                school.getSchoolName(),
                school.getSchoolType(),
                school.getEducationLevel(),
                school.getDistrict(),
                school.getProvince(),
                school.getPhysicalAddress(),
                school.getCapacity(),
                school.getStatus(),
                school.getContactFullname(),
                school.getContactPosition(),
                school.getContactEmail(),
                school.getContactPhone(),
                school.getContactAltPhone(),
                school.isProvisioned(),
                school.getUpdatedAt()
        );

        if (updatedRows == 0) {
            throw new IllegalStateException("Failed to update school with ID: " + school.getSchoolId());
        }

        log.info("Updated school with ID: {}", school.getSchoolId());
        return school;
    }
}