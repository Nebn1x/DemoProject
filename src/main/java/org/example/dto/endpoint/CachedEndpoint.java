package org.example.dto.endpoint;

import org.example.entity.MockEndpoint;

import java.io.Serializable;

/**
 * Легкий immutable-об'єкт для кешування в Redis.
 * Не тягне LAZY-зв'язки Entity (user), тому безпечно серіалізується.
 */
public record CachedEndpoint(
        String responseBody,
        Integer responseStatus,
        String contentType,
        Integer delayMs
) implements Serializable {

    public static CachedEndpoint from(MockEndpoint e) {
        return new CachedEndpoint(
                e.getResponseBody(),
                e.getResponseStatus(),
                e.getContentType(),
                e.getDelayMs()
        );
    }
}