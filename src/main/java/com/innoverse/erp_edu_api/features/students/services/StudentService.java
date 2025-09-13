package com.innoverse.erp_edu_api.features.students.services;

import com.innoverse.erp_edu_api.features.students.web.dtos.CreateStudentRequest;
import com.innoverse.erp_edu_api.features.students.web.dtos.StudentDto;
import com.innoverse.erp_edu_api.features.students.web.dtos.UpdateStudentRequest;
import com.innoverse.erp_edu_api.features.students.domain.Student;
import com.innoverse.erp_edu_api.features.students.services.ports.StudentServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService implements StudentServicePort {
    private final StudentRepository studentRepository;

    @Override
    public StudentDto createStudent(CreateStudentRequest request) {
        log.info("Creating new student: {} {}", request.firstName(), request.lastName());
        Student student = StudentMapper.toEntity(request);
        Student savedStudent = studentRepository.save(student);
        return StudentMapper.toDto(savedStudent);
    }

    @Override
    public Optional<StudentDto> getStudentById(UUID studentId) {
        log.info("Fetching student by ID: {}", studentId);
        return studentRepository.findById(studentId)
                .map(StudentMapper::toDto);
    }

    @Override
    public Optional<StudentDto> getStudentByNameAndDob(String firstName, String lastName, LocalDate dateOfBirth) {
        log.info("Fetching student by name and DOB: {} {}, {}", firstName, lastName, dateOfBirth);
        return studentRepository.findByFirstNameAndLastNameAndDateOfBirth(firstName, lastName, dateOfBirth)
                .map(StudentMapper::toDto);
    }

    @Override
    public List<StudentDto> getStudentsByFirstName(String firstName) {
        log.info("Fetching students by first name: {}", firstName);
        return studentRepository.findByFirstName(firstName).stream()
                .map(StudentMapper::toDto)
                .toList();
    }

    @Override
    public List<StudentDto> getStudentsByLastName(String lastName) {
        log.info("Fetching students by last name: {}", lastName);
        return studentRepository.findByLastName(lastName).stream()
                .map(StudentMapper::toDto)
                .toList();
    }

    @Override
    public List<StudentDto> getStudentsByGender(Student.Gender gender) {
        log.info("Fetching students by gender: {}", gender);
        return studentRepository.findByGender(gender).stream()
                .map(StudentMapper::toDto)
                .toList();
    }

    @Override
    public List<StudentDto> getAllStudents() {
        log.info("Fetching all students");
        return studentRepository.findAll().stream()
                .map(StudentMapper::toDto)
                .toList();
    }

    @Override
    public StudentDto updateStudent(UUID studentId, UpdateStudentRequest request) {
        log.info("Updating student with ID: {}", studentId);
        Student existingStudent = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));

        Student updatedStudent = StudentMapper.updateEntity(existingStudent, request);
        Student savedStudent = studentRepository.update(updatedStudent);
        return StudentMapper.toDto(savedStudent);
    }

    @Override
    public void deleteStudent(UUID studentId) {
        log.info("Deleting student with ID: {}", studentId);
        studentRepository.deleteById(studentId);
    }

    @Override
    public boolean studentExists(UUID studentId) {
        return studentRepository.existsById(studentId);
    }
}