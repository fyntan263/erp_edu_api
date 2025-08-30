package com.innoverse.erp_edu_api.features.students.api.dtos;

import com.innoverse.erp_edu_api.features.students.Student;
import lombok.Builder;

import java.time.LocalDate;

public record UpdateStudentRequest(
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        Student.Gender gender
) {
    @Builder
    public UpdateStudentRequest {
    }
}
