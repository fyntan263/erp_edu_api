package com.innoverse.erp_edu_api.schools.web;

import com.innoverse.erp_edu_api.schools.web.dtos.CreateSchoolRequest;
import com.innoverse.erp_edu_api.schools.web.dtos.SchoolDto;
import com.innoverse.erp_edu_api.schools.services.ports.SchoolServicePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/register")
@RequiredArgsConstructor
@Tag(name = "Schools", description = "API for managing educational institutions")
public class SchoolController {

    private final SchoolServicePort schoolService;

    @PostMapping
    @Operation(summary = "Register a new school", description = "Creates a new school with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "School created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "School with email or MoPSE number already exists")
    })
    public ResponseEntity<SchoolDto> registerSchool(
            @Valid @RequestBody CreateSchoolRequest request) {
        log.info("Registering new school: {}", request.getSchoolName());
        SchoolDto schoolDto = schoolService.registerSchool(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(schoolDto);
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}