package org.example.aop.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.example.service.RequestLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoggingAspect")
class LoggingAspectTest {

    @Mock
    private RequestLogService requestLogService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @InjectMocks
    private LoggingAspect loggingAspect;

    @Test
    @DisplayName("логує запит і витягує userHash + шлях без префікса")
    void shouldLogRequestAndExtractHashSuccessfully() throws Throwable {
        String testUrl = "/mock/a1b2c3d4/api/users";
        when(request.getRequestURI()).thenReturn(testUrl);
        when(request.getMethod()).thenReturn("GET");

        when(joinPoint.proceed()).thenReturn(ResponseEntity.ok().build());

        Object result = loggingAspect.logMockRequest(joinPoint);

        assertEquals(200, ((ResponseEntity<?>) result).getStatusCode().value());

        verify(joinPoint).proceed();

        // Аспект тепер передає шлях БЕЗ префікса /mock/{hash} - тобто /api/users
        verify(requestLogService).saveLogAsync(
                eq("a1b2c3d4"),
                eq("GET"),
                eq("/api/users"),
                eq(200),
                anyInt()
        );
    }
}
