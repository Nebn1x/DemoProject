package org.example.aop.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
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

    public Object logMockRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        Object result = null;
        int statusCode = 500;

        try {
            result = joinPoint.proceed();

            if (result instanceof ResponseEntity<?> responseEntity) {
               statusCode = responseEntity.getStatusCode().value();
            }
            else {
                statusCode = 200;
            }

            return result;
        } finally {
            long delayMs = System.currentTimeMillis() - startTime;
            String method = request.getMethod();
            String fullpath = request.getRequestURI();

            String userHash = extractUserHash(fullpath);

            if (userHash != null) {
                requestLogService.saveLogAsync(userHash, method, fullpath, statusCode, (int) delayMs);
            }
        }
    }

    private String extractUserHash(String path) {
        String[] parts = path.split("/");
        if (parts.length >= 3 && "mock".equals(parts[1])) {
            return parts[2];
        }
        return null;
    }
}


