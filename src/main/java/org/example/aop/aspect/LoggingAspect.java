package org.example.aop.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.service.RequestLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Aspect
@RequiredArgsConstructor
public class LoggingAspect {

    private final RequestLogService requestLogService;
    private final HttpServletRequest request;

    /**
     * Обгортає виклик MockController.handleMockRequest:
     * заміряє latency, дістає статус, асинхронно пише RequestLog.
     */
    @Around("execution(* org.example.controller.MockController.handleMockRequest(..))")
    public Object logMockRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        int statusCode = 500;
        Object result;

        try {
            result = joinPoint.proceed();
            if (result instanceof ResponseEntity<?> responseEntity) {
                statusCode = responseEntity.getStatusCode().value();
            } else {
                statusCode = 200;
            }
            return result;
        } catch (Throwable ex) {
            statusCode = 404; // mock не знайдено / помилка
            throw ex;
        } finally {
            long latencyMs = System.currentTimeMillis() - startTime;
            String method = request.getMethod();
            String fullUri = request.getRequestURI();          // /mock/{userHash}/api/users

            String userHash = extractUserHash(fullUri);
            String mockPath = extractMockPath(fullUri, userHash); // /api/users

            if (userHash != null) {
                requestLogService.saveLogAsync(userHash, method, mockPath, statusCode, (int) latencyMs);
            }
        }
    }

    private String extractUserHash(String uri) {
        String[] parts = uri.split("/");
        // ["", "mock", "{userHash}", "api", "users"]
        if (parts.length >= 3 && "mock".equals(parts[1])) {
            return parts[2];
        }
        return null;
    }

    /**
     * Шлях БЕЗ префікса /mock/{userHash} — щоб збігався з MockEndpoint.path у БД.
     */
    private String extractMockPath(String uri, String userHash) {
        if (userHash == null) return uri;
        String prefix = "/mock/" + userHash;
        String path = uri.substring(prefix.length());
        return path.isEmpty() ? "/" : path;
    }
}