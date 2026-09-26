package com.dispatchtrack.studentmanagement.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Uniform JSON error body returned for every error response, so API
 * consumers only need to handle one shape regardless of which exception
 * triggered it.
 *
 * Example body:
 * {
 *   "timestamp": "2026-09-24T10:15:30",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Student not found with id : '42'",
 *   "path": "/students/42"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private int status;
    private String error;
    private String message;
    private String path;

    /** Populated only for validation errors: field name -> validation message. */
    private Map<String, String> fieldErrors;
}
