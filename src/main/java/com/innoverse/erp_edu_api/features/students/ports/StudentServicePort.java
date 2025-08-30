package com.innoverse.erp_edu_api.features.students.ports;


import com.innoverse.erp_edu_api.features.students.Student;
import com.innoverse.erp_edu_api.features.students.api.dtos.CreateStudentRequest;
import com.innoverse.erp_edu_api.features.students.api.dtos.StudentDto;
import com.innoverse.erp_edu_api.features.students.api.dtos.UpdateStudentRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentServicePort {
    StudentDto createStudent(CreateStudentRequest request);
    Optional<StudentDto> getStudentById(UUID studentId);
    List<StudentDto> getAllStudents();
    List<StudentDto> getStudentsByFirstName(String firstName);
    List<StudentDto> getStudentsByLastName(String lastName);
    List<StudentDto> getStudentsByGender(Student.Gender gender);
    Optional<StudentDto> getStudentByNameAndDob(String firstName, String lastName, LocalDate dateOfBirth);
    StudentDto updateStudent(UUID studentId, UpdateStudentRequest request);
    boolean studentExists(UUID studentId);
    void deleteStudent(UUID studentId);
}