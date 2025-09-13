package com.innoverse.erp_edu_api.common.errors;



import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    private String code;              // Custom error code (e.g., EDU_202)
    private String error;              // e.g., "Bad Request"
    private String message;           // e.g., "Invalid request payload"
    private int status;               // HTTP Status code
    private String path;              // URI path
    private LocalDateTime timestamp; // When the error occurred
    private String debugMessage;     // Internal (e.g., exception message)
    private List<FieldValidationError> errors; // Field-level validation errors

    public static ApiError of(HttpStatus status, String message, String path) {
        return ApiError.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }

//    public static ApiError withDetails(HttpStatus status, String message, String path, String debugMessage) {
//        return ApiError.of(status, message, path).builder()
//                .debugMessage(debugMessage)
//                .build();
//    }
}