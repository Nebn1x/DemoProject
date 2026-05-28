package org.example.dto.endpoint;

import org.example.entity.MockEndpoint;

import java.time.LocalDateTime;
import java.util.UUID;

public record EndpointResponse(
        UUID id,
        String method,
        String path,
        String fullUrl,
        String responseBody,
        Integer responseStatus,
        String contentType,
        Integer delayMs,
        LocalDateTime expiresAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Конструюємо повний URL, який отримує користувач для виклику mock.
     */
    public static EndpointResponse from(MockEndpoint e, String baseUrl) {
        String userHash = e.getUser().getUserHash();
        String fullUrl = "%s/mock/%s%s".formatted(baseUrl, userHash, e.getPath());

        return new EndpointResponse(
                e.getId(),
                e.getMethod(),
                e.getPath(),
                fullUrl,
                e.getResponseBody(),
                e.getResponseStatus(),
                e.getContentType(),
                e.getDelayMs(),
                e.getExpiresAt(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
