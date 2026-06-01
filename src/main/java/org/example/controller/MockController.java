package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.dto.endpoint.CachedEndpoint;
import org.example.exception.EndpointNotFoundException;
import org.example.service.MockResolverService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mock")
@RequiredArgsConstructor
public class MockController {

    private final MockResolverService resolverService;

    /**
     * Catch-all: ловить будь-який метод і будь-який шлях після /mock/{userHash}/.
     * Логування робить LoggingAspect (через @Around), тут лише віддаємо відповідь.
     */
    @RequestMapping("/{userHash}/**")
    public ResponseEntity<String> handleMockRequest(
            @PathVariable String userHash,
            HttpServletRequest request
    ) {
        String fullUri = request.getRequestURI();             // /mock/aB3xK9p2/api/users
        String prefix = "/mock/" + userHash;                  // /mock/aB3xK9p2
        String mockPath = fullUri.substring(prefix.length()); // /api/users
        if (mockPath.isEmpty()) {
            mockPath = "/";
        }

        String method = request.getMethod();

        // Один SQL-запит (через кешований сервіс), без findAll().stream()
        CachedEndpoint endpoint = resolverService.resolve(userHash, method, mockPath);
        if (endpoint == null) {
            throw new EndpointNotFoundException(
                    "Mock не знайдено: " + method + " " + mockPath);
        }

        // Опціональна затримка відповіді
        if (endpoint.delayMs() != null && endpoint.delayMs() > 0) {
            try {
                Thread.sleep(endpoint.delayMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return ResponseEntity
                .status(endpoint.responseStatus())
                .contentType(parseContentType(endpoint.contentType()))
                .body(endpoint.responseBody());
    }

    private MediaType parseContentType(String ct) {
        try {
            return MediaType.parseMediaType(ct != null ? ct : "application/json");
        } catch (Exception e) {
            return MediaType.APPLICATION_JSON;
        }
    }
}