package com.innoverse.erp_edu_api.features.students.api.dtos;

import com.innoverse.erp_edu_api.features.students.Student;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record StudentDto(
        UUID studentId,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        Student.Gender gender,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    @Builder
    public StudentDto {
    }
}
