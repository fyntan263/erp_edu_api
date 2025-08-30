package com.innoverse.erp_edu_api.schools.api.dtos;


import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateSchoolRequest {
    @Size(max = 255, message = "School name cannot exceed 255 characters")
    private String schoolName;

    private String schoolType;
    private String educationLevel;

    @Size(max = 50, message = "District cannot exceed 50 characters")
    private String district;

    @Size(max = 50, message = "Province cannot exceed 50 characters")
    private String province;

    @Size(max = 255, message = "Physical address cannot exceed 255 characters")
    private String physicalAddress;

    @PositiveOrZero(message = "Capacity must be zero or positive")
    private Integer capacity;

    @Email(message = "School email must be valid")
    @Size(max = 255, message = "School email cannot exceed 255 characters")
    private String schoolEmail;

    @Size(max = 50, message = "MoPSE number cannot exceed 50 characters")
    private String mopseNo;

    private String status;

    // Contact person info
    @Size(max = 150, message = "Contact full name cannot exceed 150 characters")
    private String contactFullname;

    @Size(max = 100, message = "Contact position cannot exceed 100 characters")
    private String contactPosition;

    @Email(message = "Contact email must be valid")
    @Size(max = 255, message = "Contact email cannot exceed 255 characters")
    private String contactEmail;

//    @Pattern(regexp = "^\\+?[0-9\\s\\-\\(\\)]{10,20}$", message = "Contact phone must be valid")
    private String contactPhone;

//    @Pattern(regexp = "^\\+?[0-9\\s\\-\\(\\)]{10,20}$", message = "Alternative phone must be valid")
    private String contactAltPhone;
}