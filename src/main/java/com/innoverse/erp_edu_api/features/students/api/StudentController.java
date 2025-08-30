package com.innoverse.erp_edu_api.features.students.api;

import com.innoverse.erp_edu_api.features.students.Student;
import com.innoverse.erp_edu_api.features.students.ports.StudentServicePort;
import com.innoverse.erp_edu_api.features.students.api.dtos.CreateStudentRequest;
import com.innoverse.erp_edu_api.features.students.api.dtos.StudentDto;
import com.innoverse.erp_edu_api.features.students.api.dtos.UpdateStudentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/schools/students")
@RequiredArgsConstructor
public class StudentController {
    private final StudentServicePort studentService;

    @PostMapping
    public ResponseEntity<StudentDto> createStudent(@RequestBody CreateStudentRequest request) {
        StudentDto createdStudent = studentService.createStudent(request);
        return ResponseEntity.ok(createdStudent);
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<StudentDto> getStudentById(@PathVariable UUID studentId) {
        return studentService.getStudentById(studentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<StudentDto>> getAllStudents() {
        List<StudentDto> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/search/first-name/{firstName}")
    public ResponseEntity<List<StudentDto>> getStudentsByFirstName(@PathVariable String firstName) {
        List<StudentDto> students = studentService.getStudentsByFirstName(firstName);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/search/last-name/{lastName}")
    public ResponseEntity<List<StudentDto>> getStudentsByLastName(@PathVariable String lastName) {
        List<StudentDto> students = studentService.getStudentsByLastName(lastName);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/search/gender/{gender}")
    public ResponseEntity<List<StudentDto>> getStudentsByGender(@PathVariable Student.Gender gender) {
        List<StudentDto> students = studentService.getStudentsByGender(gender);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/search")
    public ResponseEntity<StudentDto> getStudentByNameAndDob(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam LocalDate dateOfBirth) {
        return studentService.getStudentByNameAndDob(firstName, lastName, dateOfBirth)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{studentId}")
    public ResponseEntity<StudentDto> updateStudent(
            @PathVariable UUID studentId,
            @RequestBody UpdateStudentRequest request) {
        StudentDto updatedStudent = studentService.updateStudent(studentId, request);
        return ResponseEntity.ok(updatedStudent);
    }

    @DeleteMapping("/{studentId}")
    public ResponseEntity<Void> deleteStudent(@PathVariable UUID studentId) {
        if (!studentService.studentExists(studentId)) {
            return ResponseEntity.notFound().build();
        }
        studentService.deleteStudent(studentId);
        return ResponseEntity.noContent().build();
    }
}