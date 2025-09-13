package com.innoverse.erp_edu_api.features.students.services;

import com.innoverse.erp_edu_api.features.students.domain.Student;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentRepository {
    Student save(Student student);
    Optional<Student> findById(UUID studentId);
    Optional<Student> findByFirstNameAndLastNameAndDateOfBirth(String firstName, String lastName, LocalDate dateOfBirth);
    List<Student> findByFirstName(String firstName);
    List<Student> findByLastName(String lastName);
    List<Student> findByGender(Student.Gender gender);
    List<Student> findAll();
    boolean existsById(UUID id);
    boolean existsByFirstNameAndLastNameAndDateOfBirth(String firstName, String lastName, LocalDate dateOfBirth);
    void deleteById(UUID studentId);
    Student update(Student student);
}