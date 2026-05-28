package org.example.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
        String error,
        String message,
        LocalDateTime timestamp,
        int status,
        Map<String, String> details
) {
    public static ErrorResponse of(String error, String message, int status) {
        return new ErrorResponse(error, message, LocalDateTime.now(), status, null);
    }

    public static ErrorResponse withDetails(String error, String message, int status, Map<String, String> details) {
        return new ErrorResponse(error, message, LocalDateTime.now(), status, details);
    }
}
