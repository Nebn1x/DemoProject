package org.example.dto.endpoint;

import jakarta.validation.constraints.*;

public record UpdateEndpointRequest(

        @Pattern(regexp = "GET|POST|PUT|PATCH|DELETE")
        String method,

        @Pattern(regexp = "^/[a-zA-Z0-9/_\\-.]*$")
        @Size(max = 500)
        String path,

        @Size(max = 1048576)
        String responseBody,

        @Min(100) @Max(599)
        Integer responseStatus,

        @Size(max = 100)
        String contentType,

        @Min(0) @Max(30000)
        Integer delayMs

) {}
