package com.innoverse.erp_edu_api.schools.api;

import com.innoverse.erp_edu_api.provisioning.domain.AcademicLevel;
import com.innoverse.erp_edu_api.schools.api.dtos.CreateSchoolRequest;
import com.innoverse.erp_edu_api.schools.api.dtos.SchoolDto;
import com.innoverse.erp_edu_api.schools.api.dtos.UpdateSchoolRequest;
import com.innoverse.erp_edu_api.schools.ports.SchoolServicePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/schools")
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

    @GetMapping("/{schoolId}")
    @Operation(summary = "Get school by ID", description = "Retrieves a school by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "School found successfully"),
            @ApiResponse(responseCode = "404", description = "School not found")
    })
    public ResponseEntity<SchoolDto> getSchoolById(
            @Parameter(description = "UUID of the school to be retrieved", required = true)
            @PathVariable UUID schoolId) {
        log.info("Fetching school with ID: {}", schoolId);
        SchoolDto schoolDto = schoolService.getSchoolById(schoolId);
        return ResponseEntity.ok(schoolDto);
    }

    @PutMapping("/{schoolId}")
    @Operation(summary = "Update school details", description = "Updates the details of an existing school")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "School updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "School not found"),
            @ApiResponse(responseCode = "409", description = "School with email or MoPSE number already exists")
    })
    public ResponseEntity<SchoolDto> updateSchool(
            @Parameter(description = "UUID of the school to be updated", required = true)
            @PathVariable UUID schoolId,
            @Valid @RequestBody UpdateSchoolRequest request) {
        log.info("Updating school with ID: {}", schoolId);
        SchoolDto schoolDto = schoolService.updateSchool(schoolId, request);
        return ResponseEntity.ok(schoolDto);
    }

    @DeleteMapping("/{schoolId}")
    @Operation(summary = "Delete a school", description = "Permanently deletes a school and its associated data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "School deleted successfully"),
            @ApiResponse(responseCode = "404", description = "School not found")
    })
    public ResponseEntity<Void> deleteSchool(
            @Parameter(description = "UUID of the school to be deleted", required = true)
            @PathVariable UUID schoolId) {
        log.info("Deleting school with ID: {}", schoolId);
        schoolService.deleteSchool(schoolId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get all schools", description = "Retrieves a list of all schools with optional pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Schools retrieved successfully")
    })
    public ResponseEntity<List<SchoolDto>> getAllSchools(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "Sort by field (e.g., schoolName,createdAt)")
            @RequestParam(defaultValue = "schoolName") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)")
            @RequestParam(defaultValue = "asc") String direction) {

        log.info("Fetching all schools - page: {}, size: {}, sort: {}, direction: {}", page, size, sortBy, direction);

        // For simplicity, returning all schools without pagination
        // In a real implementation, you would implement pagination in your service
        List<SchoolDto> schools = schoolService.getAllSchools();
        return ResponseEntity.ok(schools);
    }

    @GetMapping("/district/{district}")
    @Operation(summary = "Get schools by district", description = "Retrieves schools located in a specific district")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Schools retrieved successfully")
    })
    public ResponseEntity<List<SchoolDto>> getSchoolsByDistrict(
            @Parameter(description = "Name of the district", required = true)
            @PathVariable String district) {
        log.info("Fetching schools in district: {}", district);
        List<SchoolDto> schools = schoolService.getSchoolsByDistrict(district);
        return ResponseEntity.ok(schools);
    }

    @GetMapping("/province/{province}")
    @Operation(summary = "Get schools by province", description = "Retrieves schools located in a specific province")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Schools retrieved successfully")
    })
    public ResponseEntity<List<SchoolDto>> getSchoolsByProvince(
            @Parameter(description = "Name of the province", required = true)
            @PathVariable String province) {
        log.info("Fetching schools in province: {}", province);
        List<SchoolDto> schools = schoolService.getSchoolsByProvince(province);
        return ResponseEntity.ok(schools);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get schools by status", description = "Retrieves schools with a specific status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Schools retrieved successfully")
    })
    public ResponseEntity<List<SchoolDto>> getSchoolsByStatus(
            @Parameter(description = "Status of the school (active, pending, suspended)", required = true)
            @PathVariable String status) {
        log.info("Fetching schools with status: {}", status);
        List<SchoolDto> schools = schoolService.getSchoolsByStatus(status);
        return ResponseEntity.ok(schools);
    }

    @PatchMapping("/{schoolId}/activate")
    @Operation(summary = "Activate a school", description = "Changes a school's status to active and provisions it")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "School activated successfully"),
            @ApiResponse(responseCode = "404", description = "School not found")
    })
    public ResponseEntity<SchoolDto> activateSchool(
            @Parameter(description = "UUID of the school to be activated", required = true)
            @PathVariable UUID schoolId) {
        log.info("Activating school with ID: {}", schoolId);
        SchoolDto schoolDto = schoolService.activateSchool(schoolId);
        return ResponseEntity.ok(schoolDto);
    }

    @PatchMapping("/{schoolId}/suspend")
    @Operation(summary = "Suspend a school", description = "Changes a school's status to suspended and deprovisions it")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "School suspended successfully"),
            @ApiResponse(responseCode = "404", description = "School not found")
    })
    public ResponseEntity<SchoolDto> suspendSchool(
            @Parameter(description = "UUID of the school to be suspended", required = true)
            @PathVariable UUID schoolId) {
        log.info("Suspending school with ID: {}", schoolId);
        SchoolDto schoolDto = schoolService.suspendSchool(schoolId);
        return ResponseEntity.ok(schoolDto);
    }

    @PostMapping("/{schoolId}/provision")
    @Operation(summary = "Assign provision to school", description = "Assigns database provisioning to a school")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Provision assigned successfully"),
            @ApiResponse(responseCode = "404", description = "School not found"),
            @ApiResponse(responseCode = "400", description = "School not eligible for provisioning")
    })
    public ResponseEntity<SchoolDto> assignProvision(
            @Parameter(description = "UUID of the school to be provisioned", required = true)
            @PathVariable UUID schoolId) {
        log.info("Assigning provision to school with ID: {}", schoolId);
        SchoolDto schoolDto = schoolService.assignProvision(schoolId);
        return ResponseEntity.ok(schoolDto);
    }

    @GetMapping("/{schoolId}/provisioned")
    @Operation(summary = "Check if school is provisioned", description = "Checks if a school has been provisioned for a specific academic level")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check completed successfully"),
            @ApiResponse(responseCode = "404", description = "School not found")
    })
    public ResponseEntity<Boolean> isSchoolProvisioned(
            @Parameter(description = "UUID of the school to check", required = true)
            @PathVariable UUID schoolId,
            @Parameter(description = "Academic level to check provisioning for", required = true)
            @RequestParam AcademicLevel academicLevel) {
        log.info("Checking if school {} is provisioned for level: {}", schoolId, academicLevel);
        boolean isProvisioned = schoolService.isSchoolProvisioned(schoolId, academicLevel);
        return ResponseEntity.ok(isProvisioned);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}