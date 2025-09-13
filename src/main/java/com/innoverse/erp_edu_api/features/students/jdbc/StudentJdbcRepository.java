package com.innoverse.erp_edu_api.features.students.jdbc;


import com.innoverse.erp_edu_api.features.students.domain.Student;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentJdbcRepository extends CrudRepository<Student, UUID> {

    Optional<Student> findByFirstNameAndLastNameAndDateOfBirth(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("dateOfBirth") LocalDate dateOfBirth
    );

    List<Student> findByFirstName(String firstName);
    List<Student> findByLastName(String lastName);
    List<Student> findByGender(Student.Gender gender);
    List<Student> findAll();

    @Query("SELECT COUNT(*) FROM students WHERE gender = :gender")
    long countByGender(@Param("gender") String gender);

    @Modifying
    @Query("""
        INSERT INTO students (
            student_id,
            first_name,
            last_name,
            date_of_birth,
            gender,
            created_at,
            updated_at
        ) VALUES (
            :studentId,
            :firstName,
            :lastName,
            :dateOfBirth,
            :gender,
            :createdAt,
            :updatedAt
        )
    """)
    void customInsert(
            @Param("studentId") UUID studentId,
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("dateOfBirth") LocalDate dateOfBirth,
            @Param("gender") String gender,
            @Param("createdAt") LocalDateTime createdAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    @Modifying
    @Query("""
        UPDATE students SET
            first_name = :firstName,
            last_name = :lastName,
            date_of_birth = :dateOfBirth,
            gender = :gender,
            updated_at = :updatedAt
        WHERE student_id = :studentId
    """)
    int customUpdate(
            @Param("studentId") UUID studentId,
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("dateOfBirth") LocalDate dateOfBirth,
            @Param("gender") String gender,
            @Param("updatedAt") LocalDateTime updatedAt
    );
}