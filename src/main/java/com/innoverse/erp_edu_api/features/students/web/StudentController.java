package com.innoverse.erp_edu_api.features.students.web;

import com.innoverse.erp_edu_api.features.students.services.ports.StudentServicePort;
import com.innoverse.erp_edu_api.features.students.web.dtos.CreateStudentRequest;
import com.innoverse.erp_edu_api.features.students.web.dtos.StudentDto;
import com.innoverse.erp_edu_api.features.students.web.dtos.UpdateStudentRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/schools/students")
@RequiredArgsConstructor
@Tag(name = "Students", description = "API for managing students")
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