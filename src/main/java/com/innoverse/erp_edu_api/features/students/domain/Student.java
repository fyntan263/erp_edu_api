package com.innoverse.erp_edu_api.features.students.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("students")
public class Student {
    @Id
    @Column("student_id")
    private UUID studentId;
    @Column("first_name")
    private String firstName;
    @Column("last_name")
    private String lastName;
    @Column("date_of_birth")
    private LocalDate dateOfBirth;
    @Column("gender")
    private Gender gender;
    @Column("created_at")
    private LocalDateTime createdAt;
    @Column("updated_at")
    private LocalDateTime updatedAt;

    public enum Gender {
        MALE, FEMALE
    }
}