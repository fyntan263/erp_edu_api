package com.innoverse.erp_edu_api.features.students.web.dtos;

import com.innoverse.erp_edu_api.features.students.domain.Student;
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
