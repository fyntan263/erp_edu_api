package com.innoverse.erp_edu_api.schools.web;

import com.innoverse.erp_edu_api.schools.web.dtos.SchoolDto;
import com.innoverse.erp_edu_api.schools.web.dtos.UpdateSchoolRequest;
import com.innoverse.erp_edu_api.schools.services.ports.SchoolServicePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/platform/schools")
@RequiredArgsConstructor
@Tag(name = "Schools", description = "API for managing educational institutions")
public class SchoolPlatformController {
    private final SchoolServicePort schoolService;

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
        SchoolDto schoolDto = schoolService.getSchoolById(schoolId).orElseThrow();
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
    @Operation(summary = "Get all schools", description = "Retrieves a list of all schools")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Schools retrieved successfully")
    })
    public ResponseEntity<List<SchoolDto>> getAllSchools() {
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
    @Operation(summary = "Activate a school", description = "Changes a school's status to active")
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
    @Operation(summary = "Suspend a school", description = "Changes a school's status to suspended")
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}