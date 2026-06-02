package org.example.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.service.RateLimitService;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Rate limiting для /mock/** через Bucket4j.
 * Якщо rate-limit вимкнено (app.rate-limit.enabled=false),
 * RateLimitService сам пропускає всі запити, не торкаючись Redis.
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String uri = request.getRequestURI();
        String userHash = extractUserHash(uri);

        if (userHash != null) {
            String endpointKey = userHash + ":" + request.getMethod() + ":" + uri;

            RateLimitService.RateLimitResult result =
                    rateLimitService.checkLimits(userHash, endpointKey);

            if (!result.allowed()) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(result.retryAfterSeconds()));
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("""
                        {"error":"RATE_LIMIT_EXCEEDED","message":"Забагато запитів. Спробуйте пізніше.","retryAfterSeconds":%d}
                        """.formatted(result.retryAfterSeconds()));
                return;
            }

            response.setHeader("X-RateLimit-Remaining", String.valueOf(result.remaining()));
        }

        filterChain.doFilter(request, response);
    }

    private String extractUserHash(String uri) {
        String[] parts = uri.split("/");
        if (parts.length >= 3 && "mock".equals(parts[1])) {
            return parts[2];
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/mock/");
    }
}