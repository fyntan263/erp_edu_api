package com.innoverse.erp_edu_api.schools.services;


import com.innoverse.erp_edu_api.schools.domain.School;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface SchoolRepository {
    School save(School school);
    School update(School school); // Add this method
    Optional<School> findById(UUID schoolId);
    Optional<School> findByEmail(String email);
    Optional<School> findByMopseNo(String mopseNo);
    List<School> findByDistrict(String district);
    List<School> findByProvince(String province);
    List<School> findByStatus(String status);
    List<School> findByDistrictAndLevel(String district, String educationLevel);
    boolean existsByEmail(String email);
    boolean existsById(UUID id);
    boolean existsByMopseNo(String mopseNo);
    List<School> findAll();
    void deleteById(UUID schoolId);
}