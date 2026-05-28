package org.example.dto.auth;

import java.util.UUID;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresInMs,
        UUID userId,
        String email,
        String userHash
) {
    public static AuthResponse of(String token, long expiresInMs, UUID userId, String email, String userHash) {
        return new AuthResponse(token, "Bearer", expiresInMs, userId, email, userHash);
    }
}
