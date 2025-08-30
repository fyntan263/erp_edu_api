package com.innoverse.erp_edu_api.features.students;

import com.innoverse.erp_edu_api.features.students.api.dtos.CreateStudentRequest;
import com.innoverse.erp_edu_api.features.students.api.dtos.StudentDto;
import com.innoverse.erp_edu_api.features.students.api.dtos.UpdateStudentRequest;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.UUID;

@UtilityClass
public class StudentMapper {

    public StudentDto toDto(Student student) {
        return StudentDto.builder()
                .studentId(student.getStudentId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .dateOfBirth(student.getDateOfBirth())
                .gender(student.getGender())
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }

    public Student toEntity(CreateStudentRequest request) {
        return Student.builder()
                .studentId(UUID.randomUUID())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .dateOfBirth(request.dateOfBirth())
                .gender(request.gender())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public Student updateEntity(Student existingStudent, UpdateStudentRequest request) {
        existingStudent.setFirstName(request.firstName());
        existingStudent.setLastName(request.lastName());
        existingStudent.setDateOfBirth(request.dateOfBirth());
        existingStudent.setGender(request.gender());
        existingStudent.setUpdatedAt(LocalDateTime.now());
        return existingStudent;
    }
}
