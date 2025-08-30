package com.innoverse.erp_edu_api.features.students.jdbc;


import com.innoverse.erp_edu_api.features.students.Student;
import com.innoverse.erp_edu_api.features.students.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class StudentPersistenceAdapter implements StudentRepository {
    private final StudentJdbcRepository jpaRepository;

    @Override
    public Student save(Student student) {
        // Check if entity already exists by ID
        if (student.getStudentId() != null && jpaRepository.existsById(student.getStudentId())) {
            throw new IllegalArgumentException("Student with ID '" + student.getStudentId() + "' already exists");
        }

        // Check if student with same name and DOB already exists
        if (student.getStudentId() == null &&
                existsByFirstNameAndLastNameAndDateOfBirth(
                        student.getFirstName(),
                        student.getLastName(),
                        student.getDateOfBirth()
                )) {
            throw new IllegalArgumentException("Student with same name and date of birth already exists");
        }

        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        if (student.getCreatedAt() == null) {
            student.setCreatedAt(now);
        }
        student.setUpdatedAt(now);

        // Generate UUID if not provided
        if (student.getStudentId() == null) {
            student.setStudentId(UUID.randomUUID());
        }

        // Use custom insert
        jpaRepository.customInsert(
                student.getStudentId(),
                student.getFirstName(),
                student.getLastName(),
                student.getDateOfBirth(),
                student.getGender() != null ? student.getGender().name() : null,
                student.getCreatedAt(),
                student.getUpdatedAt()
        );

        log.info("Saved student with ID: {}", student.getStudentId());
        return student;
    }

    @Override
    public Optional<Student> findById(UUID studentId) {
        return jpaRepository.findById(studentId);
    }

    @Override
    public Optional<Student> findByFirstNameAndLastNameAndDateOfBirth(String firstName, String lastName, LocalDate dateOfBirth) {
        return jpaRepository.findByFirstNameAndLastNameAndDateOfBirth(firstName, lastName, dateOfBirth);
    }

    @Override
    public List<Student> findByFirstName(String firstName) {
        return jpaRepository.findByFirstName(firstName);
    }

    @Override
    public List<Student> findByLastName(String lastName) {
        return jpaRepository.findByLastName(lastName);
    }

    @Override
    public List<Student> findByGender(Student.Gender gender) {
        return jpaRepository.findByGender(gender);
    }

    @Override
    public List<Student> findAll() {
        return (List<Student>) jpaRepository.findAll();
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public boolean existsByFirstNameAndLastNameAndDateOfBirth(String firstName, String lastName, LocalDate dateOfBirth) {
        return jpaRepository.findByFirstNameAndLastNameAndDateOfBirth(firstName, lastName, dateOfBirth).isPresent();
    }

    @Override
    public void deleteById(UUID studentId) {
        jpaRepository.deleteById(studentId);
        log.info("Deleted student with ID: {}", studentId);
    }

    @Override
    public Student update(Student student) {
        if (student.getStudentId() == null) {
            throw new IllegalArgumentException("Student ID cannot be null for update");
        }

        if (!jpaRepository.existsById(student.getStudentId())) {
            throw new IllegalArgumentException("Student with ID '" + student.getStudentId() + "' does not exist");
        }

        // Check for duplicate name and DOB (excluding current student)
        Optional<Student> existingWithSameDetails = jpaRepository.findByFirstNameAndLastNameAndDateOfBirth(
                student.getFirstName(),
                student.getLastName(),
                student.getDateOfBirth()
        );
        if (existingWithSameDetails.isPresent() &&
                !existingWithSameDetails.get().getStudentId().equals(student.getStudentId())) {
            throw new IllegalArgumentException("Student with same name and date of birth already exists");
        }

        // Update timestamp
        student.setUpdatedAt(LocalDateTime.now());

        // Use custom update
        int updatedRows = jpaRepository.customUpdate(
                student.getStudentId(),
                student.getFirstName(),
                student.getLastName(),
                student.getDateOfBirth(),
                student.getGender() != null ? student.getGender().name() : null,
                student.getUpdatedAt()
        );

        if (updatedRows == 0) {
            throw new IllegalStateException("Failed to update student with ID: " + student.getStudentId());
        }

        log.info("Updated student with ID: {}", student.getStudentId());
        return student;
    }
}